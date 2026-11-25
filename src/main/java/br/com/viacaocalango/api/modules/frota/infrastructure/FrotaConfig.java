package br.com.viacaocalango.api.modules.frota.infrastructure;

import br.com.viacaocalango.api.modules.frota.domain.service.CalculadoraAlocacaoIdeal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class FrotaConfig {

    @Bean
    CalculadoraAlocacaoIdeal calculadoraAlocacaoIdeal() {
        return new CalculadoraAlocacaoIdeal();
    }
}
