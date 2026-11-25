package br.com.viacaocalango.api.modules.motoristas.domain.valueobject;

import br.com.viacaocalango.api.shared.domain.DadoInvalido;
import java.util.UUID;

public record MotoristaId(UUID valor) {

    public MotoristaId {
        if (valor == null) {
            throw new DadoInvalido("Identificador do motorista e obrigatorio");
        }
    }

    public static MotoristaId novo() {
        return new MotoristaId(UUID.randomUUID());
    }

    public static MotoristaId de(UUID valor) {
        return new MotoristaId(valor);
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
