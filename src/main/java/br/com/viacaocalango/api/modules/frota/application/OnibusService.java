package br.com.viacaocalango.api.modules.frota.application;

import br.com.viacaocalango.api.modules.frota.domain.model.Onibus;
import br.com.viacaocalango.api.modules.frota.domain.repository.OnibusRepository;
import br.com.viacaocalango.api.modules.frota.domain.valueobject.Capacidade;
import br.com.viacaocalango.api.modules.frota.domain.valueobject.OnibusId;
import br.com.viacaocalango.api.modules.frota.domain.valueobject.Quilometragem;
import br.com.viacaocalango.api.shared.domain.RecursoNaoEncontrado;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OnibusService {

    private final OnibusRepository repositorio;

    public OnibusService(OnibusRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional
    public Onibus criar(CriarOnibusComando comando) {
        Onibus onibus =
                Onibus.cadastrar(
                        comando.tipo(),
                        Capacidade.de(comando.capacidade()),
                        Quilometragem.de(comando.quilometragemTotal()));
        repositorio.salvar(onibus);
        return onibus;
    }

    @Transactional
    public Onibus registrarQuilometragem(UUID id, int quilometragemAtual) {
        Onibus onibus = obterParaAlteracao(id);
        onibus.registrarQuilometragem(Quilometragem.de(quilometragemAtual));
        repositorio.salvar(onibus);
        return onibus;
    }

    @Transactional
    public Onibus concluirRevisao(UUID id) {
        Onibus onibus = obterParaAlteracao(id);
        onibus.concluirRevisao();
        repositorio.salvar(onibus);
        return onibus;
    }

    @Transactional(readOnly = true)
    public Onibus buscar(UUID id) {
        return repositorio
                .buscarPorId(OnibusId.de(id))
                .orElseThrow(() -> new RecursoNaoEncontrado("Onibus nao encontrado"));
    }

    @Transactional(readOnly = true)
    public List<Onibus> listar(Boolean precisaRevisao) {
        return repositorio.listarTodos().stream()
                .filter(onibus ->
                        precisaRevisao == null || onibus.precisaRevisao() == precisaRevisao)
                .toList();
    }

    private Onibus obterParaAlteracao(UUID id) {
        return repositorio
                .buscarParaAlteracao(OnibusId.de(id))
                .orElseThrow(() -> new RecursoNaoEncontrado("Onibus nao encontrado"));
    }
}
