package br.com.viacaocalango.api.modules.passagens.presentation;

import br.com.viacaocalango.api.modules.passagens.application.ResultadoPagamento;
import br.com.viacaocalango.api.modules.passagens.domain.model.FormaPagamento;
import br.com.viacaocalango.api.modules.passagens.domain.model.Pagamento;
import br.com.viacaocalango.api.modules.passagens.domain.model.StatusPagamento;
import br.com.viacaocalango.api.modules.passagens.domain.model.StatusPassagem;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

public record PagamentoResponse(
        @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") String id,
        String passagemId,
        FormaPagamento forma,
        @Schema(example = "24000") long valorCentavos,
        StatusPagamento status,
        @Schema(description = "CONFIRMADA apenas quando o pagamento e aprovado")
                StatusPassagem statusPassagem,
        OffsetDateTime registradoEm) {

    static PagamentoResponse de(ResultadoPagamento resultado) {
        Pagamento pagamento = resultado.pagamento();
        return new PagamentoResponse(
                pagamento.id().toString(),
                pagamento.passagemId().toString(),
                pagamento.forma(),
                pagamento.valorCentavos(),
                pagamento.status(),
                resultado.statusPassagem(),
                pagamento.registradoEm());
    }
}
