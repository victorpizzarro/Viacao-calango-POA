# Contratos da API

**Estado:** módulo Frota e cadastro de Motoristas implementados. A documentação navegável está no Swagger (`/docs`) e o documento OpenAPI em `/openapi.json`. Mudanças de contrato exigem atualização deste arquivo.

## Padrões

- Prefixo `/api/v1` nos endpoints de negócio; `/health` fica fora dele.
- JSON com campos em `snake_case`. Enums em maiúsculas (`EXECUTIVO`). Parâmetros de URL também em `snake_case` (`precisa_revisao`).
- Identificadores são UUID.
- Valores monetários em centavos inteiros (`*_centavos`); datas em ISO 8601 com offset (para os próximos módulos).

## Erros

Todo erro tem o mesmo formato:

```json
{
  "codigo": "DADOS_INVALIDOS",
  "mensagem": "Dados de entrada invalidos",
  "detalhes": [{ "campo": "tipo", "mensagem": "não deve ser nulo" }]
}
```

| Status | `codigo` | Quando |
|---|---|---|
| 404 | `NAO_ENCONTRADO` | Recurso inexistente |
| 409 | `REGRA_DE_NEGOCIO` | A operação viola uma regra no estado atual (ex.: odômetro retrocedendo) |
| 409 | `CONFLITO` | Conflito de concorrência ou integridade; pode ser repetida |
| 422 | `DADOS_INVALIDOS` | Dado que nunca seria aceito: campo ausente, tipo errado, capacidade fora de 23/28/32, UUID malformado |
| 4xx | `REQUISICAO_INVALIDA` | Rota inexistente, método não permitido e afins |
| 500 | `ERRO_INTERNO` | Erro inesperado, sem detalhes internos |

`detalhes[].campo` usa o mesmo `snake_case` do JSON enviado.

## Rotas

| Método | Caminho | Módulo | Sucesso | Propósito |
|---|---|---|---|---|
| GET | `/health` | sistema | 200 | Status da API |
| POST | `/api/v1/onibus` | Frota | 201 | Criar ônibus |
| GET | `/api/v1/onibus` | Frota | 200 | Listar a frota (filtro `precisa_revisao`) |
| GET | `/api/v1/onibus/{id}` | Frota | 200 | Consultar ônibus |
| PATCH | `/api/v1/onibus/{id}/quilometragem` | Frota | 200 | Registrar quilometragem |
| POST | `/api/v1/onibus/{id}/revisoes` | Frota | 200 | Concluir revisão |
| GET | `/api/v1/onibus/alocacao-ideal` | Frota | 200 | Calcular a combinação ideal de ônibus |
| POST | `/api/v1/motoristas` | Motoristas | 201 | Cadastrar motorista |
| GET | `/api/v1/motoristas` | Motoristas | 200 | Listar motoristas |
| GET | `/api/v1/motoristas/{id}` | Motoristas | 200 | Consultar motorista |

### Frota

**`POST /onibus`** recebe `tipo` (`LEITO`, `SEMI_LEITO`, `EXECUTIVO`), `capacidade` (23, 28 ou 32) e, opcionalmente, `quilometragem_total` (padrão 0).

**`GET /onibus`** lista a frota. Com `precisa_revisao=true` retorna os ônibus que completaram 10.000 km sem revisão; com `false`, os demais.

**`PATCH /onibus/{id}/quilometragem`** recebe `quilometragem_atual`. Retroceder o odômetro gera 409.

**`POST /onibus/{id}/revisoes`** não tem corpo. Zera os km desde a revisão e preserva o odômetro total. Retorna 200 porque a revisão não gera recurso próprio.

Essas rotas retornam `id`, `tipo`, `capacidade`, `quilometragem_total`, `quilometragem_ultima_revisao`, `km_desde_revisao`, `precisa_revisao` e `disponivel_para_operacao`.

**`GET /onibus/alocacao-ideal?passageiros=60`** retorna `passageiros`, `capacidades`, `total_de_onibus` e `lugares_vazios`. Minimiza os lugares vazios e, no empate, o número de ônibus. É apenas um cálculo: não altera viagens nem considera os ônibus existentes (ADR-011).

### Motoristas

**`POST /api/v1/motoristas`** recebe somente o nome obrigatório. Nome ausente, nulo, vazio ou composto apenas por espaços em branco retorna 422 (`DADOS_INVALIDOS`). O UUID é gerado pelo sistema. Nomes repetidos são permitidos.

```json
{ "nome": "Maria da Silva" }
```

Retorna 201 com o motorista persistido:

```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "nome": "Maria da Silva"
}
```

**`GET /api/v1/motoristas`** retorna 200 com um array de objetos com `id` e `nome`, sem ordem garantida. Sem cadastros, retorna `[]`.

**`GET /api/v1/motoristas/{id}`** retorna 200 com `id` e `nome`. UUID malformado retorna 422 (`DADOS_INVALIDOS`); UUID válido sem cadastro retorna 404 (`NAO_ENCONTRADO`). Os erros seguem o formato compartilhado acima.

Esta entrega contempla apenas o cadastro. O cálculo e a persistência da escala serão integrados em uma entrega futura.
