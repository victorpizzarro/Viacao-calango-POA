package br.com.viacaocalango.api.modules.viagens.domain.valueobject;

import br.com.viacaocalango.api.shared.domain.DadoInvalido;

public record TarifaBaseKm(double valor) {

    public TarifaBaseKm {
        if (valor <= 0) {
            throw new DadoInvalido("Tarifa por km deve ser maior que zero");
        }
    }

    public double calcular(double distanciaKm) {
        if (distanciaKm < 0) {
            throw new DadoInvalido("Distancia nao pode ser negativa");
        }

        return valor * distanciaKm;
    }
}