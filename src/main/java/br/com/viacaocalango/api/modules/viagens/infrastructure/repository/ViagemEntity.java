package br.com.viacaocalango.api.modules.viagens.infrastructure.repository;

import br.com.viacaocalango.api.modules.viagens.domain.entity.StatusViagem;
import br.com.viacaocalango.api.modules.viagens.domain.entity.Viagem;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "viagem")
class ViagemEntity {
    @Id private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rota_id", nullable = false)
    private RotaEntity rota;

    @Column(name = "data_hora_partida", nullable = false)
    private OffsetDateTime dataHoraPartida;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusViagem status;

    // Gerada somente no INSERT pelo banco; salvar novamente o mesmo UUID nao muda a ordem.
    @Column(insertable = false, updatable = false, nullable = false)
    private Long sequencia;

    protected ViagemEntity() {}

    ViagemEntity(Viagem viagem, RotaEntity rota) {
        id = viagem.id();
        this.rota = rota;
        dataHoraPartida = viagem.dataHoraPartida();
        status = viagem.status();
    }

    Viagem paraDominio() {
        return new Viagem(id, rota.paraDominio(), dataHoraPartida, status);
    }
}
