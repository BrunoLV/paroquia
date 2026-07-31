# Skill do Agente de IA: Testes Unitários em Java (Especialista em TDD)

## 1. Visão Geral e Contexto

Esta skill capacita o Agente de IA a gerar testes unitários de forma autônoma e rigorosa para aplicações Java, seguindo estritamente os princípios do Desenvolvimento Orientado a Testes (TDD). O agente opera dentro de um ambiente de execução (test harness) para validar o código, garantir alta cobertura de testes e assegurar o isolamento arquitetural (isolando a unidade sob teste usando dublês de teste/mocks).

## 2. Fluxo de Execução: O Ciclo TDD

O Agente deve executar as tarefas de teste seguindo estritamente o padrão **Red-Green-Refactor**. Ele nunca deve gerar código de produção sem antes definir ou atualizar suas respectivas asserções de teste.

```
   ┌────────────────────────────────────────────────────────┐
   │                                                        │
   ▼                                                        │
[ RED ] ─────────────────► [ GREEN ] ─────────────────► [ REFACTOR ]
Escreva um teste que      Escreva o código mínimo       Melhore o código
falha para uma regra.     de produção para passar.      mantendo o teste verde.

```

1. **RED (Vermelho)**: Escreva um teste unitário preciso focado em uma única regra de negócio ou caso de borda. Execute o harness para verificar se ele falha (`AssertionError`, comportamento ausente, etc.). Não crie mocks da própria classe que está sendo testada.
2. **GREEN (Verde)**: Escreva a quantidade *mínima* de código de produção necessária para fazer o teste que falhava passar. Evite superengenharia ou antecipar funcionalidades futuras.
3. **REFACTOR (Refatorar)**: Melhore a estrutura, legibilidade e desempenho do código sem alterar seu comportamento externo. Execute os testes novamente de imediato para garantir que continuem verdes.

## 3. Tecnologias e Diretrizes de Ferramentas

O Agente deve gerar testes exclusivamente utilizando as bibliotecas modernas que são padrão de mercado:

* **Ambiente de Execução:** Java 17+ (utilizando sintaxes modernas como Records, Blocks e Expressões Switch quando aplicável).
* **Framework de Testes:** JUnit 5 (Jupyter).
* *NÃO utilize anotações do JUnit 4 (a anotação `@Test` deve vir obrigatoriamente de `org.junit.jupiter.api.Test`).*
* Prefira anotações de ciclo de vida como `@BeforeEach` e `@AfterEach` para limpezas e configurações iniciais.


* **Framework de Mocks:** Mockito 5+.
* Utilize `@ExtendWith(MockitoExtension.class)` para gerenciar o ciclo de vida dos mocks nativamente.
* Isole a classe sob teste criando mocks de repositórios de banco de dados, clientes externos e serviços pesados usando `@Mock` e `@InjectMocks`.



## 4. Restrições Arquiteturais (Limites do Teste Unitário Puro)

Para garantir que estes sejam **Testes Unitários** puros e não cruzem a linha para Testes de Integração:

1. **Sem Inicialização de Contexto:** Nunca utilize anotações de contexto do Spring Boot, tais como `@SpringBootTest`, `@DataJpaTest` ou `@WebMvcTest`.
2. **Sem I/O Externo:** Não permita conexões reais com bancos de dados, chamadas HTTP de rede, gravações no sistema de arquivos ou subida de containers (ex: Testcontainers). Tudo o que estiver fora da lógica de negócio central da classe alvo deve ser simulado via Mockito (`when(...).thenReturn(...)`).
3. **Velocidade:** Os testes devem ser otimizados para executar na casa dos milissegundos.

## 5. Padrão Estrutural (Blueprint AAA)

Todo teste unitário gerado pelo Agente deve seguir rigorosamente a estrutura **Arrange-Act-Assert** (Organizar, Agir, Asserir), garantindo legibilidade máxima:

```java
@Test
@DisplayName("Deve rejeitar senha quando ela contiver menos de 8 caracteres")
void deveRejeitarSenhaQuandoElaContiverMenosDeOitoCaracteres() {
    // 1. Arrange (Organizar): Configura o objeto alvo, inputs e comportamentos dos mocks
    ValidadorSenha validador = new ValidadorSenha();
    String senhaCurta = "1234567";

    // 2. Act (Agir): Executa o método exato sob teste
    boolean resultado = validador.ehValida(senhaCurta);

    // 3. Assert (Asserir): Verifica o resultado com mensagens descritivas
    assertFalse(resultado, "Uma senha com menos de 8 caracteres deve ser marcada como inválida.");
}

```

## 6. Checklist de Casos de Borda e Robustez

Antes de dar uma tarefa por concluída, o Agente deve verificar se cobriu os seguintes cenários:

* **Limites de Nulos e Vazios:** Como o código lida com entradas `null`, strings vazias, zero ou números negativos?
* **Tratamento de Exceções:** Validar se as exceções corretas são lançadas sob estados inválidos utilizando `assertThrows()`.
* **Idempotência:** Garantir que executar o mesmo teste múltiplas vezes gere resultados idênticos, sem efeitos colaterais por estados compartilhados.