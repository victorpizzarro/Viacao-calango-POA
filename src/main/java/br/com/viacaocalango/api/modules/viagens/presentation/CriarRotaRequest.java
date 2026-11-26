package br.com.viacaocalango.api.modules.viagens.presentation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CriarRotaRequest(
        @NotBlank String nome, @NotEmpty List<@Valid ParadaRequest> paradas) {}
