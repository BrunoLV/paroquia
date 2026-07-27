# Skill do Agente de IA: Cobertura Rigorosa de Especificação e Cenários no TDD

## 1. Visão Geral e Contexto
Esta skill atua como uma diretriz de endurecimento (*hardening*) de comportamento para o Agente de IA durante o fluxo de Desenvolvimento Orientado a Testes (TDD). O objetivo central é **eliminar lacunas de teste**, forçando o agente a mapear explicitamente a especificação funcional fornecida e garantir que 100% dos cenários de código implementados possuam testes equivalentes criados *antes* ou *junto* com a implementação.

## 2. Regra de Ouro do Endurecimento (Hardening Constraint)
O Agente está **estritamente proibido** de:
1.  Escrever ou propor qualquer linha de código de produção que não tenha sido motivada por um critério de aceitação ou cenário da especificação.
2.  Deixar cenários descritos na especificação sem uma respectiva asserção automatizada.
3.  Implementar fluxos alternativos, "atalhos" técnicos ou otimizações de contorno no código de produção sem antes mapear e escrever o teste de falha correspondente para aquele desvio.

## 3. Fluxo de Pareamento: Especificação ↔ Teste ↔ Código
Para cada tarefa de desenvolvimento, o Agente deve obrigatoriamente seguir a matriz de rastreabilidade abaixo:


```

[Item da Especificação / Critério de Aceitação]
│
▼
[Cenário de Teste Unitário Automático (RED)]
│
▼
[Código Mínimo de Produção Gerado (GREEN)]

```

*   **Mapeamento de Requisitos:** Antes de gerar código, o agente deve extrair e listar todos os comportamentos esperados (Caminho Feliz, Caminho Alternativo e Regras de Bloqueio) da especificação.
*   **Contrato Fechado:** O teste deve validar exatamente o comportamento descrito na especificação. Se a especificação exige "Retornar erro 422 quando saldo for insuficiente", o teste deve asserir especificamente o tipo de erro/exceção e o cenário de saldo.

## 4. Estrutura de Teste Pautada na Especificação (Blueprint BDD/AAA)
Os testes gerados devem usar `@DisplayName` descritivos que espelhem os critérios de aceitação da especificação técnica, facilitando a auditoria do harness.

```java
@Test
@DisplayName("Especificação REQ-01: Deve rejeitar transferência quando o saldo da conta for menor que o valor solicitado")
void deveRejeitarTransferenciaQuandoSaldoForInsuficiente() {
    // 1. Arrange (Baseado no cenário da especificação)
    Conta contaOrigem = new Conta(BigDecimal.valueOf(50.00)); // Saldo insuficiente
    Conta contaDestino = new Conta(BigDecimal.valueOf(10.00));
    BigDecimal valorTransferencia = BigDecimal.valueOf(100.00);

    // 2. Act
    Executable acao = () -> servico.transferir(contaOrigem, contaDestino, valorTransferencia);

    // 3. Assert (Garante que a regra de negócio exata foi violada)
    SaldoInsuficienteException excecao = assertThrows(SaldoInsuficienteException.class, acao,
        "Deveria lançar SaldoInsuficienteException conforme especificado na regra REQ-01.");
    
    assertEquals("Saldo disponível (50.00) é menor que o valor da transferência (100.00).", excecao.getMessage());
}

```

## 5. Checklist Mandatório de Validação de Escopo

O Agente deve auto-avaliar sua resposta com o seguinte checklist antes de exibir a saída:

* **Rastreabilidade:** Cada método de produção gerado aponta para qual caso de teste? E cada caso de teste aponta para qual item da especificação?
* **Cenários Ocultos:** Se foi adicionado um bloco `if (param == null)` ou um `catch` no código de produção, existe um teste específico cobrindo o comportamento desse `if` ou `catch`? *(Se não houver, o código de produção está violando o endurecimento do TDD e deve ser removido ou testado)*.
* **Validação de Asserção:** Os asserts estão validando o efeito colateral real esperado pela especificação (ex: mutação de estado, chamada de mock de contrato, retorno específico) ou apenas cobrindo a linha de código passivamente? *(Exija asserções estritas de valor e comportamento)*.

```
