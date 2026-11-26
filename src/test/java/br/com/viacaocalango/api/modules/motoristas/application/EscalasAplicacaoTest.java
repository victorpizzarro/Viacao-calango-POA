package br.com.viacaocalango.api.modules.motoristas.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.datasource.url=jdbc:h2:mem:escalas_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000",
            "spring.datasource.username=sa",
            "spring.datasource.password=",
            "logging.level.root=WARN"
        })
class EscalasAplicacaoTest {
    @LocalServerPort private int port;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private DataSource dataSource;
    private final HttpClient client =
            HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private String onibusId;

    @BeforeEach
    void preparar() throws Exception {
        jdbc.update("DELETE FROM motorista_escala_trecho");
        jdbc.update("DELETE FROM motorista_escala");
        jdbc.update("DELETE FROM motorista_periodo_dirigido");
        jdbc.update("DELETE FROM motoristas");
        jdbc.update("DELETE FROM onibus");
        onibusId =
                criar(
                                "/api/v1/onibus",
                                """
                                {"tipo":"EXECUTIVO","capacidade":32}
                                """)
                        .read("$.id");
    }

    @Test
    void onibusEmRevisaoImpedeEscalaEAposRevisaoPermiteMesmaSolicitacao() throws Exception {
        motorista("Condutor elegivel");
        String viagemId = viagemCurta("2026-11-26T08:00:00-03:00", 240);
        String corpo = corpoEscala(null);
        String url = escalaUrl(viagemId);

        var quilometragem =
                enviar(
                        "PATCH",
                        "/api/v1/onibus/" + onibusId + "/quilometragem",
                        "{\"quilometragem_atual\":10000}");
        assertThat(quilometragem.statusCode()).isEqualTo(200);
        assertThat(JsonPath.<Boolean>read(quilometragem.body(), "$.disponivel_para_operacao"))
                .isFalse();

        var impedida = enviar("POST", url, corpo);
        assertThat(impedida.statusCode()).isEqualTo(409);
        assertThat(JsonPath.<String>read(impedida.body(), "$.codigo"))
                .isEqualTo("REGRA_DE_NEGOCIO");
        assertThat(JsonPath.<String>read(impedida.body(), "$.mensagem"))
                .isEqualTo("Onibus indisponivel para operacao");
        assertThat(contar("motorista_escala")).isZero();
        assertThat(contar("motorista_escala_trecho")).isZero();

        var revisao = enviar("POST", "/api/v1/onibus/" + onibusId + "/revisoes", "");
        assertThat(revisao.statusCode()).isEqualTo(200);
        assertThat(JsonPath.<Boolean>read(revisao.body(), "$.disponivel_para_operacao")).isTrue();
        var liberada = enviar("POST", url, corpo);
        assertThat(liberada.statusCode()).isEqualTo(201);
        assertThat(JsonPath.<String>read(liberada.body(), "$.viagem_id")).isEqualTo(viagemId);
        assertThat(contar("motorista_escala")).isEqualTo(1);
        assertThat(contar("motorista_escala_trecho")).isEqualTo(1);
    }

    @Test
    void trocaEmJuizDeForaQuandoSubstitutoCompletaDozeHorasAoMeioDia() throws Exception {
        String primeiro = motorista("Primeiro");
        String segundo = motorista("Substituto");
        periodo(segundo, "2026-11-26T00:00:00-03:00");
        var viagem = viagemCompleta();

        var resposta = enviar("POST", escalaUrl(viagem.id()), corpoEscala(viagem.paradaTroca()));
        assertThat(resposta.statusCode()).isEqualTo(201);
        var json = JsonPath.parse(resposta.body());
        assertThat(json.<String>read("$.viagem_id")).isEqualTo(viagem.id());
        assertThat(json.<String>read("$.onibus_id")).isEqualTo(onibusId);
        assertThat(json.<List<String>>read("$.trechos[*].motorista_id"))
                .containsExactly(primeiro, segundo);
        assertThat(json.<List<Integer>>read("$.trechos[*].distancia_km")).containsExactly(250, 260);
        assertThat(json.<List<String>>read("$.trocas[*].parada")).containsExactly("Juiz de Fora");
        assertThat(OffsetDateTime.parse(json.read("$.trocas[0].horario")).toInstant())
                .isEqualTo(OffsetDateTime.parse("2026-11-26T12:00:00-03:00").toInstant());
        assertThat(OffsetDateTime.parse(json.read("$.trechos[0].partida")).toInstant())
                .isEqualTo(OffsetDateTime.parse("2026-11-26T08:00:00-03:00").toInstant());
        assertThat(OffsetDateTime.parse(json.read("$.trechos[1].chegada")).toInstant())
                .isEqualTo(OffsetDateTime.parse("2026-11-26T15:00:00-03:00").toInstant());
        assertThat(json.<String>read("$.trocas[0].motorista_anterior_id")).isEqualTo(primeiro);
        assertThat(json.<String>read("$.trocas[0].novo_motorista_id")).isEqualTo(segundo);
        assertThat(contar("motorista_escala")).isEqualTo(1);
        assertThat(contar("motorista_escala_trecho")).isEqualTo(2);
        var consulta = enviar("GET", escalaUrl(viagem.id()), null);
        assertThat(consulta.statusCode()).isEqualTo(200);
        assertThat(JsonPath.parse(consulta.body()).jsonString()).isEqualTo(json.jsonString());
        assertThat(
                        enviar("POST", escalaUrl(viagem.id()), corpoEscala(viagem.paradaTroca()))
                                .statusCode())
                .isEqualTo(409);
        assertThat(contar("motorista_escala_trecho")).isEqualTo(2);
    }

    @Test
    void onzeHorasECinquentaENoveMinutosRetorna409SemEscalaParcial() throws Exception {
        motorista("Primeiro");
        periodo(motorista("Substituto"), "2026-11-26T00:01:00-03:00");
        var viagem = viagemCompleta();
        var resposta = enviar("POST", escalaUrl(viagem.id()), corpoEscala(viagem.paradaTroca()));
        assertThat(resposta.statusCode()).isEqualTo(409);
        assertThat(JsonPath.read(resposta.body(), "$.codigo").toString())
                .isEqualTo("REGRA_DE_NEGOCIO");
        assertThat(contar("motorista_escala")).isZero();
        assertThat(contar("motorista_escala_trecho")).isZero();
        assertThat(enviar("GET", escalaUrl(viagem.id()), null).statusCode()).isEqualTo(404);
    }

    @Test
    void reservaSobrepostaImpedeMotoristaMesmoSemPeriodosDirigidos() throws Exception {
        motorista("Sem direcao anterior");
        String reserva = viagemCurta("2026-11-26T10:00:00-03:00", 60);
        assertThat(enviar("POST", escalaUrl(reserva), corpoEscala(null)).statusCode())
                .isEqualTo(201);
        assertThat(contar("motorista_periodo_dirigido")).isZero();
        String outra = viagemCurta("2026-11-26T08:00:00-03:00", 240);
        var resposta = enviar("POST", escalaUrl(outra), corpoEscala(null));
        assertThat(resposta.statusCode()).isEqualTo(409);
        assertThat(JsonPath.read(resposta.body(), "$.codigo").toString())
                .isEqualTo("REGRA_DE_NEGOCIO");
        assertThat(contar("motorista_escala")).isEqualTo(1);
        assertThat(contar("motorista_escala_trecho")).isEqualTo(1);
    }

    @Test
    void duasRequisicoesBloqueadasReleemReservasEApenasUmaConfirma() throws Exception {
        String motorista = motorista("Unico");
        String primeira = viagemCurta("2026-11-26T08:00:00-03:00", 240);
        String segunda = viagemCurta("2026-11-26T09:00:00-03:00", 240);
        // Segura a linha antes das duas chamadas HTTP: ambas precisam chegar ao bloqueio real.
        try (var connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (var statement =
                    connection.prepareStatement(
                            "SELECT id FROM motoristas WHERE id = ? FOR UPDATE")) {
                statement.setObject(1, UUID.fromString(motorista));
                try (var rs = statement.executeQuery()) {
                    assertThat(rs.next()).isTrue();
                }
            }
            var a =
                    client.sendAsync(
                            request("POST", escalaUrl(primeira), corpoEscala(null)),
                            HttpResponse.BodyHandlers.ofString());
            var b =
                    client.sendAsync(
                            request("POST", escalaUrl(segunda), corpoEscala(null)),
                            HttpResponse.BodyHandlers.ofString());
            try {
                long limite = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
                while (bloqueados() < 2 && System.nanoTime() < limite) {
                    Thread.sleep(20);
                }
                assertThat(bloqueados())
                        .as("duas transacoes HTTP aguardando bloqueio no banco")
                        .isEqualTo(2);
                assertThat(contar("motorista_escala")).isZero();
            } finally {
                connection.commit();
            }
            var respostas = List.of(a.get(15, TimeUnit.SECONDS), b.get(15, TimeUnit.SECONDS));
            assertThat(respostas)
                    .extracting(HttpResponse::statusCode)
                    .containsExactlyInAnyOrder(201, 409);
            assertThat(contar("motorista_escala")).isEqualTo(1);
            assertThat(contar("motorista_escala_trecho")).isEqualTo(1);
        }
    }

    @Test
    void falhaNoSegundoInsertDesfazCabecalhoEPrimeiroTrecho() throws Exception {
        motorista("Primeiro");
        periodo(motorista("Substituto"), "2026-11-26T00:00:00-03:00");
        var viagem = viagemCompleta();
        jdbc.execute(
                "ALTER TABLE motorista_escala_trecho ADD CONSTRAINT falha_segundo_trecho CHECK (ordem < 2)");
        try {
            assertThat(
                            enviar(
                                            "POST",
                                            escalaUrl(viagem.id()),
                                            corpoEscala(viagem.paradaTroca()))
                                    .statusCode())
                    .isEqualTo(409);
            assertThat(contar("motorista_escala")).isZero();
            assertThat(contar("motorista_escala_trecho")).isZero();
        } finally {
            jdbc.execute(
                    "ALTER TABLE motorista_escala_trecho DROP CONSTRAINT falha_segundo_trecho");
        }
    }

    @Test
    void semAutorizacaoNaoTrocaEValidacoesUsamErrosShared() throws Exception {
        motorista("Primeiro");
        motorista("Segundo");
        var viagem = viagemCompleta();
        assertThat(enviar("POST", escalaUrl(viagem.id()), corpoEscala(null)).statusCode())
                .isEqualTo(409);
        assertThat(
                        enviar(
                                        "POST",
                                        escalaUrl(viagem.id()),
                                        corpoEscala(UUID.randomUUID().toString()))
                                .statusCode())
                .isEqualTo(422);
        assertThat(enviar("POST", escalaUrl(viagem.id()), "{}").statusCode()).isEqualTo(422);
        assertThat(
                        enviar(
                                        "POST",
                                        escalaUrl(viagem.id()),
                                        "{\"onibus_id\":\""
                                                + onibusId
                                                + "\",\"paradas_troca_autorizada\":[null]}")
                                .statusCode())
                .isEqualTo(422);
        assertThat(
                        enviar("POST", escalaUrl(UUID.randomUUID().toString()), corpoEscala(null))
                                .statusCode())
                .isEqualTo(404);
        assertThat(
                        enviar(
                                        "POST",
                                        escalaUrl(viagem.id()),
                                        "{\"onibus_id\":\"" + UUID.randomUUID() + "\"}")
                                .statusCode())
                .isEqualTo(404);
        assertThat(
                        enviar(
                                        "POST",
                                        "/api/v1/motoristas/"
                                                + UUID.randomUUID()
                                                + "/periodos-dirigidos",
                                        "{\"inicio\":\"2026-11-25T20:00:00-03:00\",\"fim\":\"2026-11-26T00:00:00-03:00\"}")
                                .statusCode())
                .isEqualTo(404);
        assertThat(contar("motorista_escala")).isZero();
    }

    @Test
    void documentaSwaggerERegrideFrotaPelaAplicacao() throws Exception {
        var openapi = enviar("GET", "/openapi.json", null);
        assertThat(openapi.statusCode()).isEqualTo(200);
        var json = JsonPath.parse(openapi.body());
        for (String codigo : List.of("201", "404", "409", "422")) {
            assertThat(
                            json.<Object>read(
                                    "$.paths['/api/v1/viagens/{id}/escala'].post.responses['"
                                            + codigo
                                            + "']"))
                    .isNotNull();
        }
        assertThat(
                        json.<String>read(
                                "$.paths['/api/v1/viagens/{id}/escala'].post.responses['409'].$ref"))
                .isEqualTo("#/components/responses/Conflito");
        assertThat(
                        json.<Object>read(
                                "$.components.schemas.AlocarMotoristasRequest.properties.onibus_id"))
                .isNotNull();
        assertThat(enviar("GET", "/api/v1/onibus/" + onibusId, null).statusCode()).isEqualTo(200);
        var km =
                enviar(
                        "PATCH",
                        "/api/v1/onibus/" + onibusId + "/quilometragem",
                        "{\"quilometragem_atual\":10000}");
        assertThat(km.statusCode()).isEqualTo(200);
        assertThat(JsonPath.read(km.body(), "$.precisa_revisao").toString()).isEqualTo("true");
        assertThat(
                        enviar(
                                        "PATCH",
                                        "/api/v1/onibus/" + onibusId + "/quilometragem",
                                        "{\"quilometragem_atual\":9999}")
                                .statusCode())
                .isEqualTo(409);
        var revisao = enviar("POST", "/api/v1/onibus/" + onibusId + "/revisoes", "");
        assertThat(revisao.statusCode()).isEqualTo(200);
        assertThat(JsonPath.read(revisao.body(), "$.km_desde_revisao").toString()).isEqualTo("0");
        var ideal = enviar("GET", "/api/v1/onibus/alocacao-ideal?passageiros=60", null);
        assertThat(ideal.statusCode()).isEqualTo(200);
        assertThat(JsonPath.read(ideal.body(), "$.lugares_vazios").toString()).isEqualTo("0");
    }

    private int bloqueados() {
        return jdbc.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.SESSIONS WHERE BLOCKER_ID IS NOT NULL",
                Integer.class);
    }

    private int contar(String tabela) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM " + tabela, Integer.class);
    }

    private String motorista(String nome) throws Exception {
        return criar("/api/v1/motoristas", "{\"nome\":\"" + nome + "\"}").read("$.id");
    }

    private void periodo(String motoristaId, String fim) throws Exception {
        criar(
                "/api/v1/motoristas/" + motoristaId + "/periodos-dirigidos",
                "{\"inicio\":\"2026-11-25T20:00:00-03:00\",\"fim\":\"" + fim + "\"}");
    }

    private ViagemTeste viagemCompleta() throws Exception {
        var rota =
                criar(
                        "/api/v1/rotas",
                        """
                        {"nome":"Rio-BH","paradas":[
                          {"nome":"Rio","km_desde_origem":0,"minutos_desde_origem":0,"ordem":1},
                          {"nome":"Juiz de Fora","km_desde_origem":250,"minutos_desde_origem":240,"ordem":2},
                          {"nome":"BH","km_desde_origem":510,"minutos_desde_origem":420,"ordem":3}]}
                        """);
        String id = criarViagem(rota.read("$.id"), "2026-11-26T08:00:00-03:00");
        return new ViagemTeste(id, rota.read("$.paradas[1].id"));
    }

    private String viagemCurta(String partida, int minutos) throws Exception {
        var rota =
                criar(
                        "/api/v1/rotas",
                        """
                        {"nome":"Rio-JF","paradas":[
                          {"nome":"Rio","km_desde_origem":0,"minutos_desde_origem":0,"ordem":1},
                          {"nome":"Juiz de Fora","km_desde_origem":250,"minutos_desde_origem":%d,"ordem":2}]}
                        """
                                .formatted(minutos));
        return criarViagem(rota.read("$.id"), partida);
    }

    private String criarViagem(String rotaId, String partida) throws Exception {
        return criar(
                        "/api/v1/viagens",
                        "{\"rota_id\":\""
                                + rotaId
                                + "\",\"data_hora_partida\":\""
                                + partida
                                + "\"}")
                .read("$.id");
    }

    private String corpoEscala(String parada) {
        return "{\"onibus_id\":\""
                + onibusId
                + "\",\"paradas_troca_autorizada\":["
                + (parada == null ? "" : "\"" + parada + "\"")
                + "]}";
    }

    private String escalaUrl(String viagemId) {
        return "/api/v1/viagens/" + viagemId + "/escala";
    }

    private com.jayway.jsonpath.DocumentContext criar(String url, String corpo) throws Exception {
        var resposta = enviar("POST", url, corpo);
        assertThat(resposta.statusCode()).as(resposta.body()).isEqualTo(201);
        return JsonPath.parse(resposta.body());
    }

    private HttpResponse<String> enviar(String metodo, String url, String corpo) throws Exception {
        return client.send(request(metodo, url, corpo), HttpResponse.BodyHandlers.ofString());
    }

    private HttpRequest request(String metodo, String url, String corpo) {
        return HttpRequest.newBuilder(URI.create("http://localhost:" + port + url))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json")
                .method(
                        metodo,
                        corpo == null
                                ? HttpRequest.BodyPublishers.noBody()
                                : HttpRequest.BodyPublishers.ofString(corpo))
                .build();
    }

    private record ViagemTeste(String id, String paradaTroca) {}
}
