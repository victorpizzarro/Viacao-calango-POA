package br.com.viacaocalango.api.modules.passagens.domain.model;

import br.com.viacaocalango.api.modules.passagens.domain.valueobject.NomePassageiro;
import br.com.viacaocalango.api.modules.passagens.domain.valueobject.PassagemId;
import br.com.viacaocalango.api.modules.passagens.domain.valueobject.PrazoDeReserva;
import br.com.viacaocalango.api.modules.passagens.domain.valueobject.Trecho;
import br.com.viacaocalango.api.modules.passagens.domain.valueobject.ViagemId;
import br.com.viacaocalango.api.shared.domain.DadoInvalido;
import br.com.viacaocalango.api.shared.domain.RegraDeNegocioViolada;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Passagem {

    private static final int MAXIMO_LOCAL_VENDA = 100;

    private final PassagemId id;
    private final ViagemId viagemId;
    private final Trecho trecho;
    private final NomePassageiro passageiro;
    private final CanalVenda canal;
    private final String localVenda;
    private final long precoCentavos;
    private final PrazoDeReserva reserva;
    private StatusPassagem status;

    private Passagem(
            PassagemId id,
            ViagemId viagemId,
            Trecho trecho,
            NomePassageiro passageiro,
            CanalVenda canal,
            String localVenda,
            long precoCentavos,
            StatusPassagem status,
            PrazoDeReserva reserva) {
        if (id == null
                || viagemId == null
                || trecho == null
                || passageiro == null
                || reserva == null) {
            throw new DadoInvalido("Passagem inconsistente");
        }
        if (canal == null) {
            throw new DadoInvalido("Canal de venda invalido");
        }
        if (status == null) {
            throw new DadoInvalido("Status da passagem invalido");
        }
        if (precoCentavos < 0) {
            throw new DadoInvalido("Preco da passagem invalido");
        }
        this.id = id;
        this.viagemId = viagemId;
        this.trecho = trecho;
        this.passageiro = passageiro;
        this.canal = canal;
        this.localVenda = normalizarLocalDeVenda(canal, localVenda);
        this.precoCentavos = precoCentavos;
        this.status = status;
        this.reserva = reserva;
    }

    public static Passagem reservar(
            ViagemId viagemId,
            Trecho trecho,
            NomePassageiro passageiro,
            CanalVenda canal,
            String localVenda,
            long precoCentavos,
            PrazoDeReserva reserva) {
        return new Passagem(
                PassagemId.novo(),
                viagemId,
                trecho,
                passageiro,
                canal,
                localVenda,
                precoCentavos,
                StatusPassagem.RESERVADA,
                reserva);
    }

    public static Passagem reconstituir(
            PassagemId id,
            ViagemId viagemId,
            Trecho trecho,
            NomePassageiro passageiro,
            CanalVenda canal,
            String localVenda,
            long precoCentavos,
            StatusPassagem status,
            PrazoDeReserva reserva) {
        return new Passagem(
                id,
                viagemId,
                trecho,
                passageiro,
                canal,
                localVenda,
                precoCentavos,
                status,
                reserva);
    }

    public boolean ocupaLugar(OffsetDateTime agora) {
        return switch (status) {
            case CONFIRMADA -> true;
            case RESERVADA -> !reserva.venceu(agora);
        };
    }

    public void garantirFormaAceita(FormaPagamento forma) {
        if (forma == null) {
            throw new DadoInvalido("Forma de pagamento e obrigatoria");
        }
        if (!canal.aceita(forma)) {
            throw new RegraDeNegocioViolada("Forma de pagamento nao aceita neste canal");
        }
    }

    public void garantirLocalDeVenda(List<String> locaisDaRota) {
        if (canal == CanalVenda.GUICHE && !locaisDaRota.contains(localVenda)) {
            throw new DadoInvalido("Local de venda nao pertence a rota");
        }
    }

    public void garantirNaoPaga() {
        if (status == StatusPassagem.CONFIRMADA) {
            throw new RegraDeNegocioViolada("Passagem ja esta paga");
        }
    }

    public void garantirReservaVigente(OffsetDateTime agora) {
        if (!ocupaLugar(agora)) {
            throw new RegraDeNegocioViolada("Reserva da passagem expirada");
        }
    }

    public void confirmar() {
        garantirNaoPaga();
        status = StatusPassagem.CONFIRMADA;
    }

    public PassagemId id() {
        return id;
    }

    public ViagemId viagemId() {
        return viagemId;
    }

    public Trecho trecho() {
        return trecho;
    }

    public NomePassageiro passageiro() {
        return passageiro;
    }

    public CanalVenda canal() {
        return canal;
    }

    public Optional<String> localVenda() {
        return Optional.ofNullable(localVenda);
    }

    public long precoCentavos() {
        return precoCentavos;
    }

    public StatusPassagem status() {
        return status;
    }

    public PrazoDeReserva reserva() {
        return reserva;
    }

    private static String normalizarLocalDeVenda(CanalVenda canal, String localVenda) {
        String local = localVenda == null || localVenda.isBlank() ? null : localVenda.strip();
        if (canal == CanalVenda.INTERNET && local != null) {
            throw new DadoInvalido("Local de venda nao se aplica a venda pela internet");
        }
        if (canal == CanalVenda.GUICHE && local == null) {
            throw new DadoInvalido("Local de venda e obrigatorio no guiche");
        }
        if (local != null && local.length() > MAXIMO_LOCAL_VENDA) {
            throw new DadoInvalido("Local de venda deve ter ate 100 caracteres");
        }
        return local;
    }

    @Override
    public boolean equals(Object outro) {
        return this == outro || (outro instanceof Passagem passagem && id.equals(passagem.id));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
