package br.com.viacaocalango.api.modules.motoristas.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
        properties = {
            "spring.datasource.url=jdbc:h2:mem:motoristas_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
            "spring.datasource.username=sa",
            "spring.datasource.password="
        })
@AutoConfigureMockMvc
class MotoristasControllerTest {

    private static final String ROTA = "/api/v1/motoristas";

    @Autowired private MockMvc mvc;
    @Autowired private JdbcTemplate jdbc;

    @BeforeEach
    void limparCadastros() {
        jdbc.update("DELETE FROM motoristas");
    }

    @Test
    void criaListaEConsultaMotoristaPersistido() throws Exception {
        String resposta =
                mvc.perform(
                                post(ROTA)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content("{\"nome\":\"João da Silva\"}"))
                        .andExpect(status().isCreated())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.nome").value("João da Silva"))
                        .andExpect(jsonPath("$.id").isString())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(StandardCharsets.UTF_8);

        String id = JsonPath.read(resposta, "$.id");
        UUID uuid = UUID.fromString(id);
        // Fora de uma transacao de teste: comprova que o POST confirmou a gravacao.
        assertThat(
                        jdbc.queryForObject(
                                "SELECT nome FROM motoristas WHERE id = ?", String.class, uuid))
                .isEqualTo("João da Silva");

        mvc.perform(get(ROTA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(id))
                .andExpect(jsonPath("$[0].nome").value("João da Silva"));

        mvc.perform(get(ROTA + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().json(resposta));
    }

    @Test
    void listaVaziaRetornaArrayVazio() throws Exception {
        mvc.perform(get(ROTA)).andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "{}",
                "{\"nome\":null}",
                "{\"nome\":\"\"}",
                "{\"nome\":\"   \"}",
                "{\"nome\":\"\\t\\n\"}"
            })
    void rejeitaNomeAusenteNuloOuEmBranco(String corpo) throws Exception {
        mvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.codigo").value("DADOS_INVALIDOS"))
                .andExpect(jsonPath("$.mensagem").isNotEmpty())
                .andExpect(jsonPath("$.detalhes[0].campo").value("nome"));

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM motoristas", Integer.class)).isZero();
    }

    @Test
    void uuidMalformadoRetornaErroCompartilhado() throws Exception {
        mvc.perform(get(ROTA + "/nao-e-uuid"))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.codigo").value("DADOS_INVALIDOS"))
                .andExpect(jsonPath("$.mensagem").isNotEmpty())
                .andExpect(jsonPath("$.detalhes").isArray());
    }

    @Test
    void motoristaInexistenteRetornaErroCompartilhado() throws Exception {
        mvc.perform(get(ROTA + "/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("NAO_ENCONTRADO"))
                .andExpect(jsonPath("$.mensagem").value("Motorista nao encontrado"))
                .andExpect(jsonPath("$.detalhes").isArray());
    }

    @Test
    void permiteNomesRepetidosESemLimiteArbitrarioDe255Caracteres() throws Exception {
        String nome = "Maria ".repeat(50);
        String corpo = "{\"nome\":\"" + nome + "\"}";

        for (int i = 0; i < 2; i++) {
            mvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON).content(corpo))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.nome").value(nome));
        }

        assertThat(jdbc.queryForObject("SELECT COUNT(DISTINCT id) FROM motoristas", Integer.class))
                .isEqualTo(2);
        mvc.perform(get(ROTA)).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void documentaRotasEReferenciasDeErroNoOpenApi() throws Exception {
        mvc.perform(get("/openapi.json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/motoristas'].post.responses['201']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/motoristas'].get.responses['200']").exists())
                .andExpect(
                        jsonPath("$.paths['/api/v1/motoristas/{id}'].get.responses['200']")
                                .exists())
                .andExpect(
                        jsonPath("$.paths['/api/v1/motoristas/{id}'].get.responses['404'].$ref")
                                .value("#/components/responses/NaoEncontrado"))
                .andExpect(
                        jsonPath("$.paths['/api/v1/motoristas/{id}'].get.responses['422'].$ref")
                                .value("#/components/responses/DadosInvalidos"));
    }
}
