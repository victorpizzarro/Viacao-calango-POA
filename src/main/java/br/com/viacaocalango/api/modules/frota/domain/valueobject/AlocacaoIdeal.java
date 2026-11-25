package br.com.viacaocalango.api.modules.frota.domain.valueobject;

import java.util.List;

public record AlocacaoIdeal(
        Passageiros passageiros, List<Capacidade> capacidades, int lugaresVazios) {

    public AlocacaoIdeal {
        capacidades = List.copyOf(capacidades);
    }

    public int totalDeOnibus() {
        return capacidades.size();
    }
}
