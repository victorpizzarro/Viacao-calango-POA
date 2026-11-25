package br.com.viacaocalango.api.modules.viagens.presentation;

import br.com.viacaocalango.api.modules.viagens.application.service.RotaService;
import br.com.viacaocalango.api.modules.viagens.domain.entity.Parada;
import br.com.viacaocalango.api.modules.viagens.domain.valueobject.KmDesdeOrigem;
import br.com.viacaocalango.api.modules.viagens.domain.valueobject.MinutosDesdeOrigem;
import br.com.viacaocalango.api.modules.viagens.domain.valueobject.ParadaId;
import br.com.viacaocalango.api.modules.viagens.domain.valueobject.RotaId;
import java.util.List;
import java.util.UUID;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/rotas")
public class RotaController {
    private final RotaService service;
    public RotaController(RotaService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RotaResponse criar(@Valid @RequestBody CriarRotaRequest request) {
        List<Parada> paradas = request.paradas().stream().map(p -> new Parada(
                ParadaId.novo(), p.nome(), new KmDesdeOrigem(p.km_desde_origem()),
                new MinutosDesdeOrigem(p.minutos_desde_origem()), p.ordem())).toList();
        return RotaResponse.de(service.criarRota(request.nome(), paradas));
    }


    @GetMapping
    public List<RotaResponse> listar() { return service.buscarTodas().stream().map(RotaResponse::de).toList(); }


    @GetMapping("/{id}")
    public RotaResponse buscar(@PathVariable UUID id) {
        return service.buscarPorId(RotaId.de(id)).map(RotaResponse::de)
                .orElseThrow(() -> new br.com.viacaocalango.api.shared.domain.RecursoNaoEncontrado("Rota nao encontrada"));
    }
}
