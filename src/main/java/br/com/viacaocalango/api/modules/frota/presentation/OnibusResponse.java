package br.com.viacaocalango.api.modules.frota.presentation;

import br.com.viacaocalango.api.modules.frota.domain.model.Onibus;
import br.com.viacaocalango.api.modules.frota.domain.model.TipoOnibus;
import io.swagger.v3.oas.annotations.media.Schema;

public record OnibusResponse(
        @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") String id,
        TipoOnibus tipo,
        @Schema(example = "28") int capacidade,
        @Schema(example = "12500") int quilometragemTotal,
        @Schema(example = "10000") int quilometragemUltimaRevisao,
        @Schema(example = "2500") int kmDesdeRevisao,
        @Schema(description = "Verdadeiro ao atingir 10.000 km desde a ultima revisao")
                boolean precisaRevisao,
        @Schema(description = "Falso enquanto o onibus aguarda revisao")
                boolean disponivelParaOperacao) {

    static OnibusResponse de(Onibus onibus) {
        return new OnibusResponse(
                onibus.id().toString(),
                onibus.tipo(),
                onibus.capacidade().lugares(),
                onibus.odometro().total().km(),
                onibus.odometro().naUltimaRevisao().km(),
                onibus.kmDesdeRevisao().km(),
                onibus.precisaRevisao(),
                onibus.disponivelParaOperacao());
    }
}
