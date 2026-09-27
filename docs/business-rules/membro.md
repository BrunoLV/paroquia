# Regras de Negócio e Fluxos: Módulo Membros de Pastorais (`membro`)

O módulo de Membro operacionaliza a relação ativa entre fiéis e as organizações paroquiais (pastorais, movimentos e serviços), registrando cargos, coordenações e histórico ministerial.

---

## 1. Visão Geral e Propósito
Permitir a gestão da participação dos leigos nas diversas frentes de evangelização da paróquia, gerenciando eleições pastorais, lideranças, voluntariado e tempo de dedicação ministerial.

---

## 2. Agregados, Entidades e Value Objects Principais
- **`MembroPastoral` (Raiz de Agregado):** Identificado por `MembroPastoralId`. Faz o vínculo entre um `FielId` e uma `OrganizacaoId`, contendo status da participação, data de ingresso, data de desligamento e histórico de cargos.
- **`CargoLideranca` (Enum):** `COORDENADOR_GERAL`, `VICE_COORDENADOR`, `SECRETARIO`, `TESOUREIRO`, `AGENTE_PASTORAL`, `COLABORADOR`.
- **`MandatoLideranca` (Value Object):** Período em que o membro exerceu uma função diretiva na pastoral.
- **`StatusMembro` (Enum):** `ATIVO`, `LICENCIADO`, `AFASTADO`, `DESLIGADO`.

---

## 3. Regras de Negócio Invariantes

### RN-MEM-01: Exclusividade de Coordenação Ativa
- Uma pastoral ou movimento deve possuir no máximo um Coordenador Geral ativo por período. Mandatos sobrepostos exigem o registro formal de transição ou término do coordenador anterior.

### RN-MEM-02: Múltiplos Vínculos Pastorais
- Um fiel pode ser membro ativo simultaneamente em mais de uma pastoral (ex.: Catequese e Pastoral da Liturgia), desde que os cargos e atribuições não gerem conflitos operacionais ou de conselho.

### RN-MEM-03: Histórico Preservado (*Soft Delete*)
- O desligamento de um membro da pastoral não apaga o registro do banco de dados; seu status transita para `DESLIGADO`, preservando a data de saída e a memória pastoral dos eventos passados em que atuou.

---

## 4. Fluxos Operacionais e Casos de Uso
1. **Engajamento e Adesão:** Inscrição de um fiel em uma pastoral.
2. **Eleição e Posse de Coordenação:** Registro do novo mandato da liderança pastoral.
3. **Escalas Ministeriais:** Fornecimento da lista de agentes aptos para escalas litúrgicas (leitores, ministros da eucaristia, coroinhas, músicos).

---

## 5. Integrações com outros Bounded Contexts
- **`fiel`:** Associação biunívoca com a pessoa física.
- **`organizacao`:** Associação com a pastoral de atuação.
- **`calendario`:** Designação de equipes de trabalho e escalas para eventos litúrgicos e celebrações.
