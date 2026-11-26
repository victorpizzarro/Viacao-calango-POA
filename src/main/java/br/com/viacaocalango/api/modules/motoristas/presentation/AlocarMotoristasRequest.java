package br.com.viacaocalango.api.modules.motoristas.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record AlocarMotoristasRequest(
        @NotNull
                @Schema(
                        description = "UUID de onibus cadastrado na Frota",
                        example = "11111111-1111-4111-8111-111111111111")
                UUID onibusId,
        @Schema(
                        description =
                                "UUIDs de paradas intermediarias da rota onde a troca e autorizada; omitido significa nenhuma",
                        example = "[\"22222222-2222-4222-8222-222222222222\"]")
                List<@NotNull UUID> paradasTrocaAutorizada) {
    public AlocarMotoristasRequest {
        paradasTrocaAutorizada =
                paradasTrocaAutorizada == null ? List.of() : paradasTrocaAutorizada;
    }
}
