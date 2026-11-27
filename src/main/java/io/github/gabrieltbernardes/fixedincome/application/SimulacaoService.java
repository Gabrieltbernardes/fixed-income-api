package io.github.gabrieltbernardes.fixedincome.application;

import io.github.gabrieltbernardes.fixedincome.api.dto.SimulacaoRequest;
import io.github.gabrieltbernardes.fixedincome.api.dto.SimulacaoResponse;
import io.github.gabrieltbernardes.fixedincome.domain.CalculadoraRendaFixa;
import io.github.gabrieltbernardes.fixedincome.domain.ResultadoSimulacao;
import io.github.gabrieltbernardes.fixedincome.domain.TipoTaxa;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Orquestra a simulação: escolhe a taxa efetiva pelo tipo de taxa, chama o domínio e monta a resposta.
 */
@Service
public class SimulacaoService {

    private final CalculadoraRendaFixa calculadora = new CalculadoraRendaFixa();

    public SimulacaoResponse simular(SimulacaoRequest request) {
        BigDecimal taxaEfetiva = taxaEfetiva(request);
        ResultadoSimulacao resultado = calculadora.calcular(
                request.produto(), request.valorAplicado(), taxaEfetiva, request.prazoDias());
        return SimulacaoResponse.de(resultado);
    }

    BigDecimal taxaEfetiva(SimulacaoRequest request) {
        if (request.tipoTaxa() == TipoTaxa.PERCENTUAL_CDI) {
            return calculadora.taxaEfetivaPercentualCdi(request.cdiAnual(), request.percentualCdi());
        }
        return request.taxaAnual();
    }
}
