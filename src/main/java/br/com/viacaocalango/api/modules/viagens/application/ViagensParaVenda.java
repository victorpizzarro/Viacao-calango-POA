package br.com.viacaocalango.api.modules.viagens.application;

import java.util.UUID;

public interface ViagensParaVenda {

    DadosViagemVenda obter(UUID viagemId);

    DadosViagemVenda obterComBloqueio(UUID viagemId);

    long precoFinalCentavos(UUID viagemId, UUID paradaOrigemId, UUID paradaDestinoId);
}
