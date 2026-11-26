package br.com.viacaocalango.api.modules.viagens.application.service;

import br.com.viacaocalango.api.modules.viagens.domain.entity.Parada;
import br.com.viacaocalango.api.modules.viagens.domain.valueobject.TarifaBaseKm;
import br.com.viacaocalango.api.shared.domain.DadoInvalido;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class PrecoFinalViagemService {
    private final ViagemService viagens;
    private final TrechoRotaService trechos;
    private final PrecoViagemService precos;
    private final Clock clock;

    public PrecoFinalViagemService(
            ViagemService viagens,
            TrechoRotaService trechos,
            PrecoViagemService precos,
            Clock clock) {
        this.viagens = viagens;
        this.trechos = trechos;
        this.precos = precos;
        this.clock = clock;
    }

    public record Cotacao(
            double distanciaKm,
            long precoCentavos,
            boolean percursoCompleto,
            boolean descontoAntecedencia) {}

    public Cotacao cotar(UUID viagemId, UUID origemId, UUID destinoId, OffsetDateTime compra) {
        var viagem = viagens.buscar(viagemId);
        var rota = viagem.rota();
        Parada origem =
                rota.getParadas().stream()
                        .filter(p -> p.getId().valor().equals(origemId))
                        .findFirst()
                        .orElseThrow(() -> new DadoInvalido("Origem nao encontrada na rota"));
        Parada destino =
                rota.getParadas().stream()
                        .filter(p -> p.getId().valor().equals(destinoId))
                        .findFirst()
                        .orElseThrow(() -> new DadoInvalido("Destino nao encontrada na rota"));
        var trecho = trechos.calcularTrecho(rota, origem, destino);
        double base = precos.calcularPreco(rota, origem, destino, new TarifaBaseKm(0.50));
        boolean completo = origem.getOrdem() == 1 && destino.getOrdem() == rota.getParadas().size();
        OffsetDateTime dataCompra = compra == null ? OffsetDateTime.now(clock) : compra;
        boolean antecipado = !dataCompra.plusDays(7).isAfter(viagem.dataHoraPartida());
        double desconto = (completo ? 0.10 : 0.0) + (antecipado ? 0.05 : 0.0);
        long centavos =
                BigDecimal.valueOf(base * (1.0 - desconto))
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(0, RoundingMode.HALF_UP)
                        .longValue();
        return new Cotacao(trecho.calcularDistancia(), centavos, completo, antecipado);
    }
}
