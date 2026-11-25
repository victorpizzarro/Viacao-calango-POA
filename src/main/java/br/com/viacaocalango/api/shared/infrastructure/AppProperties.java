package br.com.viacaocalango.api.shared.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app")
public record AppProperties(String nome, String ambiente) {}
