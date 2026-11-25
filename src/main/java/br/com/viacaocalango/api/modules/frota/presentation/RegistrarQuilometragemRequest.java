package br.com.viacaocalango.api.modules.frota.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RegistrarQuilometragemRequest(
        @NotNull
                @Min(0)
                @Schema(description = "Leitura atual do odometro, em km", example = "12500")
                Integer quilometragemAtual) {}
