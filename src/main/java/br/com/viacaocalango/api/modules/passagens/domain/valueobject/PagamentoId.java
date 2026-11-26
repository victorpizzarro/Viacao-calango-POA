package br.com.viacaocalango.api.modules.passagens.domain.valueobject;

import br.com.viacaocalango.api.shared.domain.DadoInvalido;
import java.util.UUID;

public record PagamentoId(UUID valor) {

    public PagamentoId {
        if (valor == null) {
            throw new DadoInvalido("Identificador do pagamento e obrigatorio");
        }
    }

    public static PagamentoId novo() {
        return new PagamentoId(UUID.randomUUID());
    }

    public static PagamentoId de(UUID valor) {
        return new PagamentoId(valor);
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
