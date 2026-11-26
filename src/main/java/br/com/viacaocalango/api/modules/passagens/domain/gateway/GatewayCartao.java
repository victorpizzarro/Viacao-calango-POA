package br.com.viacaocalango.api.modules.passagens.domain.gateway;

import br.com.viacaocalango.api.modules.passagens.domain.model.StatusPagamento;
import br.com.viacaocalango.api.modules.passagens.domain.valueobject.TokenCartao;

public interface GatewayCartao {

    StatusPagamento autorizar(TokenCartao token, long valorCentavos);
}
