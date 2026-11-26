package br.com.viacaocalango.api.modules.viagens.infrastructure.repository;

import br.com.viacaocalango.api.modules.viagens.domain.entity.Rota;
import br.com.viacaocalango.api.modules.viagens.domain.valueobject.RotaId;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class RotaRepositoryImpl implements RotaRepository {
    private final RotaJpaRepository jpa;

    public RotaRepositoryImpl(RotaJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional
    public Rota salvar(Rota rota) {
        jpa.saveAndFlush(new RotaEntity(rota));
        return rota;
    }

    @Override
    public Optional<Rota> buscarPorId(RotaId id) {
        return jpa.findById(id.valor()).map(RotaEntity::paraDominio);
    }

    @Override
    public List<Rota> buscarTodas() {
        return jpa.findAll().stream().map(RotaEntity::paraDominio).toList();
    }

    @Override
    @Transactional
    public void excluir(RotaId id) {
        jpa.deleteById(id.valor());
    }
}
