package com.exemplo.fixedincome.domain;

/**
 * Produtos de renda fixa suportados. LCI e LCA são isentas de IR para pessoa física.
 */
public enum Produto {

    CDB(false),
    LCI(true),
    LCA(true);

    private final boolean isento;

    Produto(boolean isento) {
        this.isento = isento;
    }

    public boolean isIsento() {
        return isento;
    }
}
