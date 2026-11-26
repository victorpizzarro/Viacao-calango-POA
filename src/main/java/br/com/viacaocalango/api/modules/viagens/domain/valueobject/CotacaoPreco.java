package br.com.viacaocalango.api.modules.viagens.domain.valueobject;

public record CotacaoPreco(KmDesdeOrigem distancia, TarifaBaseKm tarifa) {

    public double calcular() {
        return tarifa.calcular(distancia.valor());
    }
}
