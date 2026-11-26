package br.com.viacaocalango.api.modules.viagens.domain.valueobject;

import br.com.viacaocalango.api.shared.domain.DadoInvalido;

public record KmDesdeOrigem(double valor) {

    public KmDesdeOrigem {
        if (valor < 0) {
            throw new DadoInvalido("Km desde origem nao pode ser negativo");
        }
    }

    public double diferenca(KmDesdeOrigem outro) {
        return Math.abs(this.valor - outro.valor);
    }
}
