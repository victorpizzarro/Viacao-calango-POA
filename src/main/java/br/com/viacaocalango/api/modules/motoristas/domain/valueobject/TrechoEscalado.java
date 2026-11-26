package br.com.viacaocalango.api.modules.motoristas.domain.valueobject;

import br.com.viacaocalango.api.shared.domain.DadoInvalido;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TrechoEscalado(
        int ordem,
        String origem,
        String destino,
        OffsetDateTime partida,
        OffsetDateTime chegada,
        int distanciaKm,
        UUID motoristaId) {

    public TrechoEscalado {
        if (ordem < 1) {
            throw new DadoInvalido("Ordem do trecho deve ser positiva");
        }
        if (origem == null || origem.isBlank() || destino == null || destino.isBlank()) {
            throw new DadoInvalido("Origem e destino do trecho escalado sao obrigatorios");
        }
        if (partida == null || chegada == null || !chegada.isAfter(partida)) {
            throw new DadoInvalido("Horarios do trecho escalado sao invalidos");
        }
        if (distanciaKm <= 0) {
            throw new DadoInvalido("Distancia do trecho escalado deve ser positiva");
        }
        if (motoristaId == null) {
            throw new DadoInvalido("Motorista do trecho escalado e obrigatorio");
        }
    }

    public static TrechoEscalado de(int ordem, TrechoViagem trecho, UUID motoristaId) {
        return new TrechoEscalado(
                ordem,
                trecho.origem(),
                trecho.destino(),
                trecho.partida(),
                trecho.chegada(),
                trecho.distanciaKm(),
                motoristaId);
    }
}
