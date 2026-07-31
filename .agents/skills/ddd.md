# Guia de Implementação Domain-Driven Design (DDD)
## Instruções para Agente IA de Desenvolvimento (Skill & Harness Java)

Este documento serve como diretriz técnica absoluta para a implementação de novas funcionalidades, refatorações e manutenção do código deste projeto Java. O agente deve seguir rigorosamente as definições conceituais e estruturais aqui descritas, baseando-se nos princípios de **Eric Evans**, **Vaughn Vernon** e **Martin Fowler**.

---

## 1. Diretrizes de Design Estratégico

Antes de codificar qualquer linha, a inteligência artificial deve contextualizar a funcionalidade dentro do design estratégico do ecossistema.

### 1.1. Linguagem Ubíqua (Ubiquitous Language)
* **Regra Ouro:** O código deve falar o idioma do negócio. Nomes de classes, métodos, variáveis e tabelas do banco de dados devem refletir os termos exatos validados pelos especialistas do domínio (*Domain Experts*).
* **Ação da IA:** Se o termo técnico diferir do termo do negócio (ex: usar `Client` quando o negócio chama de `Subscriber`), a IA deve pausar ou priorizar estritamente o termo do negócio (`Subscriber`).

### 1.2. Contextos Delimitados (Bounded Contexts)
* Cada microsserviço ou módulo encapsula um único Contexto Delimitado.
* **Isolamento de Modelos:** O mesmo conceito do mundo real pode ter representações diferentes em contextos diferentes.
    * *Exemplo:* Um `Produto` no contexto de *Catálogo* possui descrição e fotos; no contexto de *Faturamento*, possui apenas ID, preço e regras fiscais.
* **Ação da IA:** Não reutilize classes de domínio entre contextos distintos. Mantenha modelos separados e utilize estratégias de integração (como *Anticorruption Layers*) se necessário.

---

## 2. Design Tático: A Arquitetura do Domínio em Java

O código deve seguir a divisão clássica do DDD técnico, isolando completamente a complexidade do negócio das tecnologias de infraestrutura (Spring, JPA, AWS, etc.).

### 2.1. Entidades (Entities)
* **Identidade Única:** Possuem uma identidade estável que persiste ao longo do tempo, independentemente de mudanças em seus atributos.
* **Mutabilidade Controlada:** Modificações de estado devem ocorrer por meio de métodos de negócio explícitos (ex: `ativarAssinatura()`), e **nunca** por *setters* genéricos (`setStatus()`).
* **Implementação Java:**
    * Utilizar IDs encapsulados (ex: `class PassaporteId`) ou `UUID`.
    * Validar a consistência interna no próprio construtor ou método de mutação.

### 2.2. Objetos de Valor (Value Objects - VOs)
* **Definidos por Atributos:** Não possuem identidade. Dois VOs são iguais se todos os seus atributos forem iguais.
* **Imutabilidade Estrita:** Uma vez criados, seu estado jamais muda. Modificações geram uma nova instância.
* **Implementação Java:** Use `record` do Java 16+ para garantir imutabilidade natural.
    * *Exemplos:* `Endereço`, `Monetário`, `CPF`, `Email`.

### 2.3. Agregados e Raízes de Agregado (Aggregates & Aggregate Roots)
* **Unidade de Consistência:** Um grupo de entidades e VOs associados que são tratados como uma única unidade para mudanças de dados.
* **Acesso Controlado:** O mundo externo só pode interagir com o agregado através da **Raiz do Agregado** (*Aggregate Root*). Entidades internas não podem ser manipuladas diretamente de fora.
* **Transacionalidade:** Modificações em um agregado devem ser salvas em uma única transação de banco de dados.

### 2.4. Serviços de Domínio (Domain Services)
* Quando uma operação ou lógica de negócio importante não pertence naturalmente a uma Entidade ou VO específico, ela deve ser encapsulada em um *Domain Service*.
* **Sem Estado:** Devem ser *stateless* e focar puramente em regras de negócio complexas que envolvem múltiplos agregados.

### 2.5. Eventos de Domínio (Domain Events)
* Notificações sobre algo relevante que aconteceu no domínio (ex: `PedidoCriado`).
* **Ação da IA:** Use para desacoplar contextos ou agregados de forma assíncrona.

---

## 3. Estrutura de Pacotes (Arquitetura Limpa / Hexagonal)

Para garantir o isolamento do domínio, a IA deve organizar o código Java conforme o padrão abaixo:

```
com.empresa.projeto.modulo
│
├── domain/                      <-- PURAMENTE NEGÓCIO (Sem dependência de Spring/JPA)
│   ├── model/                   <-- Entidades, Agregados, Value Objects
│   ├── repository/              <-- Interfaces dos Repositories (Apenas contratos)
│   ├── service/                 <-- Domain Services (Regras complexas)
│   └── exception/               <-- Exceções de negócio
│
├── application/                 <-- ORQUESTRAÇÃO E FLUXO
│   ├── usecase/                 <-- Casos de uso / Serviços de Aplicação
│   └── dto/                     <-- Data Transfer Objects (Request/Response)
│
└── infrastructure/              <-- TECNOLOGIA E MUNDO EXTERNO
    ├── configuration/           <-- Beans do Spring, Seguridade
    ├── db/                      <-- Entidades JPA (@Entity), Repositories Spring Data
    ├── messaging/               <-- Consumidores/Produtores Kafka, RabbitMQ
    └── rest/                    <-- Controllers REST (@RestController)
```

---

## 4. Checklist para a IA antes de submeter código

Antes de considerar uma tarefa concluída, o agente IA deve validar os seguintes pontos:

1. [ ] **Zero vazamento de Infra:** As classes dentro do pacote `domain` possuem anotações como `@Entity`, `@Table`, `@Column` ou `@Table` do Spring Data? *Se sim, refatore. O domínio deve usar Java Puro.*
2. [ ] **Encapsulamento:** Existem métodos `set` públicos nas entidades? *Se sim, remova-os e substitua por métodos que expressem comportamento de negócio.*
3. [ ] **Validação de Invariantes:** O modelo permite a criação de um objeto inválido (ex: `Preco` negativo)? *Garanta que o construtor jogue uma exceção de domínio se os dados estiverem inconsistentes.*
4. [ ] **Interface de Repositório:** A interface do repositório está no pacote `domain/repository` e sua implementação concreta em `infrastructure/db`? *O domínio define a necessidade; a infraestrutura resolve a tecnologia.*

---

## 5. Referências Teóricas Incorporadas
* **Eric Evans:** *Domain-Driven Design: Tackling Complexity in the Heart of Software* (Conceitos fundamentais de Linguagem Ubíqua e Design Tático).
* **Vaughn Vernon:** *Implementing Domain-Driven Design* (Padrões práticos de Agregados e arquitetura com foco em código).
* **Martin Fowler:** *Anemic Domain Model* (Combate ao modelo anêmico; proteção da lógica dentro das entidades).