package com.exemplo.fixedincome;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FixedIncomeApplicationTests {

    @Autowired
    private MockMvc mvc;

    @Test
    void contextoSobeEHealthRespondeUp() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void simulacaoFuncionaComOContextoCompleto() throws Exception {
        mvc.perform(post("/api/v1/simulacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"produto":"CDB","tipoTaxa":"PREFIXADA","valorAplicado":10000,"taxaAnual":12,"prazoDias":540}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorLiquido").value(11505.96));
    }

    @Test
    void openApiEstaDisponivel() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/simulacoes']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/aliquotas-ir']").exists());
    }
}
