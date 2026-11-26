package br.com.viacaocalango.api.modules.passagens.infrastructure;

import br.com.viacaocalango.api.modules.passagens.domain.model.Passagem;
import br.com.viacaocalango.api.modules.passagens.domain.repository.PassagemRepository;
import br.com.viacaocalango.api.modules.passagens.domain.valueobject.PassagemId;
import br.com.viacaocalango.api.modules.passagens.domain.valueobject.ViagemId;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
class PassagemRepositoryJpa implements PassagemRepository {

    private final PassagemJpaRepository jpa;

    PassagemRepositoryJpa(PassagemJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void salvar(Passagem passagem) {
        jpa.save(new PassagemEntity(passagem));
    }

    @Override
    public Optional<Passagem> buscarParaAlteracao(PassagemId id) {
        return jpa.buscarComBloqueio(id.valor()).map(PassagemEntity::paraDominio);
    }

    @Override
    public List<Passagem> listarPorViagem(ViagemId viagemId) {
        return jpa.listarPorViagem(viagemId.valor()).stream()
                .map(PassagemEntity::paraDominio)
                .toList();
    }
}
