# fixed-income-api

![CI](https://github.com/Gabrieltbernardes/fixed-income-api/actions/workflows/ci.yml/badge.svg)

API REST em Spring Boot que simula aplicações de renda fixa (CDB, LCI e LCA) e devolve
valor bruto, IR pela tabela regressiva e valor líquido. É stateless: não usa banco,
autenticação ou integrações externas.

## Como rodar

Pré-requisito: Java 21 (ou só Docker).

```bash
./mvnw spring-boot:run          # Windows: mvnw.cmd spring-boot:run
```

Com Docker:

```bash
docker build -t fixed-income-api .
docker run --rm -p 8080:8080 fixed-income-api
```

Testes + cobertura (o build quebra abaixo de 80% de linhas no total ou de 100% no pacote `domain`):

```bash
./mvnw verify
# relatório: target/site/jacoco/index.html
```

## Endpoints

| Método e rota | O que faz |
| --- | --- |
| `POST /api/v1/simulacoes` | Calcula bruto, IR e líquido (200) |
| `GET /api/v1/aliquotas-ir` | Lista as quatro faixas da tabela regressiva |
| `GET /actuator/health` | Health check |
| `GET /swagger-ui.html` | Documentação OpenAPI |

### Exemplos

CDB prefixado, R$ 10.000 a 12% a.a. por 540 dias:

```bash
curl -X POST http://localhost:8080/api/v1/simulacoes \
  -H "Content-Type: application/json" \
  -d '{"produto":"CDB","tipoTaxa":"PREFIXADA","valorAplicado":10000,"taxaAnual":12,"prazoDias":540}'
```

```json
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
```

LCI a 110% de um CDI de 13,5% a.a.:

```bash
curl -X POST http://localhost:8080/api/v1/simulacoes \
  -H "Content-Type: application/json" \
  -d '{"produto":"LCI","tipoTaxa":"PERCENTUAL_CDI","valorAplicado":5000,"percentualCdi":110,"cdiAnual":13.5,"prazoDias":365}'
```

Requisição inválida (400, Problem Details):

```bash
curl -X POST http://localhost:8080/api/v1/simulacoes \
  -H "Content-Type: application/json" \
  -d '{"produto":"CDB","tipoTaxa":"PREFIXADA","valorAplicado":10000,"taxaAnual":12,"prazoDias":10}'
```

```json
{
  "type": "about:blank",
  "title": "Requisição inválida",
  "status": 400,
  "detail": "Um ou mais campos são inválidos.",
  "instance": "/api/v1/simulacoes",
  "errors": [
    { "campo": "prazoDias", "mensagem": "deve ser maior ou igual a 30" }
  ]
}
```

## Campos da requisição

| Campo | Regra |
| --- | --- |
| `produto` | CDB, LCI ou LCA; obrigatório |
| `tipoTaxa` | PREFIXADA ou PERCENTUAL_CDI; obrigatório |
| `valorAplicado` | Maior que 0, até 2 casas decimais |
| `taxaAnual` | % a.a.; obrigatório se PREFIXADA (e proibido se PERCENTUAL_CDI) |
| `percentualCdi` | %; obrigatório se PERCENTUAL_CDI (e proibido se PREFIXADA) |
| `cdiAnual` | % a.a.; obrigatório se PERCENTUAL_CDI (e proibido se PREFIXADA) |
| `prazoDias` | Inteiro de 30 a 3650 |

## Regras de cálculo

IR regressivo sobre o rendimento, pelo prazo em dias corridos. LCI e LCA são isentas.

| Prazo (dias corridos) | Alíquota |
| --- | --- |
| Até 180 | 22,5% |
| 181 a 360 | 20,0% |
| 361 a 720 | 17,5% |
| Acima de 720 | 15,0% |

- `valorBruto = P × (1 + i)^(d/365)`, com base de 365 dias corridos.
- Percentual do CDI: `i = CDI anual × percentual` (simplificação; o exato usa o CDI diário).
- O IR é calculado sobre o rendimento **sem arredondar**.
- Bruto e IR são arredondados para 2 casas (`HALF_EVEN`) só na saída, e o líquido é
  `bruto arredondado − IR arredondado`, para que a resposta feche ao centavo.

Fora do escopo: IOF, come-cotas, marcação a mercado, persistência e busca automática do CDI.

## Estrutura

```text
src/main/java/io/github/gabrieltbernardes/fixedincome
├── api            controllers, DTOs, validação e handler de erros
├── application    SimulacaoService (orquestra o cálculo)
└── domain         regras puras em Java, sem Spring
```

## CI/CD

`.github/workflows/ci.yml`:

- **build** (todo push na `main` e todo PR): `./mvnw -B verify` e upload do relatório JaCoCo.
- **docker** (depois do build): builda a imagem e, só em push na `main`, publica
  `ghcr.io/<usuario>/<repo>:latest` e `:<sha>` no GHCR.
