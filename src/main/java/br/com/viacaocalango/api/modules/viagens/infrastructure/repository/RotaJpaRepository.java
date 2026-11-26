package br.com.viacaocalango.api.modules.viagens.infrastructure.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface RotaJpaRepository extends JpaRepository<RotaEntity, UUID> {}
