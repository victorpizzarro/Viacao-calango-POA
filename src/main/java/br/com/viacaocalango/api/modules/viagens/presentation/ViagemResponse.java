package br.com.viacaocalango.api.modules.viagens.presentation;
import br.com.viacaocalango.api.modules.viagens.domain.entity.Viagem;
import br.com.viacaocalango.api.modules.viagens.domain.entity.StatusViagem;
import java.time.OffsetDateTime;
import java.util.UUID;
public record ViagemResponse(UUID id, UUID rota_id, String nome_rota, OffsetDateTime data_hora_partida, StatusViagem status) {
    public static ViagemResponse de(Viagem v) { return new ViagemResponse(v.id(), v.rota().getId().valor(), v.rota().getNome(), v.dataHoraPartida(), v.status()); }
}
