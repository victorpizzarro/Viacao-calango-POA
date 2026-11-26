package br.com.viacaocalango.api.modules.passagens.presentation;

import br.com.viacaocalango.api.modules.passagens.application.Disponibilidade;
import br.com.viacaocalango.api.modules.passagens.domain.valueobject.ParadaId;
import br.com.viacaocalango.api.modules.passagens.domain.valueobject.TrechoOcupacao;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;

public record DisponibilidadeResponse(
        @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") String viagemId,
        @Schema(example = "32") int capacidade,
        List<TrechoResponse> trechos) {

    public record TrechoResponse(
            String paradaOrigemId,
            @Schema(example = "A") String paradaOrigemNome,
            String paradaDestinoId,
            @Schema(example = "B") String paradaDestinoNome,
            @Schema(example = "0") int lugaresOcupados,
            @Schema(example = "32") int lugaresDisponiveis) {

        static TrechoResponse de(TrechoOcupacao trecho, Map<ParadaId, String> nomes) {
            return new TrechoResponse(
                    trecho.origem().toString(),
                    nomes.get(trecho.origem()),
                    trecho.destino().toString(),
                    nomes.get(trecho.destino()),
                    trecho.lugaresOcupados(),
                    trecho.lugaresDisponiveis());
        }
    }

    static DisponibilidadeResponse de(Disponibilidade disponibilidade) {
        List<TrechoResponse> trechos =
                disponibilidade.trechos().stream()
                        .map(trecho -> TrechoResponse.de(trecho, disponibilidade.nomesDasParadas()))
                        .toList();
        return new DisponibilidadeResponse(
                disponibilidade.viagemId().toString(), disponibilidade.capacidade(), trechos);
    }
}
