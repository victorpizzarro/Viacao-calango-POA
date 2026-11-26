package br.com.viacaocalango.api.modules.passagens.domain.valueobject;

import br.com.viacaocalango.api.shared.domain.DadoInvalido;

public record TrechoOcupacao(
        ParadaId origem, ParadaId destino, int lugaresOcupados, int lugaresDisponiveis) {

    public TrechoOcupacao {
        if (origem == null || destino == null) {
            throw new DadoInvalido("Trecho de ocupacao inconsistente");
        }
        if (lugaresOcupados < 0 || lugaresDisponiveis < 0) {
            throw new DadoInvalido("Quantidade de lugares inconsistente");
        }
    }
}
