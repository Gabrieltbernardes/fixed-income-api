package com.exemplo.fixedincome.api.dto;

import com.exemplo.fixedincome.domain.FaixaIr;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Faixa da tabela regressiva de IR")
public record FaixaIrResponse(
        @Schema(example = "1") int prazoMinimoDias,
        @Schema(example = "180", nullable = true, description = "null na última faixa (sem limite)") Integer prazoMaximoDias,
        @Schema(example = "22.5", description = "Alíquota em %") BigDecimal aliquota,
        @Schema(example = "Até 180 dias") String descricao) {

    public static FaixaIrResponse de(FaixaIr faixa) {
        String descricao = faixa.getPrazoMaximoDias() == null
                ? "Acima de " + (faixa.getPrazoMinimoDias() - 1) + " dias"
                : faixa.getPrazoMinimoDias() == 1
                ? "Até " + faixa.getPrazoMaximoDias() + " dias"
                : faixa.getPrazoMinimoDias() + " a " + faixa.getPrazoMaximoDias() + " dias";
        return new FaixaIrResponse(faixa.getPrazoMinimoDias(), faixa.getPrazoMaximoDias(), faixa.getAliquota(), descricao);
    }
}
