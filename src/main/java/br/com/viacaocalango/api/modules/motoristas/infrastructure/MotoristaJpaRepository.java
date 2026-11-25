package br.com.viacaocalango.api.modules.motoristas.infrastructure;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface MotoristaJpaRepository extends JpaRepository<MotoristaEntity, UUID> {}
