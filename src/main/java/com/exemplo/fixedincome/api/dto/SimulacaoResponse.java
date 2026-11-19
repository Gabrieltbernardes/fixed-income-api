package com.exemplo.fixedincome.api.dto;

import com.exemplo.fixedincome.domain.Produto;
import com.exemplo.fixedincome.domain.ResultadoSimulacao;

import java.math.BigDecimal;

/**
 * Resultado de uma simulação. Valores monetários com 2 casas; taxas e alíquotas em percentual.
 */
public record SimulacaoResponse(
        Produto produto,
        int prazoDias,
        BigDecimal taxaAnualEfetiva,
        BigDecimal valorAplicado,
        BigDecimal valorBruto,
        BigDecimal rendimentoBruto,
        boolean isento,
        BigDecimal aliquotaIr,
        BigDecimal valorIr,
        BigDecimal valorLiquido,
        BigDecimal rendimentoLiquido) {

    public static SimulacaoResponse de(ResultadoSimulacao r) {
        return new SimulacaoResponse(
                r.produto(),
                r.prazoDias(),
                formatarTaxa(r.taxaAnualEfetiva()),
                r.valorAplicado(),
                r.valorBruto(),
                r.rendimentoBruto(),
                r.isento(),
                r.aliquotaIr(),
                r.valorIr(),
                r.valorLiquido(),
                r.rendimentoLiquido());
    }

    /** Exibe a taxa com no mínimo 2 casas (12 -> 12.00) sem perder casas significativas (15.015 continua 15.015). */
    static BigDecimal formatarTaxa(BigDecimal taxa) {
        int casas = Math.max(2, taxa.stripTrailingZeros().scale());
        return taxa.setScale(casas);
    }
}
