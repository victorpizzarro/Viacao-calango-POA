package br.com.viacaocalango.api.modules.passagens.infrastructure;

import br.com.viacaocalango.api.modules.passagens.domain.model.FormaPagamento;
import br.com.viacaocalango.api.modules.passagens.domain.model.Pagamento;
import br.com.viacaocalango.api.modules.passagens.domain.model.StatusPagamento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "pagamento")
class PagamentoEntity {

    @Id private UUID id;

    @Column(name = "passagem_id")
    private UUID passagemId;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private FormaPagamento forma;

    @Column(name = "valor_centavos")
    private long valorCentavos;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private StatusPagamento status;

    @Column(name = "registrado_em")
    private OffsetDateTime registradoEm;

    protected PagamentoEntity() {}

    PagamentoEntity(Pagamento pagamento) {
        id = pagamento.id().valor();
        passagemId = pagamento.passagemId().valor();
        forma = pagamento.forma();
        valorCentavos = pagamento.valorCentavos();
        status = pagamento.status();
        registradoEm = pagamento.registradoEm();
    }
}
