package br.com.viacaocalango.api.modules.motoristas.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.viacaocalango.api.modules.motoristas.domain.model.Motorista;
import br.com.viacaocalango.api.modules.motoristas.domain.valueobject.MotoristaId;
import br.com.viacaocalango.api.shared.domain.DadoInvalido;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class MotoristaTest {

    @Test
    void cadastroGeraIdentidadesDistintasMesmoComNomesIguais() {
        Motorista primeiro = Motorista.cadastrar("João da Silva");
        Motorista segundo = Motorista.cadastrar("João da Silva");

        assertThat(primeiro.nome()).isEqualTo("João da Silva");
        assertThat(primeiro.id().valor()).isNotNull();
        assertThat(primeiro.id()).isNotEqualTo(segundo.id());
        assertThat(primeiro).isNotEqualTo(segundo);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    void nomeInvalidoNaoPodeEntrarNoDominio(String nome) {
        assertThatThrownBy(() -> Motorista.cadastrar(nome)).isInstanceOf(DadoInvalido.class);
        assertThatThrownBy(() -> Motorista.reconstituir(MotoristaId.novo(), nome))
                .isInstanceOf(DadoInvalido.class);
    }

    @Test
    void reconstituicaoPreservaIdentidadeEIgualdadeDoAgregado() {
        Motorista original = Motorista.cadastrar("Maria da Silva");
        Motorista reconstituido = Motorista.reconstituir(original.id(), original.nome());

        assertThat(reconstituido.id()).isEqualTo(original.id());
        assertThat(reconstituido.nome()).isEqualTo(original.nome());
        assertThat(reconstituido).isEqualTo(original).hasSameHashCodeAs(original);
    }

    @Test
    void identidadeObrigatoriaNaReconstituicao() {
        assertThatThrownBy(() -> Motorista.reconstituir(null, "Maria da Silva"))
                .isInstanceOf(DadoInvalido.class);
    }

    @Test
    void valueObjectPreservaUuidEComparaPorValor() {
        UUID uuid = UUID.randomUUID();

        assertThat(MotoristaId.de(uuid)).isEqualTo(new MotoristaId(uuid));
        assertThat(MotoristaId.de(uuid).toString()).isEqualTo(uuid.toString());
        assertThatThrownBy(() -> MotoristaId.de(null)).isInstanceOf(DadoInvalido.class);
    }
}
