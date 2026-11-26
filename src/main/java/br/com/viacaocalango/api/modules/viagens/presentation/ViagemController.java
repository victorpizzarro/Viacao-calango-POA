package br.com.viacaocalango.api.modules.viagens.presentation;

import br.com.viacaocalango.api.modules.viagens.application.service.ViagemService;
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
@RequestMapping("/api/v1/viagens")
@Tag(name = "viagens")
public class ViagemController {
    private final ViagemService service;

    public ViagemController(ViagemService service) {
        this.service = service;
    }

    @Operation(
            summary = "Criar viagem",
            description = "Reutiliza a rota cadastrada. Armazenamento de Viagens em memoria.")
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
}
