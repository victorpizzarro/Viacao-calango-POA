package br.com.viacaocalango.api.modules.viagens.infrastructure;

import br.com.viacaocalango.api.modules.frota.application.OnibusService;
import br.com.viacaocalango.api.modules.motoristas.application.EscalaService;
import br.com.viacaocalango.api.modules.viagens.application.*;
import br.com.viacaocalango.api.modules.viagens.application.service.PrecoFinalViagemService;
import br.com.viacaocalango.api.modules.viagens.application.service.ViagemService;
import br.com.viacaocalango.api.modules.viagens.domain.entity.StatusViagem;
import br.com.viacaocalango.api.shared.domain.RegraDeNegocioViolada;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ViagensParaVendaAdapter implements ViagensParaVenda {
    private final ViagemService viagens;
    private final EscalaService escalas;
    private final OnibusService frota;
    private final PrecoFinalViagemService precos;
    private final JdbcTemplate jdbc;

    public ViagensParaVendaAdapter(
            ViagemService viagens,
            EscalaService escalas,
            OnibusService frota,
            PrecoFinalViagemService precos,
            JdbcTemplate jdbc) {
        this.viagens = viagens;
        this.escalas = escalas;
        this.frota = frota;
        this.precos = precos;
        this.jdbc = jdbc;
    }

    @Override
    @Transactional(readOnly = true)
    public DadosViagemVenda obter(UUID viagemId) {
        var viagem = viagens.buscar(viagemId);
        if (viagem.status() != StatusViagem.AGENDADA) {
            throw new RegraDeNegocioViolada("Viagem nao esta agendada para venda");
        }
        if (jdbc.queryForList(
                        "SELECT viagem_id FROM motorista_escala WHERE viagem_id = ?",
                        UUID.class,
                        viagemId)
                .isEmpty()) {
            throw new RegraDeNegocioViolada(
                    "Viagem sem escala: capacidade para venda indisponivel");
        }
        var escala = escalas.buscar(viagemId);
        int capacidade = frota.buscar(escala.onibusId()).capacidade().lugares();
        var paradas =
                viagem.rota().getParadas().stream()
                        .map(p -> new DadosParadaVenda(p.getId().valor(), p.getNome()))
                        .toList();
        // Viagens ainda nao possui cadastro oficial de guiches. Nao inferir locais de nomes de
        // paradas.
        return new DadosViagemVenda(
                viagem.id(), viagem.dataHoraPartida(), capacidade, paradas, List.of());
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public DadosViagemVenda obterComBloqueio(UUID viagemId) {
        viagens.buscar(viagemId); // Preserva 404 para UUID inexistente.
        var ids =
                jdbc.queryForList(
                        "SELECT viagem_id FROM motorista_escala WHERE viagem_id = ? FOR UPDATE",
                        UUID.class,
                        viagemId);
        if (ids.isEmpty()) {
            throw new RegraDeNegocioViolada(
                    "Viagem sem escala: capacidade para venda indisponivel");
        }
        // Bloqueio JDBC no mesmo DataSource da transacao JPA, liberado somente no commit/rollback.
        return obter(viagemId);
    }

    @Override
    public long precoFinalCentavos(UUID viagemId, UUID paradaOrigemId, UUID paradaDestinoId) {
        return precos.cotar(viagemId, paradaOrigemId, paradaDestinoId, null).precoCentavos();
    }
}
