package br.com.viacaocalango.api.modules.viagens.infrastructure.repository;
import br.com.viacaocalango.api.modules.viagens.domain.entity.Viagem;
import java.util.*;
public class ViagemRepositoryImpl implements ViagemRepository {
    private final Map<UUID, Viagem> dados = new LinkedHashMap<>();
    public Viagem salvar(Viagem viagem) { dados.put(viagem.id(), viagem); return viagem; }
    public Optional<Viagem> buscar(UUID id) { return Optional.ofNullable(dados.get(id)); }
    public List<Viagem> listar() { return new ArrayList<>(dados.values()); }
}
