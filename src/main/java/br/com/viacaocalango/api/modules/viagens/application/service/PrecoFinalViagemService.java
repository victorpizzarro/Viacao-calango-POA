package br.com.viacaocalango.api.modules.viagens.application.service;

import br.com.viacaocalango.api.modules.viagens.domain.entity.Parada;
import br.com.viacaocalango.api.modules.viagens.domain.entity.Viagem;
import br.com.viacaocalango.api.modules.viagens.domain.valueobject.TarifaBaseKm;
import br.com.viacaocalango.api.shared.domain.DadoInvalido;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import org.springframework.stereotype.Service;

@Service
public class PrecoFinalViagemService {
    private static final TarifaBaseKm TARIFA = new TarifaBaseKm(0.50);

    private final ViagemService viagens;
    private final Clock clock;

    public PrecoFinalViagemService(ViagemService viagens, Clock clock) {
        this.viagens = viagens;
        this.clock = clock;
    }

    public record Cotacao(
            double distanciaKm,
            long precoCentavos,
            boolean percursoCompleto,
            boolean descontoAntecedencia) {}

    public Cotacao cotar(UUID viagemId, UUID origemId, UUID destinoId, OffsetDateTime compra) {
        return cotar(
                viagens.buscar(viagemId),
                p -> p.getId().valor().equals(origemId),
                p -> p.getId().valor().equals(destinoId),
                compra);
    }

    public Cotacao cotarPorNomes(
            UUID viagemId, String origem, String destino, OffsetDateTime compra) {
        return cotar(
                viagens.buscar(viagemId),
                p -> p.getNome().equalsIgnoreCase(origem),
                p -> p.getNome().equalsIgnoreCase(destino),
                compra);
    }

    private Cotacao cotar(
            Viagem viagem,
            Predicate<Parada> filtroOrigem,
            Predicate<Parada> filtroDestino,
            OffsetDateTime compra) {
        List<Parada> paradas = viagem.rota().getParadas();
        Parada origem = buscarParada(paradas, filtroOrigem, "Origem nao encontrada na rota");
        Parada destino = buscarParada(paradas, filtroDestino, "Destino nao encontrada na rota");
        if (origem.getOrdem() >= destino.getOrdem()) {
            throw new DadoInvalido("A origem deve vir antes do destino na rota");
        }

        double distancia = destino.getKmDesdeOrigem().diferenca(origem.getKmDesdeOrigem());
        double base = TARIFA.calcular(distancia);
        boolean completo = origem.getOrdem() == 1 && destino.getOrdem() == paradas.size();
        OffsetDateTime dataCompra = compra == null ? OffsetDateTime.now(clock) : compra;
        boolean antecipado = !dataCompra.plusDays(7).isAfter(viagem.dataHoraPartida());
        double desconto = (completo ? 0.10 : 0.0) + (antecipado ? 0.05 : 0.0);
        long centavos =
                BigDecimal.valueOf(base * (1.0 - desconto))
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(0, RoundingMode.HALF_UP)
                        .longValue();
        return new Cotacao(distancia, centavos, completo, antecipado);
    }

    private Parada buscarParada(List<Parada> paradas, Predicate<Parada> filtro, String mensagem) {
        return paradas.stream()
                .filter(filtro)
                .findFirst()
                .orElseThrow(() -> new DadoInvalido(mensagem));
    }
}
