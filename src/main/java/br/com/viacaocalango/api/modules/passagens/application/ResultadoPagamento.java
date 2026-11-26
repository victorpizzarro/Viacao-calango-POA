package br.com.viacaocalango.api.modules.passagens.application;

import br.com.viacaocalango.api.modules.passagens.domain.model.Pagamento;
import br.com.viacaocalango.api.modules.passagens.domain.model.StatusPassagem;

public record ResultadoPagamento(Pagamento pagamento, StatusPassagem statusPassagem) {}
