package br.com.viacaocalango.api.modules.motoristas.domain.valueobject;

import br.com.viacaocalango.api.shared.domain.DadoInvalido;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record SolicitacaoEscala(
        UUID onibusId, List<TrechoViagem> trechos, List<CandidatoCondutor> candidatos) {

    public SolicitacaoEscala {
        if (onibusId == null) {
            throw new DadoInvalido("Identificador do onibus e obrigatorio");
        }
        if (trechos == null || trechos.isEmpty()) {
            throw new DadoInvalido("Viagem deve possuir ao menos um trecho");
        }
        trechos = List.copyOf(trechos);
        validarContinuidade(trechos);
        candidatos = candidatos == null ? List.of() : List.copyOf(candidatos);
        validarCandidatosUnicos(candidatos);
    }

    private static void validarContinuidade(List<TrechoViagem> trechos) {
        for (int i = 1; i < trechos.size(); i++) {
            TrechoViagem anterior = trechos.get(i - 1);
            TrechoViagem atual = trechos.get(i);
            if (!anterior.destino().equalsIgnoreCase(atual.origem())) {
                throw new DadoInvalido(
                        "Parada de origem do trecho deve coincidir com o destino do trecho"
                                + " anterior");
            }
            if (atual.partida().isBefore(anterior.chegada())) {
                throw new DadoInvalido(
                        "Trecho nao pode partir antes da chegada do trecho anterior");
            }
        }
    }

    private static void validarCandidatosUnicos(List<CandidatoCondutor> candidatos) {
        Set<UUID> ids = new HashSet<>();
        for (CandidatoCondutor candidato : candidatos) {
            if (!ids.add(candidato.motoristaId())) {
                throw new DadoInvalido("Lista de candidatos nao pode conter motoristas repetidos");
            }
        }
    }
}
