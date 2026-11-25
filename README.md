# Viação Calango API

API REST acadêmica baseada no minimundo da Viação Calango (`Minimundo_1IHM_Passageiros.pdf`), em **Java 21 + Spring Boot 4**. A arquitetura é um **monólito modular em camadas, organizado por DDD**, com **Value Objects** no domínio.

> **Estado atual:** somente o módulo **Frota** está implementado. Viagens, Passagens e Motoristas entram nos próximos ciclos, seguindo o mesmo molde (veja [docs/ARQUITETURA.md](docs/ARQUITETURA.md)).

## Requisitos

- **JDK 21**
- Maven **não** precisa ser instalado: use o wrapper (`./mvnw`; no Windows, `mvnw.cmd`)
- PostgreSQL é opcional (o H2 em arquivo basta para desenvolvimento)

## Executar

```bash
./mvnw spring-boot:run
```

| O quê | Onde |
|---|---|
| Swagger UI | http://127.0.0.1:8080/docs |
| OpenAPI JSON | http://127.0.0.1:8080/openapi.json |
| Status | http://127.0.0.1:8080/health |

A primeira execução baixa o Maven e as dependências e cria as tabelas pelas migrações Flyway. Para configurar o ambiente, copie `.env.example` para `.env` (**nunca versione o `.env`**).

## Roteiro para demonstrar a Frota no Swagger

1. `POST /api/v1/onibus` — crie um ônibus (`tipo`, `capacidade` 23/28/32, `quilometragem_total` opcional).
2. `PATCH /api/v1/onibus/{id}/quilometragem` — informe `10000`: o ônibus passa a `precisa_revisao: true`.
3. `GET /api/v1/onibus?precisa_revisao=true` — lista os ônibus a encaminhar para revisão.
4. `POST /api/v1/onibus/{id}/revisoes` — conclui a revisão; `km_desde_revisao` volta a 0.
5. `GET /api/v1/onibus/alocacao-ideal?passageiros=60` — combinação com o mínimo de lugares vazios.

## Qualidade

```bash
./mvnw verify          # compila e confere a formatação (Spotless)
./mvnw spotless:apply  # corrige a formatação
```

## Documentação

- [docs/ARQUITETURA.md](docs/ARQUITETURA.md) — camadas, DDD e Value Objects
- [docs/CONTRATOS_API.md](docs/CONTRATOS_API.md) — rotas e formato de erros
