package br.com.viacaocalango.api.modules.passagens.infrastructure;

import br.com.viacaocalango.api.modules.passagens.domain.service.PoliticaDeReserva;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class PassagensConfig {

    @Bean
    PoliticaDeReserva politicaDeReserva(PassagensProperties propriedades) {
        return new PoliticaDeReserva(propriedades.reservaMinutos());
    }
}
