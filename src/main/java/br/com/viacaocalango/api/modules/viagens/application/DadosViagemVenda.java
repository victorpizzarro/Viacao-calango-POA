package br.com.viacaocalango.api.modules.viagens.application;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record DadosViagemVenda(
        UUID id,
        OffsetDateTime dataSaida,
        int capacidade,
        List<DadosParadaVenda> paradas,
        List<String> locaisVenda) {

    public DadosViagemVenda {
        paradas = List.copyOf(paradas);
        locaisVenda = List.copyOf(locaisVenda);
    }
}
