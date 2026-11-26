package br.com.viacaocalango.api.modules.motoristas.domain.valueobject;

import br.com.viacaocalango.api.shared.domain.DadoInvalido;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TrocaCondutor(
        String parada, OffsetDateTime horario, UUID motoristaAnteriorId, UUID novoMotoristaId) {

    public TrocaCondutor {
        if (parada == null || parada.isBlank()) {
            throw new DadoInvalido("Parada de troca e obrigatoria");
        }
        if (horario == null) {
            throw new DadoInvalido("Horario de troca e obrigatorio");
        }
        if (motoristaAnteriorId == null || novoMotoristaId == null) {
            throw new DadoInvalido("Motoristas da troca sao obrigatorios");
        }
        if (motoristaAnteriorId.equals(novoMotoristaId)) {
            throw new DadoInvalido("Troca exige motoristas distintos");
        }
    }
}
