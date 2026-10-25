# Regras de Negócio e Fluxos: Módulo Importação & Exportação (`importacao`)

O módulo de Importação & Exportação viabiliza a ingestão massiva de dados paroquiais e a extração estruturada de documentos oficiais nos padrões da Igreja e da administração civil.

---

## 1. Visão Geral e Propósito
Possibilitar a migração segura de dados prévios (planilhas de fiéis, históricos de batismo, dízimo) e a geração profissional de relatórios em formatos abertos e interoperáveis (PDF, Word DOCX, PowerPoint PPTX).

---

## 2. Agregados, Entidades e Value Objects Principais
- **`LoteImportacao` (Raiz de Agregado):** Identificado por `LoteImportacaoId`. Contém arquivo de origem, tipo de carga, total de linhas, linhas processadas com sucesso, linhas com erro, status do processamento e log de inconsistências.
- **`StatusLote` (Enum):** `AGUARDANDO_PROCESSAMENTO`, `PROCESSANDO`, `CONCLUIDO`, `CONCLUIDO_COM_FALHAS`, `FALHA_CRITICA`.
- **`TipoExportacao` (Enum):** `AGENDA_MENSAL_PPTX`, `RELATORIO_PATRIMONIAL_PDF`, `LIVRO_TOMBO_PDF`, `CERTIDAO_BATISMO_PDF`.

---

## 3. Regras de Negócio Invariantes

### RN-IMP-01: Validação Transacional Atômica e Sanitização
- Toda importação em lote deve realizar pré-validação sintática e semântica linha a linha antes da gravação definitiva. Linhas inconsistentes (CPF inválido, e-mail malformado) são isoladas em relatório de falha sem corromper a base de dados.

### RN-IMP-02: Paridade e Consentimento de Fiéis Importados
- Fiéis importados em lote devem receber a marcação de termo de consentimento inicial sob a base legal de execução de serviços pastorais/legítimo interesse, exigindo atualização no primeiro atendimento presencial.

### RN-IMP-03: Exportações Fidedignas com Identidade Visual
- Documentos oficiais gerados em Word e PDF devem estampar o cabeçalho canônico da Paróquia (Diocese, Paróquia, Pároco em exercício, data de emissão e assinatura). Apresentações litúrgicas em PowerPoint devem adotar as cores litúrgicas correspondentes ao tempo do ano.

---

## 4. Fluxos Operacionais e Casos de Uso
1. **Importação de Fiéis:** Upload de planilha Excel -> Pré-visualização -> Validação -> Carga atômica.
2. **Exportação de Apresentação Litúrgica:** Seleção do mês no calendário -> Geração de slides PPTX formatados para telões da igreja.
3. **Exportação de Atas:** pertence ao módulo Conselho (ver `conselho.md`, RN-CON-08): Word e PDF, em layout estruturado ou narrativo, com o cabeçalho canônico da Paróquia.

---

## 5. Integrações com outros Bounded Contexts
- **`fiel` / `clero`:** Ingestão de cadastros.
- **`calendario`:** Extração de programações e slides litúrgicos.
- **`conselho`:** Geração de atas formais.
