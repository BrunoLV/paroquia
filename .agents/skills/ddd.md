# Guia de Implementação Domain-Driven Design (DDD)

## Instruções para Agente IA de Desenvolvimento (Skill & Harness Java)

Este documento serve como diretriz técnica e arquitetural absoluta para a implementação de novas funcionalidades, refatorações e manutenção deste projeto Java. O agente deve seguir rigorosamente as definições conceituais e estruturais aqui descritas, baseando-se nos princípios de **Eric Evans**, **Vaughn Vernon** e **Martin Fowler**.

---

## 1. O Código como Documentação Viva do Domínio (*Living Documentation*)

O código-fonte deve atuar como a especificação formal do negócio. Desenvolvedores e agentes devem ser capazes de compreender as regras da organização lendo diretamente as classes Java.

### 1.1. Regra de Autoexplicação e Comentários de Domínio

* **Javadoc de Negócio Obrigatório:** Todas as Raízes de Agregado, Entidades, Value Objects, Domain Services, Domain Events e Specifications DEVEM conter blocos de Javadoc conceituais explicando o **porquê** da regra, o papel daquele artefato no negócio e o contexto da Linguagem Ubíqua.
* **Proibição de Comentários Técnicos Óbvios:** Comentários como `// Construtor da classe` ou `// Setter de data` são proibidos. Em contrapartida, anotações de regras de negócio são obrigatórias:
```java
/**
 * Representa a inscrição formal de um catequizando na Paróquia.
 *
 * REGRA DE NEGÓCIO (RN-CAT-02):
 * Um catequizando só pode ser enturmado se tiver a idade mínima de 8 anos
 * completos até o início do ciclo catequético e se houver consentimento
 * formal de ao menos um responsável legal registrado.
 */
public class InscricaoCatequese { ... }

```


* **Rastreabilidade de Regras:** Sempre que um método aplicar uma regra complexa ou cálculo, mencione o nome do conceito ou código de regra validado com os *Domain Experts*.

---

## 2. Diretrizes de Design Estratégico

Antes de codificar, o agente deve contextualizar a funcionalidade dentro do mapa estratégico da organização.

### 2.1. Linguagem Ubíqua (*Ubiquitous Language*)

* **Vocabulário Oficial:** O código deve falar o idioma do negócio sem ambiguidades. Nomes de classes, métodos, atributos, parâmetros e exceções devem refletir os termos exatos do domínio.
* Se um termo técnico colidir com o termo de negócio (ex.: `Client` vs. `Fiel`), priorize categoricamente o vocabulário do negócio (`Fiel`).

### 2.2. Categorização de Subdomínios

O agente deve identificar onde a funcionalidade se enquadra para dosar a complexidade da modelagem:

* **Core Domain (Domínio Central):** O diferencial competitivo e atividade-fim do sistema. Exige tolerância zero a modelos anêmicos, isolamento absoluto de infraestrutura e máxima riqueza de invariantes.
* **Supporting Subdomain (Subdomínio Auxiliar):** Funcionalidades complementares desenvolvidas sob medida, com modelagem orientada a processos específicos.
* **Generic Subdomain (Subdomínio Genérico):** Problemas padronizados de mercado (autenticação, envio de notificações, relatórios brutos). Favoreça soluções de prateleira ou implementações pragmáticas sem superengenharia.

### 2.3. Contextos Delimitados (*Bounded Contexts*) e Context Mapping

* Cada módulo ou microsserviço encapsula um único Contexto Delimitado com limites linguísticos explícitos. Não compartilhe classes de domínio entre contextos distintos.
* **Upstream / Downstream (U/D):** O agente deve identificar a direção de dependência entre contextos antes de modelar integrações.
* **Camada Anticorrupção (*Anti-Corruption Layer - ACL*):** Ao consumir contratos de contextos upstream, legados ou APIs externas, NUNCA utilize os DTOs ou entidades externas dentro do pacote `domain`. Crie adaptadores e tradutores na infraestrutura (`infrastructure/acl/`) para converter dados externos em conceitos nativos do domínio local.
* **Serviço Aberto / Linguagem Publicada (*Open Host Service / Published Language - OHS/PL*):** Ao expor capacidades para outros contextos, defina schemas de integração explícitos (APIs REST/Eventos versionados), sem vazar o modelo interno de domínio.

---

## 3. Design Tático: Modelagem Orientada a Objetos em Java

O pacote `domain` deve ser escrito em **Java puro**, livre de anotações ou dependências de frameworks (Spring, JPA, Hibernate, Jackson, etc.).

### 3.1. Identificadores Fortemente Tipados (*Strongly Typed IDs*)

* Combata a obsessão por primitivos. Todo Agregado e Entidade deve ter um identificador exclusivo encapsulado em um `record` imutável, prevenindo trocas acidentais de parâmetros.
```java
/**
 * Identificador único e fortemente tipado de um Fiel.
 */
public record FielId(UUID value) {
    public FielId {
        Objects.requireNonNull(value, "O identificador do Fiel não pode ser nulo.");
    }
    public static FielId gerarNovo() {
        return new FielId(UUID.randomUUID());
    }
}

```



### 3.2. Objetos de Valor (*Value Objects - VOs*)

* **Definidos por Atributos:** Não possuem identidade própria; dois VOs com o mesmo estado são equivalentes.
* **Imutabilidade e Validação no Construtor:** Use `record` do Java 17+. O construtor compacto deve conter a validação imediata de invariantes.
```java
/**
 * Representa o endereço canônico residencial do fiel.
 * Imutável: qualquer alteração de endereço resulta em uma nova instância.
 */
public record Endereco(String logradouro, String numero, Cep cep, Cidade cidade) {
    public Endereco {
        if (logradouro == null || logradouro.isBlank()) {
            throw new EnderecoInvalidoException("O logradouro é obrigatório.");
        }
    }
}

```



### 3.3. Entidades (*Entities*) e Raízes de Agregado (*Aggregate Roots*)

* **Identidade Estável:** Diferenciam-se por seu ID tipado, persistindo mesmo se seus atributos mudarem.
* **Mutabilidade Controlada (Métodos de Negócio):** Proibição absoluta de *setters* genéricos (`setStatus`, `setNome`). O estado só é alterado por métodos que descrevem eventos ou comandos de negócio (`registrarCrisma()`, `transferirComunidade()`).
* **Relacionamento Entre Agregados Exclusivamente por ID:** Um agregado NUNCA deve conter uma referência direta de objeto para outro agregado. Mantenha apenas o identificador tipado correspondente (ex.: `ParoquiaId paroquiaOrigemId`), garantindo baixo acoplamento e consistência modular.
* **Regra Fundamental de Transacionalidade:** Em uma única transação de banco de dados, modifique **apenas UM Agregado**. A consistência entre múltiplos agregados deve ser orquestrada de forma **eventual**.

### 3.4. Fábricas do Domínio (*Domain Factories*)

* Agregados complexos cuja criação envolve validações de estado inicial ou montagem de grafos de objetos internos devem usar métodos de fábrica estáticos expressivos ou classes `Factory` dedicadas:
```java
public static Fiel matricularNovoFiel(DadosPessoais dados, ParoquiaId paroquiaOrigem) {
    // Validações de criação e emissão do evento de boas-vindas
}

```



### 3.5. Especificações de Negócio (*Specification Pattern*)

* Regras de validação ou critérios de elegibilidade complexos e reutilizáveis devem ser isolados em classes de especificação, desacoplando a lógica da entidade:
```java
/**
 * Avalia se um fiel cumpre todos os requisitos canônicos para assumir a função de padrinho.
 */
public class PadrinhoElegivelSpecification implements DomainSpecification<Fiel> {
    @Override
    public boolean isSatisfiedBy(Fiel fiel) {
        return fiel.isBatizado() && fiel.isCrismado() && !fiel.possuiImpedimentoCanonico();
    }
}

```



### 3.6. Serviços de Domínio (*Domain Services*)

* Operações de negócio complexas que envolvem múltiplos agregados ou dependem de regras que não pertencem conceitualmente a uma única entidade pertencem a um *Domain Service*.
* Devem ser *stateless* e localizados em `domain/service/`.

### 3.6.1. Policies versus Services

* **Variação é Policy:** regras de cálculo, autorização, seleção ou decisão que variam por tipo, perfil, campanha ou contexto são contratos `*Policy` (Strategy), localizados em `domain/policy/` ou `domain/<aggregate>/policy/`; é proibido escondê-las em cadeias `if/else` ou `switch/case` de `*Service`.
* **Policy pura:** uma Policy é stateless e zero-I/O. Não recebe nem injeta repositórios, clientes HTTP, cache, DTO REST, entidades JPA, `SecurityContext` ou anotações de framework. O Application Service busca os fatos necessários e entrega agregados, Value Objects ou snapshots imutáveis em memória.
* **Tell, Don't Ask:** a Policy calcula e devolve uma decisão ou Value Object. Ela nunca altera uma entidade/agregado por setter; a mutação pertence ao método semântico do agregado, como `aplicarRegra(policy)`.
* **Uso legítimo de Service:** Application Services orquestram portas, transações, mensageria e I/O. Domain Services coordenam conceitos entre agregados sem variação algorítmica. Nenhum dos dois substitui uma Strategy de domínio.

### 3.7. Eventos de Domínio (*Domain Events*)

* Representam fatos consumados relevantes para o negócio (`BatismoCelebrado`, `FielInativado`).
* **Padrão de Coleta Interna:** A Raiz do Agregado armazena internamente os eventos gerados durante a execução dos métodos de negócio. A camada de aplicação extrai e despacha esses eventos após a persistência bem-sucedida da transação:
```java
public abstract class AggregateRoot<ID> {
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    protected void registerEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = List.copyOf(this.domainEvents);
        this.domainEvents.clear();
        return events;
    }
}

```



### 3.8. Coordenação de Múltiplos Agregados (*Process Managers / Sagas*)

* Processos de longa duração ou que exigem alterações coordenadas em mais de um agregado pertencem à camada de aplicação via *Process Managers* ou *Sagas*. Eles escutam eventos de domínio, disparam novos comandos sequenciais e executam compensações em caso de falhas.

---

## 4. Estrutura de Pacotes (Clean / Hexagonal)

A organização do código Java deve respeitar o isolamento absoluto do domínio:

```
com.empresa.projeto.modulo
│
├── domain/                               <-- CAMADA PURA DE NEGÓCIO (Zero dependências externas)
│   ├── model/                            <-- Pacotes agrupados por subdomínios coesos
│   │   ├── fiel/                         <-- Agregado Fiel (Fiel, FielId, Documentos, etc.)
│   │   └── sacramento/                   <-- Agregado Sacramento (Batismo, Matrimonio, etc.)
│   ├── repository/                       <-- Portas de saída: Interfaces de repositórios (contratos puros)
│   ├── service/                          <-- Domain Services (regras que cruzam entidades)
│   ├── specification/                    <-- Predicados de regras reutilizáveis
│   ├── event/                            <-- Contratos de Domain Events (records imutáveis)
│   └── exception/                        <-- Exceções de regras de negócio (DomainException)
│
├── application/                          <-- ORQUESTRAÇÃO DE FLUXOS E CASOS DE USO
│   ├── usecase/                          <-- Serviços de Aplicação / Input Ports
│   ├── dto/                              <-- Command, Query e Result DTOs
│   └── saga/                             <-- Process Managers para orquestrar múltiplos agregados
│
└── infrastructure/                       <-- TECNOLOGIA, DETALHES E INTEGRAÇÕES
    ├── configuration/                    <-- Spring @Configuration (registra Domain Services como Beans)
    ├── db/                               <-- Persistência de Dados
    │   ├── entity/                       <-- Modelos JPA (@Entity, @Table) isolados do domínio
    │   ├── repository/                   <-- Spring Data JPA Repositories
    │   └── mapper/                       <-- Mapeadores Bidirecionais (Domain Model <-> JPA Entity)
    ├── acl/                              <-- Anti-Corruption Layer (Adaptadores para APIs externas)
    ├── messaging/                        <-- Dispatchers e Listeners (Kafka, RabbitMQ, Spring Events)
    └── rest/                             <-- Controllers HTTP (@RestController, OpenAPI)

```

---

## 5. Mapeamento de Persistência e Integração com Frameworks

1. **Separação Rígida entre Modelo de Domínio e Modelo de Tabela (JPA):**
* O modelo de domínio (`domain/model/`) NÃO deve ter anotações `@Entity`, `@Id` ou `@Column`.
* A persistência deve definir entidades JPA dedicadas em `infrastructure/db/entity/`.
* O adaptador do repositório em `infrastructure/db/` usa mappers (manuais ou MapStruct) para carregar o modelo de domínio puro e salvar o estado no modelo relacional.


2. **Injeção de Dependências de Classes de Domínio:**
* Nunca anote classes de `domain/` com `@Service` ou `@Component`.
* Crie uma classe de configuração na infraestrutura para instanciá-las:
```java
@Configuration
public class DomainConfiguration {
    @Bean
    public MatricularFielDomainService matricularFielDomainService(FielRepository fielRepository) {
        return new MatricularFielDomainService(fielRepository);
    }
}

```





---

## 6. Checklist de Qualidade DDD para o Agente IA

Antes de concluir qualquer tarefa ou PR, verifique rigorosamente:

1. [ ] **Documentação Viva:** Todos os agregados, VOs e métodos contêm Javadoc explicando a regra de negócio e a terminologia ubíqua correspondente?
2. [ ] **Isolamento de Frameworks:** O pacote `domain` está 100% livre de bibliotecas externas (Spring, JPA, Hibernate)?
3. [ ] **Encapsulamento e Imutabilidade:** As entidades usam métodos semânticos em vez de `setters` públicos? Os Value Objects e IDs foram implementados com `record`?
4. [ ] **Identidade Tipada:** Os agregados utilizam IDs fortemente tipados (`record EntidadeId`) em vez de `UUID`/`Long` primitivos?
5. [ ] **Limite de Agregados:** Os relacionamentos entre agregados distintos usam apenas seus respectivos IDs, sem carregar instâncias de objetos diretamente?
6. [ ] **Transação Única:** A mutação de estado se limita a no máximo um único agregado por transação no Use Case?
7. [ ] **Anti-Corruption Layer (ACL):** Dados de sistemas externos ou outros bounded contexts são traduzidos antes de atingir o domínio?
8. [ ] **Desacoplamento de Eventos:** Os eventos de domínio são instanciados na camada de domínio e despachados na camada de aplicação após a persistência?
