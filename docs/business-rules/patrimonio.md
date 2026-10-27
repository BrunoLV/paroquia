# Regras de Negócio e Fluxos: Módulo Patrimônio (`patrimonio`)

O módulo de Patrimônio é responsável pela catalogação, tombamento, manutenção e desincorporação dos bens móveis da Paróquia, e pela sua disponibilidade para reserva.

---

## 1. Visão Geral e Propósito
Prover inventário fidedigno dos bens móveis (equipamentos eletrônicos, mobiliário, paramentos e itens de infraestrutura), com identificação única, posse fixa numa unidade territorial e controle da indisponibilidade por manutenção. O bem só é emprestado por **reserva** (módulo `reserva`); o patrimônio apenas reflete no bem o estado resultante.

---

## 2. Agregados, Entidades e Value Objects Principais
- **`AtivoPatrimonial` (Raiz de Agregado):** identificado por `AtivoPatrimonialId`. Possui nome, código de tombamento, descrição, categoria, data e valor de aquisição, a organização proprietária, a elegibilidade a reserva (`reservavel`), o status, quem o cadastrou e as datas de criação, atualização e exclusão lógica. Após o cadastro **só a descrição muda**.
- **`AtivoManutencao` (Raiz de Agregado):** período de indisponibilidade técnica de um ativo (conserto, revisão ou reparo), com descrição do reparo, previsão de retorno e, ao concluir, a data real de fim. Associa-se ao ativo por `AtivoPatrimonialId`; enquanto não concluída, está **em aberto**.
- **`CodigoTombamento` (Value Object):** código no padrão sequencial anual `PAT-AAAA-NNNN`, gerado pelo sistema.
- **`PeriodoManutencao` (Value Object):** início da indisponibilidade e previsão de retorno, com `fimPrevista` posterior ao início.
- **`ReservaImpeditiva` (projeção):** reserva ativa que impede enviar o ativo para manutenção ou excluí-lo; é traduzida do módulo `reserva` por camada anticorrupção.
- **`AtivoCategoria` (Enum):** `ELETRONICO`, `MOBILIARIO`, `PARAMENTO`, `INFRAESTRUTURA`. Apresentada na interface como **tipo** do bem, definida no cadastro e imutável.
- **`AtivoStatus` (Enum):** `DISPONIVEL`, `RESERVADO`, `EM_MANUTENCAO`.
- **`TipoNotaAtivoPatrimonial` (Enum):** `ALOCACAO` e `DEVOLUCAO`, notas de observação do bem.

---

## 3. Regras de Negócio Invariantes

### RN-PAT-01: Tombamento Único, Gerado pelo Sistema e Imutável
- O código de tombamento é gerado pelo sistema no cadastro, em sequência anual (`PAT-2026-0001`), é único em toda a paróquia e **nunca é digitado nem alterado**. Um código fora do padrão não é aceito por nenhum caminho de persistência.

### RN-PAT-02: Imutabilidade Estrutural e Posse Fixa
- Depois do cadastro, só a **descrição** pode ser alterada. Nome, código, categoria, datas, valor, organização proprietária e elegibilidade a reserva são fixos.
- A posse é fixa: **não há transferência** de bens entre comunidades. Só a Matriz e as Comunidades podem possuir ativos (a separação patrimonial paroquial); uma pastoral, movimento ou conselho nunca é proprietário.
- O valor de aquisição não pode ser negativo.

### RN-PAT-03: Status do Bem
- O bem nasce `DISPONIVEL`. `EM_MANUTENCAO` tem **precedência** sobre `RESERVADO`: enquanto o bem está em manutenção, a existência de reserva não altera o status.
- `RESERVADO` e `DISPONIVEL` refletem as reservas ativas do bem. A sincronização acontece fora do agregado, por evento a cada mudança de reserva e por rotina diária de conferência.
- Um bem excluído não muda de status.

### RN-PAT-04: Manutenção Bloqueia a Agenda
- Só um bem `DISPONIVEL` é enviado para manutenção, por período determinado. O envio é **recusado** se houver reservas pendentes ou confirmadas no intervalo (que precisam ser canceladas antes) ou se já houver manutenção em aberto.
- Enquanto a manutenção está em aberto, a janela bloqueia novas reservas do bem e a consulta de disponibilidade a considera.
- O envio para manutenção e a criação de reservas do mesmo bem são serializados pelo bloqueio da linha do bem: uma reserva não é criada sobre um período em que a manutenção está sendo registrada, e vice-versa. O envio grava o histórico de manutenção e o status do bem de forma atômica, para que `EM_MANUTENCAO` nunca fique ausente de uma manutenção em aberto.
- Só um bem marcado como reservável, e não excluído, pode ser reservado (ver `reserva.md`).
- Concluir a manutenção registra a data real de fim (não anterior ao início), devolve o bem a `DISPONIVEL` e dispara o recálculo de `RESERVADO`.

### RN-PAT-05: Exclusão Lógica (Desincorporação)
- O bem nunca é apagado: a exclusão é lógica. É **recusada** se houver manutenção em aberto ou reservas futuras pendentes ou confirmadas.
- O parecer do Conselho de Assuntos Econômicos e a licença do Bispo Diocesano para a alienação de bens litúrgicos (Cân. 537) ocorrem fora do sistema; este registra apenas a desincorporação.

### RN-PAT-06: Alçada de Gestão
- Gerem ativos o Pároco, o Bispo Diocesano, a Secretaria Paroquial e os membros do Conselho de Assuntos Econômicos, que têm visão de **toda** a paróquia, e os coordenadores, restritos à **sua** unidade territorial. Os demais perfis não gerem.

---

## 4. Fluxos Operacionais e Casos de Uso
1. **Cadastro e Tombamento:** o gestor cadastra o bem numa unidade territorial proprietária; o sistema gera o código e define o status `DISPONIVEL`.
2. **Edição da Descrição:** único campo editável do bem.
3. **Manutenção:** o gestor envia o bem para manutenção (com checagem prévia de reservas conflitantes) e depois a conclui. O histórico de manutenções do bem fica disponível.
4. **Exclusão Lógica:** o gestor desincorpora o bem, respeitadas as guardas da RN-PAT-05.
5. **Acompanhamento do Acervo:** consulta de bens por organização e indicador de ociosidade (proporção de bens `DISPONIVEL` sobre o total incorporado).

---

## 5. Integrações com outros Bounded Contexts
- **`reserva`:** único caminho de empréstimo do bem. O patrimônio consulta as reservas impeditivas e o módulo de reservas consulta as janelas de manutenção, cada um por camada anticorrupção.
- **`organizacao`:** unidade territorial (Matriz ou Comunidade) proprietária do bem.
- **`iam`:** papéis que definem a alçada de gestão.
