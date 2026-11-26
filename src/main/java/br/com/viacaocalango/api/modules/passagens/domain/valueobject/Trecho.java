package br.com.viacaocalango.api.modules.passagens.domain.valueobject;

import br.com.viacaocalango.api.shared.domain.DadoInvalido;

public record Trecho(ParadaId origem, ParadaId destino) {

    public Trecho {
        if (origem == null || destino == null) {
            throw new DadoInvalido("Origem e destino do trecho sao obrigatorios");
        }
        if (origem.equals(destino)) {
            throw new DadoInvalido("Origem e destino do trecho devem ser diferentes");
        }
    }

    public static Trecho de(ParadaId origem, ParadaId destino) {
        return new Trecho(origem, destino);
    }
}
