# Specification: RBAC Eclesial, Governança Paroquial e Motor da Agenda (CPP & CAE)

## Overview
Implementação da governança eclesial, padronização de papéis (RBAC) e regras da dinâmica pastoral paroquial conforme as diretrizes do Vaticano e da CNBB. Esta especificação formaliza os **Organismos Paroquiais** (Conselho Pastoral Paroquial - CPP, Conselho Econômico Paroquial - CAE, Clero, Pastorais Orgânicas e Movimentos Eclesiais Vinculados), bem como as regras estritas de prioridade e validação de agenda comunitária.

## Functional Requirements

### 1. Nomenclaturas Oficiais e Classificação dos Organismos Paroquiais
- Padronizar nas telas (Organograma, Cadastro, Configurações, Permissões) os termos eclesiais oficiais:
  - Substituir o termo genérico "Organização" por **"Organismo Paroquial"**.
  - **Conselho Pastoral Paroquial (CPP)** e **Conselho Econômico Paroquial (CAE)**.
- **Divisão das Pastorais Orgânicas por Setores (Tríplice Múnus):**
  - *Setor Santificação/Liturgia:* Ministros, Coroinhas, Leitores, Músicos.
  - *Setor Formação/Ensinar:* Catequese (Infantil/Adultos), Batismo, Crisma, Pastoral Familiar.
  - *Setor Social/Caridade:* Pastoral da Criança, Pastoral da Saúde, Vicentinos.
- **Movimentos Eclesiais Vinculados:** RCC, Equipes de Nossa Senhora, Focolares, etc., com autonomia de membros/reuniões internas e subordinação pastoral local absoluta ao Pároco.

### 2. Reformulação de Enums e Matriz de Permissões (RBAC)
- **Papéis Eclesiais:**
  - `BISPO_DIOCESANO`: Auditoria e governança máxima diocesana.
  - `PAROCO`: Administrador máximo com poder de veto absoluto sobre qualquer atividade, alteração de liderança ou criação de organismos paroquiais.
  - `VIGARIO` / `DIACONO`: Assistentes do clero paroquial e ordem sacra.
  - `MEMBRO_CPP`: Conselho consultivo/supervisor pastoral (validação do plano pastoral anual e da agenda comunitária).
  - `MEMBRO_CAE`: Conselho de assuntos econômicos e gestão financeira.
  - `COORDENADOR_PASTORAL`: Liderança executiva de pastoral orgânica (escrita limitada aos dados da sua própria pastoral).
  - `COORDENADOR_MOVIMENTO`: Liderança de movimento eclesial (escrita limitada à gestão interna do seu movimento).
  - `MEMBRO_PASTORAL`: Integrante ativo de pastoral orgânica.
  - `MEMBRO_MOVIMENTO`: Integrante ativo de movimento eclesial.
  - `SECRETARIO`: Apoio administrativo paroquial.

### 3. Motor de Validação e Prioridade da Agenda Comunitária (Cadastro de Eventos)
- **Níveis de Prioridade de Eventos (1 a 4):**
  - **Nível 1 (Bloqueio Geral):** Solenidades da Paróquia (Padroeira, Semana Santa, Corpus Christi). O sistema **bloqueia automaticamente** a criação de qualquer evento de pastoral ou movimento nessas datas.
  - **Nível 2 (Prioridade Territorial):** Missas Dominicais e de Preceito na Matriz e Capelas.
  - **Nível 3 (Ação Orgânica):** Calendário regular das Pastorais Orgânicas (ex: horários fixos de Catequese e Liturgia).
  - **Nível 4 (Atividades de Movimentos):** Grupos de oração, cenáculos e reuniões de movimentos.
- **Regras de Conflito de Interesses e Espaço Físico:**
  - **Regra de Coincidência de Movimento:** O sistema rejeita o agendamento de evento público/externo de *Movimento Eclesial* que coincida em horário e território com atividade oficial de *Pastoral Orgânica*, a menos que a flag `autorizadoPeloCpp = true` esteja marcada.
  - **Prioridade de Reserva de Espaço:** Priorizar reservas de salas/auditórios/igrejas para Pastorais de Formação e Liturgia antes de aprovar reuniões recorrentes de movimentos.

## Non-Functional Requirements
- **Zero Regressão:** Manter 100% de aprovação na suíte de testes de backend (`./gradlew check`) e frontend (`npm test`).

## Acceptance Criteria
- [ ] O enum `PapelOrganizacional`, `TipoOrganizacao`, `CategoriaEvento` e a validação de eventos contêm as regras de níveis 1 a 4.
- [ ] O cadastro de eventos bloqueia automaticamente datas de Solenidades Nível 1 para pastorais e movimentos.
- [ ] Conflitos entre Movimentos e Pastorais exigem a flag `autorizadoPeloCpp` para aprovação.
- [ ] As telas e formulários utilizam exclusivamente os termos oficiais "Organismo Paroquial", "Conselho Pastoral Paroquial (CPP)" e "Conselho Econômico Paroquial (CAE)".
- [ ] Suíte de testes unitários e de integração valida todas as regras de bloqueio e permissão.

## Out of Scope
- Módulo Financeiro e de Dízimo (postergado para track futura).
- Cruzamento de CEP/Geolocalização para vínculo de paroquianos.
