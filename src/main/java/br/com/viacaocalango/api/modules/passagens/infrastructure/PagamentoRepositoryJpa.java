package br.com.viacaocalango.api.modules.passagens.infrastructure;

import br.com.viacaocalango.api.modules.passagens.domain.model.Pagamento;
import br.com.viacaocalango.api.modules.passagens.domain.repository.PagamentoRepository;
import org.springframework.stereotype.Repository;

@Repository
class PagamentoRepositoryJpa implements PagamentoRepository {

    private final PagamentoJpaRepository jpa;

    PagamentoRepositoryJpa(PagamentoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void salvar(Pagamento pagamento) {
        jpa.save(new PagamentoEntity(pagamento));
    }
}
