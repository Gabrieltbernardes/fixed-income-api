package com.exemplo.fixedincome.api.dto;

import com.exemplo.fixedincome.domain.TipoTaxa;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TaxaConsistenteValidator implements ConstraintValidator<TaxaConsistente, SimulacaoRequest> {

    @Override
    public boolean isValid(SimulacaoRequest request, ConstraintValidatorContext context) {
        if (request == null || request.tipoTaxa() == null) {
            // @NotNull em tipoTaxa já reporta a ausência
            return true;
        }
        context.disableDefaultConstraintViolation();
        boolean valido = true;

        if (request.tipoTaxa() == TipoTaxa.PREFIXADA) {
            valido &= exigir(request.taxaAnual() != null, "taxaAnual", "é obrigatório quando tipoTaxa é PREFIXADA", context);
            valido &= exigir(request.percentualCdi() == null, "percentualCdi", "não deve ser informado quando tipoTaxa é PREFIXADA", context);
            valido &= exigir(request.cdiAnual() == null, "cdiAnual", "não deve ser informado quando tipoTaxa é PREFIXADA", context);
        } else {
            valido &= exigir(request.percentualCdi() != null, "percentualCdi", "é obrigatório quando tipoTaxa é PERCENTUAL_CDI", context);
            valido &= exigir(request.cdiAnual() != null, "cdiAnual", "é obrigatório quando tipoTaxa é PERCENTUAL_CDI", context);
            valido &= exigir(request.taxaAnual() == null, "taxaAnual", "não deve ser informado quando tipoTaxa é PERCENTUAL_CDI", context);
        }
        return valido;
    }

    private static boolean exigir(boolean condicao, String campo, String mensagem, ConstraintValidatorContext context) {
        if (!condicao) {
            context.buildConstraintViolationWithTemplate(mensagem)
                    .addPropertyNode(campo)
                    .addConstraintViolation();
        }
        return condicao;
    }
}
