package br.com.viacaocalango.api.modules.passagens.domain.model;

import br.com.viacaocalango.api.modules.passagens.domain.valueobject.ParadaId;
import br.com.viacaocalango.api.modules.passagens.domain.valueobject.Trecho;
import br.com.viacaocalango.api.modules.passagens.domain.valueobject.TrechoOcupacao;
import br.com.viacaocalango.api.shared.domain.DadoInvalido;
import br.com.viacaocalango.api.shared.domain.RegraDeNegocioViolada;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class OcupacaoViagem {

    private final int capacidade;
    private final List<ParadaId> paradas;
    private final Map<ParadaId, Integer> indices;
    private final int[] ocupados;

    private OcupacaoViagem(
            int capacidade,
            List<ParadaId> paradas,
            Map<ParadaId, Integer> indices,
            int[] ocupados) {
        this.capacidade = capacidade;
        this.paradas = paradas;
        this.indices = indices;
        this.ocupados = ocupados;
    }

    public static OcupacaoViagem de(
            int capacidade,
            List<ParadaId> paradasEmOrdem,
            Collection<Passagem> passagens,
            OffsetDateTime agora) {
        if (capacidade < 1) {
            throw new DadoInvalido("Capacidade da viagem invalida");
        }
        List<ParadaId> paradas = List.copyOf(paradasEmOrdem);
        if (paradas.size() < 2) {
            throw new DadoInvalido("Paradas da viagem inconsistentes");
        }
        Map<ParadaId, Integer> indices = new HashMap<>();
        for (int i = 0; i < paradas.size(); i++) {
            if (indices.putIfAbsent(paradas.get(i), i) != null) {
                throw new DadoInvalido("Paradas da viagem inconsistentes");
            }
        }
        // Embarque soma uma vaga ocupada, desembarque a devolve no segmento seguinte.
        int[] variacao = new int[paradas.size()];
        for (Passagem passagem : passagens) {
            if (passagem.ocupaLugar(agora)) {
                var intervalo = intervaloDe(indices, passagem.trecho());
                variacao[intervalo.origem()]++;
                variacao[intervalo.destino()]--;
            }
        }
        int[] ocupados = new int[paradas.size() - 1];
        int total = 0;
        for (int i = 0; i < ocupados.length; i++) {
            total += variacao[i];
            ocupados[i] = total;
        }
        return new OcupacaoViagem(capacidade, paradas, indices, ocupados);
    }

    public void garantirLugar(Trecho trecho) {
        var intervalo = intervaloDe(indices, trecho);
        for (int posicao = intervalo.origem(); posicao < intervalo.destino(); posicao++) {
            if (ocupados[posicao] >= capacidade) {
                throw new RegraDeNegocioViolada("Nao ha lugar disponivel no trecho escolhido");
            }
        }
    }

    public List<TrechoOcupacao> trechos() {
        return IntStream.range(0, ocupados.length).mapToObj(this::trechoEm).toList();
    }

    private TrechoOcupacao trechoEm(int posicao) {
        return new TrechoOcupacao(
                paradas.get(posicao),
                paradas.get(posicao + 1),
                ocupados[posicao],
                Math.max(0, capacidade - ocupados[posicao]));
    }

    private static Intervalo intervaloDe(Map<ParadaId, Integer> indices, Trecho trecho) {
        Integer origem = indices.get(trecho.origem());
        Integer destino = indices.get(trecho.destino());
        if (origem == null || destino == null) {
            throw new DadoInvalido("Parada nao pertence a viagem");
        }
        if (origem >= destino) {
            throw new DadoInvalido("Destino deve vir depois da origem no percurso");
        }
        return new Intervalo(origem, destino);
    }

    private record Intervalo(int origem, int destino) {}
}
