package br.com.viacaocalango.api.modules.viagens.presentation;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;
public record CriarViagemRequest(@NotNull UUID rota_id, @NotNull OffsetDateTime data_hora_partida) {}
