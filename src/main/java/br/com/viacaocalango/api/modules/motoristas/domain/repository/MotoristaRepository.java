package br.com.viacaocalango.api.modules.motoristas.domain.repository;

import br.com.viacaocalango.api.modules.motoristas.domain.model.Motorista;
import br.com.viacaocalango.api.modules.motoristas.domain.valueobject.MotoristaId;
import java.util.List;
import java.util.Optional;

public interface MotoristaRepository {

    void salvar(Motorista motorista);

    Optional<Motorista> buscarPorId(MotoristaId id);

    List<Motorista> listarTodos();
}
