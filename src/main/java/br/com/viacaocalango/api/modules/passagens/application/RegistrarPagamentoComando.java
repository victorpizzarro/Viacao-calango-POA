package br.com.viacaocalango.api.modules.passagens.application;

import br.com.viacaocalango.api.modules.passagens.domain.model.FormaPagamento;
import java.util.UUID;

public record RegistrarPagamentoComando(
        UUID passagemId, FormaPagamento forma, String tokenCartao) {}
