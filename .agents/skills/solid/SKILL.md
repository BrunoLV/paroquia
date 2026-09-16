---
name: solid
description: "Princípios SOLID aplicados à arquitetura Java 25: SRP, OCP com Pattern Matching e Sealed Types, LSP, ISP com interfaces funcionais e DIP com injeção por construtor imutável."
---

# Skill do Agente de IA: Engenharia e Arquitetura de Código Java 25 (SOLID Avançado)

## 1. Visão Geral e Contexto

Esta skill capacita o Agente de IA a projetar, refatorar e auditar código de produção em **Java 25** aplicando os princípios **SOLID** em nível de excelência técnica. O objetivo é produzir código com baixo acoplamento, alta coesão, extensibilidade real e clareza documental, combatendo ativamente tanto o acoplamento espaguete quanto o excesso de engenharia (*overengineering* e interfaces fantasma).

O código deve tirar proveito das capacidades contemporâneas do Java 25: **Sealed Hierarchies**, **Pattern Matching for `switch` com desconstrução de Records**, **Records com compact constructors**, **Unnamed Variables (`_`)**, e concorrência estruturada sem quebra de imutabilidade.

---

## 2. O Código como Documentação dos Padrões Arquiteturais

O código deve expressar com clareza as intenções arquiteturais que justificaram cada separação estrutural e escolha de padrão.

* **Justificativa Arquitetural no Javadoc:** Ao isolar uma interface, aplicar uma estratégia polimórfica ou compor objetos, documente a motivação e o ator atendido:
  ```java
  /**
   * Estratégia de precificação dinâmica para assinaturas corporativas.
   *
   * DECISÃO DE DESIGN (OCP / Strategy Pattern):
   * Isolada como estratégia polimórfica para permitir que novas modalidades
   * de desconto de volume sejam introduzidas sem alteração no motor de faturamento.
   *
   * ATOR ASSOCIADO (SRP):
   * Time de Estratégia Comercial e Precificação.
   */
  public final class PrecificacaoVolumeStrategy implements CalculoMensalidadeStrategy { ... }
  ```
* **Rastreabilidade de Contratos (LSP):** Métodos que implementam regras regidas por contratos estritos de substituição devem documentar explicitamente suas pré e pós-condições garantidas.

---

## 3. Diretrizes dos Princípios SOLID em Java 25

### 3.1. S — Single Responsibility Principle (Princípio da Responsabilidade Única)

* **Definição Canônica:** Um módulo/classe deve responder a **um, e apenas um, ator (stakeholder/usuário)** do negócio.
* **Equilíbrio contra Fragmentação Anêmica:** Responsabilidade única não significa "um método por classe". Evite fragmentar agregados coesos em microsserviços internos de métodos únicos (ex.: `CriarX`, `ValidarX`, `SalvarX`). Agrupe comportamentos e dados que mudam pelas mesmas razões operacionais do mesmo ator.
* **Ação do Agente:** Separe explicitamente camadas com ritmos de mudança diferentes: regras de negócio (domínio), persistência/IO (infraestrutura) e orquestração de fluxo (aplicação). Utilize `record` para transportar estados imutáveis e isolar a estrutura de dados das regras de orquestração.

### 3.2. O — Open/Closed Principle (Princípio Aberto/Fechado)

* **Definição:** Entidades de software devem ser abertas para extensão, mas fechadas para modificação.
* **Composição sobre Herança:** Favoreça o padrão *Strategy* ou composição funcional sobre árvores profundas de herança de classes concretas.
* **Ação do Agente no Java 25 (Exaustividade com `sealed` e Pattern Matching):**
  * Quando o conjunto de variantes de domínio for aberto ou desconhecido: utilize **interfaces polimórficas** tradicionais ou `@FunctionalInterface` via composição/Strategy.
  * Quando o conjunto de variantes for restrito e conhecido pelo domínio (ex.: Tipos de Pagamento, Estados de um Pedido): use **`sealed interface` com `records` e Pattern Matching no `switch`**. O compilador do Java 25 garante a checagem exaustiva de tipos sem blocos `default` artificiais, permitindo extensão controlada e detectando inconsistências em tempo de compilação.

### 3.3. L — Liskov Substitution Principle (Princípio da Substituição de Liskov)

* **Definição (Design by Contract):** Objetos de um subtipo devem substituir objetos do supertipo sem violar a consistência do sistema ou alterar as garantias do contrato original.
* **As Quatro Regras de Ouro do Contrato:**
  1. **Não fortaleça pré-condições:** O subtipo não pode exigir parâmetros mais restritos do que o tipo base declarou (ex.: rejeitar argumentos que a interface declarava como válidos).
  2. **Não enfraqueça pós-condições:** O subtipo deve entregar todas as garantias do tipo base (ex.: nunca retorne `null` onde a interface garantia uma lista ou instância válida; retorne `Optional.empty()` ou coleções vazias imutáveis como `List.of()`).
  3. **Preserve invariantes:** O subtipo não pode violar regras internas que o tipo base sempre manteve íntegras.
  4. **Não lance exceções inesperadas:** É expressamente proibido sobrescrever métodos lançando `UnsupportedOperationException` ou introduzir exceções não previstas na semântica do contrato.

### 3.4. I — Interface Segregation Principle (Princípio da Segregação de Interfaces)

* **Definição:** Clientes não devem ser forçados a depender de interfaces com métodos que não utilizam.
* **Interfaces Papel (*Role Interfaces*):** Projete interfaces orientadas ao papel que o consumidor precisa, não ao inventário completo de capacidades do provedor.
* **Interfaces Funcionais e Lambdas:** Utilize `@FunctionalInterface` para contratos de comportamento único. Isso viabiliza o uso de expressões Lambda e *Method References*, evitando a criação de classes desnecessárias para comportamentos pontuais.
* **Parâmetros Não Utilizados:** Caso implemente contratos de terceiros com parâmetros dispensáveis na sua regra específica, use *Unnamed Variables* (`_`) do Java moderno para explicitar que aquele dado não interfere na computação.

### 3.5. D — Dependency Inversion Principle (Princípio da Inversão de Dependência)

* **Definição:**
  1. Módulos de alto nível (regras de negócio) não devem depender de módulos de baixo nível (infraestrutura, bancos, APIs externas). Ambos devem depender de abstrações.
  2. **A Abstração Pertence ao Consumidor:** A interface de serviço deve ser definida e residir junto ao módulo de alto nível que a consome (Input/Output Port), nunca orientada aos detalhes da tecnologia que a implementa.
* **Injeção Estrita via Construtor:** Toda dependência obrigatória deve ser imutável (`private final`) e injetada exclusivamente pelo construtor. Injeção de dependência por reflexão em campos (`@Autowired` em propriedades privadas) é proibida.

---

## 4. Regras Anti-Overengineering (Equilíbrio Pragmático)

O Agente deve evitar o "SOLID cego", que degrada a legibilidade e a manutenibilidade do ecossistema:

* **Proibição do Anti-Pattern `*Impl` por Reflexo:**
  * Não crie interfaces artificiais (`UsuarioService` e `UsuarioServiceImpl`) se existe apenas uma implementação e não há requisito de polimorfismo ou fronteira arquitetural (ex.: banco de dados ou integração externa).
  * Use classes concretas simples para serviços de aplicação diretos. Crie interfaces quando houver polimorfismo genuíno (*Strategy*), desacoplamento de I/O (*Repository* / *Gateway*) ou fronteira de domínio.
* **Prematuro vs. Necessário (YAGNI):** Não projete pontos de extensão para cenários hipotéticos que não decorrem dos requisitos presentes no código atual.

---

## 5. Blueprint Estrutural (Java 25)

Exemplo de faturamento demonstrando o uso harmônico de SOLID com os recursos do Java 25:

```java
package com.empresa.faturamento.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * [I] Contrato funcional segregado e enxuto.
 * Define o cálculo de desconto para uma transação.
 */
@FunctionalInterface
public interface RegraDesconto {
    BigDecimal calcular(ItemCobranca item);
}

/**
 * [S] Estrutura de dados de negócio imutável.
 * Validação de invariantes no construtor compacto do record.
 */
public record ItemCobranca(String identificador, BigDecimal valorBase, int quantidade) {
    public ItemCobranca {
        Objects.requireNonNull(identificador, "Identificador não pode ser nulo");
        if (valorBase == null || valorBase.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor base deve ser estritamente positivo");
        }
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser superior a zero");
        }
    }
}

/**
 * [O] e [L] Domínio fechado para variações conhecidas via Sealed Interface.
 * Permite pattern matching exaustivo sem necessidade de 'default' em switch expressions.
 */
public sealed interface ModalidadePagamento permits ModalidadePagamento.Pix, ModalidadePagamento.CartaoCredito, ModalidadePagamento.Boleto {
    record Pix(String chaveDestino, boolean instantaneo) implements ModalidadePagamento {}
    record CartaoCredito(String tokenCartao, int parcelas) implements ModalidadePagamento {}
    record Boleto(String linhaDigitavel, int diasVencimento) implements ModalidadePagamento {}
}

/**
 * [S] e [O] Estratégia concreta para desconto progressivo de atacado.
 * [L] Respeita o contrato: não fortalece pré-condições e nunca retorna null.
 */
public final class DescontoAtacadoStrategy implements RegraDesconto {
    private static final int QUANTIDADE_CORTE = 10;
    private static final BigDecimal FATOR_DESCONTO = new BigDecimal("0.10"); // 10%

    @Override
    public BigDecimal calcular(ItemCobranca item) {
        if (item.quantidade() >= QUANTIDADE_CORTE) {
            return item.valorBase()
                       .multiply(BigDecimal.valueOf(item.quantidade()))
                       .multiply(FATOR_DESCONTO);
        }
        return BigDecimal.ZERO;
    }
}

/**
 * [D] Output Port: a abstração é modelada pelas necessidades do negócio e pertence a ele.
 */
public interface GatewayPagamento {
    void processarTransacao(ItemCobranca item, BigDecimal valorFinal, ModalidadePagamento modalidade);
}

/**
 * [S] Orquestrador do fluxo de faturamento.
 * [D] Depende exclusivamente de abstrações injetadas via construtor.
 */
public final class ProcessadorFaturamento {
    private final List<RegraDesconto> regrasDesconto;
    private final GatewayPagamento gatewayPagamento;

    public ProcessadorFaturamento(List<RegraDesconto> regrasDesconto, GatewayPagamento gatewayPagamento) {
        this.regrasDesconto = List.copyOf(Objects.requireNonNull(regrasDesconto));
        this.gatewayPagamento = Objects.requireNonNull(gatewayPagamento);
    }

    public BigDecimal liquidar(ItemCobranca item, ModalidadePagamento modalidade) {
        BigDecimal totalBruto = item.valorBase().multiply(BigDecimal.valueOf(item.quantidade()));

        // [O] Aplicação declarativa e polimórfica das regras de desconto
        BigDecimal totalDescontos = regrasDesconto.stream()
            .map(regra -> regra.calcular(item))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal valorLiquido = totalBruto.subtract(totalDescontos).max(BigDecimal.ZERO);

        // [O] e [L] Pattern matching exaustivo no Java 25 com desconstrução de Records
        BigDecimal taxaProcessamento = switch (modalidade) {
            case ModalidadePagamento.Pix(_, boolean instantaneo) -> 
                instantaneo ? BigDecimal.ZERO : new BigDecimal("0.50");
            case ModalidadePagamento.CartaoCredito(_, int parcelas) when parcelas > 1 -> 
                valorLiquido.multiply(new BigDecimal("0.03"));
            case ModalidadePagamento.CartaoCredito _ -> 
                valorLiquido.multiply(new BigDecimal("0.015"));
            case ModalidadePagamento.Boleto _ -> 
                new BigDecimal("2.00");
        };

        BigDecimal valorFinal = valorLiquido.add(taxaProcessamento);
        gatewayPagamento.processarTransacao(item, valorFinal, modalidade);

        return valorFinal;
    }
}
```

---

## 6. Checklist de Qualidade SOLID para o Agente IA

Antes de submeter código ou considerar uma tarefa concluída, valide:

1. [ ] **Coerência de Ator (SRP):** A classe agrupa apenas dados e comportamentos que atendem ao mesmo ator e mudam sob as mesmas motivações de negócio?
2. [ ] **Contratos e Subtipagem (LSP):** As implementações cumprem todas as garantias da interface? Nenhuma implementação lança exceções inesperadas ou enfraquece pós-condições (como retornar `null`)?
3. [ ] **Extensão Polimórfica e Exaustividade (OCP):**
   * Variações abertas utilizam estratégias, lambdas ou composição em vez de cadeias de condicionais?
   * Variações restritas utilizam `sealed interface` com `switch` exaustivo do Java 25 sem cláusulas `default` desnecessárias?
4. [ ] **Segregação Funcional (ISP):** Interfaces unitárias foram marcadas com `@FunctionalInterface`? Parâmetros ignorados em contratos herdados foram expressos com `_`?
5. [ ] **Propriedade da Abstração e Injeção (DIP):** As interfaces de fronteira pertencem ao módulo que as consome? As dependências são imutáveis (`final`) e fornecidas pelo construtor?
6. [ ] **Anti-Overengineering Check:** Não foram criadas interfaces artificiais sem polimorfismo comprovado (como o anti-pattern `*Impl`)?
