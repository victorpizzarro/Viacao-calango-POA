package br.com.viacaocalango.api.modules.motoristas.infrastructure;

import br.com.viacaocalango.api.modules.motoristas.application.EscalaRepository;
import br.com.viacaocalango.api.modules.motoristas.domain.valueobject.*;
import br.com.viacaocalango.api.shared.domain.RecursoNaoEncontrado;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class EscalaRepositoryJdbc implements EscalaRepository {
    private final JdbcTemplate jdbc;

    EscalaRepositoryJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<UUID> bloquearCandidatos() {
        return jdbc.queryForList("SELECT id FROM motoristas ORDER BY id FOR UPDATE", UUID.class);
    }

    @Override
    public void bloquearMotorista(UUID id) {
        var encontrados =
                jdbc.queryForList(
                        "SELECT id FROM motoristas WHERE id = ? FOR UPDATE", UUID.class, id);
        if (encontrados.isEmpty()) {
            throw new RecursoNaoEncontrado("Motorista nao encontrado");
        }
    }

    @Override
    public CandidatoCondutor carregarCandidato(UUID id) {
        var periodos =
                jdbc.query(
                        "SELECT inicio, fim FROM motorista_periodo_dirigido WHERE motorista_id = ? ORDER BY inicio",
                        (rs, n) ->
                                new IntervaloHorario(
                                        rs.getObject("inicio", OffsetDateTime.class),
                                        rs.getObject("fim", OffsetDateTime.class)),
                        id);
        var alocacoes =
                jdbc.query(
                        "SELECT partida, chegada FROM motorista_escala_trecho WHERE motorista_id = ? ORDER BY partida",
                        (rs, n) ->
                                new IntervaloHorario(
                                        rs.getObject("partida", OffsetDateTime.class),
                                        rs.getObject("chegada", OffsetDateTime.class)),
                        id);
        return periodos.isEmpty() && alocacoes.isEmpty()
                ? CandidatoCondutor.semHistorico(id)
                : new CandidatoCondutor(id, periodos, alocacoes);
    }

    @Override
    public Optional<EscalaViagem> buscar(UUID viagemId) {
        var onibus =
                jdbc.queryForList(
                        "SELECT onibus_id FROM motorista_escala WHERE viagem_id = ?",
                        UUID.class,
                        viagemId);
        if (onibus.isEmpty()) {
            return Optional.empty();
        }
        var trechos =
                jdbc.query(
                        "SELECT * FROM motorista_escala_trecho WHERE viagem_id = ? ORDER BY ordem",
                        (rs, n) ->
                                new TrechoEscalado(
                                        rs.getInt("ordem"),
                                        rs.getString("origem"),
                                        rs.getString("destino"),
                                        rs.getObject("partida", OffsetDateTime.class),
                                        rs.getObject("chegada", OffsetDateTime.class),
                                        rs.getInt("distancia_km"),
                                        rs.getObject("motorista_id", UUID.class)),
                        viagemId);
        return Optional.of(EscalaViagem.aPartirDeTrechos(onibus.getFirst(), trechos));
    }

    @Override
    public void salvar(UUID viagemId, EscalaViagem escala) {
        jdbc.update(
                "INSERT INTO motorista_escala (viagem_id, onibus_id) VALUES (?, ?)",
                viagemId,
                escala.onibusId());
        for (var trecho : escala.trechos()) {
            jdbc.update(
                    "INSERT INTO motorista_escala_trecho (viagem_id, ordem, origem, destino, partida, chegada, distancia_km, motorista_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    viagemId,
                    trecho.ordem(),
                    trecho.origem(),
                    trecho.destino(),
                    trecho.partida(),
                    trecho.chegada(),
                    trecho.distanciaKm(),
                    trecho.motoristaId());
        }
    }

    @Override
    public void registrarPeriodo(UUID motoristaId, IntervaloHorario periodo) {
        jdbc.update(
                "INSERT INTO motorista_periodo_dirigido (id, motorista_id, inicio, fim) VALUES (?, ?, ?, ?)",
                UUID.randomUUID(),
                motoristaId,
                periodo.inicio(),
                periodo.fim());
    }
}
