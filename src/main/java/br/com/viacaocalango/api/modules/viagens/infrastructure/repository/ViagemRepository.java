package br.com.viacaocalango.api.modules.viagens.infrastructure.repository;

import br.com.viacaocalango.api.modules.viagens.domain.entity.Viagem;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ViagemRepository {
    Viagem salvar(Viagem viagem);

    Optional<Viagem> buscar(UUID id);

    List<Viagem> listar();
}
