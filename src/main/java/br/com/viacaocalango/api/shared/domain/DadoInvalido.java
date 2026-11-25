package br.com.viacaocalango.api.shared.domain;

public class DadoInvalido extends RegraDeNegocioViolada {

    public DadoInvalido(String mensagem) {
        super(mensagem);
    }
}
