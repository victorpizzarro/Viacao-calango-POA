package br.com.viacaocalango.api.modules.passagens.presentation;

import br.com.viacaocalango.api.modules.passagens.domain.model.CanalVenda;
import br.com.viacaocalango.api.modules.passagens.domain.model.Passagem;
import br.com.viacaocalango.api.modules.passagens.domain.model.StatusPassagem;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

public record PassagemResponse(
        @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") String id,
        String viagemId,
        String paradaOrigemId,
        String paradaDestinoId,
        @Schema(example = "Maria") String passageiroNome,
        CanalVenda canal,
        @Schema(description = "Preenchido apenas nas vendas de guiche") String localVenda,
        @Schema(description = "Preco fixado no momento da compra", example = "24000")
                long precoCentavos,
        StatusPassagem status,
        @Schema(description = "Ate quando a reserva ocupa lugar sem pagamento")
                OffsetDateTime reservadaAte) {

    static PassagemResponse de(Passagem passagem) {
        return new PassagemResponse(
                passagem.id().toString(),
                passagem.viagemId().toString(),
                passagem.trecho().origem().toString(),
                passagem.trecho().destino().toString(),
                passagem.passageiro().toString(),
                passagem.canal(),
                passagem.localVenda().orElse(null),
                passagem.precoCentavos(),
                passagem.status(),
                passagem.reserva().venceEm());
    }
}
