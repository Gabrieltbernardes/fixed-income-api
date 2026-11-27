package io.github.gabrieltbernardes.fixedincome.domain;

import java.math.BigDecimal;

/**
 * Resultado de uma simulação. Valores monetários já arredondados para 2 casas (HALF_EVEN).
 * Taxas e alíquotas em percentual.
 */
public record ResultadoSimulacao(
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
}
