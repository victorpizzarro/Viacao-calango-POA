package br.com.viacaocalango.api.modules.passagens.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.viacaocalango.api.modules.viagens.application.ViagensParaVenda;
import com.jayway.jsonpath.JsonPath;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.IllegalTransactionStateException;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.datasource.url=jdbc:h2:mem:venda_integrada;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000",
            "spring.datasource.username=sa",
            "spring.datasource.password=",
            "logging.level.root=WARN"
        })
class VendaIntegradaTest {
    @TestConfiguration
    static class Relogio {
        @Bean
        @Primary
        Clock clockTeste() {
            return Clock.fixed(Instant.parse("2026-11-26T12:00:00Z"), ZoneOffset.UTC);
        }
    }

    @LocalServerPort private int port;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private DataSource dataSource;
    @Autowired private ViagensParaVenda viagens;
    private final HttpClient client = HttpClient.newHttpClient();
    private String viagem, origem, intermediaria, destino;

    @BeforeEach
    void preparar() throws Exception {
        for (String tabela :
                List.of(
                        "pagamento",
                        "passagem",
                        "motorista_escala_trecho",
                        "motorista_escala",
                        "motorista_periodo_dirigido",
                        "motoristas",
                        "onibus")) {
            jdbc.update("DELETE FROM " + tabela);
        }
        var rota =
                criar(
                        "rotas",
                        """
                        {"nome":"Rio-BH","paradas":[
                          {"nome":"Rio","km_desde_origem":0,"minutos_desde_origem":0,"ordem":1},
                          {"nome":"Juiz de Fora","km_desde_origem":250,"minutos_desde_origem":240,"ordem":2},
                          {"nome":"BH","km_desde_origem":510,"minutos_desde_origem":420,"ordem":3}]}
                        """);
        origem = rota.read("$.paradas[0].id");
        intermediaria = rota.read("$.paradas[1].id");
        destino = rota.read("$.paradas[2].id");
        viagem =
                criar(
                                "viagens",
                                "{\"rota_id\":\""
                                        + rota.read("$.id")
                                        + "\",\"data_hora_partida\":\"2026-12-10T08:00:00-03:00\"}")
                        .read("$.id");
        // Dois onibus com capacidades distintas: a venda precisa usar o efetivamente escalado.
        criar("onibus", "{\"tipo\":\"EXECUTIVO\",\"capacidade\":32}");
        String onibus = criar("onibus", "{\"tipo\":\"LEITO\",\"capacidade\":23}").read("$.id");
        criar("motoristas", "{\"nome\":\"Primeiro\"}");
        criar("motoristas", "{\"nome\":\"Segundo\"}");
        criar(
                "viagens/" + viagem + "/escala",
                "{\"onibus_id\":\""
                        + onibus
                        + "\",\"paradas_troca_autorizada\":[\""
                        + intermediaria
                        + "\"]}");
    }

    @Test
    void cotacaoCompraPagamentoEOcupacaoUsamMesmaViagemEOnibus() throws Exception {
        var preco =
                enviar(
                        "GET",
                        "viagens/"
                                + viagem
                                + "/preco?origem=Rio&destino=Juiz%20de%20Fora&data_compra=2026-11-26T12:00:00Z",
                        null);
        assertThat(preco.statusCode()).isEqualTo(200);
        int cotado = JsonPath.read(preco.body(), "$.preco_centavos");
        assertThat(cotado).isEqualTo(11875);
        var passagem = criar("passagens", corpo(origem, intermediaria));
        assertThat(passagem.<Integer>read("$.preco_centavos")).isEqualTo(cotado);
        var pagamento =
                criar(
                        "pagamentos",
                        "{\"passagem_id\":\""
                                + passagem.read("$.id")
                                + "\",\"forma\":\"CARTAO\",\"token_cartao\":\"aprovado-teste\"}");
        assertThat(pagamento.<String>read("$.status_passagem")).isEqualTo("CONFIRMADA");
        assertThat(pagamento.<Integer>read("$.valor_centavos")).isEqualTo(cotado);
        assertThat(jdbc.queryForObject("SELECT valor_centavos FROM pagamento", Long.class))
                .isEqualTo((long) cotado);
        var disponibilidade = disponibilidade();
        assertThat(disponibilidade.<Integer>read("$.capacidade")).isEqualTo(23);
        assertThat(disponibilidade.<List<Integer>>read("$.trechos[*].lugares_ocupados"))
                .containsExactly(1, 0);
        assertThat(disponibilidade.<List<Integer>>read("$.trechos[*].lugares_disponiveis"))
                .containsExactly(22, 23);
        criar("passagens", corpo(intermediaria, destino));
        assertThat(disponibilidade().<List<Integer>>read("$.trechos[*].lugares_ocupados"))
                .containsExactly(1, 1);
        var completo = enviar("GET", "viagens/" + viagem + "/preco?origem=Rio&destino=BH", null);
        var reservaCompleta = criar("passagens", corpo(origem, destino));
        assertThat(reservaCompleta.<Integer>read("$.preco_centavos"))
                .isEqualTo(JsonPath.<Integer>read(completo.body(), "$.preco_centavos"))
                .isEqualTo(21675);
    }

    @Test
    void duasTransacoesDisputamUltimaVagaSemSobrevenda() throws Exception {
        for (int i = 0; i < 22; i++) {
            criar("passagens", corpo(origem, intermediaria));
        }
        try (var connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (var stmt =
                    connection.prepareStatement(
                            "SELECT viagem_id FROM motorista_escala WHERE viagem_id = ? FOR UPDATE")) {
                stmt.setObject(1, UUID.fromString(viagem));
                try (var rs = stmt.executeQuery()) {
                    assertThat(rs.next()).isTrue();
                }
            }
            var a =
                    client.sendAsync(
                            request("POST", "passagens", corpo(origem, intermediaria)),
                            HttpResponse.BodyHandlers.ofString());
            var b =
                    client.sendAsync(
                            request("POST", "passagens", corpo(origem, intermediaria)),
                            HttpResponse.BodyHandlers.ofString());
            try {
                long limite = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
                while (bloqueados() < 2 && System.nanoTime() < limite) {
                    Thread.sleep(20);
                }
                assertThat(bloqueados())
                        .as("duas compras aguardando bloqueio real ate o fim da transacao")
                        .isEqualTo(2);
            } finally {
                connection.commit();
            }
            var respostas = List.of(a.get(15, TimeUnit.SECONDS), b.get(15, TimeUnit.SECONDS));
            assertThat(respostas)
                    .extracting(HttpResponse::statusCode)
                    .containsExactlyInAnyOrder(201, 409);
            assertThat(
                            jdbc.queryForObject(
                                    "SELECT COUNT(*) FROM passagem WHERE viagem_id = ?",
                                    Integer.class,
                                    UUID.fromString(viagem)))
                    .isEqualTo(23);
        }
        assertThat(disponibilidade().<List<Integer>>read("$.trechos[*].lugares_ocupados"))
                .containsExactly(23, 0);
        var esgotada = enviar("POST", "passagens", corpo(origem, intermediaria));
        assertThat(esgotada.statusCode()).isEqualTo(409);
        assertThat(JsonPath.<String>read(esgotada.body(), "$.codigo"))
                .isEqualTo("REGRA_DE_NEGOCIO");
        criar("passagens", corpo(intermediaria, destino)); // Segmento seguinte continua livre.
    }

    @Test
    void rejeitaViagemTrechoOuDadosDeVendaIndisponiveis() throws Exception {
        assertThat(
                        enviar("GET", "viagens/" + UUID.randomUUID() + "/disponibilidade", null)
                                .statusCode())
                .isEqualTo(404);
        assertThat(
                        enviar(
                                        "POST",
                                        "passagens",
                                        corpo(origem, intermediaria)
                                                .replace(viagem, UUID.randomUUID().toString()))
                                .statusCode())
                .isEqualTo(404);
        assertThat(enviar("POST", "passagens", corpo(destino, origem)).statusCode()).isEqualTo(422);
        assertThat(
                        enviar("POST", "passagens", corpo(UUID.randomUUID().toString(), destino))
                                .statusCode())
                .isEqualTo(422);
        var guiche =
                enviar(
                        "POST",
                        "passagens",
                        corpo(origem, intermediaria).replace("INTERNET", "GUICHE"));
        assertThat(guiche.statusCode()).isEqualTo(409);
        assertThat(JsonPath.<String>read(guiche.body(), "$.mensagem"))
                .contains("locais de venda nao cadastrados");
        jdbc.update("DELETE FROM motorista_escala_trecho");
        jdbc.update("DELETE FROM motorista_escala");
        assertThat(enviar("GET", "viagens/" + viagem + "/disponibilidade", null).statusCode())
                .isEqualTo(409);
        var semEscala = enviar("POST", "passagens", corpo(origem, intermediaria));
        assertThat(semEscala.statusCode()).isEqualTo(409);
        assertThat(JsonPath.<String>read(semEscala.body(), "$.mensagem"))
                .contains("capacidade para venda indisponivel");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM passagem", Integer.class)).isZero();
    }

    @Test
    void bloqueioExigeTransacaoExterna() {
        assertThatThrownBy(() -> viagens.obterComBloqueio(UUID.fromString(viagem)))
                .isInstanceOf(IllegalTransactionStateException.class);
    }

    private int bloqueados() {
        return jdbc.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.SESSIONS WHERE BLOCKER_ID IS NOT NULL",
                Integer.class);
    }

    private com.jayway.jsonpath.DocumentContext disponibilidade() throws Exception {
        var resposta = enviar("GET", "viagens/" + viagem + "/disponibilidade", null);
        assertThat(resposta.statusCode()).isEqualTo(200);
        return JsonPath.parse(resposta.body());
    }

    private String corpo(String origemId, String destinoId) {
        return "{\"viagem_id\":\""
                + viagem
                + "\",\"parada_origem_id\":\""
                + origemId
                + "\",\"parada_destino_id\":\""
                + destinoId
                + "\",\"passageiro_nome\":\"Maria\",\"canal\":\"INTERNET\"}";
    }

    private com.jayway.jsonpath.DocumentContext criar(String path, String corpo) throws Exception {
        var r = enviar("POST", path, corpo);
        assertThat(r.statusCode()).as(r.body()).isEqualTo(201);
        return JsonPath.parse(r.body());
    }

    private HttpResponse<String> enviar(String method, String path, String corpo) throws Exception {
        return client.send(request(method, path, corpo), HttpResponse.BodyHandlers.ofString());
    }

    private HttpRequest request(String method, String path, String corpo) {
        return HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/" + path))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json")
                .method(
                        method,
                        corpo == null
                                ? HttpRequest.BodyPublishers.noBody()
                                : HttpRequest.BodyPublishers.ofString(corpo))
                .build();
    }
}
