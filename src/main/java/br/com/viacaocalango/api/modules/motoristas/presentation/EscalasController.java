package br.com.viacaocalango.api.modules.motoristas.presentation;

import br.com.viacaocalango.api.modules.motoristas.application.EscalaService;
import br.com.viacaocalango.api.shared.presentation.RespostasApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/viagens/{id}/escala")
@Tag(name = "motoristas")
public class EscalasController {
    private final EscalaService service;

    public EscalasController(EscalaService service) {
        this.service = service;
    }

    @Operation(
            summary = "Alocar motoristas da viagem",
            description =
                    "Carrega rota, horarios e distancias de Viagens; consulta todos os motoristas cadastrados, historico e reservas no servidor. Bloqueia candidatos, rele as alocacoes e persiste a escala completa atomicamente. Limites e descanso sao avaliados por PlanejadorEscala.")
    @ApiResponse(
            responseCode = "201",
            description = "Escala completa persistida, incluindo trechos e trocas")
    @ApiResponse(responseCode = "404", ref = RespostasApi.NAO_ENCONTRADO)
    @ApiResponse(responseCode = "409", ref = RespostasApi.CONFLITO)
    @ApiResponse(responseCode = "422", ref = RespostasApi.DADOS_INVALIDOS)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EscalaResponse alocar(
            @PathVariable UUID id, @Valid @RequestBody AlocarMotoristasRequest corpo) {
        return EscalaResponse.de(
                id, service.alocar(id, corpo.onibusId(), corpo.paradasTrocaAutorizada()));
    }

    @Operation(
            summary = "Consultar escala persistida",
            description = "Trechos persistidos e trocas reconstruidas pelo resultado do dominio.")
    @ApiResponse(responseCode = "200", description = "Escala encontrada")
    @ApiResponse(responseCode = "404", ref = RespostasApi.NAO_ENCONTRADO)
    @ApiResponse(responseCode = "422", ref = RespostasApi.DADOS_INVALIDOS)
    @GetMapping
    public EscalaResponse buscar(@PathVariable UUID id) {
        return EscalaResponse.de(id, service.buscar(id));
    }
}
