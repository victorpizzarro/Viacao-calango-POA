package br.com.viacaocalango.api.modules.passagens.domain.valueobject;

import br.com.viacaocalango.api.shared.domain.DadoInvalido;
import java.util.UUID;

public record ViagemId(UUID valor) {

    public ViagemId {
        if (valor == null) {
            throw new DadoInvalido("Identificador da viagem e obrigatorio");
        }
    }

    public static ViagemId de(UUID valor) {
        return new ViagemId(valor);
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
