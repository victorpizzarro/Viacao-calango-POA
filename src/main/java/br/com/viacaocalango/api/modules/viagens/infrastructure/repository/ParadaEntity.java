package br.com.viacaocalango.api.modules.viagens.infrastructure.repository;

import br.com.viacaocalango.api.modules.viagens.domain.entity.Parada;
import br.com.viacaocalango.api.modules.viagens.domain.valueobject.*;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "parada")
class ParadaEntity {
    @Id private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rota_id", nullable = false)
    private RotaEntity rota;

    @Column(nullable = false, columnDefinition = "text")
    private String nome;

    @Column(name = "km_desde_origem", nullable = false)
    private double kmDesdeOrigem;

    @Column(name = "minutos_desde_origem", nullable = false)
    private int minutosDesdeOrigem;

    @Column(nullable = false)
    private int ordem;

    protected ParadaEntity() {}

    ParadaEntity(Parada parada, RotaEntity rota) {
        id = parada.getId().valor();
        this.rota = rota;
        nome = parada.getNome();
        kmDesdeOrigem = parada.getKmDesdeOrigem().valor();
        minutosDesdeOrigem = parada.getMinutosDesdeOrigem().valor();
        ordem = parada.getOrdem();
    }

    Parada paraDominio() {
        return new Parada(
                ParadaId.de(id),
                nome,
                new KmDesdeOrigem(kmDesdeOrigem),
                new MinutosDesdeOrigem(minutosDesdeOrigem),
                ordem);
    }
}
