package br.com.viacaocalango.api.modules.motoristas.application;

import br.com.viacaocalango.api.modules.frota.application.OnibusService;
import br.com.viacaocalango.api.modules.motoristas.domain.service.PlanejadorEscala;
import br.com.viacaocalango.api.modules.motoristas.domain.valueobject.*;
import br.com.viacaocalango.api.shared.domain.RecursoNaoEncontrado;
import br.com.viacaocalango.api.shared.domain.RegraDeNegocioViolada;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EscalaService {
    private final EscalaRepository repository;
    private final ViagensParaEscala viagens;
    private final OnibusService onibus;
    private final PlanejadorEscala planejador;

    public EscalaService(
            EscalaRepository repository,
            ViagensParaEscala viagens,
            OnibusService onibus,
            PlanejadorEscala planejador) {
        this.repository = repository;
        this.viagens = viagens;
        this.onibus = onibus;
        this.planejador = planejador;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public EscalaViagem alocar(UUID viagemId, UUID onibusId, List<UUID> paradasTrocaAutorizada) {
        var trechos = viagens.obterTrechos(viagemId, paradasTrocaAutorizada);
        if (!onibus.buscar(onibusId).disponivelParaOperacao()) {
            throw new RegraDeNegocioViolada("Onibus indisponivel para operacao");
        }
        // Bloqueia inclusive candidatos sem historico. A mesma ordem evita deadlocks.
        var ids = repository.bloquearCandidatos();
        if (repository.buscar(viagemId).isPresent()) {
            throw new RegraDeNegocioViolada("Viagem ja possui escala");
        }
        // READ_COMMITTED garante nova leitura apos aguardar o commit de outro planejador.
        var candidatos = ids.stream().map(repository::carregarCandidato).toList();
        EscalaViagem escala =
                planejador.planejar(new SolicitacaoEscala(onibusId, trechos, candidatos));
        repository.salvar(viagemId, escala);
        return escala;
    }

    @Transactional(readOnly = true)
    public EscalaViagem buscar(UUID viagemId) {
        return repository
                .buscar(viagemId)
                .orElseThrow(() -> new RecursoNaoEncontrado("Escala nao encontrada"));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public IntervaloHorario registrarPeriodo(UUID motoristaId, IntervaloHorario periodo) {
        // Escrita do historico participa do mesmo protocolo de bloqueio do planejamento.
        repository.bloquearMotorista(motoristaId);
        repository.registrarPeriodo(motoristaId, periodo);
        return periodo;
    }
}
