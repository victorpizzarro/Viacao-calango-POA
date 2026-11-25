package br.com.viacaocalango.api.shared.domain;

public class RegraDeNegocioViolada extends RuntimeException {

    public RegraDeNegocioViolada(String mensagem) {
        super(mensagem);
    }
}
