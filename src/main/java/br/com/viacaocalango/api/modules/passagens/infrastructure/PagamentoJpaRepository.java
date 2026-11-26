package br.com.viacaocalango.api.modules.passagens.infrastructure;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface PagamentoJpaRepository extends JpaRepository<PagamentoEntity, UUID> {}
