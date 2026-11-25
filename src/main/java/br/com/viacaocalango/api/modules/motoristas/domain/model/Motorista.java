package br.com.viacaocalango.api.modules.motoristas.domain.model;

import br.com.viacaocalango.api.modules.motoristas.domain.valueobject.MotoristaId;
import br.com.viacaocalango.api.shared.domain.DadoInvalido;
import java.util.Objects;

public class Motorista {

    private final MotoristaId id;
    private final String nome;

    private Motorista(MotoristaId id, String nome) {
        if (id == null) {
            throw new DadoInvalido("Identificador do motorista e obrigatorio");
        }
        if (nome == null || nome.isBlank()) {
            throw new DadoInvalido("Nome do motorista e obrigatorio");
        }
        this.id = id;
        this.nome = nome;
    }

    public static Motorista cadastrar(String nome) {
        return new Motorista(MotoristaId.novo(), nome);
    }

    public static Motorista reconstituir(MotoristaId id, String nome) {
        return new Motorista(id, nome);
    }

    public MotoristaId id() {
        return id;
    }

    public String nome() {
        return nome;
    }

    @Override
    public boolean equals(Object outro) {
        return this == outro || (outro instanceof Motorista motorista && id.equals(motorista.id));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
