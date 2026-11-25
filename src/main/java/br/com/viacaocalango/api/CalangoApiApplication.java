package br.com.viacaocalango.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CalangoApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CalangoApiApplication.class, args);
    }
}
