package br.com.viacaocalango.api.modules.motoristas.presentation;

import br.com.viacaocalango.api.modules.motoristas.application.EscalaService;
import br.com.viacaocalango.api.modules.motoristas.domain.valueobject.IntervaloHorario;
import br.com.viacaocalango.api.shared.presentation.RespostasApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/motoristas/{id}/periodos-dirigidos")
@Tag(name = "motoristas")
public class PeriodosDirigidosController {
    private final EscalaService service;

    public PeriodosDirigidosController(EscalaService service) {
        this.service = service;
    }

    @Operation(
            summary = "Registrar periodo efetivamente dirigido",
            description =
                    "Persiste o historico consultado pelo planejador. Adquire o mesmo bloqueio do motorista usado pela alocacao.")
    @ApiResponse(responseCode = "201", description = "Periodo registrado")
    @ApiResponse(responseCode = "404", ref = RespostasApi.NAO_ENCONTRADO)
    @ApiResponse(responseCode = "409", ref = RespostasApi.CONFLITO)
    @ApiResponse(responseCode = "422", ref = RespostasApi.DADOS_INVALIDOS)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IntervaloHorario registrar(
            @PathVariable UUID id, @Valid @RequestBody RegistrarPeriodoDirigidoRequest corpo) {
        return service.registrarPeriodo(id, new IntervaloHorario(corpo.inicio(), corpo.fim()));
    }
}
