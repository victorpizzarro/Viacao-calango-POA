package br.com.viacaocalango.api.modules.viagens.application.service;

import br.com.viacaocalango.api.modules.viagens.domain.entity.Parada;
import br.com.viacaocalango.api.modules.viagens.domain.entity.Rota;
import br.com.viacaocalango.api.modules.viagens.domain.valueobject.RotaId;
import br.com.viacaocalango.api.modules.viagens.infrastructure.repository.RotaRepository;
import java.util.List;
import java.util.Optional;

public class RotaService {

    private final RotaRepository rotaRepository;

    public RotaService(RotaRepository rotaRepository) {
        this.rotaRepository = rotaRepository;
    }

    public Rota criarRota(String nome, List<Parada> paradas) {
        Rota rota = new Rota(RotaId.novo(), nome, paradas);

        return rotaRepository.salvar(rota);
    }

    public Optional<Rota> buscarPorId(RotaId id) {
        return rotaRepository.buscarPorId(id);
    }

    public List<Rota> buscarTodas() {
        return rotaRepository.buscarTodas();
    }
}
