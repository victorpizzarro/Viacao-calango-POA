package br.com.viacaocalango.api.modules.frota.infrastructure;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface OnibusJpaRepository extends JpaRepository<OnibusEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from OnibusEntity o where o.id = :id")
    Optional<OnibusEntity> buscarComBloqueio(@Param("id") UUID id);
}
