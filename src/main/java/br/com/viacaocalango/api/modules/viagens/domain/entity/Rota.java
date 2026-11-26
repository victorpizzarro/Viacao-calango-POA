package br.com.viacaocalango.api.modules.viagens.domain.entity;

import br.com.viacaocalango.api.modules.viagens.domain.valueobject.RotaId;
import br.com.viacaocalango.api.shared.domain.DadoInvalido;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Rota {

    private final RotaId id;
    private final String nome;
    private final List<Parada> paradas;

    public Rota(RotaId id, String nome, List<Parada> paradas) {
        if (nome == null || nome.isBlank()) {
            throw new DadoInvalido("Nome da rota e obrigatorio");
        }

        if (paradas == null || paradas.size() < 2) {
            throw new DadoInvalido("A rota deve possuir pelo menos duas paradas");
        }

        this.id = id;
        this.nome = nome;
        this.paradas = new ArrayList<>(paradas);

        ordenarParadas();
        validarParadas();
    }

    private void ordenarParadas() {
        paradas.sort(Comparator.comparingInt(Parada::getOrdem));
    }

    private void validarParadas() {
        for (int i = 0; i < paradas.size(); i++) {
            if (paradas.get(i).getOrdem() != i + 1) {
                throw new DadoInvalido("A ordem das paradas deve ser sequencial");
            }
        }
    }

    public RotaId getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public List<Parada> getParadas() {
        return List.copyOf(paradas);
    }

    public Parada getOrigem() {
        return paradas.get(0);
    }

    public Parada getDestino() {
        return paradas.get(paradas.size() - 1);
    }
}
