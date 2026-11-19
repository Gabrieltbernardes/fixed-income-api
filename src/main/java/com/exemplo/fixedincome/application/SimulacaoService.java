package com.exemplo.fixedincome.application;

import com.exemplo.fixedincome.api.dto.SimulacaoRequest;
import com.exemplo.fixedincome.api.dto.SimulacaoResponse;
import com.exemplo.fixedincome.domain.CalculadoraRendaFixa;
import com.exemplo.fixedincome.domain.ResultadoSimulacao;
import com.exemplo.fixedincome.domain.TipoTaxa;
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
