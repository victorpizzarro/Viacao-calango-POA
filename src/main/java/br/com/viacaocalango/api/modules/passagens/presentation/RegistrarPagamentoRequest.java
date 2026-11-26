package br.com.viacaocalango.api.modules.passagens.presentation;

import br.com.viacaocalango.api.modules.passagens.domain.model.FormaPagamento;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RegistrarPagamentoRequest(
        @NotNull @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") UUID passagemId,
        @NotNull @Schema(example = "CARTAO") FormaPagamento forma,
        @Schema(description = "Obrigatorio no cartao; tokens iniciados por recusar sao negados")
                String tokenCartao) {}
