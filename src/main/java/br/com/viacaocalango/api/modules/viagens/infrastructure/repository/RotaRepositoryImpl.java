package br.com.viacaocalango.api.modules.viagens.infrastructure.repository;

import br.com.viacaocalango.api.modules.viagens.domain.entity.Rota;
import br.com.viacaocalango.api.modules.viagens.domain.valueobject.RotaId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RotaRepositoryImpl implements RotaRepository {

    private final Map<RotaId, Rota> rotas = new HashMap<>();

    @Override
    public Rota salvar(Rota rota) {
        rotas.put(rota.getId(), rota);
        return rota;
    }

    @Override
    public Optional<Rota> buscarPorId(RotaId id) {
        return Optional.ofNullable(rotas.get(id));
    }

    @Override
    public List<Rota> buscarTodas() {
        return new ArrayList<>(rotas.values());
    }

    @Override
    public void excluir(RotaId id) {
        rotas.remove(id);
    }
}
