package br.com.viacaocalango.api.modules.viagens.presentation;

import br.com.viacaocalango.api.modules.viagens.domain.entity.Parada;
import java.util.UUID;

public record ParadaResponse(
        UUID id, String nome, double km_desde_origem, int minutos_desde_origem, int ordem) {
    public static ParadaResponse de(Parada p) {
        return new ParadaResponse(
                p.getId().valor(),
                p.getNome(),
                p.getKmDesdeOrigem().valor(),
                p.getMinutosDesdeOrigem().valor(),
                p.getOrdem());
    }
}
