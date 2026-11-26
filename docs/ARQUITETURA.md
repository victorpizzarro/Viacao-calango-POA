# Arquitetura da Viação Calango API

## Contexto

Este trabalho foi desenvolvido na disciplina de **4POA** como entrega da **AV2**. O ponto de partida é o minimundo da Viação Calango (`Minimundo_1IHM_Passageiros.pdf`), que descreve uma empresa de transporte interestadual com frota própria, rotas com paradas, venda de passagens por trecho, escala de motoristas e controle de revisão dos ônibus.

O sistema foi implementado como uma API REST em Java com Spring Boot. O desenvolvimento segue a divisão do minimundo em módulos e, nesta etapa, está implementado o módulo de **Frota**. Os demais módulos seguirão a mesma estrutura.

## Estilo arquitetural

A aplicação é um monólito modular: um único artefato implantável, dividido internamente em módulos independentes que representam contextos delimitados do domínio. Cada módulo é organizado em camadas, e a modelagem do domínio segue o Domain-Driven Design (DDD), com uso intensivo de Value Objects.

A escolha por um monólito modular, e não por microsserviços, atende ao tamanho do problema. Os módulos já ficam separados por fronteiras claras, o que facilita uma eventual extração futura, mas o sistema continua simples de executar e de avaliar.

## Módulos

| Módulo | Situação | Responsabilidade no minimundo |
|---|---|---|
| Frota | Implementado | Ônibus de 23, 28 ou 32 lugares (leito, semi-leito e executivo), controle de quilometragem, revisão a cada 10.000 km e alocação ideal de ônibus |
| Viagens | Cadastro/consulta em memória integrados | Rotas, paradas, viagens, preço por tipo de ônibus e promoções |
| Passagens | Reservas, ocupação e pagamentos integrados | Venda por trecho, ocupação e pagamento por cartão ou em dinheiro |
| Motoristas | Cadastro, histórico e escala implementados | Limites de 6 horas e 400 km, descanso de 12 horas e trocas em paradas autorizadas |
| Shared | Implementado | Exceções de domínio, tratamento de erros e configuração do OpenAPI |

## Estrutura de pacotes

O pacote raiz é `br.com.viacaocalango.api`. O módulo de Frota está organizado assim:

```
shared/
  domain/            DadoInvalido, RegraDeNegocioViolada, RecursoNaoEncontrado
  infrastructure/    AppProperties, OpenApiConfig
  presentation/      TratadorDeErros, ErroResponse, RespostasApi, HealthController
modules/frota/
  domain/
    model/           Onibus (raiz do agregado), TipoOnibus
    valueobject/     OnibusId, Capacidade, Quilometragem, Odometro, Passageiros, AlocacaoIdeal
    service/         CalculadoraAlocacaoIdeal
    repository/      OnibusRepository
  application/       OnibusService, AlocacaoIdealService, CriarOnibusComando
  infrastructure/    OnibusEntity, OnibusJpaRepository, OnibusRepositoryJpa, FrotaConfig
  presentation/      FrotaController e os DTOs de requisição e resposta
```

## Camadas

- **domain**: contém o modelo do negócio (agregado, Value Objects, serviço de domínio e a interface do repositório). É escrito em Java puro, sem dependência de Spring, JPA ou bibliotecas de serialização.
- **application**: contém os casos de uso, que coordenam o domínio e definem os limites de transação.
- **infrastructure**: contém as implementações técnicas, como o adaptador do repositório com Spring Data JPA e a configuração dos beans.
- **presentation**: contém os controllers REST, os DTOs e a documentação Swagger.

A dependência entre camadas segue a direção presentation, application e domain. A infraestrutura depende do domínio, e não o contrário, pois implementa a interface de repositório declarada nele.

## Value Objects

Os conceitos do negócio que possuem regra própria foram modelados como Value Objects, implementados como `record` imutáveis. Cada um valida seus dados na construção, de modo que não existe instância inválida em memória, e a comparação é feita por valor.

| Value Object | Regra que representa |
|---|---|
| `OnibusId` | Identidade do ônibus, que não pode ser nula |
| `Capacidade` | Somente 23, 28 ou 32 lugares, conforme o minimundo |
| `Quilometragem` | Valor inteiro não negativo, com subtração que só aceita uma leitura anterior menor ou igual |
| `Odometro` | Total percorrido e quilometragem da última revisão; não retrocede, e indica revisão quando passam 10.000 km sem revisão |
| `Passageiros` | Quantidade entre 1 e 100.000 |
| `AlocacaoIdeal` | Resultado do cálculo, com as capacidades escolhidas, o total de ônibus e os lugares vazios |

As regras de negócio ficam concentradas nesses objetos. A validação dos DTOs na camada de apresentação verifica apenas presença e tipo dos campos, e a regra em si, como a capacidade permitida, vem do domínio.

## Agregado Onibus

`Onibus` é a raiz do agregado de Frota. Tipo e capacidade são definidos no cadastro e não mudam depois. A quilometragem evolui apenas pelos comportamentos `registrarQuilometragem` e `concluirRevisao`, ambos delegados ao `Odometro`. A igualdade entre ônibus é definida pela identidade.

O odômetro total nunca é reiniciado: a revisão apenas atualiza a referência de quilometragem da última revisão, e o ônibus precisa de nova revisão quando a diferença chega a 10.000 km.

## Alocação ideal

A combinação de ônibus para uma quantidade de passageiros é calculada pelo serviço de domínio `CalculadoraAlocacaoIdeal`. O critério é minimizar os lugares vazios e, em caso de empate, o número de ônibus. O cálculo usa programação dinâmica sobre as capacidades permitidas, de forma semelhante ao problema clássico do troco.

## Escala de motoristas

A escolha do condutor de cada trecho da viagem e a definição das paradas de troca são realizadas pelo serviço de domínio `PlanejadorEscala`, implementado em Java puro em `modules/motoristas/domain`.

- **Limites contínuos e descanso:** cada condutor pode dirigir no máximo **6 horas** e **400 km** contínuos entre trocas; paradas comuns não zeram os acumuladores. Para assumir um novo bloco de direção, o condutor deve ter pelo menos **12 horas** de descanso desde o fim da última direção e não pode possuir alocações conflitantes.
- **Primeira jornada sem histórico:** `CandidatoCondutor.semHistorico` só representa condutores sem períodos dirigidos **e sem alocações existentes**. Sem direção anterior, mas com reserva, o adaptador preserva a lista de alocações no construtor de `CandidatoCondutor`.
- **Antecipação na última parada viável:** o algoritmo avalia o percurso à frente e antecipa a substituição para a última parada com troca autorizada sempre que seguir até uma parada comum seguinte ultrapassaria 6 horas ou 400 km. Caso não exista escala completa viável, lança `RegraDeNegocioViolada` sem retornar escala parcial.

A integração usa `EscalaService` para coordenar `ViagensParaEscala`, o cadastro da Frota, `EscalaRepository` e o bean do `PlanejadorEscala` original. O adaptador de Viagens usa o recorte de `origin/feature/viagens` descrito em `CONTRATOS_API.md`; não há segundo modelo persistido de viagem.

O repositório de escala usa JDBC sobre o mesmo DataSource gerenciado pela transação Spring/JPA. `READ_COMMITTED`, bloqueio pessimista das linhas de todos os candidatos em ordem de UUID e leitura de reservas após adquirir os bloqueios impedem alocações incompatíveis simultâneas, inclusive quando o histórico de direção está vazio. Cabeçalho e todos os trechos são gravados juntos; trocas são derivadas pelo resultado do domínio. A V4 reserva o esquema de escala sem colidir com a V3 de Passagens. O histórico efetivamente dirigido tem armazenamento próprio; reservas vêm dos trechos das escalas.

## Persistência

O banco de dados em desenvolvimento é o H2, gravado em arquivo, com opção de PostgreSQL por meio das variáveis de ambiente. O esquema é criado por migrações Flyway, e o Hibernate apenas valida se as entidades correspondem às tabelas.

A entidade JPA (`OnibusEntity`) pertence à infraestrutura e é distinta do agregado do domínio. O adaptador `OnibusRepositoryJpa` faz a conversão entre os dois. As alterações de um ônibus usam bloqueio pessimista, o que evita atualizações perdidas em acessos simultâneos.

## Erros e documentação da API

Todas as respostas de erro seguem o mesmo formato, com `codigo`, `mensagem` e `detalhes`. As exceções de domínio são traduzidas para os status HTTP 404 (recurso não encontrado), 409 (regra de negócio violada) e 422 (dado inválido). O JSON da API usa nomes de campo em `snake_case`.

A documentação das rotas é gerada com springdoc e fica disponível no Swagger, em `/docs`. Os detalhes de cada rota estão em `CONTRATOS_API.md`, e as decisões e premissas assumidas estão em `DECISOES.md`.

## Venda integrada

`ViagensParaVendaAdapter` liga a venda à viagem e rota oficiais e ao ônibus selecionado pela escala, cuja capacidade vem de Frota. `PrecoFinalViagemService` centraliza a composição do preço oficial para a consulta REST e a compra. A V3 original de Passagens convive com V1, V2 e V4.

A transação de compra usa `READ_COMMITTED` e bloqueia a linha persistida da escala até o commit, antes de consultar a ocupação por segmento e gravar a reserva. O adaptador exige uma transação externa; um monitor Java não substitui esse bloqueio. Sem escala não há capacidade oficial para venda (409); sem cadastro de locais em Viagens, guichê permanece indisponível (409), com venda pela internet habilitada. Viagens permanece em memória; sua persistência definitiva e a autorização oficial de trocas continuam pendentes.
