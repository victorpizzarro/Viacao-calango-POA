package br.com.viacaocalango.api.modules.passagens.application;

import br.com.viacaocalango.api.modules.passagens.domain.model.OcupacaoViagem;
import br.com.viacaocalango.api.modules.passagens.domain.model.Passagem;
import br.com.viacaocalango.api.modules.passagens.domain.repository.PassagemRepository;
import br.com.viacaocalango.api.modules.passagens.domain.service.PoliticaDeReserva;
import br.com.viacaocalango.api.modules.passagens.domain.valueobject.NomePassageiro;
import br.com.viacaocalango.api.modules.passagens.domain.valueobject.ParadaId;
import br.com.viacaocalango.api.modules.passagens.domain.valueobject.Trecho;
import br.com.viacaocalango.api.modules.passagens.domain.valueobject.ViagemId;
import br.com.viacaocalango.api.modules.viagens.application.DadosParadaVenda;
import br.com.viacaocalango.api.modules.viagens.application.DadosViagemVenda;
import br.com.viacaocalango.api.modules.viagens.application.ViagensParaVenda;
import br.com.viacaocalango.api.shared.domain.RegraDeNegocioViolada;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PassagemService {

    private final PassagemRepository repositorio;
    private final ViagensParaVenda viagens;
    private final PoliticaDeReserva politicaDeReserva;
    private final Clock clock;

    public PassagemService(
            PassagemRepository repositorio,
            ViagensParaVenda viagens,
            PoliticaDeReserva politicaDeReserva,
            Clock clock) {
        this.repositorio = repositorio;
        this.viagens = viagens;
        this.politicaDeReserva = politicaDeReserva;
        this.clock = clock;
    }

    @Transactional
    public Passagem comprar(ComprarPassagemComando comando) {
        DadosViagemVenda viagem = viagens.obterComBloqueio(comando.viagemId());
        OffsetDateTime agora = OffsetDateTime.now(clock);
        garantirQueNaoPartiu(viagem, agora);
        Trecho trecho = trechoDe(comando);
        ocupacaoDe(viagem, agora).garantirLugar(trecho);
        long preco =
                viagens.precoFinalCentavos(
                        viagem.id(), comando.paradaOrigemId(), comando.paradaDestinoId());
        Passagem passagem =
                Passagem.reservar(
                        ViagemId.de(viagem.id()),
                        trecho,
                        NomePassageiro.de(comando.passageiroNome()),
                        comando.canal(),
                        comando.localVenda(),
                        preco,
                        politicaDeReserva.iniciarEm(agora));
        passagem.garantirLocalDeVenda(viagem.locaisVenda());
        repositorio.salvar(passagem);
        return passagem;
    }

    @Transactional(readOnly = true)
    public Disponibilidade consultarDisponibilidade(UUID viagemId) {
        DadosViagemVenda viagem = viagens.obter(viagemId);
        OcupacaoViagem ocupacao = ocupacaoDe(viagem, OffsetDateTime.now(clock));
        return new Disponibilidade(
                viagem.id(), viagem.capacidade(), ocupacao.trechos(), nomesDasParadas(viagem));
    }

    private void garantirQueNaoPartiu(DadosViagemVenda viagem, OffsetDateTime agora) {
        if (!viagem.dataSaida().isAfter(agora)) {
            throw new RegraDeNegocioViolada("Viagem ja partiu");
        }
    }

    private Trecho trechoDe(ComprarPassagemComando comando) {
        return Trecho.de(
                ParadaId.de(comando.paradaOrigemId()), ParadaId.de(comando.paradaDestinoId()));
    }

    private OcupacaoViagem ocupacaoDe(DadosViagemVenda viagem, OffsetDateTime agora) {
        List<ParadaId> paradas =
                viagem.paradas().stream().map(parada -> ParadaId.de(parada.id())).toList();
        List<Passagem> passagens = repositorio.listarPorViagem(ViagemId.de(viagem.id()));
        return OcupacaoViagem.de(viagem.capacidade(), paradas, passagens, agora);
    }

    private Map<ParadaId, String> nomesDasParadas(DadosViagemVenda viagem) {
        Map<ParadaId, String> nomes = new LinkedHashMap<>();
        for (DadosParadaVenda parada : viagem.paradas()) {
            nomes.put(ParadaId.de(parada.id()), parada.nome());
        }
        return nomes;
    }
}
