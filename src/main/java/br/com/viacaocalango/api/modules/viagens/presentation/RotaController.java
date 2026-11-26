package br.com.viacaocalango.api.modules.viagens.presentation;

import br.com.viacaocalango.api.modules.viagens.application.service.RotaService;
import br.com.viacaocalango.api.modules.viagens.domain.entity.Parada;
import br.com.viacaocalango.api.modules.viagens.domain.valueobject.KmDesdeOrigem;
import br.com.viacaocalango.api.modules.viagens.domain.valueobject.MinutosDesdeOrigem;
import br.com.viacaocalango.api.modules.viagens.domain.valueobject.ParadaId;
import br.com.viacaocalango.api.modules.viagens.domain.valueobject.RotaId;
import br.com.viacaocalango.api.shared.presentation.RespostasApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/rotas")
@Tag(name = "viagens")
public class RotaController {
    private final RotaService service;

    public RotaController(RotaService service) {
        this.service = service;
    }

    @Operation(
            summary = "Criar rota com paradas",
            description =
                    "Km e minutos sao acumulados desde a origem; retorna os UUIDs das paradas. Cadastro em memoria.")
    @ApiResponse(responseCode = "201", description = "Rota criada")
    @ApiResponse(responseCode = "422", ref = RespostasApi.DADOS_INVALIDOS)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RotaResponse criar(@Valid @RequestBody CriarRotaRequest request) {
        List<Parada> paradas =
                request.paradas().stream()
                        .map(p ->
                                new Parada(
                                        ParadaId.novo(),
                                        p.nome(),
                                        new KmDesdeOrigem(p.km_desde_origem()),
                                        new MinutosDesdeOrigem(p.minutos_desde_origem()),
                                        p.ordem()))
                        .toList();
        return RotaResponse.de(service.criarRota(request.nome(), paradas));
    }

    @Operation(summary = "Listar rotas")
    @GetMapping
    public List<RotaResponse> listar() {
        return service.buscarTodas().stream().map(RotaResponse::de).toList();
    }

    @Operation(summary = "Consultar rota e paradas")
    @ApiResponse(responseCode = "404", ref = RespostasApi.NAO_ENCONTRADO)
    @ApiResponse(responseCode = "422", ref = RespostasApi.DADOS_INVALIDOS)
    @GetMapping("/{id}")
    public RotaResponse buscar(@PathVariable UUID id) {
        return service.buscarPorId(RotaId.de(id))
                .map(RotaResponse::de)
                .orElseThrow(() ->
                        new br.com.viacaocalango.api.shared.domain.RecursoNaoEncontrado(
                                "Rota nao encontrada"));
    }
}
