package br.com.viacaocalango.api.modules.passagens.application;

import br.com.viacaocalango.api.modules.passagens.domain.valueobject.ParadaId;
import br.com.viacaocalango.api.modules.passagens.domain.valueobject.TrechoOcupacao;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record Disponibilidade(
        UUID viagemId,
        int capacidade,
        List<TrechoOcupacao> trechos,
        Map<ParadaId, String> nomesDasParadas) {

    public Disponibilidade {
        trechos = List.copyOf(trechos);
        nomesDasParadas = Map.copyOf(nomesDasParadas);
    }
}
