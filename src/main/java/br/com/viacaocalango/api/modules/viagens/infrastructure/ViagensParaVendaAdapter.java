package br.com.viacaocalango.api.modules.viagens.infrastructure;

import br.com.viacaocalango.api.modules.frota.application.OnibusService;
import br.com.viacaocalango.api.modules.viagens.application.*;
import br.com.viacaocalango.api.modules.viagens.application.service.PrecoFinalViagemService;
import br.com.viacaocalango.api.modules.viagens.application.service.ViagemService;
import br.com.viacaocalango.api.modules.viagens.domain.entity.StatusViagem;
import br.com.viacaocalango.api.modules.viagens.domain.entity.Viagem;
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
    private final OnibusService frota;
    private final PrecoFinalViagemService precos;
    private final JdbcTemplate jdbc;

    public ViagensParaVendaAdapter(
            ViagemService viagens,
            OnibusService frota,
            PrecoFinalViagemService precos,
            JdbcTemplate jdbc) {
        this.viagens = viagens;
        this.frota = frota;
        this.precos = precos;
        this.jdbc = jdbc;
    }

    @Override
    @Transactional(readOnly = true)
    public DadosViagemVenda obter(UUID viagemId) {
        var viagem = viagens.buscar(viagemId);
        garantirAgendada(viagem);
        return dadosParaVenda(viagem, buscarOnibus(viagemId, false));
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public DadosViagemVenda obterComBloqueio(UUID viagemId) {
        var viagem = viagens.buscar(viagemId);
        UUID onibusId = buscarOnibus(viagemId, true);
        garantirAgendada(viagem);
        return dadosParaVenda(viagem, onibusId);
    }

    private UUID buscarOnibus(UUID viagemId, boolean bloquear) {
        String sql = "SELECT onibus_id FROM motorista_escala WHERE viagem_id = ?";
        // A compra mantem o bloqueio ate a reserva ser gravada na mesma transacao.
        var ids = jdbc.queryForList(bloquear ? sql + " FOR UPDATE" : sql, UUID.class, viagemId);
        return ids.stream()
                .findFirst()
                .orElseThrow(() ->
                        new RegraDeNegocioViolada(
                                "Viagem sem escala: capacidade para venda indisponivel"));
    }

    private void garantirAgendada(Viagem viagem) {
        if (viagem.status() != StatusViagem.AGENDADA) {
            throw new RegraDeNegocioViolada("Viagem nao esta agendada para venda");
        }
    }

    private DadosViagemVenda dadosParaVenda(Viagem viagem, UUID onibusId) {
        int capacidade = frota.buscar(onibusId).capacidade().lugares();
        var paradas =
                viagem.rota().getParadas().stream()
                        .map(p -> new DadosParadaVenda(p.getId().valor(), p.getNome()))
                        .toList();
        // Viagens ainda nao cadastra locais de venda para guiches.
        return new DadosViagemVenda(
                viagem.id(), viagem.dataHoraPartida(), capacidade, paradas, List.of());
    }

    @Override
    public long precoFinalCentavos(UUID viagemId, UUID paradaOrigemId, UUID paradaDestinoId) {
        return precos.cotar(viagemId, paradaOrigemId, paradaDestinoId, null).precoCentavos();
    }
}
