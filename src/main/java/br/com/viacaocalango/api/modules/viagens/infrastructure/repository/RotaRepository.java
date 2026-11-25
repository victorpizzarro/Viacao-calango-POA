package br.com.viacaocalango.api.modules.viagens.infrastructure.repository;

import br.com.viacaocalango.api.modules.viagens.domain.entity.Rota;
import br.com.viacaocalango.api.modules.viagens.domain.valueobject.RotaId;

import java.util.List;
import java.util.Optional;

public interface RotaRepository {

    Rota salvar(Rota rota);

    Optional<Rota> buscarPorId(RotaId id);

    List<Rota> buscarTodas();

    void excluir(RotaId id);
}
