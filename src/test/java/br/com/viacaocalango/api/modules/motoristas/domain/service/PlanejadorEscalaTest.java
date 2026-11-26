package br.com.viacaocalango.api.modules.motoristas.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.viacaocalango.api.modules.motoristas.domain.valueobject.CandidatoCondutor;
import br.com.viacaocalango.api.modules.motoristas.domain.valueobject.EscalaViagem;
import br.com.viacaocalango.api.modules.motoristas.domain.valueobject.IntervaloHorario;
import br.com.viacaocalango.api.modules.motoristas.domain.valueobject.SolicitacaoEscala;
import br.com.viacaocalango.api.modules.motoristas.domain.valueobject.TrechoViagem;
import br.com.viacaocalango.api.modules.motoristas.domain.valueobject.TrocaCondutor;
import br.com.viacaocalango.api.shared.domain.DadoInvalido;
import br.com.viacaocalango.api.shared.domain.RegraDeNegocioViolada;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PlanejadorEscalaTest {

    private static final ZoneOffset FUSO = ZoneOffset.ofHours(-3);
    private final PlanejadorEscala planejador = new PlanejadorEscala();

    @Test
    void concluiTrechoNoLimiteExatoDeSeisHorasEExigeTrocaNoProximo() {
        UUID onibusId = UUID.randomUUID();
        UUID motorista1 = UUID.randomUUID();
        UUID motorista2 = UUID.randomUUID();

        OffsetDateTime t0 = dataHora(8, 0);
        TrechoViagem trecho1 =
                new TrechoViagem("Rio", "Juiz de Fora", t0, t0.plusHours(3), 180, true);
        TrechoViagem trecho2 =
                new TrechoViagem(
                        "Juiz de Fora",
                        "Barbacena",
                        t0.plusHours(3).plusMinutes(15),
                        t0.plusHours(6).plusMinutes(15),
                        120,
                        false);
        TrechoViagem trecho3 =
                new TrechoViagem(
                        "Barbacena",
                        "Belo Horizonte",
                        t0.plusHours(6).plusMinutes(30),
                        t0.plusHours(7),
                        50,
                        true);

        EscalaViagem escala =
                planejador.planejar(
                        new SolicitacaoEscala(
                                onibusId,
                                List.of(trecho1, trecho2, trecho3),
                                List.of(
                                        CandidatoCondutor.semHistorico(motorista1),
                                        CandidatoCondutor.semHistorico(motorista2))));

        assertThat(escala.trechos())
                .extracting(t -> t.motoristaId())
                .containsExactly(motorista1, motorista1, motorista2);
        assertThat(escala.trocas())
                .containsExactly(
                        new TrocaCondutor(
                                "Barbacena",
                                t0.plusHours(6).plusMinutes(30),
                                motorista1,
                                motorista2));
    }

    @Test
    void concluiTrechoNoLimiteExatoDeQuatrocentosKmEExigeTrocaNoProximo() {
        UUID onibusId = UUID.randomUUID();
        UUID motorista1 = UUID.randomUUID();
        UUID motorista2 = UUID.randomUUID();

        OffsetDateTime t0 = dataHora(6, 0);
        TrechoViagem trecho1 =
                new TrechoViagem("Origem", "Parada A", t0, t0.plusHours(2), 250, true);
        TrechoViagem trecho2 =
                new TrechoViagem(
                        "Parada A", "Parada B", t0.plusHours(2), t0.plusHours(4), 150, false);
        TrechoViagem trecho3 =
                new TrechoViagem(
                        "Parada B",
                        "Destino",
                        t0.plusHours(4),
                        t0.plusHours(4).plusMinutes(30),
                        20,
                        true);

        EscalaViagem escala =
                planejador.planejar(
                        new SolicitacaoEscala(
                                onibusId,
                                List.of(trecho1, trecho2, trecho3),
                                List.of(
                                        CandidatoCondutor.semHistorico(motorista1),
                                        CandidatoCondutor.semHistorico(motorista2))));

        assertThat(escala.trechos())
                .extracting(t -> t.motoristaId())
                .containsExactly(motorista1, motorista1, motorista2);
        assertThat(escala.trocas()).hasSize(1);
        assertThat(escala.trocas().getFirst().parada()).isEqualTo("Parada B");
    }

    @Test
    void trocaAntesQuandoCincoHorasEMeiaETrezentosECinquentaKmUltrapassariamSeisHoras() {
        UUID onibusId = UUID.randomUUID();
        UUID motorista1 = UUID.randomUUID();
        UUID motorista2 = UUID.randomUUID();

        OffsetDateTime t0 = dataHora(7, 0);
        TrechoViagem trecho1 =
                new TrechoViagem(
                        "Cidade A", "Cidade B", t0, t0.plusHours(5).plusMinutes(30), 350, true);
        TrechoViagem trecho2 =
                new TrechoViagem(
                        "Cidade B",
                        "Cidade C",
                        t0.plusHours(5).plusMinutes(30),
                        t0.plusHours(6).plusMinutes(15),
                        40,
                        true);

        EscalaViagem escala =
                planejador.planejar(
                        new SolicitacaoEscala(
                                onibusId,
                                List.of(trecho1, trecho2),
                                List.of(
                                        CandidatoCondutor.semHistorico(motorista1),
                                        CandidatoCondutor.semHistorico(motorista2))));

        assertThat(escala.trechos())
                .extracting(t -> t.motoristaId())
                .containsExactly(motorista1, motorista2);
        assertThat(escala.trocas())
                .containsExactly(
                        new TrocaCondutor(
                                "Cidade B",
                                t0.plusHours(5).plusMinutes(30),
                                motorista1,
                                motorista2));
    }

    @Test
    void trocaAntesQuandoDistanciaUltrapassaQuatrocentosKmMesmoComTempoMenorQueSeisHoras() {
        UUID onibusId = UUID.randomUUID();
        UUID motorista1 = UUID.randomUUID();
        UUID motorista2 = UUID.randomUUID();

        OffsetDateTime t0 = dataHora(9, 0);
        TrechoViagem trecho1 =
                new TrechoViagem("Ponto 1", "Ponto 2", t0, t0.plusHours(4), 380, true);
        TrechoViagem trecho2 =
                new TrechoViagem(
                        "Ponto 2",
                        "Ponto 3",
                        t0.plusHours(4),
                        t0.plusHours(4).plusMinutes(20),
                        21,
                        true);

        EscalaViagem escala =
                planejador.planejar(
                        new SolicitacaoEscala(
                                onibusId,
                                List.of(trecho1, trecho2),
                                List.of(
                                        CandidatoCondutor.semHistorico(motorista1),
                                        CandidatoCondutor.semHistorico(motorista2))));

        assertThat(escala.trechos())
                .extracting(t -> t.motoristaId())
                .containsExactly(motorista1, motorista2);
        assertThat(escala.trocas()).hasSize(1);
        assertThat(escala.trocas().getFirst().parada()).isEqualTo("Ponto 2");
    }

    @Test
    void aceitaSubstitutoComExatamenteDozeHorasDeDescanso() {
        UUID onibusId = UUID.randomUUID();
        UUID motorista1 = UUID.randomUUID();
        UUID motorista2 = UUID.randomUUID();

        OffsetDateTime t0 = dataHora(4, 0);
        TrechoViagem trecho1 = new TrechoViagem("Origem", "Hub", t0, t0.plusHours(4), 250, true);
        TrechoViagem trecho2 =
                new TrechoViagem("Hub", "Destino", t0.plusHours(4), t0.plusHours(7), 200, true);

        OffsetDateTime fimUltimaDirecaoSubstituto = t0.plusHours(4).minusHours(12);
        CandidatoCondutor substitutoDescansado =
                new CandidatoCondutor(
                        motorista2,
                        List.of(
                                IntervaloHorario.de(
                                        fimUltimaDirecaoSubstituto.minusHours(3),
                                        fimUltimaDirecaoSubstituto)),
                        List.of());

        EscalaViagem escala =
                planejador.planejar(
                        new SolicitacaoEscala(
                                onibusId,
                                List.of(trecho1, trecho2),
                                List.of(
                                        CandidatoCondutor.semHistorico(motorista1),
                                        substitutoDescansado)));

        assertThat(escala.trechos())
                .extracting(t -> t.motoristaId())
                .containsExactly(motorista1, motorista2);
    }

    @Test
    void recusaSubstitutoComOnzeHorasECinquentaENoveMinutosDeDescanso() {
        UUID onibusId = UUID.randomUUID();
        UUID motorista1 = UUID.randomUUID();
        UUID motorista2 = UUID.randomUUID();

        OffsetDateTime t0 = dataHora(4, 0);
        TrechoViagem trecho1 = new TrechoViagem("Origem", "Hub", t0, t0.plusHours(4), 250, true);
        TrechoViagem trecho2 =
                new TrechoViagem("Hub", "Destino", t0.plusHours(4), t0.plusHours(7), 200, true);

        OffsetDateTime fimUltimaDirecao = t0.plusHours(4).minusHours(11).minusMinutes(59);
        CandidatoCondutor substitutoSemDescanso =
                new CandidatoCondutor(
                        motorista2,
                        List.of(
                                IntervaloHorario.de(
                                        fimUltimaDirecao.minusHours(2), fimUltimaDirecao)),
                        List.of());

        assertThatThrownBy(() ->
                        planejador.planejar(
                                new SolicitacaoEscala(
                                        onibusId,
                                        List.of(trecho1, trecho2),
                                        List.of(
                                                CandidatoCondutor.semHistorico(motorista1),
                                                substitutoSemDescanso))))
                .isInstanceOf(RegraDeNegocioViolada.class);
    }

    @Test
    void recusaSubstitutoJaAlocadoEmHorarioIncompativel() {
        UUID onibusId = UUID.randomUUID();
        UUID motorista1 = UUID.randomUUID();
        UUID motorista2 = UUID.randomUUID();

        OffsetDateTime t0 = dataHora(8, 0);
        TrechoViagem trecho1 = new TrechoViagem("Origem", "Hub", t0, t0.plusHours(4), 250, true);
        TrechoViagem trecho2 =
                new TrechoViagem("Hub", "Destino", t0.plusHours(4), t0.plusHours(7), 200, true);

        CandidatoCondutor substitutoOcupado =
                new CandidatoCondutor(
                        motorista2,
                        List.of(),
                        List.of(IntervaloHorario.de(t0.plusHours(3), t0.plusHours(6))));

        assertThatThrownBy(() ->
                        planejador.planejar(
                                new SolicitacaoEscala(
                                        onibusId,
                                        List.of(trecho1, trecho2),
                                        List.of(
                                                CandidatoCondutor.semHistorico(motorista1),
                                                substitutoOcupado))))
                .isInstanceOf(RegraDeNegocioViolada.class);
    }

    @Test
    void antecipaTrocaParaUltimaParadaAutorizadaAntesDeParadaComum() {
        UUID onibusId = UUID.randomUUID();
        UUID motorista1 = UUID.randomUUID();
        UUID motorista2 = UUID.randomUUID();

        OffsetDateTime t0 = dataHora(6, 0);
        TrechoViagem trecho1 =
                new TrechoViagem(
                        "Terminal A", "Parada Autorizada B", t0, t0.plusHours(3), 200, true);
        TrechoViagem trecho2 =
                new TrechoViagem(
                        "Parada Autorizada B",
                        "Parada Comum C",
                        t0.plusHours(3),
                        t0.plusHours(5),
                        150,
                        true);
        TrechoViagem trecho3 =
                new TrechoViagem(
                        "Parada Comum C",
                        "Terminal D",
                        t0.plusHours(5),
                        t0.plusHours(7),
                        100,
                        false);

        EscalaViagem escala =
                planejador.planejar(
                        new SolicitacaoEscala(
                                onibusId,
                                List.of(trecho1, trecho2, trecho3),
                                List.of(
                                        CandidatoCondutor.semHistorico(motorista1),
                                        CandidatoCondutor.semHistorico(motorista2))));

        assertThat(escala.trechos())
                .extracting(t -> t.motoristaId())
                .containsExactly(motorista1, motorista2, motorista2);
        assertThat(escala.trocas())
                .containsExactly(
                        new TrocaCondutor(
                                "Parada Autorizada B", t0.plusHours(3), motorista1, motorista2));
    }

    @Test
    void falhaQuandoTrechoIndivisivelExcedeLimiteOuFaltaSubstituto() {
        UUID onibusId = UUID.randomUUID();
        UUID motorista1 = UUID.randomUUID();
        OffsetDateTime t0 = dataHora(8, 0);

        TrechoViagem trechoAcimaTempo =
                new TrechoViagem("A", "B", t0, t0.plusHours(6).plusMinutes(1), 300, true);
        TrechoViagem trechoAcimaDistancia =
                new TrechoViagem("A", "B", t0, t0.plusHours(4), 401, true);

        assertThatThrownBy(() ->
                        planejador.planejar(
                                new SolicitacaoEscala(
                                        onibusId,
                                        List.of(trechoAcimaTempo),
                                        List.of(CandidatoCondutor.semHistorico(motorista1)))))
                .isInstanceOf(RegraDeNegocioViolada.class);

        assertThatThrownBy(() ->
                        planejador.planejar(
                                new SolicitacaoEscala(
                                        onibusId,
                                        List.of(trechoAcimaDistancia),
                                        List.of(CandidatoCondutor.semHistorico(motorista1)))))
                .isInstanceOf(RegraDeNegocioViolada.class);
    }

    @Test
    void consideraMotoristaSemHistoricoAptoNaPrimeiraJornada() {
        UUID onibusId = UUID.randomUUID();
        UUID motoristaSemHistorico = UUID.randomUUID();
        OffsetDateTime t0 = dataHora(10, 0);

        TrechoViagem trecho = new TrechoViagem("Origem", "Destino", t0, t0.plusHours(3), 210, true);

        EscalaViagem escala =
                planejador.planejar(
                        new SolicitacaoEscala(
                                onibusId,
                                List.of(trecho),
                                List.of(CandidatoCondutor.semHistorico(motoristaSemHistorico))));

        assertThat(escala.trechos()).hasSize(1);
        assertThat(escala.trechos().getFirst().motoristaId()).isEqualTo(motoristaSemHistorico);
        assertThat(escala.trocas()).isEmpty();
    }

    @Test
    void rejeitaDadosIncoerentesComDadoInvalido() {
        OffsetDateTime t0 = dataHora(10, 0);

        assertThatThrownBy(() -> new TrechoViagem("A", "B", t0, t0.minusMinutes(10), 100, true))
                .isInstanceOf(DadoInvalido.class);
        assertThatThrownBy(() -> new TrechoViagem("A", "B", t0, t0.plusHours(1), 0, true))
                .isInstanceOf(DadoInvalido.class);
        assertThatThrownBy(() -> planejador.planejar(null)).isInstanceOf(DadoInvalido.class);

        TrechoViagem t1 = new TrechoViagem("A", "B", t0, t0.plusHours(1), 50, true);
        TrechoViagem t2Descontinuo =
                new TrechoViagem("C", "D", t0.plusHours(1), t0.plusHours(2), 50, true);

        assertThatThrownBy(() ->
                        new SolicitacaoEscala(
                                UUID.randomUUID(), List.of(t1, t2Descontinuo), List.of()))
                .isInstanceOf(DadoInvalido.class);
    }

    private static OffsetDateTime dataHora(int hora, int minuto) {
        return OffsetDateTime.of(2026, 11, 25, hora, minuto, 0, 0, FUSO);
    }
}
