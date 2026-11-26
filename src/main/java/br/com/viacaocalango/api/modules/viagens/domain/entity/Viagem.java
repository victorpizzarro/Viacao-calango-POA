package br.com.viacaocalango.api.modules.viagens.domain.entity;

import br.com.viacaocalango.api.shared.domain.DadoInvalido;
import java.time.OffsetDateTime;
import java.util.UUID;

public record Viagem(UUID id, Rota rota, OffsetDateTime dataHoraPartida, StatusViagem status) {
    public Viagem {
        if (id == null || rota == null || dataHoraPartida == null || status == null)
            throw new DadoInvalido("Dados da viagem sao obrigatorios");
    }

    public static Viagem nova(Rota rota, OffsetDateTime partida) {
        return new Viagem(UUID.randomUUID(), rota, partida, StatusViagem.AGENDADA);
    }
}
