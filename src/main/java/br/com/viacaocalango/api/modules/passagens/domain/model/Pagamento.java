package br.com.viacaocalango.api.modules.passagens.domain.model;

import br.com.viacaocalango.api.modules.passagens.domain.valueobject.PagamentoId;
import br.com.viacaocalango.api.modules.passagens.domain.valueobject.PassagemId;
import br.com.viacaocalango.api.shared.domain.DadoInvalido;
import java.time.OffsetDateTime;

public record Pagamento(
        PagamentoId id,
        PassagemId passagemId,
        FormaPagamento forma,
        long valorCentavos,
        StatusPagamento status,
        OffsetDateTime registradoEm) {

    public Pagamento {
        if (id == null
                || passagemId == null
                || forma == null
                || status == null
                || registradoEm == null) {
            throw new DadoInvalido("Pagamento inconsistente");
        }
        if (valorCentavos < 0) {
            throw new DadoInvalido("Valor do pagamento invalido");
        }
    }

    public static Pagamento registrar(
            PassagemId passagemId,
            FormaPagamento forma,
            long valorCentavos,
            StatusPagamento status,
            OffsetDateTime registradoEm) {
        return new Pagamento(
                PagamentoId.novo(), passagemId, forma, valorCentavos, status, registradoEm);
    }

    public boolean aprovado() {
        return status == StatusPagamento.APROVADO;
    }
}
