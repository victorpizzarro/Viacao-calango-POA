package br.com.viacaocalango.api.modules.passagens.domain.repository;

import br.com.viacaocalango.api.modules.passagens.domain.model.Passagem;
import br.com.viacaocalango.api.modules.passagens.domain.valueobject.PassagemId;
import br.com.viacaocalango.api.modules.passagens.domain.valueobject.ViagemId;
import java.util.List;
import java.util.Optional;

public interface PassagemRepository {

    void salvar(Passagem passagem);

    Optional<Passagem> buscarParaAlteracao(PassagemId id);

    List<Passagem> listarPorViagem(ViagemId viagemId);
}
