package br.com.viacaocalango.api.shared.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Formato padrao de todas as respostas de erro")
public record ErroResponse(
        @Schema(description = "Codigo estavel do erro", example = "REGRA_DE_NEGOCIO") String codigo,
        @Schema(description = "Mensagem legivel", example = "Odometro nao pode retroceder")
                String mensagem,
        @Schema(description = "Campos invalidos, quando houver") List<DetalheErro> detalhes) {

    public record DetalheErro(
            @Schema(example = "capacidade") String campo,
            @Schema(example = "nao deve ser nulo") String mensagem) {}

    static ErroResponse de(String codigo, String mensagem) {
        return new ErroResponse(codigo, mensagem, List.of());
    }
}
