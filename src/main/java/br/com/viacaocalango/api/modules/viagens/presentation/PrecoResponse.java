package br.com.viacaocalango.api.modules.viagens.presentation;
public record PrecoResponse(double distancia_km, long preco_centavos, double preco_reais, boolean percurso_completo, boolean desconto_antecedencia) {}
