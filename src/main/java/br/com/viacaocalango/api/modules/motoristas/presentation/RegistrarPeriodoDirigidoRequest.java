package br.com.viacaocalango.api.modules.motoristas.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public record RegistrarPeriodoDirigidoRequest(
        @NotNull @Schema(example = "2026-11-25T20:00:00-03:00") OffsetDateTime inicio,
        @NotNull @Schema(example = "2026-11-26T00:00:00-03:00") OffsetDateTime fim) {}
