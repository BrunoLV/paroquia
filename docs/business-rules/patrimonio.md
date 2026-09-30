# Regras de Negócio e Fluxos: Módulo Patrimônio (`patrimonio`)

O módulo de Patrimônio é responsável pela catalogação, tombamento, conservação e controle de movimentação dos bens materiais da Paróquia.

---

## 1. Visão Geral e Propósito
Prover inventário fidedigno de bens móveis e imóveis, alfaias, vasos sagrados, paramentos, equipamentos tecnológicos e veículos, controlando empréstimos, custódia e termos de responsabilidade.

---

## 2. Agregados, Entidades e Value Objects Principais
- **`ItemPatrimonial` (Raiz de Agregado):** Identificado por `ItemPatrimonialId`. Possui número de tombamento único, descrição, categoria, estado de conservação, valor de aquisição (`BigDecimal`), data de aquisição, localização atual (`LocalId`) e responsável pela custódia (`FielId` ou `UsuarioId`).
- **`CategoriaPatrimonio` (Enum):** `ARTE_SACRA_VASOS_SAGRADOS`, `PARAMENTOS_LITURGICOS`, `EQUIPAMENTO_SOM_AUDIOVISUAL`, `MOBILIARIO`, `VEICULO`, `IMÓVEL`.
- **`EstadoConservacao` (Enum):** `EXCELENTE`, `BOM`, `REGULAR`, `DANIFICADO`, `INSERVIVEL`.
- **`StatusPatrimonio` (Enum):** `DISPONIVEL`, `ALOCADO`, `EM_MANUTENCAO`, `BAIXADO_DESCARTADO`, `DOADO`.

---

## 3. Regras de Negócio Invariantes

### RN-PAT-01: Unicidade de Tombamento
- O número de tombamento é único e imutável após atribuído. Não pode haver duplicidade em todo o âmbito paroquial.

### RN-PAT-02: Termo de Responsabilidade e Custódia
- Bens que saiam das dependências físicas da paróquia ou sejam alocados a pastorais específicas exigem emissão de termo de responsabilidade com identificação do fiel responsável.

### RN-PAT-03: Bens Litúrgicos, Alienação e Parecer do CAE (Cân. 537)
- Vasos sagrados (cálices, cibórios, custódias), arte sacra e bens de grande porte possuem tratamento diferenciado: o Conselho de Assuntos Econômicos (`MEMBRO_CONSELHO_ECONOMICO`) emite parecer consultivo e auditoria patrimonial, cabendo exclusivamente ao **`PAROCO`** (com licença do **`BISPO_DIOCESANO`** quando exigido pelo direito universal/particular) a decisão executiva de alienação, manutenção extraordinária ou baixa.

### RN-PAT-04: Alocação Conflitante em Eventos
- Um item patrimonial não pode ser alocado simultaneamente para dois eventos distintos que ocorram no mesmo horário.

---

## 4. Fluxos Operacionais e Casos de Uso
1. **Tombamento e Entrada de Ativos:** Registro com número patrimonial, fotos, nota fiscal e local de guarda pela `SECRETARIA_PAROQUIAL` ou `MEMBRO_CONSELHO_ECONOMICO`.
2. **Alocação para Eventos:** Reserva de projetor, microfones ou mobiliário para eventos cadastrados no calendário.
3. **Inventário Anual:** Conferência periódica de bens em cada comunidade com emissão de parecer pelo CAE (`MEMBRO_CONSELHO_ECONOMICO`) e homologação pelo `PAROCO` para prestação de contas à Diocese.

---

## 5. Integrações com outros Bounded Contexts
- **`calendario`:** Reserva de equipamentos móveis durante o período de celebrações ou festas.
- **`local`:** Vínculo de custódia e localização física de bens.
- **`conselho`:** Pareceres consultivos patrimoniais emitidos pelo CAE/CAEP para decisão final do `PAROCO`.

