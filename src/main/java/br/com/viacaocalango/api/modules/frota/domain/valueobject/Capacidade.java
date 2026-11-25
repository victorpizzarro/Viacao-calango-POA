package br.com.viacaocalango.api.modules.frota.domain.valueobject;

import br.com.viacaocalango.api.shared.domain.DadoInvalido;
import java.util.List;

public record Capacidade(int lugares) {

    private static final List<Integer> PERMITIDAS = List.of(23, 28, 32);

    public Capacidade {
        if (!PERMITIDAS.contains(lugares)) {
            throw new DadoInvalido("Capacidade deve ser 23, 28 ou 32 lugares");
        }
    }

    public static Capacidade de(int lugares) {
        return new Capacidade(lugares);
    }

    public static List<Capacidade> permitidas() {
        return PERMITIDAS.stream().map(Capacidade::new).toList();
    }
}
