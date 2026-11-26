package br.com.viacaocalango.api.modules.motoristas.application;

import br.com.viacaocalango.api.modules.motoristas.domain.valueobject.TrechoViagem;
import java.util.List;
import java.util.UUID;

public interface ViagensParaEscala {
    List<TrechoViagem> obterTrechos(UUID viagemId, List<UUID> paradasTrocaAutorizada);
}
