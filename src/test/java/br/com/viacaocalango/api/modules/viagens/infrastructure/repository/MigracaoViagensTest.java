package br.com.viacaocalango.api.modules.viagens.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

class MigracaoViagensTest {
    @Test
    void v5PreservaEscalasPassagensEPagamentosLegadosSemFabricarViagens() {
        String url =
                "jdbc:h2:mem:legado_" + UUID.randomUUID() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
        var source = new SingleConnectionDataSource(url, "sa", "", true);
        Flyway.configure().dataSource(source).target("4").load().migrate();
        var jdbc = new JdbcTemplate(source);
        UUID onibus = UUID.randomUUID(),
                motorista = UUID.randomUUID(),
                viagem = UUID.randomUUID(),
                passagem = UUID.randomUUID(),
                pagamento = UUID.randomUUID();
        jdbc.update("INSERT INTO onibus VALUES (?, 'LEITO', 23, 0, 0)", onibus);
        jdbc.update("INSERT INTO motoristas VALUES (?, 'Legado')", motorista);
        jdbc.update("INSERT INTO motorista_escala VALUES (?, ?)", viagem, onibus);
        jdbc.update(
                "INSERT INTO motorista_escala_trecho VALUES (?, 1, 'Rio', 'JF', TIMESTAMP WITH TIME ZONE '2026-11-26 08:00:00-03:00', TIMESTAMP WITH TIME ZONE '2026-11-26 12:00:00-03:00', 250, ?)",
                viagem,
                motorista);
        jdbc.update(
                "INSERT INTO passagem VALUES (?, ?, ?, ?, 'Maria', 'INTERNET', NULL, 12500, 'CONFIRMADA', CURRENT_TIMESTAMP)",
                passagem,
                viagem,
                UUID.randomUUID(),
                UUID.randomUUID());
        jdbc.update(
                "INSERT INTO pagamento VALUES (?, ?, 'CARTAO', 12500, 'APROVADO', CURRENT_TIMESTAMP)",
                pagamento,
                passagem);
        Flyway.configure().dataSource(source).load().migrate();
        assertThat(jdbc.queryForObject("SELECT viagem_id FROM motorista_escala", UUID.class))
                .isEqualTo(viagem);
        assertThat(
                        jdbc.queryForObject(
                                "SELECT viagem_id FROM passagem WHERE id = ?",
                                UUID.class,
                                passagem))
                .isEqualTo(viagem);
        assertThat(
                        jdbc.queryForObject(
                                "SELECT valor_centavos FROM pagamento WHERE id = ?",
                                Long.class,
                                pagamento))
                .isEqualTo(12500L);
        assertThat(
                        jdbc.queryForObject(
                                "SELECT COUNT(*) FROM motorista_escala_trecho", Integer.class))
                .isEqualTo(1);
        for (String tabela : java.util.List.of("viagem", "rota", "parada")) {
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM " + tabela, Integer.class))
                    .isZero();
        }
        source.destroy();
    }
}
