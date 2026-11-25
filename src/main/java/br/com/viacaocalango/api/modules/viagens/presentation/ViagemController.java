package br.com.viacaocalango.api.modules.viagens.presentation;
import br.com.viacaocalango.api.modules.viagens.application.service.PrecoViagemService;
import br.com.viacaocalango.api.modules.viagens.application.service.TrechoRotaService;
import br.com.viacaocalango.api.modules.viagens.application.service.ViagemService;
import br.com.viacaocalango.api.modules.viagens.domain.entity.Parada;
import br.com.viacaocalango.api.modules.viagens.domain.entity.Viagem;
import br.com.viacaocalango.api.shared.domain.DadoInvalido;
import br.com.viacaocalango.api.modules.viagens.domain.valueobject.TarifaBaseKm;
import br.com.viacaocalango.api.modules.viagens.domain.valueobject.TrechoRota;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/v1/viagens")
public class ViagemController {
    private static final double TARIFA_KM = 0.50;
    private final ViagemService service;
    private final TrechoRotaService trechoService;
    private final PrecoViagemService precoService;
    public ViagemController(ViagemService service, TrechoRotaService trechoService, PrecoViagemService precoService) { this.service = service; this.trechoService = trechoService; this.precoService = precoService; }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public ViagemResponse criar(@Valid @RequestBody CriarViagemRequest request) { return ViagemResponse.de(service.criar(request.rota_id(), request.data_hora_partida())); }
    @GetMapping public List<ViagemResponse> listar() { return service.listar().stream().map(ViagemResponse::de).toList(); }

    @GetMapping("/{id}/preco")
    public PrecoResponse preco(@PathVariable UUID id, @RequestParam String origem, @RequestParam String destino,
            @RequestParam(required=false) OffsetDateTime data_compra) {
        Viagem viagem = service.buscar(id);
        Parada pOrigem = viagem.rota().getParadas().stream().filter(p -> p.getNome().equalsIgnoreCase(origem)).findFirst().orElseThrow(() -> new DadoInvalido("Origem nao encontrada na rota"));
        Parada pDestino = viagem.rota().getParadas().stream().filter(p -> p.getNome().equalsIgnoreCase(destino)).findFirst().orElseThrow(() -> new DadoInvalido("Destino nao encontrada na rota"));
        TrechoRota trecho = trechoService.calcularTrecho(viagem.rota(), pOrigem, pDestino);
        double distancia = trecho.calcularDistancia();
        double precoBase = precoService.calcularPreco(viagem.rota(), pOrigem, pDestino, new TarifaBaseKm(TARIFA_KM));
        boolean completo = pOrigem.getOrdem() == 1 && pDestino.getOrdem() == viagem.rota().getParadas().size();
        OffsetDateTime compra = data_compra == null ? OffsetDateTime.now() : data_compra;
        boolean antecipado = !compra.plusDays(7).isAfter(viagem.dataHoraPartida());
        double desconto = (completo ? 0.10 : 0.0) + (antecipado ? 0.05 : 0.0);
        long centavos = BigDecimal.valueOf(precoBase * (1.0 - desconto)).multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).longValue();
        return new PrecoResponse(distancia, centavos, centavos / 100.0, completo, antecipado);
    }
}
