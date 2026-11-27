package io.github.gabrieltbernardes.fixedincome.api.dto;

import io.github.gabrieltbernardes.fixedincome.domain.Produto;
import io.github.gabrieltbernardes.fixedincome.domain.TipoTaxa;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Dados de entrada de uma simulação.
 * Para PREFIXADA informe {@code taxaAnual}; para PERCENTUAL_CDI informe {@code percentualCdi} e {@code cdiAnual}.
 */
@TaxaConsistente
@Schema(description = "Dados da aplicação a simular")
public record SimulacaoRequest(

        @Schema(example = "CDB")
        @NotNull(message = "é obrigatório")
        Produto produto,

        @Schema(example = "PREFIXADA")
        @NotNull(message = "é obrigatório")
        TipoTaxa tipoTaxa,

        @Schema(example = "10000.00", description = "Valor aplicado em reais, até 2 casas decimais")
        @NotNull(message = "é obrigatório")
        @DecimalMin(value = "0", inclusive = false, message = "deve ser maior que 0")
        @Digits(integer = 12, fraction = 2, message = "deve ter no máximo 12 dígitos inteiros e 2 casas decimais")
        BigDecimal valorAplicado,

        @Schema(example = "12.0", description = "Taxa anual em % a.a. Obrigatória se PREFIXADA")
        @DecimalMin(value = "0", inclusive = false, message = "deve ser maior que 0")
        @Digits(integer = 3, fraction = 4, message = "deve ter no máximo 3 dígitos inteiros e 4 casas decimais")
        BigDecimal taxaAnual,

        @Schema(example = "110.0", description = "Percentual do CDI. Obrigatório se PERCENTUAL_CDI")
        @DecimalMin(value = "0", inclusive = false, message = "deve ser maior que 0")
        @Digits(integer = 3, fraction = 4, message = "deve ter no máximo 3 dígitos inteiros e 4 casas decimais")
        BigDecimal percentualCdi,

        @Schema(example = "13.5", description = "CDI anual em % a.a. Obrigatório se PERCENTUAL_CDI")
        @DecimalMin(value = "0", inclusive = false, message = "deve ser maior que 0")
        @Digits(integer = 3, fraction = 4, message = "deve ter no máximo 3 dígitos inteiros e 4 casas decimais")
        BigDecimal cdiAnual,

        @Schema(example = "540", description = "Prazo em dias corridos, de 30 a 3650")
        @NotNull(message = "é obrigatório")
        @Min(value = 30, message = "deve ser maior ou igual a 30")
        @Max(value = 3650, message = "deve ser menor ou igual a 3650")
        Integer prazoDias) {
}
