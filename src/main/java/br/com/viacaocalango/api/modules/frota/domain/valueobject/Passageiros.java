package br.com.viacaocalango.api.modules.frota.domain.valueobject;

import br.com.viacaocalango.api.shared.domain.DadoInvalido;

public record Passageiros(int quantidade) {

    private static final int MAXIMO = 100_000;

    public Passageiros {
        if (quantidade < 1 || quantidade > MAXIMO) {
            throw new DadoInvalido("Quantidade de passageiros deve estar entre 1 e 100000");
        }
    }

    public static Passageiros de(int quantidade) {
        return new Passageiros(quantidade);
    }
}
