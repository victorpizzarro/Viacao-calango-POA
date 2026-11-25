package br.com.viacaocalango.api.shared.domain;

public class RecursoNaoEncontrado extends RuntimeException {

    public RecursoNaoEncontrado(String mensagem) {
        super(mensagem);
    }
}
