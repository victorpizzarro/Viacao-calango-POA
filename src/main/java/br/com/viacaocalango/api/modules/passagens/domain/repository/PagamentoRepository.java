package br.com.viacaocalango.api.modules.passagens.domain.repository;

import br.com.viacaocalango.api.modules.passagens.domain.model.Pagamento;

public interface PagamentoRepository {

    void salvar(Pagamento pagamento);
}
