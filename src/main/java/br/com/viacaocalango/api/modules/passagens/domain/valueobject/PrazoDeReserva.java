package br.com.viacaocalango.api.modules.passagens.domain.valueobject;

import br.com.viacaocalango.api.shared.domain.DadoInvalido;
import java.time.OffsetDateTime;

public record PrazoDeReserva(OffsetDateTime venceEm) {

    public PrazoDeReserva {
        if (venceEm == null) {
            throw new DadoInvalido("Prazo de reserva e obrigatorio");
        }
    }

    public static PrazoDeReserva ate(OffsetDateTime venceEm) {
        return new PrazoDeReserva(venceEm);
    }

    public boolean venceu(OffsetDateTime agora) {
        return !agora.isBefore(venceEm);
    }
}
