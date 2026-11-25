package br.com.viacaocalango.api.modules.frota.domain.valueobject;

import br.com.viacaocalango.api.shared.domain.DadoInvalido;
import java.util.UUID;

public record OnibusId(UUID valor) {

    public OnibusId {
        if (valor == null) {
            throw new DadoInvalido("Identificador do onibus e obrigatorio");
        }
    }

    public static OnibusId novo() {
        return new OnibusId(UUID.randomUUID());
    }

    public static OnibusId de(UUID valor) {
        return new OnibusId(valor);
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
