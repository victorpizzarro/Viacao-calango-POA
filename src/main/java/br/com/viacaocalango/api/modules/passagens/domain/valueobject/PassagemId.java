package br.com.viacaocalango.api.modules.passagens.domain.valueobject;

import br.com.viacaocalango.api.shared.domain.DadoInvalido;
import java.util.UUID;

public record PassagemId(UUID valor) {

    public PassagemId {
        if (valor == null) {
            throw new DadoInvalido("Identificador da passagem e obrigatorio");
        }
    }

    public static PassagemId novo() {
        return new PassagemId(UUID.randomUUID());
    }

    public static PassagemId de(UUID valor) {
        return new PassagemId(valor);
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
