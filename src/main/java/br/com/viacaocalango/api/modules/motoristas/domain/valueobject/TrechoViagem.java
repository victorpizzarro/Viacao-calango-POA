package br.com.viacaocalango.api.modules.motoristas.domain.valueobject;

import br.com.viacaocalango.api.shared.domain.DadoInvalido;
import java.time.Duration;
import java.time.OffsetDateTime;

public record TrechoViagem(
        String origem,
        String destino,
        OffsetDateTime partida,
        OffsetDateTime chegada,
        int distanciaKm,
        boolean trocaAutorizadaNaOrigem) {

    public TrechoViagem {
        if (origem == null || origem.isBlank()) {
            throw new DadoInvalido("Parada de origem do trecho e obrigatoria");
        }
        if (destino == null || destino.isBlank()) {
            throw new DadoInvalido("Parada de destino do trecho e obrigatoria");
        }
        if (partida == null || chegada == null) {
            throw new DadoInvalido("Horarios de partida e chegada sao obrigatorios");
        }
        if (!chegada.isAfter(partida)) {
            throw new DadoInvalido("Horario de chegada deve ser posterior ao de partida");
        }
        if (distanciaKm <= 0) {
            throw new DadoInvalido("Distancia do trecho deve ser maior que zero");
        }
        origem = origem.trim();
        destino = destino.trim();
    }

    public Duration duracao() {
        return Duration.between(partida, chegada);
    }

    public IntervaloHorario intervalo() {
        return new IntervaloHorario(partida, chegada);
    }
}
