package io.github.gabrieltbernardes.fixedincome.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TabelaIrRegressivoTest {

    @ParameterizedTest(name = "{0} dias -> {1}%")
    @CsvSource({
            "30, 22.5", "180, 22.5",
            "181, 20.0", "360, 20.0",
            "361, 17.5", "720, 17.5",
            "721, 15.0", "3650, 15.0"
    })
    void deveAplicarAliquotaDaFaixa(int prazoDias, BigDecimal aliquotaEsperada) {
        assertThat(TabelaIrRegressivo.aliquotaPara(prazoDias))
                .isEqualByComparingTo(aliquotaEsperada);
    }

    @ParameterizedTest(name = "{0} dias -> {1}")
    @CsvSource({
            "1, ATE_180_DIAS",
            "180, ATE_180_DIAS",
            "181, DE_181_A_360_DIAS",
            "720, DE_361_A_720_DIAS",
            "721, ACIMA_DE_720_DIAS",
            "100000, ACIMA_DE_720_DIAS"
    })
    void deveEncontrarAFaixa(int prazoDias, FaixaIr esperada) {
        assertThat(TabelaIrRegressivo.faixaPara(prazoDias)).isEqualTo(esperada);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
    void deveRejeitarPrazoNaoPositivo(int prazoDias) {
        assertThatThrownBy(() -> TabelaIrRegressivo.faixaPara(prazoDias))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("prazoDias");
    }

    @Test
    void deveListarAsQuatroFaixasEmOrdemCrescente() {
        assertThat(TabelaIrRegressivo.faixas())
                .extracting(FaixaIr::getAliquota)
                .usingElementComparator(BigDecimal::compareTo)
                .containsExactly(
                        new BigDecimal("22.5"), new BigDecimal("20.0"),
                        new BigDecimal("17.5"), new BigDecimal("15.0"));
    }

    @Test
    void faixasDevemSerContiguasESemSobreposicao() {
        var faixas = TabelaIrRegressivo.faixas();
        assertThat(faixas.get(0).getPrazoMinimoDias()).isEqualTo(1);
        for (int i = 1; i < faixas.size(); i++) {
            assertThat(faixas.get(i).getPrazoMinimoDias())
                    .isEqualTo(faixas.get(i - 1).getPrazoMaximoDias() + 1);
        }
        assertThat(faixas.get(faixas.size() - 1).getPrazoMaximoDias()).isNull();
    }
}
