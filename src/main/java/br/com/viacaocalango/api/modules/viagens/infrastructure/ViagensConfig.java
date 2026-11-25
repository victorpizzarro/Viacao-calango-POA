package br.com.viacaocalango.api.modules.viagens.infrastructure;

import br.com.viacaocalango.api.modules.viagens.application.service.PrecoViagemService;
import br.com.viacaocalango.api.modules.viagens.application.service.RotaService;
import br.com.viacaocalango.api.modules.viagens.application.service.TrechoRotaService;
import br.com.viacaocalango.api.modules.viagens.application.service.ViagemService;
import br.com.viacaocalango.api.modules.viagens.infrastructure.repository.RotaRepository;
import br.com.viacaocalango.api.modules.viagens.infrastructure.repository.RotaRepositoryImpl;
import br.com.viacaocalango.api.modules.viagens.infrastructure.repository.ViagemRepository;
import br.com.viacaocalango.api.modules.viagens.infrastructure.repository.ViagemRepositoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ViagensConfig {
    @Bean RotaRepository rotaRepository() { return new RotaRepositoryImpl(); }
    @Bean ViagemRepository viagemRepository() { return new ViagemRepositoryImpl(); }
    @Bean RotaService rotaService(RotaRepository repository) { return new RotaService(repository); }
    @Bean TrechoRotaService trechoRotaService() { return new TrechoRotaService(); }
    @Bean PrecoViagemService precoViagemService() { return new PrecoViagemService(); }
    @Bean ViagemService viagemService(ViagemRepository repository, RotaService rotaService) { return new ViagemService(repository, rotaService); }
}
