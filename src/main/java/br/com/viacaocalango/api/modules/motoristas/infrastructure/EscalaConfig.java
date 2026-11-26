package br.com.viacaocalango.api.modules.motoristas.infrastructure;

import br.com.viacaocalango.api.modules.motoristas.domain.service.PlanejadorEscala;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class EscalaConfig {
    @Bean
    PlanejadorEscala planejadorEscala() {
        return new PlanejadorEscala();
    }
}
