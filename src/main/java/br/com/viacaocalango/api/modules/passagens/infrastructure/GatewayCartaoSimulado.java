package br.com.viacaocalango.api.modules.passagens.infrastructure;

import br.com.viacaocalango.api.modules.passagens.domain.gateway.GatewayCartao;
import br.com.viacaocalango.api.modules.passagens.domain.model.StatusPagamento;
import br.com.viacaocalango.api.modules.passagens.domain.valueobject.TokenCartao;
import org.springframework.stereotype.Component;

@Component
class GatewayCartaoSimulado implements GatewayCartao {

    private static final String PREFIXO_RECUSADO = "recusar";

    @Override
    public StatusPagamento autorizar(TokenCartao token, long valorCentavos) {
        if (token.valor().startsWith(PREFIXO_RECUSADO)) {
            return StatusPagamento.RECUSADO;
        }
        return StatusPagamento.APROVADO;
    }
}
