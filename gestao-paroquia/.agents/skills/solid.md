# Skill do Agente de IA: Arquitetura de Código Java (Especialista em SOLID)

## 1. Visão Geral e Contexto

Esta skill capacita o Agente de IA a gerar, refatorar e avaliar código de produção em Java seguindo estritamente os princípios **SOLID**. O objetivo principal é garantir que o agente produza código com baixo acoplamento, alta coesão, extensível e de fácil manutenção, eliminando designs rígidos ou classes monolíticas.

## 2. Diretrizes dos Princípios SOLID

O Agente deve aplicar os cinco princípios fundamentais de forma cirúrgica durante a geração ou refatoração do código:

* **S - Single Responsibility Principle (Princípio da Responsabilidade Única):**
* Cada classe deve ter uma, e apenas uma, razão para mudar.
* *Ação:* Separe regras de negócio, persistência, validação e notificações em classes distintas.


* **O - Open/Closed Principle (Princípio Aberto/Fechado):**
* Classes e módulos devem estar abertos para extensão, mas fechados para modificação.
* *Ação:* Utilize interfaces ou classes abstratas. Comportamentos novos ou variantes devem ser adicionados por meio de novas implementações, nunca alterando o código existente.


* **L - Liskov Substitution Principle (Princípio da Substituição de Liskov):**
* As subclasses/implementações devem ser perfeitamente substituíveis por suas classes pai/interfaces sem quebrar o comportamento esperado.
* *Ação:* Evite sobrescrever métodos lançando `UnsupportedOperationException` ou alterando drasticamente a assinatura semântica do contrato.


* **I - Interface Segregation Principle (Princípio da Segregação de Interfaces):**
* Uma classe não deve ser forçada a depender de métodos que ela não utiliza.
* *Ação:* Crie interfaces pequenas, específicas e focadas em papéis únicos, em vez de interfaces gerais e extensas.


* **D - Dependency Inversion Principle (Princípio da Inversão de Dependência):**
* Módulos de alto nível não devem depender de módulos de baixo nível; ambos devem depender de abstrações.
* *Ação:* Dependa sempre de interfaces, nunca de classes concretas. Injete as dependências estritamente através do construtor da classe.



## 3. Diretrizes de Escrita e Sintaxe Java

* **Java Moderno:** Utilize recursos do Java 17+ (como Records para estruturas de dados imutáveis, correspondência de padrões e blocos de texto) para reduzir o código boilerplate.
* **Imutabilidade:** Priorize atributos `final` e injeção via construtor para garantir a segurança de threads e consistência do estado do objeto.

## 4. Blueprint Estrutural (Exemplo de Implementação SOLID)

O agente deve estruturar o código dividindo contratos, especializações e o orquestrador de fluxo de forma desacoplada:

```java
// [I] e [O] - Interface enxuta e aberta para novas extensões
public interface RegraValidacaoSenha {
    boolean verificar(String senha);
}

// [S] - Responsabilidade única: validar apenas o tamanho
public class RegraTamanhoMinimo implements RegraValidacaoSenha {
    private static final int TAMANHO_MINIMO = 8;
    
    @Override
    public boolean verificar(String senha) {
        return senha != null && senha.length() >= TAMANHO_MINIMO;
    }
}

// [S] - Responsabilidade única: validar apenas a presença de números
public class RegraContemNumero implements RegraValidacaoSenha {
    @Override
    public boolean verificar(String senha) {
        return senha != null && senha.matches(".*\\d.*");
    }
}

// [D] - Classe de alto nível depende exclusivamente de abstrações
public class ValidadorSenha {
    private final List<RegraValidacaoSenha> regras;

    // Inversão de dependência via construtor
    public ValidadorSenha(List<RegraValidacaoSenha> regras) {
        this.regras = List.copyOf(regras);
    }

    public boolean ehValida(String senha) {
        // Processamento coeso utilizando Streams do Java
        return regras.stream().allMatch(regra -> regra.verificar(senha));
    }
}

```

## 5. Checklist de Revisão de Código (Anti-Patterns a Evitar)

Antes de finalizar qualquer entrega, o Agente deve validar o código gerado contra os seguintes desvios:

* **God Classes / Classes Monolíticas:** A classe gerada faz validação, formatação e salvamento no banco de dados ao mesmo tempo? (Se sim, quebra o **S**; divida-a).
* **Instanciação Direta (`new`):** A classe principal está instanciando suas dependências internas usando a palavra-chave `new`? (Se sim, quebra o **D**; altere para injeção via construtor).
* **Acoplamento por Condicionais:** A inclusão de uma nova regra de negócio exige a adição de um novo bloco `if/else` ou `switch` no fluxo principal? (Se sim, quebra o **O**; utilize polimorfismo/interfaces).
* **Contratos Violados:** Uma implementação da interface falha silenciosamente ou lança exceções inesperadas para burlar o contrato? (Se sim, quebra o **L**; adeque o design).