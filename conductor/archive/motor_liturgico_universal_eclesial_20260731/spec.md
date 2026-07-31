# Specification: Motor Litúrgico Universal (`LiturgicalEngine`), Provisionamento, Precedência e Governança (Backend & UI)

## Overview
Esta especificação consolida a arquitetura litúrgica universal da aplicação paroquial, cobrindo:
1. **O Motor Litúrgico Universal (`LiturgicalEngine`):** Componente de cálculo puro (stateless) usando Java 25 Records que calcula o Ano Litúrgico nominal, datas limites, Ciclo de Leituras Dominicais (`anoNominal % 3 == 1` -> A, `2` -> B, `0` -> C), marcos/solenidades móveis (Offsets da Páscoa Meeus/Jones/Butcher) e o catálogo de Solenidades Fixas com cores e fatores de demanda.
2. **Provisionamento e Ciclos Instanciados (`LiturgicalProvisioningService`):** Persistência das datas calculadas na tabela de Anos Litúrgicos e Solenidades com flag `origem = 'AUTOMATICO'`.
3. **Regra de Precedência Litúrgica (Resolução Diária na UI/API):** Algoritmo de resolução de conflito por peso de precedência:
   - 1. Tríduo Pascal (Peso 1)
   - 2. Solenidades Instanciadas (Peso 2)
   - 3. Tempos Fortes: Quaresma e Advento (Peso 3)
   - 4. Tempos Festivos: Natal e Páscoa (Peso 4)
   - 5. Tempo Comum (Peso 5)
4. **Edição Manual, Imutabilidade Eclesial e Controle de Acesso Estrito:**
   - **Permissão Exclusiva:** Ajustes manuais e edições nos cadastros de Marcos, Anos e Ciclos Litúrgicos só podem ser realizados por usuários com perfil `PAROCO` ou `VIGARIO`. Demais perfis recebem `ForbiddenOperationException`.
   - **Imutabilidade Eclesial:** Lançamento de `ImmutableEclesialRuleException` ao tentar alterar `cicloLetra` ou `Domingo de Páscoa`.
   - **Auditoria:** Atualização com `origem = 'AJUSTADO_MANUAL'` e log com `justificativa_pastoral`.
5. **Alinhamento do Frontend UI (`Calendar.tsx` e `liturgicalCalendar.ts`):** Aplicação estrita da fórmula do ciclo e resolução de cores/rótulos seguindo a mesma hierarquia de precedência litúrgica.

## Functional Requirements

### 1. Motor Litúrgico Universal (`LiturgicalEngine`)
- Imutável e orientado a Java 25 Records: `LiturgicalOutput` record e `MarcoLiturgico` record.
- **Cálculo da Páscoa (Meeus/Jones/Butcher)** e marcos móveis: Quarta-feira de Cinzas (-46d), Quarta Santa (-4d), Quinta Santa (-3d), Sábado Santo (-1d), Ascensão (+42d), Pentecostes (+49d), Santíssima Trindade (+56d), Corpus Christi (+60d), Sagrado Coração (+68d), Cristo Rei (Domingo antes do Advento) e Epifania (janela 2-8 Jan).
- **Catálogo de Solenidades Fixas:** Santa Maria Mãe de Deus (01/01), São José (19/03), Anunciação (25/03), Natividade S. João (24/06), S. Pedro e S. Paulo (29/06, VERMELHO), Assunção (15/08), Todos os Santos (01/11), Imaculada Conceição (08/12), Natal (25/12).
- **Ciclo de Leituras Dominicais:** `1 -> A`, `2 -> B`, `0 -> C`.

### 2. Provisionamento Anual (`LiturgicalProvisioningService`)
- Persistir Ano Litúrgico e registros de Solenidades (Fixas e Móveis) com tipo `SOLENIDADE` e `origem = 'AUTOMATICO'`.

### 3. Regra de Precedência Diária (API & UI)
- Ao resolver a cor/tempo litúrgico de uma data, ordenar conflitos pelo menor `peso_precedencia` (1 a 5).

### 4. Edição Manual e Governança Eclesial
- Criar e lançar `ImmutableEclesialRuleException` se houver tentativa de alterar `cicloLetra` ou data do `Domingo de Páscoa`.
- Controle de acesso estrito permitindo edição apenas para `PAROCO` ou `VIGARIO`.
- Definir `origem = 'AJUSTADO_MANUAL'` e registrar log de auditoria com `justificativa_pastoral`.

### 5. Sincronização com o Frontend UI
- Atualizar `liturgicalCalendar.ts` e `Calendar.tsx` para aplicar a fórmula universal do ciclo e a regra de precedência visual.

## Non-Functional Requirements
- **Imutabilidade e Pureza:** Componentes lógicos stateless.
- **Governança de Código:** Sem wildcard imports e sem inline FQNs (`AGENTS.md`).

## Acceptance Criteria
- [ ] `LiturgicalEngine` calcula com precisão 100% dos limites, ciclo e marcos móveis/fixos.
- [ ] A regra universal do ciclo (`1 -> A, 2 -> B, 0 -> C`) é testada e aprovada no backend e frontend.
- [ ] O `LiturgicalProvisioningService` persiste solenidades automáticas e respeita a hierarquia de precedência (1 a 5).
- [ ] Ajustes manuais nos cadastros litúrgicos são permitidos EXCLUSIVAMENTE para `PAROCO` ou `VIGARIO` (outros perfis recebem exceção de segurança).
- [ ] Tentativas de edição manual em campos imutáveis disparam `ImmutableEclesialRuleException`.
- [ ] A UI reflete a mesma tabela de precedência e o ciclo correto.
- [ ] Todos os testes unitários e de integração passam com 100% de sucesso.

## Out of Scope
- Alteração no esquema de autenticação OAuth2 / JWT (reaproveita as roles existentes).
