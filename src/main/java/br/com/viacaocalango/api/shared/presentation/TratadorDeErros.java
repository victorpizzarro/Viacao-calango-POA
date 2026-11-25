package br.com.viacaocalango.api.shared.presentation;

import br.com.viacaocalango.api.shared.domain.DadoInvalido;
import br.com.viacaocalango.api.shared.domain.RecursoNaoEncontrado;
import br.com.viacaocalango.api.shared.domain.RegraDeNegocioViolada;
import br.com.viacaocalango.api.shared.presentation.ErroResponse.DetalheErro;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class TratadorDeErros {

    private static final Logger LOG = LoggerFactory.getLogger(TratadorDeErros.class);
    private static final HttpStatus DADOS_INVALIDOS = HttpStatus.UNPROCESSABLE_CONTENT;

    @ExceptionHandler(RecursoNaoEncontrado.class)
    ResponseEntity<ErroResponse> naoEncontrado(RecursoNaoEncontrado erro) {
        return resposta(HttpStatus.NOT_FOUND, "NAO_ENCONTRADO", erro.getMessage());
    }

    @ExceptionHandler(DadoInvalido.class)
    ResponseEntity<ErroResponse> dadoInvalido(DadoInvalido erro) {
        return resposta(DADOS_INVALIDOS, "DADOS_INVALIDOS", erro.getMessage());
    }

    @ExceptionHandler(RegraDeNegocioViolada.class)
    ResponseEntity<ErroResponse> regraViolada(RegraDeNegocioViolada erro) {
        return resposta(HttpStatus.CONFLICT, "REGRA_DE_NEGOCIO", erro.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErroResponse> corpoInvalido(MethodArgumentNotValidException erro) {
        List<DetalheErro> detalhes =
                erro.getBindingResult().getFieldErrors().stream()
                        .map(campo ->
                                new DetalheErro(
                                        emSnakeCase(campo.getField()), campo.getDefaultMessage()))
                        .toList();
        return ResponseEntity.status(DADOS_INVALIDOS)
                .body(new ErroResponse("DADOS_INVALIDOS", "Dados de entrada invalidos", detalhes));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ErroResponse> jsonIlegivel(HttpMessageNotReadableException erro) {
        return resposta(
                DADOS_INVALIDOS,
                "DADOS_INVALIDOS",
                "JSON malformado ou com valor de tipo invalido");
    }

    @ExceptionHandler({
        MethodArgumentTypeMismatchException.class,
        MissingServletRequestParameterException.class
    })
    ResponseEntity<ErroResponse> parametroInvalido(Exception erro) {
        return resposta(DADOS_INVALIDOS, "DADOS_INVALIDOS", "Parametro ausente ou invalido");
    }

    @ExceptionHandler({
        DataIntegrityViolationException.class,
        PessimisticLockingFailureException.class
    })
    ResponseEntity<ErroResponse> conflitoDeBanco(Exception erro) {
        LOG.warn("Conflito de concorrencia ou integridade", erro);
        return resposta(
                HttpStatus.CONFLICT,
                "CONFLITO",
                "A operacao conflita com outra alteracao; tente novamente");
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErroResponse> inesperado(Exception erro) {
        if (erro instanceof ErrorResponse doSpring) {
            HttpStatusCode status = doSpring.getStatusCode();
            return ResponseEntity.status(status)
                    .body(ErroResponse.de("REQUISICAO_INVALIDA", doSpring.getBody().getTitle()));
        }
        LOG.error("Erro inesperado", erro);
        return resposta(
                HttpStatus.INTERNAL_SERVER_ERROR, "ERRO_INTERNO", "Erro interno do servidor");
    }

    private static String emSnakeCase(String campo) {
        return campo.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT);
    }

    private ResponseEntity<ErroResponse> resposta(
            HttpStatus status, String codigo, String mensagem) {
        return ResponseEntity.status(status).body(ErroResponse.de(codigo, mensagem));
    }
}
