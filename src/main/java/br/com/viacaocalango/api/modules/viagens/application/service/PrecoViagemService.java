package br.com.viacaocalango.api.modules.viagens.application.service;

import br.com.viacaocalango.api.modules.viagens.domain.entity.Parada;
import br.com.viacaocalango.api.modules.viagens.domain.entity.Rota;
import br.com.viacaocalango.api.modules.viagens.domain.valueobject.CotacaoPreco;
import br.com.viacaocalango.api.modules.viagens.domain.valueobject.TarifaBaseKm;
import br.com.viacaocalango.api.modules.viagens.domain.valueobject.TrechoRota;

public class PrecoViagemService {

    private final TrechoRotaService trechoRotaService;

    public PrecoViagemService() {
        this.trechoRotaService = new TrechoRotaService();
    }

    public double calcularPreco(Rota rota, Parada origem, Parada destino, TarifaBaseKm tarifa) {
        TrechoRota trecho = trechoRotaService.calcularTrecho(rota, origem, destino);

        CotacaoPreco cotacao =
                new CotacaoPreco(
                        new br.com.viacaocalango.api.modules.viagens.domain.valueobject
                                .KmDesdeOrigem(trecho.calcularDistancia()),
                        tarifa);

        return cotacao.calcular();
    }
}
