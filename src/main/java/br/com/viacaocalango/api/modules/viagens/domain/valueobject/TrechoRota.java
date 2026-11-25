
package br.com.viacaocalango.api.modules.viagens.domain.valueobject;

public record TrechoRota(
        KmDesdeOrigem origem,
        KmDesdeOrigem destino,
        MinutosDesdeOrigem tempoOrigem,
        MinutosDesdeOrigem tempoDestino
) {

    public double calcularDistancia() {
        return destino.diferenca(origem);
    }

    public int calcularDuracao() {
        return tempoDestino.diferenca(tempoOrigem);
    }
}
