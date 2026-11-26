package br.com.viacaocalango.api.modules.passagens.infrastructure;

import br.com.viacaocalango.api.modules.passagens.domain.model.CanalVenda;
import br.com.viacaocalango.api.modules.passagens.domain.model.Passagem;
import br.com.viacaocalango.api.modules.passagens.domain.model.StatusPassagem;
import br.com.viacaocalango.api.modules.passagens.domain.valueobject.NomePassageiro;
import br.com.viacaocalango.api.modules.passagens.domain.valueobject.ParadaId;
import br.com.viacaocalango.api.modules.passagens.domain.valueobject.PassagemId;
import br.com.viacaocalango.api.modules.passagens.domain.valueobject.PrazoDeReserva;
import br.com.viacaocalango.api.modules.passagens.domain.valueobject.Trecho;
import br.com.viacaocalango.api.modules.passagens.domain.valueobject.ViagemId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "passagem")
class PassagemEntity {

    @Id private UUID id;

    @Column(name = "viagem_id")
    private UUID viagemId;

    @Column(name = "parada_origem_id")
    private UUID paradaOrigemId;

    @Column(name = "parada_destino_id")
    private UUID paradaDestinoId;

    @Column(name = "passageiro_nome", length = 120)
    private String passageiroNome;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private CanalVenda canal;

    @Column(name = "local_venda", length = 100)
    private String localVenda;

    @Column(name = "preco_centavos")
    private long precoCentavos;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private StatusPassagem status;

    @Column(name = "reservada_ate")
    private OffsetDateTime reservadaAte;

    protected PassagemEntity() {}

    PassagemEntity(Passagem passagem) {
        id = passagem.id().valor();
        viagemId = passagem.viagemId().valor();
        paradaOrigemId = passagem.trecho().origem().valor();
        paradaDestinoId = passagem.trecho().destino().valor();
        passageiroNome = passagem.passageiro().valor();
        canal = passagem.canal();
        localVenda = passagem.localVenda().orElse(null);
        precoCentavos = passagem.precoCentavos();
        status = passagem.status();
        reservadaAte = passagem.reserva().venceEm();
    }

    Passagem paraDominio() {
        return Passagem.reconstituir(
                PassagemId.de(id),
                ViagemId.de(viagemId),
                Trecho.de(ParadaId.de(paradaOrigemId), ParadaId.de(paradaDestinoId)),
                NomePassageiro.de(passageiroNome),
                canal,
                localVenda,
                precoCentavos,
                status,
                PrazoDeReserva.ate(reservadaAte));
    }
}
