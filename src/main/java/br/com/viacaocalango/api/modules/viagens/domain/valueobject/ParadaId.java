package br.com.viacaocalango.api.modules.viagens.domain.valueobject;

import br.com.viacaocalango.api.shared.domain.DadoInvalido;
import java.util.UUID;

public record ParadaId(UUID valor) {

    public ParadaId {
        if (valor == null) {
            throw new DadoInvalido("Identificador da parada e obrigatorio");
        }
    }

    public static ParadaId novo() {
        return new ParadaId(UUID.randomUUID());
    }

    public static ParadaId de(UUID valor) {
        return new ParadaId(valor);
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
