package br.com.viacaocalango.api.modules.motoristas.presentation;

import br.com.viacaocalango.api.modules.motoristas.domain.model.Motorista;
import io.swagger.v3.oas.annotations.media.Schema;

public record MotoristaResponse(
        @Schema(
                        description = "UUID gerado pelo sistema",
                        example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
                String id,
        @Schema(example = "Maria da Silva") String nome) {

    static MotoristaResponse de(Motorista motorista) {
        return new MotoristaResponse(motorista.id().toString(), motorista.nome());
    }
}
