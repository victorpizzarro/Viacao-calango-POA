package br.com.viacaocalango.api.modules.passagens.application;

import br.com.viacaocalango.api.modules.passagens.domain.model.CanalVenda;
import java.util.UUID;

public record ComprarPassagemComando(
        UUID viagemId,
        UUID paradaOrigemId,
        UUID paradaDestinoId,
        String passageiroNome,
        CanalVenda canal,
        String localVenda) {}
