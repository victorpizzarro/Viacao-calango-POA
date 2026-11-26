# Contratos da API

**Estado:** Frota, Passagens, cadastro, histórico e escala de Motoristas implementados; recorte de rotas e viagens integrado para fornecer o percurso. A documentação navegável está no Swagger (`/docs`) e o documento OpenAPI em `/openapi.json`. Mudanças de contrato exigem atualização deste arquivo.

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
| GET | `/api/v1/viagens/{id}/disponibilidade` | Passagens | 200 | Consultar ocupação por segmento |
| POST | `/api/v1/passagens` | Passagens | 201 | Reservar passagem |
| POST | `/api/v1/pagamentos` | Passagens | 201 | Registrar pagamento |
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
| GET | `/api/v1/viagens/{id}/preco` | Viagens | 200 | Consultar preço do trecho |
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

Foi reutilizado somente o recorte necessário de `origin/feature/viagens` (`ca28350`, autor Thiago Farias): entidades `Viagem`, `Rota` e `Parada`, identificadores, km/minutos acumulados, repositórios, serviços de cadastro e DTOs. Na conciliação com a branch oficial, foram incorporados os beans de precificação e cálculo de trecho e a rota de consulta de preço, preservando a consulta de viagem por UUID e o Swagger. Os repositórios usam métodos sincronizados para proteger leitura, escrita e cópia das listagens; Viagens mantém LinkedHashMap e sua ordem de inserção, e Rotas mantém HashMap sem promessa de ordem.

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

A migração **`V4__escalas_motoristas.sql`** cria `motorista_periodo_dirigido`, `motorista_escala` e `motorista_escala_trecho`, com FKs para cadastro/Frota, índices e uma escala por UUID de viagem. Todas as branches publicadas foram inspecionadas após `fetch`: V1 é Frota, V2 é cadastro, V3 está em `origin/feat/passagens` (`e347583`); V4 estava livre. A integração de Passagens incorpora agora a V3 original, mantendo V1, V2 e V4 sem alteração. `viagem_id` é referência lógica ao UUID de Viagens, pois a branch de origem não publica tabela de viagens.

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

### Conciliação de Viagens

`GET /api/v1/viagens/{id}/preco?origem=Rio&destino=Juiz%20de%20Fora&data_compra=2026-11-25T08:00:00-03:00` usa os nomes das paradas da rota oficial e a data de compra opcional (padrão: agora). Mantém o cálculo oficial: tarifa de 0,50 por km, desconto de 10% no percurso completo e mais 5% com antecedência mínima de sete dias. Retorna `distancia_km`, `preco_centavos`, `preco_reais`, `percurso_completo` e `desconto_antecedencia`, conforme `PrecoResponse`.

Permanecem pendentes a persistência definitiva de Viagens e a consulta no backend da autorização de troca por parada. A conciliação dos arquivos não resolve essas pendências.

### Passagens

**`GET /viagens/{id}/disponibilidade`** retorna `viagem_id`, `capacidade` e `trechos`. Cada trecho liga duas paradas consecutivas e traz `parada_origem_id`, `parada_origem_nome`, `parada_destino_id`, `parada_destino_nome`, `lugares_ocupados` e `lugares_disponiveis`. A capacidade é controlada por trecho: quem desce numa parada libera o lugar para quem embarca nela. Reservas vencidas não ocupam lugar.

**`POST /passagens`** recebe `viagem_id`, `parada_origem_id`, `parada_destino_id`, `passageiro_nome`, `canal` (`INTERNET` ou `GUICHE`) e `local_venda`. A passagem nasce `RESERVADA`, com o preço fixado na compra e a reserva válida por 15 minutos (`reservada_ate`). Retorna `id`, `viagem_id`, `parada_origem_id`, `parada_destino_id`, `passageiro_nome`, `canal`, `local_venda`, `preco_centavos`, `status` e `reservada_ate`.

- `local_venda` é obrigatório no `GUICHE` e deve ser um local de venda da rota; na `INTERNET` não é aceito (422).
- A origem deve vir antes do destino no percurso e ambas devem pertencer à viagem (422).
- Viagem que já partiu (409) e trecho sem lugar disponível (409) violam regras de negócio.
- A compra bloqueia a viagem até gravar a passagem, de modo que vendas simultâneas não ultrapassam a capacidade.

**`POST /pagamentos`** recebe `passagem_id`, `forma` (`CARTAO` ou `DINHEIRO`) e `token_cartao`. A internet aceita somente cartão e o guichê somente dinheiro (409). O `token_cartao` é obrigatório no cartão; nenhum dado de cartão é armazenado. Retorna `id`, `passagem_id`, `forma`, `valor_centavos`, `status` (`APROVADO` ou `RECUSADO`), `status_passagem` e `registrado_em`.

- Pagamento recusado também retorna 201: a tentativa é registrada e a reserva continua válida. Com token iniciado por `recusar` o gateway simulado nega o pagamento.
- Pagamento aprovado muda a passagem para `CONFIRMADA`.
- Passagem já paga (409) e reserva vencida (409) não aceitam novo pagamento.

### Venda integrada com Viagens, Frota e Escalas

Há um único adaptador Spring de `ViagensParaVenda`: `ViagensParaVendaAdapter`. A origem dos dados é:

| Campo | Fonte |
|---|---|
| UUID, partida, status | `ViagemService` oficial; somente viagem `AGENDADA` é vendável |
| UUIDs, nomes e ordem das paradas | `viagem.rota().getParadas()` oficial, ordenado pela rota |
| Ônibus selecionado | `motorista_escala`, consultado por `EscalaService` |
| Capacidade | `OnibusService.buscar(escala.onibusId()).capacidade().lugares()`; nenhum valor padrão |
| Preço final em centavos | `PrecoFinalViagemService`, extração do cálculo já usado na consulta de preço; delega distância e preço base a `TrechoRotaService` e `PrecoViagemService`, preservando tarifa, descontos e arredondamento oficiais |
| Momento da compra | `Clock` compartilhado; consulta de preço aceita `data_compra` para cotar nas mesmas condições |
| Locais de venda | Não há cadastro oficial em Viagens; lista vazia, sem inferir guichês a partir dos nomes de paradas |

Viagem inexistente retorna 404. Viagem existente sem escala retorna 409 `REGRA_DE_NEGOCIO` por capacidade indisponível, na compra e na disponibilidade. Venda por `INTERNET` funciona sem `local_venda`. Enquanto não houver fonte oficial de locais de venda, a compra por `GUICHE` retorna 409 `REGRA_DE_NEGOCIO` com mensagem de locais não cadastrados. Não foi criado cadastro paralelo.

A compra usa uma transação `READ_COMMITTED`. `obterComBloqueio` exige transação externa (`MANDATORY`) e executa `SELECT ... FOR UPDATE` sobre a linha da escala da viagem. O bloqueio é mantido pelo mesmo DataSource/transação JPA até confirmar ou desfazer a gravação da passagem, cobrindo a leitura da ocupação. Não depende do monitor dos mapas em memória. Uma compra que esperou consulta as reservas já confirmadas antes de verificar a última vaga. Reservas vencidas não consomem capacidade; o pagamento aprovado mantém a ocupação e confirma a passagem. Essa solução usa a escala persistida existente sem criar uma tabela de viagens.

Exemplo de fluxo no Swagger (substitua os UUIDs pelos retornados): cadastre a rota Rio–Juiz de Fora–BH do roteiro anterior, viagem futura, ônibus, dois motoristas e escala. Consulte `/api/v1/viagens/{id}/preco?origem=Rio&destino=Juiz%20de%20Fora` e reserve:

```json
{
  "viagem_id": "UUID_DA_VIAGEM",
  "parada_origem_id": "UUID_DO_RIO",
  "parada_destino_id": "UUID_DE_JUIZ_DE_FORA",
  "passageiro_nome": "Maria",
  "canal": "INTERNET"
}
```

Em `POST /api/v1/pagamentos`, use o `id` da passagem:

```json
{
  "passagem_id": "UUID_DA_PASSAGEM",
  "forma": "CARTAO",
  "token_cartao": "aprovado-teste"
}
```

O gateway é o simulado da branch de Passagens. Compare `valor_centavos` do pagamento, `preco_centavos` da passagem e a cotação do mesmo trecho na mesma condição de antecedência. Consulte `/api/v1/viagens/{id}/disponibilidade`: Rio–Juiz de Fora ocupa apenas o primeiro segmento; o embarque em Juiz de Fora pode usar o lugar no segmento seguinte.

`VendaIntegradaTest` usa HTTP, Flyway, JPA, planejador e Frota reais: ônibus selecionado de 23 lugares entre veículos de capacidades diferentes; preço parcial e completo com descontos, pagamento aprovado, ocupação por segmento, viagem inexistente (404), trecho invertido/estranho (422), ausência de escala/guichês (409), exigência de transação externa e duas transações bloqueadas disputando a última vaga (um 201, um 409, exatamente 23 reservas persistidas). Os 38 testes anteriores permanecem na execução.

Continuam pendentes a persistência definitiva de Viagens e a autorização oficial de troca por parada no backend. Depois de um reinício, as viagens em memória precisam de tratamento na etapa de persistência: esta entrega não recria nem inventa UUIDs para viagens anteriores.
