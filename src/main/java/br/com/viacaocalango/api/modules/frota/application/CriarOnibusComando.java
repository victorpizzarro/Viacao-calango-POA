package br.com.viacaocalango.api.modules.frota.application;

import br.com.viacaocalango.api.modules.frota.domain.model.TipoOnibus;

public record CriarOnibusComando(TipoOnibus tipo, int capacidade, int quilometragemTotal) {}
