package io.github.gabrieltbernardes.fixedincome;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(
        title = "fixed-income-api",
        version = "v1",
        description = "Simula aplicações de renda fixa (CDB, LCI, LCA) e devolve valor bruto, "
                + "IR pela tabela regressiva e valor líquido."))
public class FixedIncomeApplication {

    public static void main(String[] args) {
        SpringApplication.run(FixedIncomeApplication.class, args);
    }
}
