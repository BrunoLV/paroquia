Aqui está a instrução reformulada e calibrada com o mesmo rigor dos guias anteriores.

Foram incorporados: o conceito de **Living Documentation** (código como documento das decisões de design), a **correção da armadilha dos String Templates** (recurso descontinuado/reavaliado no OpenJDK pós-Java 21/22, redirecionando para `formatted()` / Text Blocks / `MessageFormat`), uso de **Scoped Values e Structured Concurrency**, tratamento seguro de `Optional`, regras de concorrência com **Virtual Threads** (evitando *pinning* com `synchronized`) e um **Blueprint estrutural de alta fidelidade**.

```markdown
# Skill do Agente de IA: Desenvolvimento Java (Especialista em Java 25 e Modern Clean Code)

## 1. Visão Geral e Contexto
Esta skill define as diretrizes arquiteturais, semânticas e idiomáticas que o Agente de IA deve seguir ao projetar, refatorar e gerar código na linguagem **Java**, com foco absoluto nos recursos de ponta do **Java 25**. O agente atua como um Arquiteto Java Principal, combinando os pilares clássicos de design de software (*Effective Java* de Joshua Bloch) com as capacidades modernas da plataforma: **Tipos de Dados Algébricos (ADT)**, **Pattern Matching exaustivo**, **Records**, **Virtual Threads** e concorrência estruturada.

---

## 2. O Código como Documentação Viva (Living Documentation)

O código Java não deve ser apenas funcional; ele deve expressar abertamente a intenção do design e as garantias do ecossistema.

* **Javadoc Intencional Obrigatório:** Todas as classes públicas, interfaces seladas, records e métodos centrais devem documentar o **porquê** de sua existência e a decisão de modelagem adotada.
* **Proibição de Comentários Redundantes:** Comentários óbvios (ex.: `// Construtor padrão`) são proibidos. Em contrapartida, justifique o uso de construções modernas:
  ```java
  /**
   * Representa a política de transição de status de um faturamento.
   *
   * DECISÃO DE DESIGN (ADT / Sealed Hierarchy):
   * Selada para restringir as mutações aos estados canônicos da regra de negócio,
   * permitindo pattern matching exaustivo no switch sem cláusula default.
   */
  public sealed interface StatusFaturamento permits Aberto, Liquidado, Estornado { ... }

```

* **Explicitação de Invariantes e Pré-condições:** Use blocos de documentação para explicitar limites de domínio validados em construtores compactos ou verificações com `Objects.requireNonNull`.

---

## 3. Idioma Java Moderno de Ultra-Alto Nível (Java 25)

O Agente deve eliminar padrões imperativos obsoletos e priorizar construções expressivas e seguras em tempo de compilação:

### 3.1. Modelagem com Tipos de Dados Algébricos (ADTs)

* **Records como Padrão para Dados:** Use `record` para toda estrutura imutável de dados (DTOs, Value Objects, eventos, tuplas de retorno).
* **Validação em Construtores Compactos:** Aplique regras de integridade diretamente no construtor compacto do record sem duplicar atribuições (`this.x = x`):
```java
public record TaxaOperacional(BigDecimal valor) {
    public TaxaOperacional {
        Objects.requireNonNull(valor, "Valor da taxa não pode ser nulo");
        if (valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Taxa não pode ser negativa");
        }
    }
}

```


* **Hierarquias Seladas (`sealed` / `permits`):** Modele variações fixas de domínio com `sealed interface` implementadas por `record`s. Permite controle absoluto sobre quem estende a árvore de tipos.

### 3.2. Pattern Matching Avançado e Desconstrução

* **Exaustividade sem `default` Artificial:** Em `switch expressions` sobre hierarquias seladas, cubra todas as variantes sem adicionar `default`. Isso faz o compilador apontar erros se uma nova variante for criada no futuro.
* **Desconstrução de Records e Guards (`when`):** Desestruture records diretamente nos braços do `switch` ou `instanceof`, filtrando estados com cláusulas `when`:
```java
case Transacao(var id, BigDecimal valor) when valor.compareTo(LIMITE_ALERTA) > 0 ->
    processarComAuditoria(id, valor);

```


* **Unnamed Patterns e Variáveis (`_`):** Utilize o underscore (`_`) para descartar explicitamente variáveis locais não lidas, sub-padrões de records ignorados ou parâmetros de lambdas desnecessários.
* **Tratamento de `null` em Switch:** Trate `case null` explicitamente no `switch` sempre que o valor de entrada puder ser nulo, evitando `NullPointerException` imprevisíveis.

### 3.3. Text Blocks e Interpolação Segura

* Utilize **Text Blocks (`"""`)** para blocos multilinha (SQL, JSON, HTML), preservando a indentação sem quebras de linha manuais (`\n`) ou concatenações com `+`.
* Para interpolação dinâmica, utilize o método idiomático `String.formatted(...)` ou `MessageFormat`, garantindo escape correto e clareza estrutural.

### 3.4. Concorrência Moderna: Virtual Threads e Concorrência Estruturada

* **Virtual Threads para I/O Bloqueante:** Para servidores web, chamadas HTTP remotas e consultas a bancos de dados, utilize `Executors.newVirtualThreadPerTaskExecutor()`.
* **Sem Bloqueio do Carrier Thread (*Anti-Pinning*):** Evite blocos `synchronized` clássicos em operações longas de I/O dentro de Virtual Threads (pois podem prender o thread do sistema operacional subjacente); substitua por `ReentrantLock` quando for necessário controle de acesso crítico.
* **Structured Concurrency e Scoped Values:** Prefira orquestrar tarefas paralelas interdependentes usando `StructuredTaskScope` e passar contexto de requisição (tokens, IDs de correlação) via `ScopedValue` em vez de `ThreadLocal` herdados.

---

## 4. Princípios Consagrados Revisitados (Effective Java)

* **Static Factory Methods:** Prefira métodos de fábrica com nomes descritivos (`of()`, `from()`, `gerarNovo()`) a construtores sobrecarregados, garantindo legibilidade imediata no ponto de chamada.
* **Imutabilidade e Cópias Defensivas:**
* Atributos de objetos mutáveis (como `Date` ou coleções legadas) devem ser convertidos para tipos modernos e imutáveis (`java.time.Instant`, `java.time.LocalDate`).
* Ao receber coleções externas em construtores, use `List.copyOf()`, `Set.copyOf()` ou `Map.copyOf()`.


* **Semântica de `Optional<T>`:**
* Use `Optional<T>` **exclusivamente como tipo de retorno** de métodos para indicar ausência natural de resultado.
* **Proibições:** Nunca passe `Optional` como parâmetro de método, nunca use `Optional` em atributos de classe/record e nunca retorne `null` de um método cujo retorno seja `Optional` (retorne sempre `Optional.empty()`).
* Nunca chame `.get()` sem verificação prévia; utilize métodos funcionais como `.map()`, `.flatMap()`, `.filter()`, `.orElseGet()` ou `.orElseThrow()`.


* **Coleções Vazias em Vez de `null`:** Métodos que retornam coleções, listas ou arrays nunca devem retornar `null`. Retorne `List.of()`, `Set.of()` ou `Collections.emptyList()`.
* **Precisão Numérica Financeira:** É terminantemente proibido o uso de `float` ou `double` para cálculos contábeis, monetários ou de precisão decimal. Utilize invariavelmente `BigDecimal` com modo de arredondamento explícito (`RoundingMode.HALF_EVEN` / `HALF_UP`).

---

## 5. Guia de Estilo, Limpeza e Convenções (Clean Code)

* **Uso Racional de `var`:** Utilize inferência de tipo local (`var`) apenas quando ela aprimorar a legibilidade eliminando redundância visual óbvia (ex.: `var processador = new ProcessadorPedido();`). Evite `var` quando o tipo de retorno for implícito ou vindo de métodos encadeados complexos (ex.: `var resultado = servico.executar();` prejudica a leitura do tipo).
* **Gerenciamento de Recursos com `try-with-resources`:** Qualquer recurso que implemente `AutoCloseable` deve ser aberto dentro do cabeçalho de um `try-with-resources`. Não use `finally` manual para invocar `.close()`.
* **Tratamento Específico de Exceções:**
* Capture exceções específicas; nunca declare blocos `catch (Exception e)` genéricos a menos que seja o manipulador de última instância na fronteira da aplicação.
* Nunca engula exceções com blocos vazios. Se uma exceção não puder ser recuperada, transforme-a em uma exceção não verificada de negócio (`DomainException`) preservando a causa raiz (`throw new ProcessamentoException("Falha ao liquidar", e);`).



---

## 6. Blueprint Estrutural (Java 25)

Exemplo completo demonstrando o casamento de Clean Code, tipos algébricos, imutabilidade, Text Blocks e Pattern Matching:

```java
package com.empresa.faturamento.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Modelo de Dados Algébrico (ADT) representando as modalidades de estorno aceitas.
 */
public sealed interface MotivoEstorno permits DesistenciaCliente, FraudeDetectada, FalhaOperacional {
    record DesistenciaCliente(String justificativa) implements MotivoEstorno {}
    record FraudeDetectada(String codigoAlerta, Instant horarioDeteccao) implements MotivoEstorno {}
    record FalhaOperacional(int codigoErro) implements MotivoEstorno {}
}

/**
 * Entidade imutável encapsulada em Record contendo invariantes de negócio.
 */
public record Faturamento(
    UUID id,
    BigDecimal subtotal,
    BigDecimal valorTaxa,
    Instant dataCriacao
) {
    public Faturamento {
        Objects.requireNonNull(id, "Identificador do faturamento não pode ser nulo");
        Objects.requireNonNull(subtotal, "Subtotal não pode ser nulo");
        Objects.requireNonNull(valorTaxa, "Valor da taxa não pode ser nulo");
        Objects.requireNonNull(dataCriacao, "Data de criação não pode ser nula");

        if (subtotal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Subtotal do faturamento deve ser positivo");
        }
        if (valorTaxa.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Taxa do faturamento não pode ser negativa");
        }
    }

    /**
     * Static Factory para criação expressiva de uma nova instância.
     */
    public static Faturamento iniciar(BigDecimal subtotal, BigDecimal taxa) {
        return new Faturamento(UUID.randomUUID(), subtotal, taxa, Instant.now());
    }

    public BigDecimal calcularTotalLiquido() {
        return subtotal.add(valorTaxa).setScale(2, RoundingMode.HALF_EVEN);
    }
}

/**
 * Orquestrador de estorno demonstrando Pattern Matching avançado no Java 25.
 */
public final class ProcessadorEstorno {

    /**
     * Processa a penalidade de estorno aplicando regras polimórficas por tipo algébrico.
     */
    public BigDecimal calcularTaxaRetencao(Faturamento faturamento, MotivoEstorno motivo) {
        Objects.requireNonNull(faturamento, "Faturamento não pode ser nulo");

        // Pattern matching com desconstrução de records, guards (when) e unnamed variables (_)
        return switch (motivo) {
            case MotivoEstorno.DesistenciaCliente(var _) -> 
                faturamento.calcularTotalLiquido().multiply(new BigDecimal("0.05"));

            case MotivoEstorno.FraudeDetectada(var _, Instant horario) 
                when horario.isAfter(Instant.now().minusSeconds(3600)) -> 
                BigDecimal.ZERO; // Isenção imediata se reportado em menos de 1 hora

            case MotivoEstorno.FraudeDetectada _ -> 
                new BigDecimal("15.00"); // Taxa administrativa padrão de auditoria

            case MotivoEstorno.FalhaOperacional(int codigo) when codigo >= 500 -> 
                BigDecimal.ZERO; // Falha do sistema não gera ônus ao cliente

            case MotivoEstorno.FalhaOperacional _ -> 
                new BigDecimal("5.00");

            case null -> throw new IllegalArgumentException("Motivo do estorno deve ser informado");
        };
    }

    /**
     * Emite recibo formatado utilizando Text Blocks do Java moderno.
     */
    public String gerarReciboCancelamento(Faturamento faturamento, BigDecimal taxaRetencao) {
        return """
            {
              "evento": "FATURAMENTO_ESTORNADO",
              "id": "%s",
              "valorOriginal": %.2f,
              "taxaRetencao": %.2f,
              "dataProcessamento": "%s"
            }
            """.formatted(
                faturamento.id(),
                faturamento.calcularTotalLiquido(),
                taxaRetencao,
                Instant.now()
            );
    }
}

```

---

## 7. Checklist de Qualidade e Anti-Patterns a Evitar

Antes de submeter código ou considerar uma tarefa concluída, o agente deve auditar os seguintes pontos:

1. [ ] **Código como Documentação:** As classes, records e hierarquias possuem comentários conceituais (Javadoc) justificando o papel arquitetural e a regra de negócio?
2. [ ] **Eliminação de Boilerplate:** Foram eliminados DTOs com getters/setters manuais, utilizando `record` com validação no construtor compacto?
3. [ ] **Precisão Monetária:** Propriedades monetárias e cálculos fiscais utilizam estritamente `BigDecimal` com arredondamento explícito?
4. [ ] **Pattern Matching e Exaustividade:**
* Foram eliminadas sequências imperativas de `if (x instanceof Y)` com casts manuais?
* As expressões `switch` cobrem todas as variantes seladas sem cláusula `default` indevida?
* Foram utilizadas unnamed variables (`_`) para valores desestruturados descartáveis?


5. [ ] **Uso Seguro de Recursos e Nulos:**
* Todos os recursos auto-fecháveis estão isolados em blocos `try-with-resources`?
* Métodos que retornam coleções entregam listas vazias (`List.of()`) em vez de `null`?
* O uso de `Optional` está restrito a tipos de retorno de métodos?


6. [ ] **Concorrência Segura em Virtual Threads:** Operações bloqueantes de I/O evitam blocos `synchronized` em favor de `ReentrantLock` ou estruturas de concorrência estruturada (`StructuredTaskScope`)?

```

```