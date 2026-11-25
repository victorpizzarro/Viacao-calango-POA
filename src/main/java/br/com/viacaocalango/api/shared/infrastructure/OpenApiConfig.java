package br.com.viacaocalango.api.shared.infrastructure;

import br.com.viacaocalango.api.shared.presentation.ErroResponse;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class OpenApiConfig {

    private static final String DESCRICAO =
            """
            API da Viacao Calango. Campos JSON em snake_case; datas em ISO 8601 com offset;
            valores monetarios em centavos inteiros.
            """;

    @Bean
    ModelResolver resolvedorDeModelos(JsonMapper mapper) {
        return new ModelResolver(mapper);
    }

    @Bean
    OpenAPI openApi(AppProperties properties) {
        Components componentes = new Components();
        ModelConverters.getInstance().readAll(ErroResponse.class).forEach(componentes::addSchemas);
        componentes
                .addResponses("NaoEncontrado", erro("Recurso nao encontrado"))
                .addResponses("Conflito", erro("Conflito com regra de negocio"))
                .addResponses("DadosInvalidos", erro("Dados de entrada invalidos"));
        return new OpenAPI()
                .info(new Info().title(properties.nome()).version("0.1.0").description(DESCRICAO))
                .components(componentes)
                .tags(
                        List.of(
                                tag("frota", "Onibus, quilometragem, revisoes e alocacao ideal"),
                                tag("sistema", "Status da API")));
    }

    private ApiResponse erro(String descricao) {
        Schema<?> esquema = new Schema<>().$ref("#/components/schemas/ErroResponse");
        return new ApiResponse()
                .description(descricao)
                .content(
                        new Content()
                                .addMediaType("application/json", new MediaType().schema(esquema)));
    }

    private Tag tag(String nome, String descricao) {
        return new Tag().name(nome).description(descricao);
    }
}
