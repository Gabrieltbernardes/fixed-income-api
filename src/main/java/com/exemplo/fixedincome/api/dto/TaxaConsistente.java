package com.exemplo.fixedincome.api.dto;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Valida a combinação entre {@code tipoTaxa} e os campos de taxa:
 * PREFIXADA exige {@code taxaAnual} e não aceita {@code percentualCdi}/{@code cdiAnual};
 * PERCENTUAL_CDI exige {@code percentualCdi} e {@code cdiAnual} e não aceita {@code taxaAnual}.
 * Cada violação é reportada no campo correspondente.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TaxaConsistenteValidator.class)
public @interface TaxaConsistente {

    String message() default "combinação inválida de tipoTaxa e campos de taxa";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
