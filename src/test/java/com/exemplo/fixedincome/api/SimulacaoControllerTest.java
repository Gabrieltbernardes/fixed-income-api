package com.exemplo.fixedincome.api;

import com.exemplo.fixedincome.application.SimulacaoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SimulacaoController.class)
@Import(SimulacaoService.class)
class SimulacaoControllerTest {

    private static final String URL = "/api/v1/simulacoes";

    @Autowired
    private MockMvc mvc;

    private ResultActions postar(String json) throws Exception {
        return mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    @Test
    void exemploConferidoDevolve200ComOJsonEsperado() throws Exception {
        postar("""
                {"produto":"CDB","tipoTaxa":"PREFIXADA","valorAplicado":10000.00,"taxaAnual":12.0,"prazoDias":540}
                """)
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                          "produto": "CDB",
                          "prazoDias": 540,
                          "taxaAnualEfetiva": 12.00,
                          "valorAplicado": 10000.00,
                          "valorBruto": 11825.40,
                          "rendimentoBruto": 1825.40,
                          "isento": false,
                          "aliquotaIr": 17.5,
                          "valorIr": 319.44,
                          "valorLiquido": 11505.96,
                          "rendimentoLiquido": 1505.96
                        }
                        """, JsonCompareMode.STRICT));
    }

    @Test
    void percentualCdiDevolve200() throws Exception {
        postar("""
                {"produto":"LCA","tipoTaxa":"PERCENTUAL_CDI","valorAplicado":5000,"percentualCdi":110.0,"cdiAnual":13.5,"prazoDias":365}
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taxaAnualEfetiva").value(14.85))
                .andExpect(jsonPath("$.isento").value(true))
                .andExpect(jsonPath("$.aliquotaIr").value(0.0))
                .andExpect(jsonPath("$.valorIr").value(0.0))
                .andExpect(jsonPath("$.valorLiquido").value(5742.50));
    }

    @Test
    void prazoNasBordas30E3650Devolve200() throws Exception {
        postar("""
                {"produto":"CDB","tipoTaxa":"PREFIXADA","valorAplicado":1000,"taxaAnual":10,"prazoDias":30}
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aliquotaIr").value(22.5));
        postar("""
                {"produto":"CDB","tipoTaxa":"PREFIXADA","valorAplicado":1000,"taxaAnual":10,"prazoDias":3650}
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aliquotaIr").value(15.0));
    }

    static Stream<Arguments> requisicoesInvalidas() {
        return Stream.of(
                // campo, json
                Arguments.of("produto", "é obrigatório",
                        """
                        {"tipoTaxa":"PREFIXADA","valorAplicado":1000,"taxaAnual":10,"prazoDias":60}"""),
                Arguments.of("tipoTaxa", "é obrigatório",
                        """
                        {"produto":"CDB","valorAplicado":1000,"taxaAnual":10,"prazoDias":60}"""),
                Arguments.of("valorAplicado", "é obrigatório",
                        """
                        {"produto":"CDB","tipoTaxa":"PREFIXADA","taxaAnual":10,"prazoDias":60}"""),
                Arguments.of("valorAplicado", "deve ser maior que 0",
                        """
                        {"produto":"CDB","tipoTaxa":"PREFIXADA","valorAplicado":0,"taxaAnual":10,"prazoDias":60}"""),
                Arguments.of("valorAplicado", "2 casas decimais",
                        """
                        {"produto":"CDB","tipoTaxa":"PREFIXADA","valorAplicado":1000.123,"taxaAnual":10,"prazoDias":60}"""),
                Arguments.of("taxaAnual", "é obrigatório quando tipoTaxa é PREFIXADA",
                        """
                        {"produto":"CDB","tipoTaxa":"PREFIXADA","valorAplicado":1000,"prazoDias":60}"""),
                Arguments.of("taxaAnual", "deve ser maior que 0",
                        """
                        {"produto":"CDB","tipoTaxa":"PREFIXADA","valorAplicado":1000,"taxaAnual":0,"prazoDias":60}"""),
                Arguments.of("percentualCdi", "não deve ser informado quando tipoTaxa é PREFIXADA",
                        """
                        {"produto":"CDB","tipoTaxa":"PREFIXADA","valorAplicado":1000,"taxaAnual":10,"percentualCdi":100,"prazoDias":60}"""),
                Arguments.of("cdiAnual", "não deve ser informado quando tipoTaxa é PREFIXADA",
                        """
                        {"produto":"CDB","tipoTaxa":"PREFIXADA","valorAplicado":1000,"taxaAnual":10,"cdiAnual":13.5,"prazoDias":60}"""),
                Arguments.of("percentualCdi", "é obrigatório quando tipoTaxa é PERCENTUAL_CDI",
                        """
                        {"produto":"CDB","tipoTaxa":"PERCENTUAL_CDI","valorAplicado":1000,"cdiAnual":13.5,"prazoDias":60}"""),
                Arguments.of("cdiAnual", "é obrigatório quando tipoTaxa é PERCENTUAL_CDI",
                        """
                        {"produto":"CDB","tipoTaxa":"PERCENTUAL_CDI","valorAplicado":1000,"percentualCdi":110,"prazoDias":60}"""),
                Arguments.of("taxaAnual", "não deve ser informado quando tipoTaxa é PERCENTUAL_CDI",
                        """
                        {"produto":"CDB","tipoTaxa":"PERCENTUAL_CDI","valorAplicado":1000,"taxaAnual":10,"percentualCdi":110,"cdiAnual":13.5,"prazoDias":60}"""),
                Arguments.of("percentualCdi", "deve ser maior que 0",
                        """
                        {"produto":"CDB","tipoTaxa":"PERCENTUAL_CDI","valorAplicado":1000,"percentualCdi":-1,"cdiAnual":13.5,"prazoDias":60}"""),
                Arguments.of("cdiAnual", "deve ser maior que 0",
                        """
                        {"produto":"CDB","tipoTaxa":"PERCENTUAL_CDI","valorAplicado":1000,"percentualCdi":110,"cdiAnual":0,"prazoDias":60}"""),
                Arguments.of("prazoDias", "é obrigatório",
                        """
                        {"produto":"CDB","tipoTaxa":"PREFIXADA","valorAplicado":1000,"taxaAnual":10}"""),
                Arguments.of("prazoDias", "deve ser maior ou igual a 30",
                        """
                        {"produto":"CDB","tipoTaxa":"PREFIXADA","valorAplicado":1000,"taxaAnual":10,"prazoDias":29}"""),
                Arguments.of("prazoDias", "deve ser menor ou igual a 3650",
                        """
                        {"produto":"CDB","tipoTaxa":"PREFIXADA","valorAplicado":1000,"taxaAnual":10,"prazoDias":3651}""")
        );
    }

    @ParameterizedTest(name = "[{index}] {0}: {1}")
    @MethodSource("requisicoesInvalidas")
    void regraDeValidacaoDevolve400EmProblemDetails(String campo, String mensagem, String json) throws Exception {
        postar(json)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("about:blank"))
                .andExpect(jsonPath("$.title").value("Requisição inválida"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.instance").value(URL))
                .andExpect(jsonPath("$.errors[?(@.campo == '%s')].mensagem", campo)
                        .value(hasItem(containsString(mensagem))));
    }

    @Test
    void variosErrosVoltamJuntosEOrdenadosPorCampo() throws Exception {
        postar("{}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(4)))
                .andExpect(jsonPath("$.errors[0].campo").value("prazoDias"))
                .andExpect(jsonPath("$.errors[1].campo").value("produto"))
                .andExpect(jsonPath("$.errors[2].campo").value("tipoTaxa"))
                .andExpect(jsonPath("$.errors[3].campo").value("valorAplicado"));
    }

    @Test
    void jsonMalformadoDevolve400() throws Exception {
        postar("{\"produto\": \"CDB\", ")
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Requisição inválida"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void enumDesconhecidoDevolve400ComValoresAceitos() throws Exception {
        postar("""
                {"produto":"POUPANCA","tipoTaxa":"PREFIXADA","valorAplicado":1000,"taxaAnual":10,"prazoDias":60}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Requisição inválida"))
                .andExpect(jsonPath("$.errors[0].campo").value("produto"))
                .andExpect(jsonPath("$.errors[0].mensagem").value(containsString("CDB, LCI, LCA")));
    }

    @Test
    void tipoErradoDevolve400() throws Exception {
        postar("""
                {"produto":"CDB","tipoTaxa":"PREFIXADA","valorAplicado":"mil","taxaAnual":10,"prazoDias":60}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].campo").value("valorAplicado"));
    }

    @Test
    void prazoFracionarioDevolve400() throws Exception {
        postar("""
                {"produto":"CDB","tipoTaxa":"PREFIXADA","valorAplicado":1000,"taxaAnual":10,"prazoDias":30.5}
                """)
                .andExpect(status().isBadRequest());
    }
}
