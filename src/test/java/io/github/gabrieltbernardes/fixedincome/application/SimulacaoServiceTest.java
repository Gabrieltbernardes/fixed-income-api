package io.github.gabrieltbernardes.fixedincome.application;

import io.github.gabrieltbernardes.fixedincome.api.dto.SimulacaoRequest;
import io.github.gabrieltbernardes.fixedincome.api.dto.SimulacaoResponse;
import io.github.gabrieltbernardes.fixedincome.domain.Produto;
import io.github.gabrieltbernardes.fixedincome.domain.TipoTaxa;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class SimulacaoServiceTest {

    private final SimulacaoService service = new SimulacaoService();

    private static BigDecimal bd(String valor) {
        return new BigDecimal(valor);
    }

    @Test
    void prefixadaUsaATaxaInformada() {
        var request = new SimulacaoRequest(Produto.CDB, TipoTaxa.PREFIXADA,
                bd("10000.00"), bd("12.0"), null, null, 540);

        assertThat(service.taxaEfetiva(request)).isEqualByComparingTo("12.0");
    }

    @Test
    void percentualCdiMultiplicaCdiPeloPercentual() {
        var request = new SimulacaoRequest(Produto.CDB, TipoTaxa.PERCENTUAL_CDI,
                bd("10000.00"), null, bd("110.0"), bd("13.5"), 540);

        assertThat(service.taxaEfetiva(request)).isEqualByComparingTo("14.85");
    }

    @Test
    void montaARespostaDoExemploConferido() {
        var request = new SimulacaoRequest(Produto.CDB, TipoTaxa.PREFIXADA,
                bd("10000"), bd("12"), null, null, 540);

        SimulacaoResponse r = service.simular(request);

        assertThat(r.produto()).isEqualTo(Produto.CDB);
        assertThat(r.prazoDias()).isEqualTo(540);
        assertThat(r.taxaAnualEfetiva()).isEqualTo(bd("12.00"));
        assertThat(r.valorAplicado()).isEqualTo(bd("10000.00"));
        assertThat(r.valorBruto()).isEqualTo(bd("11825.40"));
        assertThat(r.rendimentoBruto()).isEqualTo(bd("1825.40"));
        assertThat(r.isento()).isFalse();
        assertThat(r.aliquotaIr()).isEqualTo(bd("17.5"));
        assertThat(r.valorIr()).isEqualTo(bd("319.44"));
        assertThat(r.valorLiquido()).isEqualTo(bd("11505.96"));
        assertThat(r.rendimentoLiquido()).isEqualTo(bd("1505.96"));
    }

    @Test
    void respostaDeLciViaCdiEhIsenta() {
        var request = new SimulacaoRequest(Produto.LCI, TipoTaxa.PERCENTUAL_CDI,
                bd("5000"), null, bd("110"), bd("13.5"), 365);

        SimulacaoResponse r = service.simular(request);

        assertThat(r.taxaAnualEfetiva()).isEqualTo(bd("14.85"));
        assertThat(r.isento()).isTrue();
        assertThat(r.valorIr()).isEqualTo(bd("0.00"));
        assertThat(r.valorLiquido()).isEqualTo(bd("5742.50"));
    }

    @Test
    void taxaEfetivaPreservaCasasSignificativas() {
        var request = new SimulacaoRequest(Produto.CDB, TipoTaxa.PERCENTUAL_CDI,
                bd("1000"), null, bd("110"), bd("13.65"), 365);

        assertThat(service.simular(request).taxaAnualEfetiva()).isEqualTo(bd("15.015"));
    }
}
