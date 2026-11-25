package br.com.viacaocalango.api.modules.motoristas.presentation;

import br.com.viacaocalango.api.modules.motoristas.application.CriarMotoristaComando;
import br.com.viacaocalango.api.modules.motoristas.application.MotoristaService;
import br.com.viacaocalango.api.shared.presentation.RespostasApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/motoristas")
@Tag(name = "motoristas")
public class MotoristasController {

    private final MotoristaService motoristaService;

    public MotoristasController(MotoristaService motoristaService) {
        this.motoristaService = motoristaService;
    }

    @Operation(
            summary = "Criar motorista",
            description = "Nome obrigatorio. O identificador UUID e gerado pelo sistema.")
    @ApiResponse(responseCode = "201", description = "Motorista criado")
    @ApiResponse(responseCode = "422", ref = RespostasApi.DADOS_INVALIDOS)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MotoristaResponse criar(@Valid @RequestBody CriarMotoristaRequest corpo) {
        return MotoristaResponse.de(
                motoristaService.criar(new CriarMotoristaComando(corpo.nome())));
    }

    @Operation(
            summary = "Listar motoristas",
            description = "Retorna todos os motoristas cadastrados.")
    @ApiResponse(responseCode = "200", description = "Motoristas encontrados")
    @GetMapping
    public List<MotoristaResponse> listar() {
        return motoristaService.listar().stream().map(MotoristaResponse::de).toList();
    }

    @Operation(summary = "Consultar motorista")
    @ApiResponse(responseCode = "200", description = "Motorista encontrado")
    @ApiResponse(responseCode = "404", ref = RespostasApi.NAO_ENCONTRADO)
    @ApiResponse(responseCode = "422", ref = RespostasApi.DADOS_INVALIDOS)
    @GetMapping("/{id}")
    public MotoristaResponse buscar(@PathVariable UUID id) {
        return MotoristaResponse.de(motoristaService.buscar(id));
    }
}
