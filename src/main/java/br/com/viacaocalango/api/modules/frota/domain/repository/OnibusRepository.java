package br.com.viacaocalango.api.modules.frota.domain.repository;

import br.com.viacaocalango.api.modules.frota.domain.model.Onibus;
import br.com.viacaocalango.api.modules.frota.domain.valueobject.OnibusId;
import java.util.List;
import java.util.Optional;

public interface OnibusRepository {

    void salvar(Onibus onibus);

    Optional<Onibus> buscarPorId(OnibusId id);

    Optional<Onibus> buscarParaAlteracao(OnibusId id);

    List<Onibus> listarTodos();
}
