package br.com.viacaocalango.api.modules.frota.presentation;

import br.com.viacaocalango.api.modules.frota.domain.model.TipoOnibus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CriarOnibusRequest(
        @NotNull @Schema(example = "EXECUTIVO") TipoOnibus tipo,
        @NotNull @Schema(description = "23, 28 ou 32 lugares", example = "28") Integer capacidade,
        @Min(0) @Schema(description = "Odometro inicial; padrao 0", example = "0")
                Integer quilometragemTotal) {}
