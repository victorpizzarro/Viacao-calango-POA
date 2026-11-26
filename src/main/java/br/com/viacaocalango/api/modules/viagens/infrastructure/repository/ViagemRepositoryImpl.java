package br.com.viacaocalango.api.modules.viagens.infrastructure.repository;

import br.com.viacaocalango.api.modules.viagens.domain.entity.Viagem;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class ViagemRepositoryImpl implements ViagemRepository {
    private final ViagemJpaRepository jpa;
    private final RotaJpaRepository rotas;

    public ViagemRepositoryImpl(ViagemJpaRepository jpa, RotaJpaRepository rotas) {
        this.jpa = jpa;
        this.rotas = rotas;
    }

    @Override
    @Transactional
    public Viagem salvar(Viagem viagem) {
        jpa.saveAndFlush(
                new ViagemEntity(viagem, rotas.getReferenceById(viagem.rota().getId().valor())));
        return viagem;
    }

    @Override
    public Optional<Viagem> buscar(UUID id) {
        return jpa.findById(id).map(ViagemEntity::paraDominio);
    }

    @Override
    public List<Viagem> listar() {
        return jpa.findAllByOrderBySequenciaAsc().stream().map(ViagemEntity::paraDominio).toList();
    }
}
