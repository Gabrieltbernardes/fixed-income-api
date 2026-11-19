package com.exemplo.fixedincome.api;

import com.exemplo.fixedincome.api.dto.SimulacaoRequest;
import com.exemplo.fixedincome.api.dto.SimulacaoResponse;
import com.exemplo.fixedincome.application.SimulacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/simulacoes")
@Tag(name = "Simulações")
public class SimulacaoController {

    private final SimulacaoService service;

    public SimulacaoController(SimulacaoService service) {
        this.service = service;
    }

    /** Responde 200, e não 201, porque nenhuma simulação é gravada. */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Calcula valor bruto, IR e valor líquido de uma aplicação")
    @ApiResponse(responseCode = "200", description = "Simulação calculada")
    @ApiResponse(responseCode = "400", description = "Requisição inválida (Problem Details)")
    public SimulacaoResponse simular(@Valid @RequestBody SimulacaoRequest request) {
        return service.simular(request);
    }
}
