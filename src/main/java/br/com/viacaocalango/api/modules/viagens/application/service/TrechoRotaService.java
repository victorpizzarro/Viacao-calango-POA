
package br.com.viacaocalango.api.modules.viagens.application.service;

import br.com.viacaocalango.api.modules.viagens.domain.entity.Parada;
import br.com.viacaocalango.api.modules.viagens.domain.entity.Rota;
import br.com.viacaocalango.api.modules.viagens.domain.valueobject.TrechoRota;
import br.com.viacaocalango.api.shared.domain.DadoInvalido;

public class TrechoRotaService {

    public TrechoRota calcularTrecho(
            Rota rota,
            Parada origem,
            Parada destino
    ) {
        validarParadas(rota, origem, destino);

        return new TrechoRota(
                origem.getKmDesdeOrigem(),
                destino.getKmDesdeOrigem(),
                origem.getMinutosDesdeOrigem(),
                destino.getMinutosDesdeOrigem()
        );
    }

    private void validarParadas(
            Rota rota,
            Parada origem,
            Parada destino
    ) {
        if (origem.getOrdem() >= destino.getOrdem()) {
            throw new DadoInvalido(
                    "A origem deve vir antes do destino na rota"
            );
        }

        if (!rota.getParadas().contains(origem)
                || !rota.getParadas().contains(destino)) {
            throw new DadoInvalido(
                    "As paradas devem pertencer a rota informada"
            );
        }
    }
}
