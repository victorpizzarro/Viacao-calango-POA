package br.com.viacaocalango.api.modules.frota.domain.service;

import br.com.viacaocalango.api.modules.frota.domain.valueobject.AlocacaoIdeal;
import br.com.viacaocalango.api.modules.frota.domain.valueobject.Capacidade;
import br.com.viacaocalango.api.modules.frota.domain.valueobject.Passageiros;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CalculadoraAlocacaoIdeal {

    private static final int SEM_COMBINACAO = Integer.MAX_VALUE;

    public AlocacaoIdeal calcular(Passageiros passageiros) {
        List<Integer> capacidades =
                Capacidade.permitidas().stream().map(Capacidade::lugares).toList();
        int limite = passageiros.quantidade() + Collections.max(capacidades);
        int[] menorQuantidade = new int[limite + 1];
        int[] ultimaCapacidade = new int[limite + 1];
        preencher(capacidades, menorQuantidade, ultimaCapacidade);

        int lugares = passageiros.quantidade();
        while (menorQuantidade[lugares] == SEM_COMBINACAO) {
            lugares++;
        }
        return new AlocacaoIdeal(
                passageiros,
                reconstruir(lugares, ultimaCapacidade),
                lugares - passageiros.quantidade());
    }

    private void preencher(List<Integer> capacidades, int[] menorQuantidade, int[] ultima) {
        for (int lugares = 1; lugares < menorQuantidade.length; lugares++) {
            menorQuantidade[lugares] = SEM_COMBINACAO;
            for (int capacidade : capacidades) {
                int restante = lugares - capacidade;
                if (restante >= 0
                        && menorQuantidade[restante] != SEM_COMBINACAO
                        && menorQuantidade[restante] + 1 < menorQuantidade[lugares]) {
                    menorQuantidade[lugares] = menorQuantidade[restante] + 1;
                    ultima[lugares] = capacidade;
                }
            }
        }
    }

    private List<Capacidade> reconstruir(int lugares, int[] ultimaCapacidade) {
        List<Integer> usadas = new ArrayList<>();
        for (int restante = lugares; restante > 0; restante -= ultimaCapacidade[restante]) {
            usadas.add(ultimaCapacidade[restante]);
        }
        usadas.sort(Collections.reverseOrder());
        return usadas.stream().map(Capacidade::de).toList();
    }
}
