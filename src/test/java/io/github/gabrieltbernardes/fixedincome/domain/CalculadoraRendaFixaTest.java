package io.github.gabrieltbernardes.fixedincome.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CalculadoraRendaFixaTest {

    private final CalculadoraRendaFixa calculadora = new CalculadoraRendaFixa();

    private static BigDecimal bd(String valor) {
        return new BigDecimal(valor);
    }

    @Test
    void exemploConferido_cdbPrefixado12AoAnoPor540Dias() {
        ResultadoSimulacao r = calculadora.calcular(Produto.CDB, bd("10000.00"), bd("12.0"), 540);

        assertThat(r.valorBruto()).isEqualTo(bd("11825.40"));
        assertThat(r.rendimentoBruto()).isEqualTo(bd("1825.40"));
        assertThat(r.isento()).isFalse();
        assertThat(r.aliquotaIr()).isEqualByComparingTo("17.5");
        assertThat(r.valorIr()).isEqualTo(bd("319.44"));
        assertThat(r.valorLiquido()).isEqualTo(bd("11505.96"));
        assertThat(r.rendimentoLiquido()).isEqualTo(bd("1505.96"));
        assertThat(r.valorAplicado()).isEqualTo(bd("10000.00"));
        assertThat(r.prazoDias()).isEqualTo(540);
        assertThat(r.produto()).isEqualTo(Produto.CDB);
        assertThat(r.taxaAnualEfetiva()).isEqualByComparingTo("12");
    }

    @Test
    void irIncideSobreRendimentoSemArredondar() {
        // Rendimento exato = 1825,3963...; 17,5% = 319,4443... -> 319,44.
        // Arredondar o rendimento antes (1825,40 x 17,5% = 319,445) deixaria o centavo
        // dependente do modo de arredondamento (HALF_UP daria 319,45).
        ResultadoSimulacao r = calculadora.calcular(Produto.CDB, bd("10000"), bd("12"), 540);

        BigDecimal irSobreRendimentoArredondado = r.rendimentoBruto().multiply(bd("0.175"));
        assertThat(irSobreRendimentoArredondado).isEqualByComparingTo("319.445");
        assertThat(r.valorIr()).isEqualTo(bd("319.44"));
    }

    @Test
    void valoresMonetariosSaemComDuasCasasEFechamAoCentavo() {
        ResultadoSimulacao r = calculadora.calcular(Produto.CDB, bd("1234.56"), bd("9.87"), 777);

        assertThat(r.valorAplicado().scale()).isEqualTo(2);
        assertThat(r.valorBruto().scale()).isEqualTo(2);
        assertThat(r.valorIr().scale()).isEqualTo(2);
        assertThat(r.valorLiquido().scale()).isEqualTo(2);
        assertThat(r.valorBruto().subtract(r.valorIr())).isEqualTo(r.valorLiquido());
        assertThat(r.valorBruto().subtract(r.valorAplicado())).isEqualTo(r.rendimentoBruto());
        assertThat(r.valorLiquido().subtract(r.valorAplicado())).isEqualTo(r.rendimentoLiquido());
    }

    @Test
    void prazoDeUmAnoRendeExatamenteATaxa() {
        ResultadoSimulacao r = calculadora.calcular(Produto.CDB, bd("1000"), bd("10"), 365);

        assertThat(r.valorBruto()).isEqualTo(bd("1100.00"));
        assertThat(r.aliquotaIr()).isEqualByComparingTo("17.5");
        assertThat(r.valorIr()).isEqualTo(bd("17.50"));
        assertThat(r.valorLiquido()).isEqualTo(bd("1082.50"));
    }

    @ParameterizedTest
    @EnumSource(value = Produto.class, names = {"LCI", "LCA"})
    void lciELcaSaoIsentas(Produto produto) {
        ResultadoSimulacao r = calculadora.calcular(produto, bd("10000.00"), bd("12.0"), 540);

        assertThat(r.isento()).isTrue();
        assertThat(r.aliquotaIr()).isEqualByComparingTo("0");
        assertThat(r.valorIr()).isEqualTo(bd("0.00"));
        assertThat(r.valorBruto()).isEqualTo(bd("11825.40"));
        assertThat(r.valorLiquido()).isEqualTo(r.valorBruto());
        assertThat(r.rendimentoLiquido()).isEqualTo(r.rendimentoBruto());
    }

    @Test
    void percentualDoCdi_110PorCentoDe13Virgula5DaTaxaDe14Virgula85() {
        assertThat(calculadora.taxaEfetivaPercentualCdi(bd("13.5"), bd("110.0")))
                .isEqualByComparingTo("14.85");
        assertThat(calculadora.taxaEfetivaPercentualCdi(bd("13.65"), bd("110")))
                .isEqualByComparingTo("15.015");
    }

    @Test
    void percentualDoCdiAlimentaOCalculo() {
        BigDecimal taxa = calculadora.taxaEfetivaPercentualCdi(bd("13.5"), bd("110"));
        ResultadoSimulacao viaCdi = calculadora.calcular(Produto.CDB, bd("5000"), taxa, 365);

        // 5000 x 1,1485 = 5742,50; IR 17,5% de 742,50 = 129,9375 -> 129,94
        assertThat(viaCdi.valorBruto()).isEqualTo(bd("5742.50"));
        assertThat(viaCdi.valorIr()).isEqualTo(bd("129.94"));
        assertThat(viaCdi.valorLiquido()).isEqualTo(bd("5612.56"));
    }

    @Test
    void prazoMinimoAplicaAMaiorAliquota() {
        ResultadoSimulacao r = calculadora.calcular(Produto.CDB, bd("10000"), bd("12"), 30);
        assertThat(r.aliquotaIr()).isEqualByComparingTo("22.5");
    }

    @Test
    void prazoMaximoAplicaAMenorAliquota() {
        ResultadoSimulacao r = calculadora.calcular(Produto.CDB, bd("10000"), bd("12"), 3650);
        assertThat(r.aliquotaIr()).isEqualByComparingTo("15.0");
    }

    @Test
    void deveRejeitarEntradasInvalidas() {
        assertThatThrownBy(() -> calculadora.calcular(null, bd("1"), bd("1"), 30))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> calculadora.calcular(Produto.CDB, null, bd("1"), 30))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> calculadora.calcular(Produto.CDB, bd("0"), bd("1"), 30))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("valorAplicado");
        assertThatThrownBy(() -> calculadora.calcular(Produto.CDB, bd("1"), bd("-1"), 30))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("taxaAnual");
        assertThatThrownBy(() -> calculadora.calcular(Produto.CDB, bd("1"), bd("1"), 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("prazoDias");
        assertThatThrownBy(() -> calculadora.taxaEfetivaPercentualCdi(bd("0"), bd("100")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cdiAnual");
        assertThatThrownBy(() -> calculadora.taxaEfetivaPercentualCdi(bd("13"), bd("0")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("percentualCdi");
    }
}
