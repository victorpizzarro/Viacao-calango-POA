package br.com.viacaocalango.api.modules.passagens.presentation;

import br.com.viacaocalango.api.modules.passagens.application.ComprarPassagemComando;
import br.com.viacaocalango.api.modules.passagens.application.PagamentoService;
import br.com.viacaocalango.api.modules.passagens.application.PassagemService;
import br.com.viacaocalango.api.modules.passagens.application.RegistrarPagamentoComando;
import br.com.viacaocalango.api.shared.presentation.RespostasApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@RequestMapping("/api/v1")
@Tag(name = "passagens")
public class PassagensController {

    private final PassagemService passagemService;
    private final PagamentoService pagamentoService;

    public PassagensController(PassagemService passagemService, PagamentoService pagamentoService) {
        this.passagemService = passagemService;
        this.pagamentoService = pagamentoService;
    }

    @Operation(
            summary = "Consultar disponibilidade por trecho",
            description =
                    "Lista cada trecho entre paradas consecutivas com os lugares ocupados e"
                            + " disponiveis. Quem desce numa parada libera o lugar para quem"
                            + " embarca nela.")
    @ApiResponse(responseCode = "200", description = "Disponibilidade calculada")
    @ApiResponse(responseCode = "404", ref = RespostasApi.NAO_ENCONTRADO)
    @ApiResponse(responseCode = "422", ref = RespostasApi.DADOS_INVALIDOS)
    @GetMapping("/viagens/{id}/disponibilidade")
    public DisponibilidadeResponse disponibilidade(@PathVariable UUID id) {
        return DisponibilidadeResponse.de(passagemService.consultarDisponibilidade(id));
    }

    @Operation(
            summary = "Comprar passagem",
            description =
                    "Reserva um lugar em qualquer trecho da viagem, com preco fixado na compra e"
                            + " reserva de 15 minutos ate o pagamento. Internet nao aceita local de"
                            + " venda; guiche exige um local da rota.")
    @ApiResponse(responseCode = "201", description = "Passagem reservada")
    @ApiResponse(responseCode = "404", ref = RespostasApi.NAO_ENCONTRADO)
    @ApiResponse(responseCode = "409", ref = RespostasApi.CONFLITO)
    @ApiResponse(responseCode = "422", ref = RespostasApi.DADOS_INVALIDOS)
    @PostMapping("/passagens")
    @ResponseStatus(HttpStatus.CREATED)
    public PassagemResponse comprar(@Valid @RequestBody ComprarPassagemRequest corpo) {
        return PassagemResponse.de(
                passagemService.comprar(
                        new ComprarPassagemComando(
                                corpo.viagemId(),
                                corpo.paradaOrigemId(),
                                corpo.paradaDestinoId(),
                                corpo.passageiroNome(),
                                corpo.canal(),
                                corpo.localVenda())));
    }

    @Operation(
            summary = "Registrar pagamento",
            description =
                    "Internet aceita cartao e guiche aceita dinheiro. O pagamento recusado e"
                            + " registrado e a reserva continua valida; o aprovado confirma a"
                            + " passagem.")
    @ApiResponse(responseCode = "201", description = "Tentativa de pagamento registrada")
    @ApiResponse(responseCode = "404", ref = RespostasApi.NAO_ENCONTRADO)
    @ApiResponse(responseCode = "409", ref = RespostasApi.CONFLITO)
    @ApiResponse(responseCode = "422", ref = RespostasApi.DADOS_INVALIDOS)
    @PostMapping("/pagamentos")
    @ResponseStatus(HttpStatus.CREATED)
    public PagamentoResponse pagar(@Valid @RequestBody RegistrarPagamentoRequest corpo) {
        return PagamentoResponse.de(
                pagamentoService.pagar(
                        new RegistrarPagamentoComando(
                                corpo.passagemId(), corpo.forma(), corpo.tokenCartao())));
    }
}
