package br.com.viacaocalango.api.modules.motoristas.domain.valueobject;

import br.com.viacaocalango.api.shared.domain.DadoInvalido;
import java.util.List;
import java.util.UUID;

public record CandidatoCondutor(
        UUID motoristaId,
        List<IntervaloHorario> periodosDirigidos,
        List<IntervaloHorario> alocacoesExistentes) {

    public CandidatoCondutor {
        if (motoristaId == null) {
            throw new DadoInvalido("Identificador do motorista candidato e obrigatorio");
        }
        periodosDirigidos = periodosDirigidos == null ? List.of() : List.copyOf(periodosDirigidos);
        alocacoesExistentes =
                alocacoesExistentes == null ? List.of() : List.copyOf(alocacoesExistentes);
    }

    public static CandidatoCondutor semHistorico(UUID motoristaId) {
        return new CandidatoCondutor(motoristaId, List.of(), List.of());
    }
}
