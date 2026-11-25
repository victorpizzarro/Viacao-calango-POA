package br.com.viacaocalango.api.modules.frota.presentation;

import br.com.viacaocalango.api.modules.frota.application.AlocacaoIdealService;
import br.com.viacaocalango.api.modules.frota.application.CriarOnibusComando;
import br.com.viacaocalango.api.modules.frota.application.OnibusService;
import br.com.viacaocalango.api.shared.presentation.RespostasApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/onibus")
@Tag(name = "frota")
public class FrotaController {

    private final OnibusService onibusService;
    private final AlocacaoIdealService alocacaoIdealService;

    public FrotaController(OnibusService onibusService, AlocacaoIdealService alocacaoIdealService) {
        this.onibusService = onibusService;
        this.alocacaoIdealService = alocacaoIdealService;
    }

    @Operation(summary = "Criar onibus", description = "Capacidade deve ser 23, 28 ou 32 lugares.")
    @ApiResponse(responseCode = "201", description = "Onibus criado")
    @ApiResponse(responseCode = "422", ref = RespostasApi.DADOS_INVALIDOS)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OnibusResponse criar(@Valid @RequestBody CriarOnibusRequest corpo) {
        int quilometragem = corpo.quilometragemTotal() == null ? 0 : corpo.quilometragemTotal();
        return OnibusResponse.de(
                onibusService.criar(
                        new CriarOnibusComando(corpo.tipo(), corpo.capacidade(), quilometragem)));
    }

    @Operation(
            summary = "Listar a frota",
            description =
                    "Sem filtro, retorna todos os onibus. Com precisa_revisao=true, retorna os que"
                            + " completaram 10.000 km sem revisao e devem ser encaminhados a"
                            + " revisao.")
    @ApiResponse(responseCode = "200", description = "Onibus encontrados")
    @ApiResponse(responseCode = "422", ref = RespostasApi.DADOS_INVALIDOS)
    @GetMapping
    public List<OnibusResponse> listar(
            @Parameter(description = "Filtra por necessidade de revisao")
                    @RequestParam(name = "precisa_revisao", required = false)
                    Boolean precisaRevisao) {
        return onibusService.listar(precisaRevisao).stream().map(OnibusResponse::de).toList();
    }

    @Operation(
            summary = "Calcular a combinacao ideal de onibus",
            description =
                    "Menor numero de lugares vazios para a quantidade de passageiros e, em caso de"
                            + " empate, menos onibus. E um calculo: nao altera viagens nem considera"
                            + " os onibus existentes na frota.")
    @ApiResponse(responseCode = "200", description = "Combinacao calculada")
    @ApiResponse(responseCode = "422", ref = RespostasApi.DADOS_INVALIDOS)
    @GetMapping("/alocacao-ideal")
    public AlocacaoIdealResponse alocacaoIdeal(@RequestParam int passageiros) {
        return AlocacaoIdealResponse.de(alocacaoIdealService.calcular(passageiros));
    }

    @Operation(summary = "Consultar onibus")
    @ApiResponse(responseCode = "200", description = "Onibus encontrado")
    @ApiResponse(responseCode = "404", ref = RespostasApi.NAO_ENCONTRADO)
    @ApiResponse(responseCode = "422", ref = RespostasApi.DADOS_INVALIDOS)
    @GetMapping("/{id}")
    public OnibusResponse buscar(@PathVariable UUID id) {
        return OnibusResponse.de(onibusService.buscar(id));
    }

    @Operation(
            summary = "Registrar quilometragem",
            description = "O odometro nao pode retroceder nem e reiniciado por revisoes.")
    @ApiResponse(responseCode = "200", description = "Quilometragem registrada")
    @ApiResponse(responseCode = "404", ref = RespostasApi.NAO_ENCONTRADO)
    @ApiResponse(responseCode = "409", ref = RespostasApi.CONFLITO)
    @ApiResponse(responseCode = "422", ref = RespostasApi.DADOS_INVALIDOS)
    @PatchMapping("/{id}/quilometragem")
    public OnibusResponse registrarQuilometragem(
            @PathVariable UUID id, @Valid @RequestBody RegistrarQuilometragemRequest corpo) {
        return OnibusResponse.de(
                onibusService.registrarQuilometragem(id, corpo.quilometragemAtual()));
    }

    @Operation(
            summary = "Concluir revisao",
            description =
                    "Zera os km desde a revisao e preserva o odometro total. "
                            + "A revisao nao gera recurso proprio: retorna o onibus atualizado.")
    @ApiResponse(responseCode = "200", description = "Revisao concluida")
    @ApiResponse(responseCode = "404", ref = RespostasApi.NAO_ENCONTRADO)
    @ApiResponse(responseCode = "422", ref = RespostasApi.DADOS_INVALIDOS)
    @PostMapping("/{id}/revisoes")
    public OnibusResponse concluirRevisao(@PathVariable UUID id) {
        return OnibusResponse.de(onibusService.concluirRevisao(id));
    }
}
