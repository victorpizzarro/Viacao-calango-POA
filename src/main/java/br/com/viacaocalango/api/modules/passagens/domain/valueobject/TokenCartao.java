package br.com.viacaocalango.api.modules.passagens.domain.valueobject;

import br.com.viacaocalango.api.shared.domain.DadoInvalido;

public record TokenCartao(String valor) {

    private static final int MAXIMO = 200;

    public TokenCartao {
        if (valor == null || valor.isBlank()) {
            throw new DadoInvalido("Token do cartao e obrigatorio");
        }
        valor = valor.strip();
        if (valor.length() > MAXIMO) {
            throw new DadoInvalido("Token do cartao deve ter ate 200 caracteres");
        }
    }

    public static TokenCartao de(String valor) {
        return new TokenCartao(valor);
    }

    @Override
    public String toString() {
        return "TokenCartao[***]";
    }
}
