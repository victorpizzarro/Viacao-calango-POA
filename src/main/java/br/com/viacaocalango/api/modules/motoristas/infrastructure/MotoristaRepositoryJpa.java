package br.com.viacaocalango.api.modules.motoristas.infrastructure;

import br.com.viacaocalango.api.modules.motoristas.domain.model.Motorista;
import br.com.viacaocalango.api.modules.motoristas.domain.repository.MotoristaRepository;
import br.com.viacaocalango.api.modules.motoristas.domain.valueobject.MotoristaId;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
class MotoristaRepositoryJpa implements MotoristaRepository {

    private final MotoristaJpaRepository jpa;

    MotoristaRepositoryJpa(MotoristaJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void salvar(Motorista motorista) {
        jpa.save(new MotoristaEntity(motorista));
    }

    @Override
    public Optional<Motorista> buscarPorId(MotoristaId id) {
        return jpa.findById(id.valor()).map(MotoristaEntity::paraDominio);
    }

    @Override
    public List<Motorista> listarTodos() {
        return jpa.findAll().stream().map(MotoristaEntity::paraDominio).toList();
    }
}
