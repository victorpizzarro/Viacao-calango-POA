package br.com.viacaocalango.api.modules.passagens.domain.service;

import br.com.viacaocalango.api.modules.passagens.domain.valueobject.PrazoDeReserva;
import br.com.viacaocalango.api.shared.domain.DadoInvalido;
import java.time.Duration;
import java.time.OffsetDateTime;

public class PoliticaDeReserva {

    private static final int MAXIMO_MINUTOS = 1_440;

    private final Duration duracao;

    public PoliticaDeReserva(int minutos) {
        if (minutos < 1 || minutos > MAXIMO_MINUTOS) {
            throw new DadoInvalido("Prazo de reserva deve estar entre 1 e 1440 minutos");
        }
        this.duracao = Duration.ofMinutes(minutos);
    }

    public PrazoDeReserva iniciarEm(OffsetDateTime agora) {
        return PrazoDeReserva.ate(agora.plus(duracao));
    }
}
