package br.com.viacaocalango.api.modules.viagens.presentation;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public record ParadaRequest(
        @NotBlank String nome,
        @NotNull @Min(0) Double km_desde_origem,
        @NotNull @Min(0) Integer minutos_desde_origem,
        @NotNull @Min(1) Integer ordem) {}
