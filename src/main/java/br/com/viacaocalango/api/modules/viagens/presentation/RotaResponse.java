package br.com.viacaocalango.api.modules.viagens.presentation;

import br.com.viacaocalango.api.modules.viagens.domain.entity.Rota;
import java.util.List;
import java.util.UUID;

public record RotaResponse(UUID id, String nome, List<ParadaResponse> paradas) {
    public static RotaResponse de(Rota rota) {
        return new RotaResponse(
                rota.getId().valor(),
                rota.getNome(),
                rota.getParadas().stream().map(ParadaResponse::de).toList());
    }
}
