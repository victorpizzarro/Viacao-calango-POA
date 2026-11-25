package br.com.viacaocalango.api.modules.motoristas.infrastructure;

import br.com.viacaocalango.api.modules.motoristas.domain.model.Motorista;
import br.com.viacaocalango.api.modules.motoristas.domain.valueobject.MotoristaId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "motoristas")
class MotoristaEntity {

    @Id private UUID id;

    @Column(nullable = false, columnDefinition = "text")
    private String nome;

    protected MotoristaEntity() {}

    MotoristaEntity(Motorista motorista) {
        id = motorista.id().valor();
        nome = motorista.nome();
    }

    Motorista paraDominio() {
        return Motorista.reconstituir(MotoristaId.de(id), nome);
    }
}
