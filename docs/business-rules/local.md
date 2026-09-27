# Regras de Negócio e Fluxos: Módulo Locais e Espaços Físicos (`local`)

O módulo de Locais gerencia a infraestrutura predial da paróquia, incluindo a Igreja Matriz, salões paroquiais, salas de catequese, capelas e centros comunitários.

---

## 1. Visão Geral e Propósito
Assegurar o mapeamento cadastral, capacidade de público, manutenção e disponibilidade operacional de todos os espaços físicos paroquiais sob jurisdição da paróquia.

---

## 2. Agregados, Entidades e Value Objects Principais
- **`Local` (Raiz de Agregado):** Identificado por `LocalId`. Armazena nome, tipo de espaço, endereço, comunidade a que pertence (`OrganizacaoId`), capacidade máxima de pessoas sentadas e em pé, restrições de uso e status ativo.
- **`TipoLocal` (Enum):** `IGREJA_MATRIZ`, `CAPELA_COMUNIDADE`, `SALAO_PAROQUIAL`, `SALA_CATEQUESE`, `AUDITORIO`, `SECRETARIA`, `CASA_PAROQUIAL`.
- **`RecursoFixo` (Value Object):** Recursos físicos instalados permanentemente (ar-condicionado, altar, presbitério, projetor fixo, sistema de som).
- **`StatusLocal` (Enum):** `ATIVO`, `INATIVO`, `EM_MANUTENCAO`.

---

## 3. Regras de Negócio Invariantes

### RN-LOC-01: Controle de Ocupação e Lotação Máxima
- Nenhum evento pode ser confirmado se a estimativa de público informada exceder a capacidade máxima legal/estrutural cadastrada para o local.

### RN-LOC-02: Bloqueio para Manutenção e Reformas
- Um espaço com status `EM_MANUTENCAO` fica automaticamente indisponível para qualquer agendamento ou reserva no período cadastrado da obra/reforma.

### RN-LOC-03: Vínculo Obrigatório com Comunidade
- Todo local físico deve pertencer a uma comunidade paroquial válida (ou à Matriz Paroquial). Locais não podem existir órfãos de jurisdição pastoral.

---

## 4. Fluxos Operacionais e Casos de Uso
1. **Cadastro e Manutenção de Espaços:** Cadastro detalhado com metragens, fotos, recursos instalados e regras de uso comunitário.
2. **Consulta de Disponibilidade:** API para consulta de janelas horárias livres para suporte ao módulo `calendario` e `reserva`.
3. **Bloqueio Emergencial:** Capacidade de o pároco ou secretário interditar temporariamente um espaço por motivos de força maior.

---

## 5. Integrações com outros Bounded Contexts
- **`calendario`:** Verificação de choques e alocação de missas e reuniões.
- **`reserva`:** Mapeamento de reservas externas (ex.: eventos particulares ou aluguéis sociais).
- **`patrimonio`:** Vinculação de bens patrimoniais instalados permanentemente no local.
