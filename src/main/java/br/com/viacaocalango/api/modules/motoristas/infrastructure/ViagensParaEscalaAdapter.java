package br.com.viacaocalango.api.modules.motoristas.infrastructure;

import br.com.viacaocalango.api.modules.motoristas.application.ViagensParaEscala;
import br.com.viacaocalango.api.modules.motoristas.domain.valueobject.TrechoViagem;
import br.com.viacaocalango.api.modules.viagens.application.service.ViagemService;
import br.com.viacaocalango.api.modules.viagens.domain.entity.StatusViagem;
import br.com.viacaocalango.api.shared.domain.DadoInvalido;
import br.com.viacaocalango.api.shared.domain.RegraDeNegocioViolada;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class ViagensParaEscalaAdapter implements ViagensParaEscala {
    private final ViagemService viagens;

    ViagensParaEscalaAdapter(ViagemService viagens) {
        this.viagens = viagens;
    }

    @Override
    public List<TrechoViagem> obterTrechos(UUID viagemId, List<UUID> paradasTrocaAutorizada) {
        var viagem = viagens.buscar(viagemId);
        if (viagem.status() != StatusViagem.AGENDADA) {
            throw new RegraDeNegocioViolada("Somente viagens agendadas podem receber escala");
        }
        var paradas = viagem.rota().getParadas();
        var autorizadas = new HashSet<>(paradasTrocaAutorizada);
        var intermediarias =
                paradas.subList(1, paradas.size() - 1).stream()
                        .map(p -> p.getId().valor())
                        .toList();
        if (autorizadas.size() != paradasTrocaAutorizada.size()
                || !intermediarias.containsAll(autorizadas)) {
            throw new DadoInvalido(
                    "Trocas devem indicar UUIDs distintos de paradas intermediarias da rota");
        }
        List<TrechoViagem> trechos = new ArrayList<>();
        for (int i = 1; i < paradas.size(); i++) {
            var origem = paradas.get(i - 1);
            var destino = paradas.get(i);
            double km = destino.getKmDesdeOrigem().valor() - origem.getKmDesdeOrigem().valor();
            // O planejador trabalha com km inteiros; nunca truncar distancias da rota.
            if (!Double.isFinite(km) || km <= 0 || km != Math.rint(km) || km > Integer.MAX_VALUE) {
                throw new DadoInvalido("Escala exige distancias crescentes em km inteiros na rota");
            }
            trechos.add(
                    new TrechoViagem(
                            origem.getNome(),
                            destino.getNome(),
                            viagem.dataHoraPartida()
                                    .plusMinutes(origem.getMinutosDesdeOrigem().valor()),
                            viagem.dataHoraPartida()
                                    .plusMinutes(destino.getMinutosDesdeOrigem().valor()),
                            (int) km,
                            autorizadas.contains(origem.getId().valor())));
        }
        return List.copyOf(trechos);
    }
}
