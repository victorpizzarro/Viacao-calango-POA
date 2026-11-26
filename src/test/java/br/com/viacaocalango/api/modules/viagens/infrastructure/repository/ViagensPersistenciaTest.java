package br.com.viacaocalango.api.modules.viagens.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.viacaocalango.api.modules.viagens.domain.entity.*;
import br.com.viacaocalango.api.modules.viagens.domain.valueobject.*;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(
        properties = {
            "spring.datasource.url=jdbc:h2:mem:viagens_persistencia;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
            "spring.datasource.username=sa",
            "spring.datasource.password=",
            "logging.level.root=WARN"
        })
class ViagensPersistenciaTest {
    @Autowired private RotaRepository rotas;
    @Autowired private ViagemRepository viagens;
    @Autowired private JdbcTemplate jdbc;

    @BeforeEach
    void limpar() {
        jdbc.update("DELETE FROM viagem");
        jdbc.update("DELETE FROM parada");
        jdbc.update("DELETE FROM rota");
    }

    private Rota rota() {
        return new Rota(
                RotaId.novo(),
                "Rio-BH",
                List.of(
                        new Parada(
                                ParadaId.novo(),
                                "BH",
                                new KmDesdeOrigem(510),
                                new MinutosDesdeOrigem(420),
                                3),
                        new Parada(
                                ParadaId.novo(),
                                "Rio",
                                new KmDesdeOrigem(0),
                                new MinutosDesdeOrigem(0),
                                1),
                        new Parada(
                                ParadaId.novo(),
                                "JF",
                                new KmDesdeOrigem(250),
                                new MinutosDesdeOrigem(240),
                                2)));
    }

    @Test
    void reconstituiRotaParadasEUuidsEmTransacoesDistintas() {
        Rota original = rota();
        rotas.salvar(original);
        var carregada = rotas.buscarPorId(original.getId()).orElseThrow();
        assertThat(carregada).isNotSameAs(original);
        assertThat(carregada.getNome()).isEqualTo(original.getNome());
        assertThat(carregada.getParadas())
                .extracting(p -> p.getId().valor())
                .containsExactlyElementsOf(
                        original.getParadas().stream().map(p -> p.getId().valor()).toList());
        assertThat(carregada.getParadas())
                .extracting(Parada::getNome)
                .containsExactly("Rio", "JF", "BH");
        assertThat(carregada.getParadas())
                .extracting(p -> p.getKmDesdeOrigem().valor())
                .containsExactly(0.0, 250.0, 510.0);
        assertThat(carregada.getParadas())
                .extracting(p -> p.getMinutosDesdeOrigem().valor())
                .containsExactly(0, 240, 420);
        rotas.salvar(carregada);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM parada", Integer.class)).isEqualTo(3);
        rotas.excluir(original.getId());
        assertThat(rotas.buscarPorId(original.getId())).isEmpty();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM parada", Integer.class)).isZero();
    }

    @Test
    void preservaOrdemDeInsercaoAoRegravarViagemECarregaStatusEPartida() {
        var rota = rotas.salvar(rota());
        var primeira = Viagem.nova(rota, OffsetDateTime.parse("2100-02-01T08:00:00-03:00"));
        var segunda = Viagem.nova(rota, OffsetDateTime.parse("2100-01-01T08:00:00-03:00"));
        viagens.salvar(primeira);
        viagens.salvar(segunda);
        var atualizada =
                new Viagem(
                        primeira.id(), rota, primeira.dataHoraPartida(), StatusViagem.EM_ANDAMENTO);
        viagens.salvar(atualizada);
        assertThat(viagens.listar())
                .extracting(Viagem::id)
                .containsExactly(primeira.id(), segunda.id());
        var recarregada = viagens.buscar(primeira.id()).orElseThrow();
        assertThat(recarregada.status()).isEqualTo(StatusViagem.EM_ANDAMENTO);
        assertThat(recarregada.dataHoraPartida().toInstant())
                .isEqualTo(primeira.dataHoraPartida().toInstant());
        assertThat(recarregada.rota().getId()).isEqualTo(rota.getId());
        assertThat(recarregada.rota().getParadas())
                .extracting(Parada::getId)
                .containsExactlyElementsOf(rota.getParadas().stream().map(Parada::getId).toList());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM viagem", Integer.class)).isEqualTo(2);
    }
}
