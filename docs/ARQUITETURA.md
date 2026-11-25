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
| Viagens | Planejado | Rotas, paradas, viagens, preço por tipo de ônibus e promoções |
| Passagens | Planejado | Venda por trecho, ocupação e pagamento por cartão ou em dinheiro |
| Motoristas | Planejado | Limites de 6 horas e 400 km, descanso de 12 horas e trocas em paradas autorizadas |
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

## Persistência

O banco de dados em desenvolvimento é o H2, gravado em arquivo, com opção de PostgreSQL por meio das variáveis de ambiente. O esquema é criado por migrações Flyway, e o Hibernate apenas valida se as entidades correspondem às tabelas.

A entidade JPA (`OnibusEntity`) pertence à infraestrutura e é distinta do agregado do domínio. O adaptador `OnibusRepositoryJpa` faz a conversão entre os dois. As alterações de um ônibus usam bloqueio pessimista, o que evita atualizações perdidas em acessos simultâneos.

## Erros e documentação da API

Todas as respostas de erro seguem o mesmo formato, com `codigo`, `mensagem` e `detalhes`. As exceções de domínio são traduzidas para os status HTTP 404 (recurso não encontrado), 409 (regra de negócio violada) e 422 (dado inválido). O JSON da API usa nomes de campo em `snake_case`.

A documentação das rotas é gerada com springdoc e fica disponível no Swagger, em `/docs`. Os detalhes de cada rota estão em `CONTRATOS_API.md`, e as decisões e premissas assumidas estão em `DECISOES.md`.
