# Regras de Negócio e Fluxos: Módulo Conselhos Paroquiais (`conselho`)

O módulo de Conselhos gerencia a governança colegiada da Paróquia por meio do Conselho Pastoral Paroquial (CPP), Conselhos Pastorais Comunitários (CPCs) e do Conselho de Assuntos Econômicos Paroquial (CAEP / CAE), em estrita conformidade com o Código de Direito Canônico (Cânn. 536 e 537).

---

## 1. Visão Geral e Propósito
Assegurar o registro canônico, acompanhamento de mandatos, atas de reunião, quórum de deliberações e prestação de contas paroquiais, respeitando a natureza consultiva dos colegiados e a presidência nata do Pároco.

---

## 2. Agregados, Entidades e Value Objects Principais
- **`Conselho` (Raiz de Agregado):** Identificado por `ConselhoId`. Representa o órgão colegiado consultivo (`CPP`, `CPC`, `CAEP`).
- **`PapelOrganizacional` (Conselhos):**
  - `SECRETARIO_CONSELHO` (`secretario-conselho`): Pauta reuniões e redige minutas de atas do CPP/CPC. Sem voto deliberativo.
  - `MEMBRO_CONSELHO_PASTORAL` (`membro-conselho-pastoral`): Conselheiro com voto consultivo para diretrizes pastorais.
  - `MEMBRO_CONSELHO_ECONOMICO` (`membro-conselho-economico`): Conselheiro com voto consultivo para orçamentos e auditoria do CAE.
- **`AtaReuniao`:** Registro formal de reunião ordinária ou extraordinária, lista de presença, pauta discutida, minutas de deliberações e homologação.

---

## 3. Regras de Negócio Invariantes

### RN-CON-01: Presidência Canônica Nativa e Voto Consultivo (Cân. 536 §2 e Cân. 537)
- O Pároco (ou Administrador Paroquial) é o presidente nato de todos os conselhos da paróquia. Os conselhos possuem **estritamente voto consultivo**, não gozando de competência deliberativa autônoma.

### RN-CON-02: Minuta pelo Secretário vs. Homologação pelo Pároco
- A redação da ata e o registro das deliberações competem ao `SECRETARIO_CONSELHO`.
- A homologação jurídica da ata, tornando-a eficaz e vinculante na paróquia, é **ato privativo e indelegável do Pároco** (`ROLE_GLOBAL_PAROCO` / `ROLE_CLERO_PAROCO`).
- O papel `ADMIN` é expressamente impedido de homologar atas de conselho.

### RN-CON-03: Homologação e Imutabilidade de Atas
- Atas de reunião homologadas pelo Pároco tornam-se **estritamente imutáveis**, admitindo correções posteriores apenas mediante ata retificadora formal.

---

## 4. Fluxos Operacionais e Casos de Uso
1. **Convocação de Reunião:** Envio de pauta formal aos conselheiros sob autoridade do Pároco.
2. **Redação de Minuta:** O `SECRETARIO_CONSELHO` insere tópicos, deliberações propostas e presenças.
3. **Homologação:** O `PAROCO` analisa a minuta e assina/homologa a ata para eficácia legal e eclesial.
4. **Exportação Canônica:** Emissão de documento formal para arquivo físico da cúria/paróquia.

---

## 5. Integrações com outros Bounded Contexts
- **`fiel`:** Vinculação dos fiéis membros aos assentos do conselho.
- **`organizacao`:** Vínculo de conselhos pastorais com as comunidades filiais e a Matriz.
- **`aprovacao`:** Pareceres de conselho que instruem decisões submetidas ao Pároco.
