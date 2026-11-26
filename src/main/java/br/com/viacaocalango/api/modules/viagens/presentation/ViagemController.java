package br.com.viacaocalango.api.modules.viagens.presentation;

import br.com.viacaocalango.api.modules.viagens.application.service.PrecoFinalViagemService;
import br.com.viacaocalango.api.modules.viagens.application.service.ViagemService;
import br.com.viacaocalango.api.modules.viagens.domain.entity.Parada;
import br.com.viacaocalango.api.modules.viagens.domain.entity.Viagem;
import br.com.viacaocalango.api.shared.domain.DadoInvalido;
import br.com.viacaocalango.api.shared.presentation.RespostasApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/viagens")
@Tag(name = "viagens")
public class ViagemController {
    private final ViagemService service;
    private final PrecoFinalViagemService precoFinal;

    public ViagemController(ViagemService service, PrecoFinalViagemService precoFinal) {
        this.service = service;
        this.precoFinal = precoFinal;
    }

    @Operation(
            summary = "Criar viagem",
            description = "Reutiliza a rota cadastrada. Viagem e rota persistidas no banco.")
    @ApiResponse(responseCode = "201", description = "Viagem criada")
    @ApiResponse(responseCode = "404", ref = RespostasApi.NAO_ENCONTRADO)
    @ApiResponse(responseCode = "422", ref = RespostasApi.DADOS_INVALIDOS)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ViagemResponse criar(@Valid @RequestBody CriarViagemRequest request) {
        return ViagemResponse.de(service.criar(request.rota_id(), request.data_hora_partida()));
    }

    @Operation(summary = "Listar viagens")
    @GetMapping
    public List<ViagemResponse> listar() {
        return service.listar().stream().map(ViagemResponse::de).toList();
    }

    @Operation(summary = "Consultar viagem")
    @ApiResponse(responseCode = "404", ref = RespostasApi.NAO_ENCONTRADO)
    @ApiResponse(responseCode = "422", ref = RespostasApi.DADOS_INVALIDOS)
    @GetMapping("/{id}")
    public ViagemResponse buscar(@PathVariable UUID id) {
        return ViagemResponse.de(service.buscar(id));
    }

    @Operation(
            summary = "Consultar preco do trecho",
            description =
                    "Calcula o preco pela rota oficial de Viagens, com descontos de percurso completo e antecedencia.")
    @ApiResponse(responseCode = "200", description = "Preco calculado")
    @ApiResponse(responseCode = "404", ref = RespostasApi.NAO_ENCONTRADO)
    @ApiResponse(responseCode = "422", ref = RespostasApi.DADOS_INVALIDOS)
    @GetMapping("/{id}/preco")
    public PrecoResponse preco(
            @PathVariable UUID id,
            @RequestParam String origem,
            @RequestParam String destino,
            @RequestParam(required = false) OffsetDateTime data_compra) {
        Viagem viagem = service.buscar(id);
        Parada pOrigem =
                viagem.rota().getParadas().stream()
                        .filter(p -> p.getNome().equalsIgnoreCase(origem))
                        .findFirst()
                        .orElseThrow(() -> new DadoInvalido("Origem nao encontrada na rota"));
        Parada pDestino =
                viagem.rota().getParadas().stream()
                        .filter(p -> p.getNome().equalsIgnoreCase(destino))
                        .findFirst()
                        .orElseThrow(() -> new DadoInvalido("Destino nao encontrada na rota"));
        var cotacao =
                precoFinal.cotar(
                        id, pOrigem.getId().valor(), pDestino.getId().valor(), data_compra);
        return new PrecoResponse(
                cotacao.distanciaKm(),
                cotacao.precoCentavos(),
                cotacao.precoCentavos() / 100.0,
                cotacao.percursoCompleto(),
                cotacao.descontoAntecedencia());
    }
}
