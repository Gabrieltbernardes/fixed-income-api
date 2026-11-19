package com.exemplo.fixedincome.domain;

/**
 * Forma de remuneração da aplicação.
 */
public enum TipoTaxa {

    /** Taxa anual efetiva informada diretamente. */
    PREFIXADA,

    /** Percentual de um CDI anual informado na requisição. */
    PERCENTUAL_CDI
}
