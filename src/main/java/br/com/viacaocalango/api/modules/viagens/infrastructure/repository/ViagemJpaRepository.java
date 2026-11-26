package br.com.viacaocalango.api.modules.viagens.infrastructure.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface ViagemJpaRepository extends JpaRepository<ViagemEntity, UUID> {
    List<ViagemEntity> findAllByOrderBySequenciaAsc();
}
