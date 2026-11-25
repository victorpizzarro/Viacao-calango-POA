package br.com.viacaocalango.api.modules.frota.domain.valueobject;

import br.com.viacaocalango.api.shared.domain.DadoInvalido;
import br.com.viacaocalango.api.shared.domain.RegraDeNegocioViolada;

public record Odometro(Quilometragem total, Quilometragem naUltimaRevisao) {

    public static final Quilometragem INTERVALO_DE_REVISAO = Quilometragem.de(10_000);

    public Odometro {
        if (total == null || naUltimaRevisao == null) {
            throw new DadoInvalido("Quilometragem inconsistente");
        }
        if (naUltimaRevisao.compareTo(total) > 0) {
            throw new DadoInvalido("Quilometragem inconsistente");
        }
    }

    public static Odometro iniciandoEm(Quilometragem inicial) {
        return new Odometro(inicial, Quilometragem.ZERO);
    }

    public Odometro registrar(Quilometragem atual) {
        if (atual.compareTo(total) < 0) {
            throw new RegraDeNegocioViolada("Odometro nao pode retroceder");
        }
        return new Odometro(atual, naUltimaRevisao);
    }

    public Odometro concluirRevisao() {
        return new Odometro(total, total);
    }

    public Quilometragem kmDesdeRevisao() {
        return total.menos(naUltimaRevisao);
    }

    public boolean precisaRevisao() {
        return kmDesdeRevisao().maiorOuIgualA(INTERVALO_DE_REVISAO);
    }
}
