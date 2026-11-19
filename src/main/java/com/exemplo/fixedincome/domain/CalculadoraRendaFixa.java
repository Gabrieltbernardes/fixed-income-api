package com.exemplo.fixedincome.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Calcula valor bruto, IR e valor líquido de uma aplicação de renda fixa.
 *
 * <p>Convenções:
 * <ul>
 *   <li>Juros compostos com base de 365 dias corridos: {@code bruto = P * (1 + i)^(d/365)}.</li>
 *   <li>O expoente fracionário usa {@link Math#pow} em {@code double}; o fator volta para
 *       {@link BigDecimal} antes de qualquer outra conta.</li>
 *   <li>O IR é calculado sobre o rendimento <b>sem arredondar</b>.</li>
 *   <li>Arredondamento para 2 casas (HALF_EVEN) só na saída: bruto e IR são arredondados e o
 *       líquido é {@code bruto arredondado - IR arredondado}, para que a resposta feche
 *       ao centavo ({@code valorBruto - valorIr == valorLiquido}).</li>
 * </ul>
 */
public class CalculadoraRendaFixa {

    public static final int CASAS_MONETARIAS = 2;
    public static final RoundingMode ARREDONDAMENTO = RoundingMode.HALF_EVEN;

    private static final BigDecimal DIAS_NO_ANO = BigDecimal.valueOf(365);
    private static final BigDecimal ZERO_PERCENT = new BigDecimal("0.0");

    /**
     * Taxa anual efetiva de uma aplicação em percentual do CDI: {@code CDI anual * percentual}.
     * Ex.: 110% de 13,5% a.a. = 14,85% a.a. Simplificação: o cálculo exato aplica o percentual
     * sobre o CDI diário.
     *
     * @param cdiAnual      CDI anual em percentual (ex.: 13.5)
     * @param percentualCdi percentual do CDI (ex.: 110.0)
     * @return taxa anual efetiva em percentual (ex.: 14.85)
     */
    public BigDecimal taxaEfetivaPercentualCdi(BigDecimal cdiAnual, BigDecimal percentualCdi) {
        exigirPositivo(cdiAnual, "cdiAnual");
        exigirPositivo(percentualCdi, "percentualCdi");
        return cdiAnual.multiply(percentualCdi).movePointLeft(2);
    }

    /**
     * @param produto         produto da aplicação
     * @param valorAplicado   valor aplicado (P), maior que zero
     * @param taxaAnualPct    taxa anual efetiva em percentual (ex.: 12.0 para 12% a.a.)
     * @param prazoDias       prazo em dias corridos, maior ou igual a 1
     */
    public ResultadoSimulacao calcular(Produto produto, BigDecimal valorAplicado,
                                       BigDecimal taxaAnualPct, int prazoDias) {
        Objects.requireNonNull(produto, "produto");
        exigirPositivo(valorAplicado, "valorAplicado");
        exigirPositivo(taxaAnualPct, "taxaAnual");
        FaixaIr faixa = TabelaIrRegressivo.faixaPara(prazoDias);

        double taxa = taxaAnualPct.movePointLeft(2).doubleValue();
        double expoente = prazoDias / DIAS_NO_ANO.doubleValue();
        BigDecimal fator = BigDecimal.valueOf(Math.pow(1 + taxa, expoente));

        BigDecimal valorBruto = valorAplicado.multiply(fator);
        BigDecimal rendimentoBruto = valorBruto.subtract(valorAplicado);

        boolean isento = produto.isIsento();
        BigDecimal aliquota = isento ? ZERO_PERCENT : faixa.getAliquota();
        BigDecimal valorIr = rendimentoBruto.multiply(aliquota.movePointLeft(2));

        BigDecimal valorAplicadoSaida = monetario(valorAplicado);
        BigDecimal valorBrutoSaida = monetario(valorBruto);
        BigDecimal valorIrSaida = monetario(valorIr);
        BigDecimal valorLiquidoSaida = valorBrutoSaida.subtract(valorIrSaida);

        return new ResultadoSimulacao(
                produto,
                prazoDias,
                taxaAnualPct,
                valorAplicadoSaida,
                valorBrutoSaida,
                valorBrutoSaida.subtract(valorAplicadoSaida),
                isento,
                aliquota,
                valorIrSaida,
                valorLiquidoSaida,
                valorLiquidoSaida.subtract(valorAplicadoSaida));
    }

    private static BigDecimal monetario(BigDecimal valor) {
        return valor.setScale(CASAS_MONETARIAS, ARREDONDAMENTO);
    }

    private static void exigirPositivo(BigDecimal valor, String campo) {
        Objects.requireNonNull(valor, campo);
        if (valor.signum() <= 0) {
            throw new IllegalArgumentException(campo + " deve ser maior que zero: " + valor);
        }
    }
}
