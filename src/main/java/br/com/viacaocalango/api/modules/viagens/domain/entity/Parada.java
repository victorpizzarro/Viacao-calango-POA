package br.com.viacaocalango.api.modules.viagens.domain.entity;

import br.com.viacaocalango.api.modules.viagens.domain.valueobject.KmDesdeOrigem;
import br.com.viacaocalango.api.modules.viagens.domain.valueobject.MinutosDesdeOrigem;
import br.com.viacaocalango.api.modules.viagens.domain.valueobject.ParadaId;
import br.com.viacaocalango.api.shared.domain.DadoInvalido;

public class Parada {

    private final ParadaId id;
    private final String nome;
    private final KmDesdeOrigem kmDesdeOrigem;
    private final MinutosDesdeOrigem minutosDesdeOrigem;
    private final int ordem;

    public Parada(
            ParadaId id,
            String nome,
            KmDesdeOrigem kmDesdeOrigem,
            MinutosDesdeOrigem minutosDesdeOrigem,
            int ordem) {
        if (nome == null || nome.isBlank()) {
            throw new DadoInvalido("Nome da parada e obrigatorio");
        }

        if (ordem <= 0) {
            throw new DadoInvalido("Ordem da parada deve ser maior que zero");
        }

        this.id = id;
        this.nome = nome;
        this.kmDesdeOrigem = kmDesdeOrigem;
        this.minutosDesdeOrigem = minutosDesdeOrigem;
        this.ordem = ordem;
    }

    public ParadaId getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public KmDesdeOrigem getKmDesdeOrigem() {
        return kmDesdeOrigem;
    }

    public MinutosDesdeOrigem getMinutosDesdeOrigem() {
        return minutosDesdeOrigem;
    }

    public int getOrdem() {
        return ordem;
    }
}
