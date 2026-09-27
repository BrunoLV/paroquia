# Regras de Negócio e Fluxos: Módulo Conselhos Paroquiais (`conselho`)

O módulo de Conselhos gerencia a governança eclesial e administrativa da Paróquia por meio do Conselho Pastoral Paroquial (CPP) / Conselho Pastoral Comunitário (CPC) e do Conselho de Assuntos Econômicos Paroquial (CAEP).

---

## 1. Visão Geral e Propósito
Assegurar o registro canônico, acompanhamento de mandatos, atas de reunião, quórum de deliberações e prestação de contas paroquiais em conformidade com o Código de Direito Canônico (Cânn. 536 e 537).

---

## 2. Agregados, Entidades e Value Objects Principais
- **`Conselho` (Raiz de Agregado):** Identificado por `ConselhoId`. Representa o órgão colegiado (`CPP`, `CPC_COMUNIDADE`, `CAEP`).
- **`MandatoConselho`:** Período de vigência do conselho (geralmente 2 a 4 anos), pároco presidente, coordenador leigo, secretário e tesoureiro.
- **`MembroConselho`:** Fiel (`FielId`) investido de função específica no conselho com data de posse e término.
- **`AtaReuniao`:** Registro formal de reunião ordinária ou extraordinária, lista de presença, pauta discutida, deliberações tomadas e assinaturas digitais ou formalizadas.
- **`Deliberacao` (Value Object):** Decisão formal tomada em reunião com votação ou consenso.

---

## 3. Regras de Negócio Invariantes

### RN-CON-01: Presidência Canônica Nativa
- O Pároco (ou Administrador Paroquial) é o presidente nato de direito de todos os conselhos da paróquia (CPP, CPCs e CAEP). Nenhuma reunião é juridicamente canônica sem sua convocação ou expressa anuência.

### RN-CON-02: Mandatos com Data de Término
- Todo conselheiro possui mandato temporalmente delimitado. O sistema emite alertas prévios de expiração de mandato para renovação e posse de novas diretorias.

### RN-CON-03: Homologação e Imutabilidade de Atas
- Atas de reunião, uma vez aprovadas pelos membros e homologadas pelo Pároco, tornam-se **estritamente imutáveis**, permitindo apenas retificações em atas subsequentes (*em tempo*).

---

## 4. Fluxos Operacionais e Casos de Uso
1. **Convocação de Reunião:** Envio de pauta e convocatória aos conselheiros.
2. **Registro de Ata:** Redação colaborativa com registro de pautas e decisões tomadas.
3. **Exportação Canônica:** Geração de documento padronizado em Word (DOCX) e PDF para arquivo físico paroquial.

---

## 5. Integrações com outros Bounded Contexts
- **`fiel`:** Associação de pessoas físicas registradas aos cargos de conselheiros.
- **`organizacao`:** Vínculo de conselhos pastorais com as comunidades e matriz.
- **`patrimonio`:** Avaliação e aprovação de reformas e alienação de bens no CAEP.
