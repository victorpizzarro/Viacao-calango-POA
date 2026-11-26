package br.com.viacaocalango.api.modules.motoristas.domain.service;

import br.com.viacaocalango.api.modules.motoristas.domain.valueobject.CandidatoCondutor;
import br.com.viacaocalango.api.modules.motoristas.domain.valueobject.EscalaViagem;
import br.com.viacaocalango.api.modules.motoristas.domain.valueobject.IntervaloHorario;
import br.com.viacaocalango.api.modules.motoristas.domain.valueobject.SolicitacaoEscala;
import br.com.viacaocalango.api.modules.motoristas.domain.valueobject.TrechoEscalado;
import br.com.viacaocalango.api.modules.motoristas.domain.valueobject.TrechoViagem;
import br.com.viacaocalango.api.shared.domain.DadoInvalido;
import br.com.viacaocalango.api.shared.domain.RegraDeNegocioViolada;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class PlanejadorEscala {

    public static final Duration TEMPO_MAXIMO_CONTINUO = Duration.ofHours(6);
    public static final int DISTANCIA_MAXIMA_CONTINUA_KM = 400;
    public static final Duration DESCANSO_MINIMO = Duration.ofHours(12);

    public EscalaViagem planejar(SolicitacaoEscala solicitacao) {
        if (solicitacao == null) {
            throw new DadoInvalido("Solicitacao de escala e obrigatoria");
        }
        List<TrechoViagem> trechos = solicitacao.trechos();
        List<CandidatoCondutor> candidatos = solicitacao.candidatos();

        if (candidatos.isEmpty()) {
            throw new RegraDeNegocioViolada("Nao ha motoristas disponiveis para escalar a viagem");
        }
        validarTrechosIndividuais(trechos);

        UUID[] escolhaPorTrecho = new UUID[trechos.size()];
        List<List<IntervaloHorario>> blocosNaViagem = new ArrayList<>(candidatos.size());
        for (int i = 0; i < candidatos.size(); i++) {
            blocosNaViagem.add(new ArrayList<>());
        }

        boolean encontrou =
                buscarEscala(
                        trechos,
                        candidatos,
                        0,
                        -1,
                        null,
                        Duration.ZERO,
                        0,
                        blocosNaViagem,
                        escolhaPorTrecho,
                        new HashSet<>());

        if (!encontrou) {
            throw new RegraDeNegocioViolada(
                    "Nao foi possivel montar uma escala completa respeitando limites, descanso e"
                            + " paradas autorizadas");
        }

        List<TrechoEscalado> trechosEscalados = new ArrayList<>(trechos.size());
        for (int i = 0; i < trechos.size(); i++) {
            trechosEscalados.add(TrechoEscalado.de(i + 1, trechos.get(i), escolhaPorTrecho[i]));
        }
        return EscalaViagem.aPartirDeTrechos(solicitacao.onibusId(), trechosEscalados);
    }

    private void validarTrechosIndividuais(List<TrechoViagem> trechos) {
        for (TrechoViagem trecho : trechos) {
            if (trecho.duracao().compareTo(TEMPO_MAXIMO_CONTINUO) > 0
                    || trecho.distanciaKm() > DISTANCIA_MAXIMA_CONTINUA_KM) {
                throw new RegraDeNegocioViolada(
                        "Trecho excede o limite maximo continuo de 6 horas ou 400 km");
            }
        }
    }

    private boolean buscarEscala(
            List<TrechoViagem> trechos,
            List<CandidatoCondutor> candidatos,
            int indiceTrecho,
            int indiceCandidatoAtual,
            OffsetDateTime inicioBlocoAtual,
            Duration tempoAcumulado,
            int kmAcumulado,
            List<List<IntervaloHorario>> blocosNaViagem,
            UUID[] escolhaPorTrecho,
            Set<ChaveEstado> estadosFalhos) {
        if (indiceTrecho == trechos.size()) {
            return true;
        }

        ChaveEstado estado =
                criarChaveEstado(
                        indiceTrecho,
                        indiceCandidatoAtual,
                        tempoAcumulado,
                        kmAcumulado,
                        blocosNaViagem);
        if (estadosFalhos.contains(estado)) {
            return false;
        }

        TrechoViagem trechoAtual = trechos.get(indiceTrecho);

        if (indiceTrecho == 0) {
            for (int c = 0; c < candidatos.size(); c++) {
                if (podeAssumirNovoBloco(candidatos.get(c), blocosNaViagem.get(c), trechoAtual)) {
                    escolhaPorTrecho[0] = candidatos.get(c).motoristaId();
                    if (buscarEscala(
                            trechos,
                            candidatos,
                            1,
                            c,
                            trechoAtual.partida(),
                            trechoAtual.duracao(),
                            trechoAtual.distanciaKm(),
                            blocosNaViagem,
                            escolhaPorTrecho,
                            estadosFalhos)) {
                        return true;
                    }
                }
            }
            estadosFalhos.add(estado);
            return false;
        }

        CandidatoCondutor atual = candidatos.get(indiceCandidatoAtual);
        if (podeContinuarBloco(
                atual,
                blocosNaViagem.get(indiceCandidatoAtual),
                tempoAcumulado,
                kmAcumulado,
                trechoAtual)) {
            escolhaPorTrecho[indiceTrecho] = atual.motoristaId();
            if (buscarEscala(
                    trechos,
                    candidatos,
                    indiceTrecho + 1,
                    indiceCandidatoAtual,
                    inicioBlocoAtual,
                    tempoAcumulado.plus(trechoAtual.duracao()),
                    kmAcumulado + trechoAtual.distanciaKm(),
                    blocosNaViagem,
                    escolhaPorTrecho,
                    estadosFalhos)) {
                return true;
            }
        }

        if (trechoAtual.trocaAutorizadaNaOrigem()) {
            IntervaloHorario blocoEncerrado =
                    new IntervaloHorario(inicioBlocoAtual, trechos.get(indiceTrecho - 1).chegada());
            blocosNaViagem.get(indiceCandidatoAtual).add(blocoEncerrado);

            for (int c = 0; c < candidatos.size(); c++) {
                if (c == indiceCandidatoAtual) {
                    continue;
                }
                if (podeAssumirNovoBloco(candidatos.get(c), blocosNaViagem.get(c), trechoAtual)) {
                    escolhaPorTrecho[indiceTrecho] = candidatos.get(c).motoristaId();
                    if (buscarEscala(
                            trechos,
                            candidatos,
                            indiceTrecho + 1,
                            c,
                            trechoAtual.partida(),
                            trechoAtual.duracao(),
                            trechoAtual.distanciaKm(),
                            blocosNaViagem,
                            escolhaPorTrecho,
                            estadosFalhos)) {
                        return true;
                    }
                }
            }

            blocosNaViagem.get(indiceCandidatoAtual).removeLast();
        }

        estadosFalhos.add(estado);
        return false;
    }

    private boolean podeAssumirNovoBloco(
            CandidatoCondutor candidato,
            List<IntervaloHorario> blocosAnterioresNaViagem,
            TrechoViagem trecho) {
        IntervaloHorario intervaloTrecho = trecho.intervalo();
        if (possuiConflitoAlocacao(candidato, intervaloTrecho)) {
            return false;
        }

        for (IntervaloHorario dirigido :
                todosPeriodosDirigidos(candidato, blocosAnterioresNaViagem)) {
            if (dirigido.sobrepoe(intervaloTrecho)) {
                return false;
            }
            if (!dirigido.fim().isAfter(trecho.partida())) {
                Duration descanso = Duration.between(dirigido.fim(), trecho.partida());
                if (descanso.compareTo(DESCANSO_MINIMO) < 0) {
                    return false;
                }
            } else if (!dirigido.inicio().isBefore(trecho.chegada())) {
                Duration descansoFuturo = Duration.between(trecho.chegada(), dirigido.inicio());
                if (descansoFuturo.compareTo(DESCANSO_MINIMO) < 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean podeContinuarBloco(
            CandidatoCondutor candidato,
            List<IntervaloHorario> blocosAnterioresNaViagem,
            Duration tempoAcumulado,
            int kmAcumulado,
            TrechoViagem trecho) {
        Duration novoTempo = tempoAcumulado.plus(trecho.duracao());
        int novoKm = kmAcumulado + trecho.distanciaKm();
        if (novoTempo.compareTo(TEMPO_MAXIMO_CONTINUO) > 0
                || novoKm > DISTANCIA_MAXIMA_CONTINUA_KM) {
            return false;
        }

        IntervaloHorario intervaloTrecho = trecho.intervalo();
        if (possuiConflitoAlocacao(candidato, intervaloTrecho)) {
            return false;
        }

        for (IntervaloHorario dirigido :
                todosPeriodosDirigidos(candidato, blocosAnterioresNaViagem)) {
            if (dirigido.sobrepoe(intervaloTrecho)) {
                return false;
            }
            if (!dirigido.inicio().isBefore(trecho.chegada())) {
                Duration descansoFuturo = Duration.between(trecho.chegada(), dirigido.inicio());
                if (descansoFuturo.compareTo(DESCANSO_MINIMO) < 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean possuiConflitoAlocacao(
            CandidatoCondutor candidato, IntervaloHorario intervaloTrecho) {
        for (IntervaloHorario alocacao : candidato.alocacoesExistentes()) {
            if (alocacao.sobrepoe(intervaloTrecho)) {
                return true;
            }
        }
        return false;
    }

    private List<IntervaloHorario> todosPeriodosDirigidos(
            CandidatoCondutor candidato, List<IntervaloHorario> blocosAnterioresNaViagem) {
        if (blocosAnterioresNaViagem.isEmpty()) {
            return candidato.periodosDirigidos();
        }
        List<IntervaloHorario> todos =
                new ArrayList<>(
                        candidato.periodosDirigidos().size() + blocosAnterioresNaViagem.size());
        todos.addAll(candidato.periodosDirigidos());
        todos.addAll(blocosAnterioresNaViagem);
        return todos;
    }

    private ChaveEstado criarChaveEstado(
            int indiceTrecho,
            int indiceCandidatoAtual,
            Duration tempoAcumulado,
            int kmAcumulado,
            List<List<IntervaloHorario>> blocosNaViagem) {
        List<List<IntervaloHorario>> copiaBlocos = new ArrayList<>(blocosNaViagem.size());
        for (List<IntervaloHorario> blocos : blocosNaViagem) {
            copiaBlocos.add(List.copyOf(blocos));
        }
        return new ChaveEstado(
                indiceTrecho,
                indiceCandidatoAtual,
                tempoAcumulado.toSeconds(),
                kmAcumulado,
                List.copyOf(copiaBlocos));
    }

    private record ChaveEstado(
            int indiceTrecho,
            int indiceCandidatoAtual,
            long segundosAcumulados,
            int kmAcumulado,
            List<List<IntervaloHorario>> blocosNaViagem) {}
}
