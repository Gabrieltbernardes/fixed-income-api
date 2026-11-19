package com.exemplo.fixedincome.api;

import com.exemplo.fixedincome.api.dto.FaixaIrResponse;
import com.exemplo.fixedincome.domain.TabelaIrRegressivo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Alíquotas de IR")
public class AliquotaController {

    @GetMapping(path = "/api/v1/aliquotas-ir", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lista as faixas da tabela regressiva de IR, em ordem crescente de prazo")
    public List<FaixaIrResponse> listar() {
        return TabelaIrRegressivo.faixas().stream()
                .map(FaixaIrResponse::de)
                .toList();
    }
}
