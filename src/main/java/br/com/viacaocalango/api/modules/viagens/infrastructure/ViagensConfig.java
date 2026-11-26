package br.com.viacaocalango.api.modules.viagens.infrastructure;

import br.com.viacaocalango.api.modules.viagens.application.service.RotaService;
import br.com.viacaocalango.api.modules.viagens.application.service.ViagemService;
import br.com.viacaocalango.api.modules.viagens.infrastructure.repository.RotaRepository;
import br.com.viacaocalango.api.modules.viagens.infrastructure.repository.ViagemRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ViagensConfig {
    @Bean
    RotaService rotaService(RotaRepository repository) {
        return new RotaService(repository);
    }

    @Bean
    ViagemService viagemService(ViagemRepository repository, RotaService rotaService) {
        return new ViagemService(repository, rotaService);
    }
}
