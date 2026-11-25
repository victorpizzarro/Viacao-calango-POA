package br.com.viacaocalango.api.modules.viagens.domain.valueobject;

import br.com.viacaocalango.api.shared.domain.DadoInvalido;

public record FaixaAntecedencia(int dias) {

    public FaixaAntecedencia {
        if (dias < 0) {
            throw new DadoInvalido("Antecedencia nao pode ser negativa");
        }
    }

    public boolean peloMenos(int diasMinimos) {
        return dias >= diasMinimos;
    }
}