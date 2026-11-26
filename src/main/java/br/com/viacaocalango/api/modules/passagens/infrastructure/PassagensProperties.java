package br.com.viacaocalango.api.modules.passagens.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("calango.passagens")
public record PassagensProperties(@DefaultValue("15") int reservaMinutos) {}
