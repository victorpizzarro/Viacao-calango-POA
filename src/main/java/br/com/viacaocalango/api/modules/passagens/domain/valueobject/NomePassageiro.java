package br.com.viacaocalango.api.modules.passagens.domain.valueobject;

import br.com.viacaocalango.api.shared.domain.DadoInvalido;

public record NomePassageiro(String valor) {

    private static final int MAXIMO = 120;

    public NomePassageiro {
        if (valor == null || valor.isBlank()) {
            throw new DadoInvalido("Nome do passageiro e obrigatorio");
        }
        valor = valor.strip();
        if (valor.length() > MAXIMO) {
            throw new DadoInvalido("Nome do passageiro deve ter ate 120 caracteres");
        }
    }

    public static NomePassageiro de(String valor) {
        return new NomePassageiro(valor);
    }

    @Override
    public String toString() {
        return valor;
    }
}
