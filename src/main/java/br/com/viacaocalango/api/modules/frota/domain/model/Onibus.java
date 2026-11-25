package br.com.viacaocalango.api.modules.frota.domain.model;

import br.com.viacaocalango.api.modules.frota.domain.valueobject.Capacidade;
import br.com.viacaocalango.api.modules.frota.domain.valueobject.Odometro;
import br.com.viacaocalango.api.modules.frota.domain.valueobject.OnibusId;
import br.com.viacaocalango.api.modules.frota.domain.valueobject.Quilometragem;
import br.com.viacaocalango.api.shared.domain.DadoInvalido;
import java.util.Objects;

public class Onibus {

    private final OnibusId id;
    private final TipoOnibus tipo;
    private final Capacidade capacidade;
    private Odometro odometro;

    private Onibus(OnibusId id, TipoOnibus tipo, Capacidade capacidade, Odometro odometro) {
        if (tipo == null) {
            throw new DadoInvalido("Tipo de onibus invalido");
        }
        this.id = id;
        this.tipo = tipo;
        this.capacidade = capacidade;
        this.odometro = odometro;
    }

    public static Onibus cadastrar(
            TipoOnibus tipo, Capacidade capacidade, Quilometragem quilometragemInicial) {
        return new Onibus(
                OnibusId.novo(), tipo, capacidade, Odometro.iniciandoEm(quilometragemInicial));
    }

    public static Onibus reconstituir(
            OnibusId id, TipoOnibus tipo, Capacidade capacidade, Odometro odometro) {
        return new Onibus(id, tipo, capacidade, odometro);
    }

    public void registrarQuilometragem(Quilometragem atual) {
        odometro = odometro.registrar(atual);
    }

    public void concluirRevisao() {
        odometro = odometro.concluirRevisao();
    }

    public Quilometragem kmDesdeRevisao() {
        return odometro.kmDesdeRevisao();
    }

    public boolean precisaRevisao() {
        return odometro.precisaRevisao();
    }

    public boolean disponivelParaOperacao() {
        return !precisaRevisao();
    }

    public OnibusId id() {
        return id;
    }

    public TipoOnibus tipo() {
        return tipo;
    }

    public Capacidade capacidade() {
        return capacidade;
    }

    public Odometro odometro() {
        return odometro;
    }

    @Override
    public boolean equals(Object outro) {
        return this == outro || (outro instanceof Onibus onibus && id.equals(onibus.id));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
