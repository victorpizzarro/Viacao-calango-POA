package br.com.viacaocalango.api.modules.viagens.domain.valueobject;

import br.com.viacaocalango.api.shared.domain.DadoInvalido;
import java.util.UUID;

public record RotaId(UUID valor) {

    public RotaId {
        if (valor == null) {
            throw new DadoInvalido("Identificador da rota e obrigatorio");
        }
    }

    public static RotaId novo() {
        return new RotaId(UUID.randomUUID());
    }

    public static RotaId de(UUID valor) {
        return new RotaId(valor);
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
