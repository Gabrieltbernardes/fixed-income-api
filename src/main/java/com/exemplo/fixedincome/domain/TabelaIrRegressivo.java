package com.exemplo.fixedincome.domain;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Converte o prazo da aplicação na faixa (e alíquota) da tabela regressiva de IR.
 */
public final class TabelaIrRegressivo {

    private TabelaIrRegressivo() {
    }

    public static FaixaIr faixaPara(int prazoDias) {
        if (prazoDias < 1) {
            throw new IllegalArgumentException("prazoDias deve ser maior ou igual a 1: " + prazoDias);
        }
        return Arrays.stream(FaixaIr.values())
                .filter(faixa -> faixa.contem(prazoDias))
                .findFirst()
                .orElseThrow();
    }

    /** Alíquota em percentual para o prazo informado (ex.: 17.5). */
    public static BigDecimal aliquotaPara(int prazoDias) {
        return faixaPara(prazoDias).getAliquota();
    }

    /** As faixas na ordem crescente de prazo. */
    public static List<FaixaIr> faixas() {
        return List.of(FaixaIr.values());
    }
}
