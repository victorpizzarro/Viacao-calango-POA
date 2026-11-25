package br.com.viacaocalango.api.modules.motoristas.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CriarMotoristaRequest(
        @NotBlank @Schema(description = "Nome obrigatorio do motorista", example = "Maria da Silva")
                String nome) {}
