# Contratos da API

**Estado:** Frota, cadastro, histórico e escala de Motoristas implementados; recorte de rotas e viagens integrado para fornecer o percurso. A documentação navegável está no Swagger (`/docs`) e o documento OpenAPI em `/openapi.json`. Mudanças de contrato exigem atualização deste arquivo.

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
| POST | `/api/v1/motoristas/{id}/periodos-dirigidos` | Motoristas | 201 | Registrar direção efetiva |
| POST | `/api/v1/rotas` | Viagens | 201 | Criar rota com paradas |
| GET | `/api/v1/rotas` | Viagens | 200 | Listar rotas |
| GET | `/api/v1/rotas/{id}` | Viagens | 200 | Consultar rota e UUIDs das paradas |
| POST | `/api/v1/viagens` | Viagens | 201 | Criar viagem em uma rota |
| GET | `/api/v1/viagens` | Viagens | 200 | Listar viagens |
| GET | `/api/v1/viagens/{id}` | Viagens | 200 | Consultar viagem |
| POST | `/api/v1/viagens/{id}/escala` | Motoristas | 201 | Planejar e reservar escala completa |
| GET | `/api/v1/viagens/{id}/escala` | Motoristas | 200 | Consultar escala persistida |

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

### Histórico de direção

**`POST /api/v1/motoristas/{id}/periodos-dirigidos`** registra um período efetivamente dirigido no servidor. Recebe `inicio` e `fim` em ISO 8601 com offset; retorna 201 com esses campos. O intervalo deve ter fim posterior ao início (422); motorista inexistente retorna 404. A escrita bloqueia a linha do motorista, assim como o planejamento. Reservas futuras são obtidas das escalas, não deste endpoint.

```json
{
  "inicio": "2026-11-25T20:00:00-03:00",
  "fim": "2026-11-26T00:00:00-03:00"
}
```

### Origem do percurso de Viagens

Foi reutilizado somente o recorte necessário de `origin/feature/viagens` (`ca28350`, autor Thiago Farias): entidades `Viagem`, `Rota` e `Parada`, identificadores, km/minutos acumulados, repositórios, serviços de cadastro e DTOs. Configuração e controller de viagem foram limitados a cadastro/consulta; precificação não foi incorporada. Os mapas foram tornados concorrentes para atender chamadas HTTP simultâneas.

`Viagem` tem UUID, rota, `data_hora_partida` e status `AGENDADA`. Cada parada tem seu UUID, nome, ordem, `km_desde_origem` e `minutos_desde_origem`. Trechos são os pares consecutivos de paradas ordenadas, seguindo a representação de `TrechoRota`: distância por diferença de km acumulados e horários por partida da viagem mais minutos acumulados. O adaptador exige km crescentes, diferenças inteiras e horários crescentes, sem arredondar distâncias para o planejador.

A branch publicada ainda **armazena rotas e viagens em memória**, sem migração ou vínculo com ônibus e sem autorização de troca nas paradas. O ônibus cadastrado em Frota e as autorizações por UUID de parada são informados ao alocar; a escala persiste a associação e o percurso utilizado. Não foi criada outra tabela de viagens, rotas ou paradas. Após reiniciar a aplicação, as escalas continuam consultáveis por UUID no banco, mas os cadastros de Viagens precisam ser recriados para novos planejamentos.

### Escala

**`POST /api/v1/viagens/{id}/escala`** recebe apenas:

```json
{
  "onibus_id": "11111111-1111-4111-8111-111111111111",
  "paradas_troca_autorizada": ["22222222-2222-4222-8222-222222222222"]
}
```

Substitua os UUIDs pelo ônibus cadastrado e pela parada intermediária da rota. `paradas_troca_autorizada` omitido, nulo ou vazio significa nenhuma troca autorizada. UUIDs repetidos, de outra rota ou de paradas não intermediárias retornam 422. Não se recebem candidatos, períodos dirigidos, reservas nem trechos do cliente.

O servidor usa todos os motoristas cadastrados e lê `motorista_periodo_dirigido` e `motorista_escala_trecho` para cada candidato. Só usa `CandidatoCondutor.semHistorico` quando **ambas** as listas estão vazias. Com reservas e sem direção anterior, constrói `new CandidatoCondutor(id, List.of(), alocacoesExistentes)`.

`EscalaService` monta `SolicitacaoEscala` e chama o método real `PlanejadorEscala.planejar`. As regras de 6h/400 km, descanso mínimo de 12h e trocas permanecem no domínio do Pablo, sem duplicação na aplicação. O retorno 201 contém `viagem_id`, `onibus_id`, `trechos` e `trocas` do resultado. Cada trecho inclui `ordem`, `origem`, `destino`, `partida`, `chegada`, `distancia_km`, `motorista_id`; cada troca inclui `parada`, `horario`, `motorista_anterior_id`, `novo_motorista_id`. Horários podem ser normalizados para UTC; `15:00Z` equivale a `12:00-03:00`.

**`GET /api/v1/viagens/{id}/escala`** retorna 200 com a escala persistida. As trocas são reconstruídas dos trechos pelo próprio `EscalaViagem.aPartirDeTrechos`. Não depende do cadastro em memória para recuperar uma escala existente.

- 404: viagem ou ônibus inexistentes no POST; escala inexistente no GET.
- 409 `REGRA_DE_NEGOCIO`: nenhuma escala completa viável ou viagem já escalada. Não há gravação parcial.
- 409 `CONFLITO`: conflito de integridade ou bloqueio, traduzido pelo tratamento compartilhado.
- 422 `DADOS_INVALIDOS`: corpo, UUID, paradas ou percurso inválidos.

#### Transação e concorrência

Uma transação `READ_COMMITTED` cobre bloqueios, leitura do histórico/reservas, planejamento e todos os inserts. Todas as linhas dos candidatos são bloqueadas por `SELECT ... FOR UPDATE` em ordem de UUID, inclusive motoristas sem histórico. **Depois dos bloqueios**, o servidor lê novamente as alocações e os períodos; uma solicitação que esperou enxerga as reservas confirmadas pela anterior. A gravação do histórico também participa desse protocolo. O bloqueio no banco funciona entre instâncias da aplicação; não depende de um mutex Java. Bloquear todos os candidatos prioriza consistência e serializa os planejamentos neste recorte local.

A migração **`V4__escalas_motoristas.sql`** cria `motorista_periodo_dirigido`, `motorista_escala` e `motorista_escala_trecho`, com FKs para cadastro/Frota, índices e uma escala por UUID de viagem. Todas as branches publicadas foram inspecionadas após `fetch`: V1 é Frota, V2 é cadastro, V3 está em `origin/feat/passagens` (`e347583`); V4 estava livre. Passagens não foi incorporada. `viagem_id` é referência lógica ao UUID de Viagens, pois a branch de origem não publica tabela de viagens.

#### Roteiro para o Swagger (`/docs`)

1. Cadastre um ônibus em `POST /api/v1/onibus` com `{"tipo":"EXECUTIVO","capacidade":32}` e dois motoristas em `POST /api/v1/motoristas`.
2. Registre o histórico do segundo motorista pelo endpoint acima, terminando à meia-noite de 26/11/2026.
3. Crie a rota em `POST /api/v1/rotas`:

```json
{
  "nome": "Rio-BH",
  "paradas": [
    {"nome":"Rio","km_desde_origem":0,"minutos_desde_origem":0,"ordem":1},
    {"nome":"Juiz de Fora","km_desde_origem":250,"minutos_desde_origem":240,"ordem":2},
    {"nome":"BH","km_desde_origem":510,"minutos_desde_origem":420,"ordem":3}
  ]
}
```

4. Use o `id` da rota e guarde o UUID de Juiz de Fora retornado em `paradas[1].id`. Crie a viagem em `POST /api/v1/viagens`:

```json
{
  "rota_id": "UUID_DA_ROTA",
  "data_hora_partida": "2026-11-26T08:00:00-03:00"
}
```

5. Em `POST /api/v1/viagens/{id}/escala`, use o UUID da viagem na URL e o corpo da seção Escala, substituindo `onibus_id` e o UUID da parada de troca pelos retornados. Resultado: Rio–Juiz de Fora 08h–12h/250 km com o primeiro motorista, Juiz de Fora–BH 12h–15h/260 km com o substituto; troca em Juiz de Fora ao meio-dia.

#### Validação automatizada

`EscalasAplicacaoTest` sobe o servidor HTTP real em porta aleatória e H2 com Flyway. Sem mocks de planejador ou persistência, verifica: descanso exato de 12h e troca em Juiz de Fora; 11h59 com HTTP 409 e zero registros; reserva sobreposta sem períodos dirigidos; duas chamadas aguardando o bloqueio real no banco, com apenas um 201 e um 409; rollback após falha no segundo insert; validações/erros compartilhados; OpenAPI e regressão dos endpoints de Frota (cadastro, consulta, quilometragem, revisão e alocação ideal). A base não tinha testes de Frota; essa cobertura foi acrescentada. Os testes existentes de cadastro e `PlanejadorEscalaTest` continuam executados pelo `./mvnw -B -ntp verify` com Java 21.
