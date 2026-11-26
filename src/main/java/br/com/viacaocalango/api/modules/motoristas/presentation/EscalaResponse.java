package br.com.viacaocalango.api.modules.motoristas.presentation;

import br.com.viacaocalango.api.modules.motoristas.domain.valueobject.*;
import java.util.List;
import java.util.UUID;

public record EscalaResponse(
        UUID viagemId, UUID onibusId, List<TrechoEscalado> trechos, List<TrocaCondutor> trocas) {
    static EscalaResponse de(UUID viagemId, EscalaViagem escala) {
        return new EscalaResponse(viagemId, escala.onibusId(), escala.trechos(), escala.trocas());
    }
}
