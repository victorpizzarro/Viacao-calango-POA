package br.com.viacaocalango.api.modules.motoristas.application;

import br.com.viacaocalango.api.modules.motoristas.domain.model.Motorista;
import br.com.viacaocalango.api.modules.motoristas.domain.repository.MotoristaRepository;
import br.com.viacaocalango.api.modules.motoristas.domain.valueobject.MotoristaId;
import br.com.viacaocalango.api.shared.domain.RecursoNaoEncontrado;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MotoristaService {

    private final MotoristaRepository repositorio;

    public MotoristaService(MotoristaRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional
    public Motorista criar(CriarMotoristaComando comando) {
        Motorista motorista = Motorista.cadastrar(comando.nome());
        repositorio.salvar(motorista);
        return motorista;
    }

    @Transactional(readOnly = true)
    public Motorista buscar(UUID id) {
        return repositorio
                .buscarPorId(MotoristaId.de(id))
                .orElseThrow(() -> new RecursoNaoEncontrado("Motorista nao encontrado"));
    }

    @Transactional(readOnly = true)
    public List<Motorista> listar() {
        return repositorio.listarTodos();
    }
}
