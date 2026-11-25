package br.com.viacaocalango.api.modules.frota.infrastructure;

import br.com.viacaocalango.api.modules.frota.domain.model.Onibus;
import br.com.viacaocalango.api.modules.frota.domain.repository.OnibusRepository;
import br.com.viacaocalango.api.modules.frota.domain.valueobject.OnibusId;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
class OnibusRepositoryJpa implements OnibusRepository {

    private final OnibusJpaRepository jpa;

    OnibusRepositoryJpa(OnibusJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void salvar(Onibus onibus) {
        jpa.save(new OnibusEntity(onibus));
    }

    @Override
    public Optional<Onibus> buscarPorId(OnibusId id) {
        return jpa.findById(id.valor()).map(OnibusEntity::paraDominio);
    }

    @Override
    public Optional<Onibus> buscarParaAlteracao(OnibusId id) {
        return jpa.buscarComBloqueio(id.valor()).map(OnibusEntity::paraDominio);
    }

    @Override
    public List<Onibus> listarTodos() {
        return jpa.findAll().stream().map(OnibusEntity::paraDominio).toList();
    }
}
