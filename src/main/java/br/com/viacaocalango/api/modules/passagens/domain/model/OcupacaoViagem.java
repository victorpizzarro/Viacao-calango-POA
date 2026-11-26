package br.com.viacaocalango.api.modules.passagens.domain.model;

import br.com.viacaocalango.api.modules.passagens.domain.valueobject.ParadaId;
import br.com.viacaocalango.api.modules.passagens.domain.valueobject.Trecho;
import br.com.viacaocalango.api.modules.passagens.domain.valueobject.TrechoOcupacao;
import br.com.viacaocalango.api.shared.domain.DadoInvalido;
import br.com.viacaocalango.api.shared.domain.RegraDeNegocioViolada;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.IntStream;

public class OcupacaoViagem {

    private final int capacidade;
    private final List<ParadaId> paradas;
    private final int[] ocupados;

    private OcupacaoViagem(int capacidade, List<ParadaId> paradas, int[] ocupados) {
        this.capacidade = capacidade;
        this.paradas = paradas;
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
        if (paradas.size() < 2 || new HashSet<>(paradas).size() != paradas.size()) {
            throw new DadoInvalido("Paradas da viagem inconsistentes");
        }
        int[] ocupados = new int[paradas.size() - 1];
        for (Passagem passagem : passagens) {
            if (passagem.ocupaLugar(agora)) {
                int[] intervalo = intervaloDe(paradas, passagem.trecho());
                for (int posicao = intervalo[0]; posicao < intervalo[1]; posicao++) {
                    ocupados[posicao]++;
                }
            }
        }
        return new OcupacaoViagem(capacidade, paradas, ocupados);
    }

    public void garantirLugar(Trecho trecho) {
        int[] intervalo = intervaloDe(paradas, trecho);
        for (int posicao = intervalo[0]; posicao < intervalo[1]; posicao++) {
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

    private static int[] intervaloDe(List<ParadaId> paradas, Trecho trecho) {
        int origem = paradas.indexOf(trecho.origem());
        int destino = paradas.indexOf(trecho.destino());
        if (origem < 0 || destino < 0) {
            throw new DadoInvalido("Parada nao pertence a viagem");
        }
        if (origem >= destino) {
            throw new DadoInvalido("Destino deve vir depois da origem no percurso");
        }
        return new int[] {origem, destino};
    }
}
