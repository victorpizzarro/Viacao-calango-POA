package br.com.viacaocalango.api.modules.motoristas.domain.valueobject;

import br.com.viacaocalango.api.shared.domain.DadoInvalido;
import java.time.Duration;
import java.time.OffsetDateTime;

public record IntervaloHorario(OffsetDateTime inicio, OffsetDateTime fim) {

    public IntervaloHorario {
        if (inicio == null || fim == null) {
            throw new DadoInvalido("Inicio e fim do intervalo sao obrigatorios");
        }
        if (!fim.isAfter(inicio)) {
            throw new DadoInvalido("Fim do intervalo deve ser posterior ao inicio");
        }
    }

    public static IntervaloHorario de(OffsetDateTime inicio, OffsetDateTime fim) {
        return new IntervaloHorario(inicio, fim);
    }

    public Duration duracao() {
        return Duration.between(inicio, fim);
    }

    public boolean sobrepoe(IntervaloHorario outro) {
        if (outro == null) {
            throw new DadoInvalido("Intervalo para comparacao e obrigatorio");
        }
        return inicio.isBefore(outro.fim) && outro.inicio.isBefore(fim);
    }
}
