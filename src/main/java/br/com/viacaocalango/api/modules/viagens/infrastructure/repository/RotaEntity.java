package br.com.viacaocalango.api.modules.viagens.infrastructure.repository;

import br.com.viacaocalango.api.modules.viagens.domain.entity.Rota;
import br.com.viacaocalango.api.modules.viagens.domain.valueobject.RotaId;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "rota")
class RotaEntity {
    @Id private UUID id;

    @Column(nullable = false, columnDefinition = "text")
    private String nome;

    @OneToMany(mappedBy = "rota", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem ASC")
    private List<ParadaEntity> paradas = new ArrayList<>();

    protected RotaEntity() {}

    RotaEntity(Rota rota) {
        id = rota.getId().valor();
        nome = rota.getNome();
        paradas =
                rota.getParadas().stream()
                        .map(p -> new ParadaEntity(p, this))
                        .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }

    Rota paraDominio() {
        return new Rota(
                RotaId.de(id), nome, paradas.stream().map(ParadaEntity::paraDominio).toList());
    }
}
