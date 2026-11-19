package com.exemplo.fixedincome.domain;

import java.math.BigDecimal;

/**
 * Faixas da tabela regressiva de IR, por prazo em dias corridos.
 * As faixas são fixas em lei, por isso ficam em um enum e não em configuração.
 */
public enum FaixaIr {

    ATE_180_DIAS(1, 180, "22.5"),
    DE_181_A_360_DIAS(181, 360, "20.0"),
    DE_361_A_720_DIAS(361, 720, "17.5"),
    ACIMA_DE_720_DIAS(721, null, "15.0");

    private final int prazoMinimoDias;
    private final Integer prazoMaximoDias;
    private final BigDecimal aliquota;

    FaixaIr(int prazoMinimoDias, Integer prazoMaximoDias, String aliquota) {
        this.prazoMinimoDias = prazoMinimoDias;
        this.prazoMaximoDias = prazoMaximoDias;
        this.aliquota = new BigDecimal(aliquota);
    }

    /** Primeiro dia (inclusive) coberto pela faixa. */
    public int getPrazoMinimoDias() {
        return prazoMinimoDias;
    }

    /** Último dia (inclusive) coberto pela faixa; {@code null} na última faixa, que não tem limite. */
    public Integer getPrazoMaximoDias() {
        return prazoMaximoDias;
    }

    /** Alíquota em percentual (ex.: 22.5 para 22,5%). */
    public BigDecimal getAliquota() {
        return aliquota;
    }

    public boolean contem(int prazoDias) {
        return prazoDias >= prazoMinimoDias && (prazoMaximoDias == null || prazoDias <= prazoMaximoDias);
    }
}
