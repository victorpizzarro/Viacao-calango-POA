package br.com.viacaocalango.api.modules.viagens.domain.valueobject;

import br.com.viacaocalango.api.shared.domain.DadoInvalido;

public record MinutosDesdeOrigem(int valor) {

    public MinutosDesdeOrigem {
        if (valor < 0) {
            throw new DadoInvalido("Minutos desde origem nao pode ser negativo");
        }
    }

    public int diferenca(MinutosDesdeOrigem outro) {
        return Math.abs(this.valor - outro.valor);
    }
}