package br.com.viacaocalango.api.modules.frota.infrastructure;

import br.com.viacaocalango.api.modules.frota.domain.model.Onibus;
import br.com.viacaocalango.api.modules.frota.domain.model.TipoOnibus;
import br.com.viacaocalango.api.modules.frota.domain.valueobject.Capacidade;
import br.com.viacaocalango.api.modules.frota.domain.valueobject.Odometro;
import br.com.viacaocalango.api.modules.frota.domain.valueobject.OnibusId;
import br.com.viacaocalango.api.modules.frota.domain.valueobject.Quilometragem;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "onibus")
class OnibusEntity {

    @Id private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TipoOnibus tipo;

    private int capacidade;

    @Column(name = "quilometragem_total")
    private int quilometragemTotal;

    @Column(name = "quilometragem_ultima_revisao")
    private int quilometragemUltimaRevisao;

    protected OnibusEntity() {}

    OnibusEntity(Onibus onibus) {
        id = onibus.id().valor();
        tipo = onibus.tipo();
        capacidade = onibus.capacidade().lugares();
        quilometragemTotal = onibus.odometro().total().km();
        quilometragemUltimaRevisao = onibus.odometro().naUltimaRevisao().km();
    }

    Onibus paraDominio() {
        return Onibus.reconstituir(
                OnibusId.de(id),
                tipo,
                Capacidade.de(capacidade),
                new Odometro(
                        Quilometragem.de(quilometragemTotal),
                        Quilometragem.de(quilometragemUltimaRevisao)));
    }
}
