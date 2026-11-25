package br.com.viacaocalango.api.modules.viagens.domain.valueobject;

import br.com.viacaocalango.api.shared.domain.DadoInvalido;

public record PrecoEmCentavos(long valor) {

    public PrecoEmCentavos {
        if (valor < 0) {
            throw new DadoInvalido("Preco nao pode ser negativo");
        }
    }

    public PrecoEmCentavos somar(PrecoEmCentavos outro) {
        return new PrecoEmCentavos(this.valor + outro.valor);
    }

    public PrecoEmCentavos subtrair(PrecoEmCentavos outro) {
        if (outro.valor > this.valor) {
            throw new DadoInvalido("Desconto maior que o preco");
        }

        return new PrecoEmCentavos(this.valor - outro.valor);
    }

    public double emReais() {
        return valor / 100.0;
    }
}