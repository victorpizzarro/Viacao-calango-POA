package br.com.viacaocalango.api.modules.motoristas.domain.valueobject;

import br.com.viacaocalango.api.shared.domain.DadoInvalido;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record EscalaViagem(
        UUID onibusId, List<TrechoEscalado> trechos, List<TrocaCondutor> trocas) {

    public EscalaViagem {
        if (onibusId == null) {
            throw new DadoInvalido("Identificador do onibus e obrigatorio");
        }
        if (trechos == null || trechos.isEmpty()) {
            throw new DadoInvalido("Escala deve conter ao menos um trecho");
        }
        trechos = List.copyOf(trechos);
        trocas = trocas == null ? List.copyOf(identificarTrocas(trechos)) : List.copyOf(trocas);
    }

    public static EscalaViagem aPartirDeTrechos(UUID onibusId, List<TrechoEscalado> trechos) {
        return new EscalaViagem(onibusId, trechos, identificarTrocas(trechos));
    }

    private static List<TrocaCondutor> identificarTrocas(List<TrechoEscalado> trechos) {
        List<TrocaCondutor> trocas = new ArrayList<>();
        for (int i = 1; i < trechos.size(); i++) {
            TrechoEscalado anterior = trechos.get(i - 1);
            TrechoEscalado atual = trechos.get(i);
            if (!anterior.motoristaId().equals(atual.motoristaId())) {
                trocas.add(
                        new TrocaCondutor(
                                atual.origem(),
                                atual.partida(),
                                anterior.motoristaId(),
                                atual.motoristaId()));
            }
        }
        return trocas;
    }
}
