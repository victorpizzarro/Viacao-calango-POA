package br.com.viacaocalango.api.modules.passagens.application;

import br.com.viacaocalango.api.modules.passagens.domain.gateway.GatewayCartao;
import br.com.viacaocalango.api.modules.passagens.domain.model.Pagamento;
import br.com.viacaocalango.api.modules.passagens.domain.model.Passagem;
import br.com.viacaocalango.api.modules.passagens.domain.model.StatusPagamento;
import br.com.viacaocalango.api.modules.passagens.domain.repository.PagamentoRepository;
import br.com.viacaocalango.api.modules.passagens.domain.repository.PassagemRepository;
import br.com.viacaocalango.api.modules.passagens.domain.valueobject.PassagemId;
import br.com.viacaocalango.api.modules.passagens.domain.valueobject.TokenCartao;
import br.com.viacaocalango.api.shared.domain.RecursoNaoEncontrado;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PagamentoService {

    private final PassagemRepository passagens;
    private final PagamentoRepository pagamentos;
    private final GatewayCartao gateway;
    private final Clock clock;

    public PagamentoService(
            PassagemRepository passagens,
            PagamentoRepository pagamentos,
            GatewayCartao gateway,
            Clock clock) {
        this.passagens = passagens;
        this.pagamentos = pagamentos;
        this.gateway = gateway;
        this.clock = clock;
    }

    @Transactional
    public ResultadoPagamento pagar(RegistrarPagamentoComando comando) {
        Passagem passagem = obterParaAlteracao(comando.passagemId());
        OffsetDateTime agora = OffsetDateTime.now(clock);
        passagem.garantirNaoPaga();
        passagem.garantirFormaAceita(comando.forma());
        passagem.garantirReservaVigente(agora);
        StatusPagamento status = processar(comando, passagem.precoCentavos());
        Pagamento pagamento =
                Pagamento.registrar(
                        passagem.id(), comando.forma(), passagem.precoCentavos(), status, agora);
        pagamentos.salvar(pagamento);
        if (pagamento.aprovado()) {
            passagem.confirmar();
            passagens.salvar(passagem);
        }
        return new ResultadoPagamento(pagamento, passagem.status());
    }

    private StatusPagamento processar(RegistrarPagamentoComando comando, long valorCentavos) {
        return switch (comando.forma()) {
            case CARTAO -> gateway.autorizar(TokenCartao.de(comando.tokenCartao()), valorCentavos);
            case DINHEIRO -> StatusPagamento.APROVADO;
        };
    }

    private Passagem obterParaAlteracao(UUID id) {
        return passagens
                .buscarParaAlteracao(PassagemId.de(id))
                .orElseThrow(() -> new RecursoNaoEncontrado("Passagem nao encontrada"));
    }
}
