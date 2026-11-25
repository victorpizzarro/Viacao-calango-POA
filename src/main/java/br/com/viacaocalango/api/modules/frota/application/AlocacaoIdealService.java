package br.com.viacaocalango.api.modules.frota.application;

import br.com.viacaocalango.api.modules.frota.domain.service.CalculadoraAlocacaoIdeal;
import br.com.viacaocalango.api.modules.frota.domain.valueobject.AlocacaoIdeal;
import br.com.viacaocalango.api.modules.frota.domain.valueobject.Passageiros;
import org.springframework.stereotype.Service;

@Service
public class AlocacaoIdealService {

    private final CalculadoraAlocacaoIdeal calculadora;

    public AlocacaoIdealService(CalculadoraAlocacaoIdeal calculadora) {
        this.calculadora = calculadora;
    }

    public AlocacaoIdeal calcular(int passageiros) {
        return calculadora.calcular(Passageiros.de(passageiros));
    }
}
