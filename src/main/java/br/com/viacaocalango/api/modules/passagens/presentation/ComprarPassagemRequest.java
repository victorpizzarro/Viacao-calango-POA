package br.com.viacaocalango.api.modules.passagens.presentation;

import br.com.viacaocalango.api.modules.passagens.domain.model.CanalVenda;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ComprarPassagemRequest(
        @NotNull @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") UUID viagemId,
        @NotNull @Schema(description = "Parada de embarque") UUID paradaOrigemId,
        @NotNull @Schema(description = "Parada de desembarque") UUID paradaDestinoId,
        @NotNull @Schema(example = "Maria") String passageiroNome,
        @NotNull @Schema(example = "INTERNET") CanalVenda canal,
        @Schema(description = "Obrigatorio no guiche; proibido na internet") String localVenda) {}
