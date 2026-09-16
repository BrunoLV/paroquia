---
name: tdd
description: "Engenharia de testes unitários e TDD em Java 25: ciclo Red-Green-Refactor estrito, JUnit 5, Mockito 5 STRICT_STUBS, AssertJ, isolamento total de I/O e testes como especificação viva."
---

# Skill do Agente de IA: Engenharia de Testes Unitários e TDD em Java 25 (Rigor de Especificação)

## 1. Visão Geral e Contexto

Esta skill capacita o Agente de IA a projetar, refatorar e executar testes unitários de nível sênior em **Java 25**, aplicando os preceitos clássicos de **Desenvolvimento Orientado a Testes (TDD)** com foco em **endurecimento de especificação (*specification hardening*)**.

O agente atua como um Engenheiro Especialista em Qualidade de Software, garantindo isolamento arquitetural rigoroso, alta velocidade de execução (milissegundos), asserções semânticas fluentes e eliminação de lacunas de cobertura. O objetivo central é transformar o conjunto de testes em uma **especificação viva e executável do negócio**, onde nenhuma linha de código de produção existe sem um teste que a tenha exigido.

---

## 2. O Teste como Documentação Viva (*Living Documentation*)

Os testes unitários constituem a documentação técnica primária e incontestável do comportamento esperado do sistema.

* **Javadoc de Cenário e Regra Obrigatório:** Cada método de teste ou classe de especificação DEVE conter metadados e documentação contextualizando o critério de aceitação coberto:
```java
/**
 * Valida a política de retenção fiscal durante liquidações antecipadas.
 *
 * REGRA DE NEGÓCIO (RN-COB-08):
 * Antecipações solicitadas em finais de semana ou feriados bancários
 * devem aplicar taxa administrativa adicional de 1,5% sobre o valor bruto.
 *
 * DECISÃO DE TESTE:
 * Isola o relógio do sistema via Instant fixo para garantir determinismo temporal.
 */
@Test
@DisplayName("Cenário RN-COB-08: Deve aplicar sobretaxa de 1,5% para antecipações em dias não úteis")
void deveAplicarSobretaxaParaAntecipacaoEmDiasNaoUteis() { ... }

```


* **Nomenclatura Semântica e `@DisplayName`:** Proibidos nomes genéricos (`teste1`, `deveFuncionar`). Os nomes dos métodos e seus `@DisplayName` devem expressar a condição inicial, a ação executada e o resultado observável (**Given-When-Then**).

---

## 3. Fluxo de Execução TDD e Regra de Ouro do Endurecimento

O agente opera estritamente sob o ciclo **Red-Green-Refactor**. É **terminantemente proibido** propor código de produção sem que um teste falho correspondente tenha sido concebido previamente.

```
   ┌────────────────────────────────────────────────────────┐
   │                                                        │
   ▼                                                        │
[ RED ] ─────────────────► [ GREEN ] ─────────────────► [ REFACTOR ]
Escreva um teste que      Escreva o código mínimo       Melhore o design mantendo
falha para uma regra.     de produção para passar.      100% dos testes verdes.

```

### 3.1. Restrições Rígidas de Endurecimento (*Hardening Constraints*)

1. **Zero Código Não Motivado:** O agente não pode adicionar blocos `if/else`, filtros, cláusulas `when`, validações de nulo ou tratamento de exceções no código de produção sem antes escrever o cenário de teste que falhe na ausência desse bloco.
2. **Matriz de Rastreabilidade Total:** Todo critério de aceitação extraído da especificação deve mapear para:

$$\text{Especificação (Critério)} \longrightarrow \text{Teste Unitário (RED)} \longrightarrow \text{Código Mínimo (GREEN)}$$


3. **Proibição de Atalhos Técnicos:** Não adicione código defensivo especulativo. Se o requisito não exige tratamento para um cenário, não o implemente até que o cenário seja formalmente especificado em um novo teste.

---

## 4. Tecnologias, Ferramentas e Idioma Java 25

Os testes devem explorar a maturidade do ecossistema moderno em Java 25:

* **Framework de Testes:** **JUnit 5 (Jupiter)** exclusivo. O uso de anotações do JUnit 4 (`org.junit.Test`) é tratado como violação grave.
* **Asserções Fluentes:** **AssertJ** (`org.assertj.core.api.Assertions.assertThat`) como padrão mandatório em vez dos asserts básicos do JUnit, garantindo legibilidade e mensagens de erro descritivas.
* **Framework de Dublês:** **Mockito 5+** com inicialização via `@ExtendWith(MockitoExtension.class)`. Mocks devem ser estritos (`Strictness.STRICT_STUBS`) para acusar stubs não utilizados (*unnecessary stubs*).
* **Recursos do Java 25:**
* Use **Records** para estruturas de massa de dados imutáveis de teste (*Test Data Builders*).
* Use **Pattern Matching** e desestruturação de Records para validar objetos complexos de resposta.
* Use **Unnamed Variables (`_`)** em lambdas de assertivas e capturas de exceções onde o objeto não precisa ser inspecionado.
* Use **Text Blocks (`"""`)** para representações estruturadas de payloads esperados (JSON, XML, CSV).



---

## 5. Limites do Teste Unitário Puro (Fronteiras Arquiteturais)

Para manter a execução em milissegundos e assegurar determinismo, o agente deve respeitar os seguintes limites:

1. **Sem Contexto Spring / Framework:** Proibido o uso de `@SpringBootTest`, `@DataJpaTest`, `@WebMvcTest` ou qualquer mecanismo que inicialize *ApplicationContext*.
2. **Sem I/O Físico ou Conexões Reais:** Proibidas conexões com bancos de dados reais, chamadas de rede HTTP, acesso ao sistema de arquivos local ou contêineres de teste (ex.: Testcontainers são restritos à camada de integração, nunca unitária).
3. **Isolamento de Fronteiras com Mocks:** Todas as portas de saída (repositórios, gateways de API, brokers de mensageria) devem ser simuladas via Mockito.
4. **Alvo Concreto Único:** Nunca use `@Mock` ou `@Spy` na classe sob teste (*Subject Under Test - SUT*). Ela deve ser instanciada concretamente via construtor ou injetada com `@InjectMocks`.

---

## 6. Padrões Estruturais e Blueprints

### 6.1. Padrão Arrange-Act-Assert (AAA) com AssertJ

Todo teste deve delimitar claramente suas fases:

* **Arrange (Organizar):** Instanciação da SUT, preparação de dados e configuração de comportamento dos dublês.
* **Act (Agir):** Execução unívoca da operação sob teste.
* **Assert (Asserir):** Verificação do estado resultante, do valor retornado e da interação com contratos externos.

### 6.2. Blueprint Completo: Teste Unitário com Java 25, Mockito e AssertJ

```java
package com.empresa.faturamento.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

/**
 * Especificação executável para o Processador de Cobrança Paroquial.
 * Valida os limites de domínio e a conformidade transacional com gateways externos.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Domínio: Processador de Cobrança")
class ProcessadorCobrancaTest {

    @Mock
    private FielRepository fielRepository;

    @Mock
    private GatewayPagamento gatewayPagamento;

    @Captor
    private ArgumentCaptor<TransacaoFiscal> transacaoCaptor;

    private ProcessadorCobranca processadorCobranca;

    private static final UUID FIEL_ID = UUID.randomUUID();
    private static final BigDecimal VALOR_PADRAO = new BigDecimal("100.00");

    @BeforeEach
    void setUp() {
        // [DIP] Injeção explícita via construtor, sem reflection opaca
        processadorCobranca = new ProcessadorCobranca(
            fielRepository,
            gatewayPagamento,
            List.of(new DescontoDizimistaAtivoStrategy())
        );
    }

    @Nested
    @DisplayName("Cenários de Sucesso e Regras de Negócio Positivas")
    class CenariosDeSucesso {

        /**
         * REGRA (RN-DIZ-01):
         * Fiel adimplente com mais de 12 meses ininterruptos de contribuição
         * tem direito a 10% de abatimento em emolumentos e taxas de certidões.
         */
        @Test
        @DisplayName("Deve aplicar 10% de desconto e disparar liquidação no gateway quando fiel for dizimista ativo")
        void deveAplicarDescontoParaDizimistaAtivoELiquidar() {
            // 1. Arrange
            var fiel = new Fiel(FIEL_ID, "Carlos Silva", StatusContribuicao.ATIVO);
            var item = new ItemTaxa("Certidão de Batismo", VALOR_PADRAO);

            given(fielRepository.buscarPorId(FIEL_ID)).willReturn(Optional.of(fiel));
            given(gatewayPagamento.processar(any())).willReturn(new ComprovanteLiquidacao(UUID.randomUUID(), StatusLiquidacao.SUCESSO));

            // 2. Act
            ReciboLiquidacao recibo = processadorCobranca.liquidarTaxa(FIEL_ID, item);

            // 3. Assert (AssertJ Fluente)
            assertThat(recibo)
                .isNotNull()
                .satisfies(r -> {
                    assertThat(r.valorCobrado()).isEqualByComparingTo("90.00");
                    assertThat(r.fielId()).isEqualTo(FIEL_ID);
                });

            // Validação de Interação de Contrato via Mockito BDD
            then(gatewayPagamento).should().processar(transacaoCaptor.capture());
            TransacaoFiscal transacaoEnviada = transacaoCaptor.getValue();
            assertThat(transacaoEnviada.valorLiquido()).isEqualByComparingTo("90.00");
        }
    }

    @Nested
    @DisplayName("Cenários de Exceção, Validação de Contrato e Invariantes")
    class CenariosDeExcecao {

        @Test
        @DisplayName("Deve lançar FielNaoEncontradoException e abortar chamada ao gateway quando ID for inexistente")
        void deveLancarExcecaoQuandoFielNaoExistir() {
            // 1. Arrange
            var item = new ItemTaxa("Taxa Casamento", VALOR_PADRAO);
            given(fielRepository.buscarPorId(FIEL_ID)).willReturn(Optional.empty());

            // 2. Act & 3. Assert combinados via AssertJ
            assertThatThrownBy(() -> processadorCobranca.liquidarTaxa(FIEL_ID, item))
                .isInstanceOf(FielNaoEncontradoException.class)
                .hasMessage("Fiel não localizado para o identificador: " + FIEL_ID);

            // Garante isolamento de efeitos colaterais
            then(gatewayPagamento).should(never()).processar(any());
        }

        @Test
        @DisplayName("Deve rejeitar taxa com valor base zerado ou negativo respeitando as invariantes do Record")
        void deveRejeitarItemComValorInvalido() {
            // 1. Arrange & 2. Act & 3. Assert direto no construtor compacto do Record
            assertThatThrownBy(() -> new ItemTaxa("Taxa Inválida", BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Valor base da taxa deve ser estritamente positivo");
        }
    }
}

```

---

## 7. Estratégias Avançadas de Teste em Java 25

### 7.1. Testes Parametrizados Baseados em Dados

Para matrizes de regras e limites numéricos, utilize `@ParameterizedTest` com `@MethodSource` em vez de duplicar métodos de teste:

```java
@ParameterizedTest(name = "Para taxa bruta de R$ {0}, o desconto retido deve ser R$ {1}")
@MethodSource("provedorMatrizDescontos")
@DisplayName("Deve calcular descontos progressivos corretamente para diferentes faixas")
void deveCalcularDescontosProgressivos(BigDecimal valorBruto, BigDecimal descontoEsperado) {
    var item = new ItemTaxa("Serviço Paroquial", valorBruto);
    BigDecimal resultado = calculadora.calcularAbatimento(item);
    assertThat(resultado).isEqualByComparingTo(descontoEsperado);
}

static Stream<Arguments> provedorMatrizDescontos() {
    return Stream.of(
        Arguments.of(new BigDecimal("50.00"), BigDecimal.ZERO),
        Arguments.of(new BigDecimal("100.00"), new BigDecimal("10.00")),
        Arguments.of(new BigDecimal("500.00"), new BigDecimal("75.00"))
    );
}

```

### 7.2. Testes com Padrões Selados e Desconstrução

Ao asserir retornos polimórficos de classes seladas (`sealed interface`), utilize a desestruturação do Java 25 dentro das asserções do AssertJ:

```java
@Test
@DisplayName("Deve retornar variante FalhaProcessamento com código de erro correto quando transação for recusada")
void deveRetornarVarianteFalha() {
    ResultadoTransacao resultado = processador.executar(comandoInvalido);

    assertThat(resultado)
        .isInstanceOfSatisfying(ResultadoTransacao.Recusada.class, recusada -> {
            assertThat(recusada.motivo()).isEqualTo("Saldo Insuficiente");
            assertThat(recusada.codigoErro()).isEqualTo(422);
        });
}

```

---

## 8. Anti-Patterns em Testes Unitários a Evitar

* **Testes com Múltiplas Razões para Falhar:** Métodos longos com 10 asserções verificando lógicas de negócio não correlacionadas. Divida em cenários atômicos.
* **Asserções Tautológicas ou Passivas:** Testes que apenas invocam métodos para cobrir linha sem verificar efeitos (`assertThatCode(() -> servico.executar()).doesNotThrowAnyException()` sem checar mutação de estado ou mocks).
* **Uso Indiscriminado de `any()` no Mockito:** Configurar `when(repo.salvar(any())).thenReturn(...)` e nunca validar se os parâmetros passados para o `salvar` estavam corretos via `ArgumentCaptor` ou argumentos literais.
* **Dependência Temporal Frágil:** Usar `Thread.sleep()` ou comparar instantes com `Instant.now()` sem tolerância. Utilize `java.time.Clock` injetável ou `within(1, ChronoUnit.SECONDS)` do AssertJ.
* **Vazamento de Mocking:** Fazer mock de classes utilitárias puras ou Value Objects/Records (`mock(BigDecimal.class)` ou `mock(FielId.class)`). Dublês devem ser restritos a interfaces de fronteira e serviços com estado.

---

## 9. Checklist de Auditoria TDD para o Agente IA

Antes de considerar qualquer entrega de testes finalizada, valide:

1. [ ] **Rastreabilidade de Especificação:** Todo teste gerado aponta explicitamente para uma regra funcional ou critério de aceitação em seu Javadoc e `@DisplayName`?
2. [ ] **Precedência TDD (RED):** Foi comprovado que o teste falhava antes da existência do código de produção correspondente?
3. [ ] **Zero Código Não Coberto:** Existe alguma cláusula condicional (`if`, `when`, `catch`) no código de produção que não seja acionada por nenhum teste da suíte?
4. [ ] **Isolamento Absoluto:** O teste executa sem `@SpringBootTest`, sem tocar em banco de dados físico, rede ou disco?
5. [ ] **Qualidade de Asserção (AssertJ):** Foi priorizado `assertThat(...)` com verificações semânticas estritas em vez de asserções superficiais?
6. [ ] **Validação de Efeitos Colaterais:** Para métodos `void`, foi validado o comportamento esperado nas portas de saída usando `then(mock).should()` ou `ArgumentCaptor`?
7. [ ] **Determinismo e Velocidade:** Todos os testes são independentes entre si e executam em milissegundos sem concorrência com estado estático compartilhado?