package io.github.gabrieltbernardes.fixedincome.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AliquotaController.class)
class AliquotaControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void listaAsQuatroFaixasNaOrdemCerta() throws Exception {
        mvc.perform(get("/api/v1/aliquotas-ir"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        [
                          {"prazoMinimoDias": 1,   "prazoMaximoDias": 180,  "aliquota": 22.5, "descricao": "Até 180 dias"},
                          {"prazoMinimoDias": 181, "prazoMaximoDias": 360,  "aliquota": 20.0, "descricao": "181 a 360 dias"},
                          {"prazoMinimoDias": 361, "prazoMaximoDias": 720,  "aliquota": 17.5, "descricao": "361 a 720 dias"},
                          {"prazoMinimoDias": 721, "prazoMaximoDias": null, "aliquota": 15.0, "descricao": "Acima de 720 dias"}
                        ]
                        """, JsonCompareMode.STRICT));
    }
}
