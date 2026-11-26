# Viação Calango API

API REST acadêmica baseada no minimundo da Viação Calango (`Minimundo_1IHM_Passageiros.pdf`), em **Java 21 + Spring Boot 4**. A arquitetura é um **monólito modular em camadas, organizado por DDD**, com **Value Objects** no domínio.

> **Estado atual:** Frota, Motoristas, Viagens e Passagens estão integrados. O fluxo demonstrável vai do cadastro da rota até o pagamento simulado da passagem pela internet. Veja [os contratos da API](docs/CONTRATOS_API.md).

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

## Demonstração integrada no Swagger

Prepare o JAR antes da apresentação. No Mac, selecione um JDK 21 instalado:

```bash
export JAVA_HOME="$(/usr/libexec/java_home -v 21)"
"$JAVA_HOME/bin/java" -version
./mvnw -B -ntp verify
```

Se o JDK 21 foi extraído manualmente e o macOS ainda não o reconhece, aponte `JAVA_HOME` para o diretório `Contents/Home` desse JDK.

Na hora da apresentação, rode o JAR já preparado com um H2 novo. Assim, o ensaio e a apresentação têm dados separados e o Maven não precisa baixar dependências nessa etapa.

```bash
CALANGO_DEMO_DIR=$(mktemp -d)
"$JAVA_HOME/bin/java" -jar target/viacao-calango-api-0.1.0.jar \
  --server.port=8080 \
  --spring.datasource.url="jdbc:h2:file:$CALANGO_DEMO_DIR/calango" \
  --spring.datasource.username=sa \
  --spring.datasource.password=
```

Abra [o Swagger](http://127.0.0.1:8080/docs). Use as rotas abaixo com o prefixo `/api/v1` e copie os IDs retornados. Se a porta estiver ocupada, escolha outra em `--server.port` e abra o Swagger nessa porta.

1. `POST /onibus` com `{"tipo":"LEITO","capacidade":23}`. Guarde o `id` do ônibus.
2. `POST /motoristas` duas vezes com `{"nome":"Maria"}` e `{"nome":"João"}`.
3. `POST /rotas` com o corpo abaixo. Guarde o `id` da rota, `paradas[0].id` (Rio) e `paradas[1].id` (Juiz de Fora).

```json
{
  "nome": "Rio–BH",
  "paradas": [
    {"nome":"Rio","ordem":1,"km_desde_origem":0,"minutos_desde_origem":0},
    {"nome":"Juiz de Fora","ordem":2,"km_desde_origem":250,"minutos_desde_origem":240},
    {"nome":"BH","ordem":3,"km_desde_origem":510,"minutos_desde_origem":420}
  ]
}
```

4. `POST /viagens` com `{"rota_id":"UUID_DA_ROTA","data_hora_partida":"2026-12-10T08:00:00-03:00"}`. Use uma data futura com mais de sete dias de antecedência para obter o preço do exemplo. Guarde o `id` da viagem.
5. `POST /viagens/{id}/escala` com `{"onibus_id":"UUID_DO_ONIBUS","paradas_troca_autorizada":["UUID_DE_JUIZ_DE_FORA"]}`. O resultado esperado é 201, com dois trechos e uma troca em Juiz de Fora.
6. `GET /viagens/{id}/preco` com `origem=Rio` e `destino=Juiz de Fora`. Deixe `data_compra` em branco para usar o horário atual. O esperado é 200, 250 km e `preco_centavos: 11875` com o desconto de antecedência.
7. `POST /passagens` com o corpo abaixo. O esperado é 201, `RESERVADA` e o mesmo preço da cotação. Guarde o `id` da passagem.

```json
{
  "viagem_id": "UUID_DA_VIAGEM",
  "parada_origem_id": "UUID_DO_RIO",
  "parada_destino_id": "UUID_DE_JUIZ_DE_FORA",
  "passageiro_nome": "Ana",
  "canal": "INTERNET"
}
```

8. `POST /pagamentos` com `{"passagem_id":"UUID_DA_PASSAGEM","forma":"CARTAO","token_cartao":"aprovado-teste"}`. O esperado é 201 com pagamento `APROVADO`, passagem `CONFIRMADA` e `valor_centavos: 11875`. O gateway é simulado.
9. `GET /viagens/{id}/disponibilidade` mostra `1` lugar ocupado no primeiro segmento e `0` no segundo. `GET /viagens/{id}` e `GET /viagens/{id}/escala` permitem consultar a viagem e a escala pelo mesmo UUID.

Caso queira demonstrar a persistência, encerre a API com `Ctrl+C` e execute novamente o comando do JAR usando o mesmo `CALANGO_DEMO_DIR`. Repita apenas as consultas do passo 9 com os UUIDs já guardados.

## Roteiro para demonstrar a Frota no Swagger

1. `POST /api/v1/onibus` — crie um ônibus (`tipo`, `capacidade` 23/28/32, `quilometragem_total` opcional).
2. `PATCH /api/v1/onibus/{id}/quilometragem` — informe `10000`: o ônibus passa a `precisa_revisao: true`.
3. `GET /api/v1/onibus?precisa_revisao=true` — lista os ônibus a encaminhar para revisão.
4. `POST /api/v1/onibus/{id}/revisoes` — conclui a revisão; `km_desde_revisao` volta a 0.
5. `GET /api/v1/onibus/alocacao-ideal?passageiros=60` — combinação com o mínimo de lugares vazios.

## Roteiro para demonstrar Motoristas no Swagger

1. `POST /api/v1/motoristas` — envie `{"nome":"Maria da Silva"}`; retorna 201 com `id` (UUID gerado) e `nome`.
2. `GET /api/v1/motoristas` — lista os cadastros; retorna `[]` se não houver motoristas.
3. `GET /api/v1/motoristas/{id}` — consulte usando o UUID retornado pelo POST.

Nome ausente ou em branco e UUID malformado retornam 422. Um UUID válido sem cadastro retorna 404.

## Qualidade

```bash
./mvnw verify          # compila e confere a formatação (Spotless)
./mvnw spotless:apply  # corrige a formatação
```

## Documentação

- [docs/ARQUITETURA.md](docs/ARQUITETURA.md) — camadas, DDD e Value Objects
- [docs/CONTRATOS_API.md](docs/CONTRATOS_API.md) — rotas e formato de erros
