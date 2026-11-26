package br.com.viacaocalango.api.modules.passagens.domain.model;

public enum CanalVenda {
    INTERNET,
    GUICHE;

    public boolean aceita(FormaPagamento forma) {
        return switch (this) {
            case INTERNET -> forma == FormaPagamento.CARTAO;
            case GUICHE -> forma == FormaPagamento.DINHEIRO;
        };
    }
}
