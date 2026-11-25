package br.com.viacaocalango.api.modules.frota.presentation;

import br.com.viacaocalango.api.modules.frota.domain.valueobject.AlocacaoIdeal;
import br.com.viacaocalango.api.modules.frota.domain.valueobject.Capacidade;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record AlocacaoIdealResponse(
        @Schema(example = "60") int passageiros,
        @Schema(description = "Capacidade de cada onibus", example = "[32, 28]")
                List<Integer> capacidades,
        @Schema(example = "2") int totalDeOnibus,
        @Schema(example = "0") int lugaresVazios) {

    static AlocacaoIdealResponse de(AlocacaoIdeal alocacao) {
        return new AlocacaoIdealResponse(
                alocacao.passageiros().quantidade(),
                alocacao.capacidades().stream().map(Capacidade::lugares).toList(),
                alocacao.totalDeOnibus(),
                alocacao.lugaresVazios());
    }
}
