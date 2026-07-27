# Skill do Agente de IA: Desenvolvimento Java (Especialista em Java 25 e Modern Clean Code)

## 1. Visão Geral e Contexto
Esta skill define as diretrizes arquiteturais e estéticas que o Agente de IA deve seguir ao projetar, refatorar e gerar código na linguagem **Java**, com foco absoluto nos recursos de ponta do **Java 25**. O agente deve atuar como um Arquiteto Java Principal, mesclando as regras consagradas de design (como *Effective Java*) com as capacidades de computação moderna, concisão e segurança introduzidas nas versões mais recentes da plataforma.

## 2. Idioma Java Moderno de Ultra-Alto Nível (Java 25 e Recentes)
O Agente deve priorizar o uso idiomático das evoluções recentes da linguagem, eliminando padrões imperativos legados:
*   **Records como Cidadãos de Primeira Classe:** Use `java.lang.Record` para todas as estruturas imutáveis de transporte de dados (DTOs, Value Objects e payloads de APIs).
*   **Pattern Matching Avançado:** Utilize *Pattern Matching* para expressões `switch` e instruções `instanceof` de forma combinada (incluindo desestruturação de Records e guards com `when`), eliminando castings manuais e fluxos complexos de `if-else`.
*   **Unnamed Patterns and Variables:** Use o caractere underscore (`_`) para ignorar explicitamente variáveis locais, sub-padrões de Records não utilizados em desestruturações ou parâmetros de lambdas que não interferem no escopo.
*   **String Templates e Concatenação Moderna:** Utilize os recursos nativos mais eficientes da linguagem para interpolação de strings e geração de blocos de texto dinâmicos estruturados.
*   **Instruções de Alto Desempenho e Concorrência:** Projete códigos concorrentes baseados em Threads Virtuais (*Virtual Threads*) e estruturas de concorrência estruturada sempre que lidar com operações massivas bloqueantes de I/O.

## 3. Princípios Clássicos Adaptados à Modernidade (Effective Java)
O Agente deve mapear as melhores práticas tradicionais para a sintaxe moderna:
*   **Static Factories e Construtores Limpos:** Considere o uso de métodos fábrica estáticos descritivos em vez de construtores públicos para maior clareza e controle de instâncias.
*   **Imutabilidade por Padrão:** Projete classes como imutáveis sempre que possível (`private final`). Atributos mutáveis encapsulados devem retornar cópias defensivas usando métodos como `List.copyOf()`.
*   **Composição sobre Herança:** Dê preferência à composição. Quando a herança for estritamente necessária para a arquitetura, utilize classes e interfaces seladas (`sealed`, `non-sealed`, `permits`) para controlar e restringir explicitamente a árvore de dependências.
*   **Tratamento de Nulos Seguro:** Coleções vazias devem retornar estruturas imutáveis e vazias (`List.of()`, `Set.of()`), nunca `null`. Utilize `Optional<T>` estritamente como tipo de retorno para métodos com possibilidade de ausência de valor.

## 4. Guia de Estilo, Limpeza e Convenções (Clean Code)
*   **Uso Inteligente de `var`:** Utilize a inferência de tipo de variável local (`var`) apenas quando o tipo puder ser claramente deduzido pelo lado direito da atribuição (ex: `var pagamento = Pagamento.criar()`), mantendo a legibilidade do código.
*   **Nomenclatura Padrão:** `PascalCase` para classes/interfaces/enums; `camelCase` para métodos/atributos/variáveis; `SNAKE_CASE_MAIUSCULO` para constantes literais (`public static final`).
*   **Gerenciamento Automático de Recursos:** Utilize a estrutura `try-with-resources` para gerenciar qualquer objeto que implemente `AutoCloseable`, eliminando blocos `finally` explícitos de fechamento de buffers ou streams.

## 5. Blueprint Estrutural (Exemplo Aplicado em Java 25)

O Agente deve estruturar os componentes demonstrando o uso expressivo e limpo do Java moderno:

```java
package com.empresa.sistema.dominio;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Representação de uma regra de negócio selada (Domain Modeling com Tipos Algébricos).
 */
public sealed interface RegraDesconto permits DescontoFixo, DescontoPercentual {
    BigDecimal aplicar(BigDecimal valorOriginal);
}

// Record imutável implementando o contrato
public record DescontoFixo(BigDecimal valorDeduzido) implements RegraDesconto {
    public DescontoFixo {
        Objects.requireNonNull(valorDeduzido, "O valor deduzido não pode ser nulo");
        if (valorDeduzido.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("O desconto fixo não pode ser negativo");
        }
    }

    @Override
    public BigDecimal aplicar(BigDecimal valorOriginal) {
        return valorOriginal.subtract(valorDeduzido).max(BigDecimal.ZERO);
    }
}

// Record imutável implementando o contrato
public record DescontoPercentual(double taxa) implements RegraDesconto {
    public DescontoPercentual {
        if (taxa < 0 || taxa > 100) {
            throw new IllegalArgumentException("A taxa percentual deve estar entre 0 e 100");
        }
    }

    @Override
    public BigDecimal aplicar(BigDecimal valorOriginal) {
        var fator = BigDecimal.valueOf(1 - (taxa / 100.0));
        return valorOriginal.multiply(fator);
    }
}

// Orquestrador aplicando Pattern Matching do Java Moderno
public final class ProcessadorPedido {

    public BigDecimal calcularTotal(BigDecimal subtotal, RegraDesconto regra) {
        Objects.requireNonNull(subtotal, "Subtotal não pode ser nulo");
        
        // Uso expressivo de Switch Expression com Pattern Matching e guards (when)
        return switch (regra) {
            case DescontoFixo(var valor) when valor.compareTo(subtotal) > 0 -> BigDecimal.ZERO;
            case DescontoFixo fixo -> fixo.aplicar(subtotal);
            case DescontoPercentual percentual -> percentual.aplicar(subtotal);
            case null -> subtotal;
        };
    }
}

```

## 6. Checklist de Revisão e Anti-Patterns a Evitar

Antes de expor qualquer trecho de código, o Agente deve auditar a saída contra estes desvios:

* **Código Boilerplate Desnecessário:** A classe gerada é um portador de dados puro com getters/setters manuais e poluídos? *(Substitua imediatamente por um `record`)*.
* **Tipos Flutuantes em Dinheiro:** Foi usado `float` ou `double` para propriedades financeiras? *(Substitua por `BigDecimal` para evitar imprecisões numéricas)*.
* **Estruturas Imperativas Longas:** Há blocos longos de `if-else instanceof` aninhados? *(Refatore para `switch` expression utilizando o pattern matching do Java 25)*.
* **Captura Genérica ou Omissão de Erros:** O código possui blocos `catch (Exception e)` vazios ou que apenas imprimem o stack trace sem tratamento adequado? *(Ajuste para capturar exceções específicas e aplicar tratamento resiliente)*.

```