package br.com.viacaocalango.api.modules.frota.domain.valueobject;

import br.com.viacaocalango.api.shared.domain.DadoInvalido;

public record Quilometragem(int km) implements Comparable<Quilometragem> {

    public static final Quilometragem ZERO = new Quilometragem(0);

    public Quilometragem {
        if (km < 0) {
            throw new DadoInvalido("Quilometragem nao pode ser negativa");
        }
    }

    public static Quilometragem de(int km) {
        return new Quilometragem(km);
    }

    public Quilometragem menos(Quilometragem anterior) {
        if (anterior.km > km) {
            throw new DadoInvalido("Quilometragem inconsistente");
        }
        return new Quilometragem(km - anterior.km);
    }

    public boolean maiorOuIgualA(Quilometragem outra) {
        return compareTo(outra) >= 0;
    }

    @Override
    public int compareTo(Quilometragem outra) {
        return Integer.compare(km, outra.km);
    }
}
