package br.com.viacaocalango.api.modules.viagens.application.service;
import br.com.viacaocalango.api.modules.viagens.domain.entity.Rota;
import br.com.viacaocalango.api.modules.viagens.domain.entity.Viagem;
import br.com.viacaocalango.api.modules.viagens.domain.valueobject.RotaId;
import br.com.viacaocalango.api.modules.viagens.infrastructure.repository.ViagemRepository;
import br.com.viacaocalango.api.shared.domain.RecursoNaoEncontrado;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
public class ViagemService {
    private final ViagemRepository repository;
    private final RotaService rotaService;
    public ViagemService(ViagemRepository repository, RotaService rotaService) { this.repository = repository; this.rotaService = rotaService; }
    public Viagem criar(UUID rotaId, OffsetDateTime partida) {
        Rota rota = rotaService.buscarPorId(RotaId.de(rotaId)).orElseThrow(() -> new RecursoNaoEncontrado("Rota nao encontrada"));
        return repository.salvar(Viagem.nova(rota, partida));
    }
    public Viagem buscar(UUID id) { return repository.buscar(id).orElseThrow(() -> new RecursoNaoEncontrado("Viagem nao encontrada")); }
    public List<Viagem> listar() { return repository.listar(); }
}
