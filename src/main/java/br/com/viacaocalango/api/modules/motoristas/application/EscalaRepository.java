package br.com.viacaocalango.api.modules.motoristas.application;

import br.com.viacaocalango.api.modules.motoristas.domain.valueobject.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EscalaRepository {
    List<UUID> bloquearCandidatos();

    void bloquearMotorista(UUID id);

    CandidatoCondutor carregarCandidato(UUID id);

    Optional<EscalaViagem> buscar(UUID viagemId);

    void salvar(UUID viagemId, EscalaViagem escala);

    void registrarPeriodo(UUID motoristaId, IntervaloHorario periodo);
}
