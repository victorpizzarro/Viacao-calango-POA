package br.com.viacaocalango.api.modules.passagens.infrastructure;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface PassagemJpaRepository extends JpaRepository<PassagemEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PassagemEntity p where p.id = :id")
    Optional<PassagemEntity> buscarComBloqueio(@Param("id") UUID id);

    @Query("select p from PassagemEntity p where p.viagemId = :viagemId")
    List<PassagemEntity> listarPorViagem(@Param("viagemId") UUID viagemId);
}
