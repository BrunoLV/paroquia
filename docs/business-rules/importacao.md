# Regras de Negócio e Fluxos: Módulo Importação (`importacao`)

O módulo de Importação viabiliza a carga em lote de dados paroquiais a partir de planilhas, em três passos, com isolamento dos erros por linha. As exportações de documentos pertencem aos módulos de cada assunto (seção 4).

---

## 1. Visão Geral e Propósito
Possibilitar a carga segura de dados prévios (vínculos de membros, organizações e eventos) a partir de arquivos CSV ou XLSX, sem gravar nada antes de o usuário corrigir os erros apontados.

---

## 2. Agregados, Entidades e Value Objects Principais
O módulo não tem agregado próprio: é um fluxo de orquestração sobre os contextos de destino.
- **`TipoEntidadeImportacao` (Enum):** `MEMBRO` (vínculo de um usuário já existente a uma organização, com papel organizacional), `ORGANIZACAO` (cadastro na hierarquia paroquial) e `EVENTO`. Cada tipo tem seu conjunto de colunas, suas regras de validação por linha e sua lógica de gravação.
- **Resultado de validação e de item:** o sumário com os totais e o detalhe de cada linha (válida ou com o problema apontado).

---

## 3. Regras de Negócio Invariantes

### RN-IMP-01: Três Passos, Validação sem Gravação e Isolamento por Linha
- A importação segue **modelo → validação → confirmação**. O modelo é um arquivo de exemplo, com cabeçalhos e linhas de amostra, para cada tipo. A **validação** (dry-run) lê o arquivo e valida cada linha isoladamente, **sem gravar nada**. A **confirmação** grava as linhas válidas.
- O erro de uma linha (identificador inexistente, papel inválido, dado malformado) é isolado e relatado: **não aborta o lote** nem impede a gravação das demais.
- Linhas em branco, inclusive as do fim de uma planilha, são ignoradas.

### RN-IMP-02: Regras do Domínio de Destino
- Cada linha confirmada passa pelas regras do contexto de destino, como se fosse cadastrada pela tela: o vínculo de membro segue a tabela de papéis do tipo de organização (`organizacao.md`, RN-ORG-07) e a organização segue a tipologia e a hierarquia (RN-ORG-01 e RN-ORG-02). A organização importada **não traz coordenador nem vice**: a liderança só muda por mandato (RN-ORG-05).

### RN-IMP-03: Acesso
- Importam o administrador, o Pároco, o Bispo Diocesano, a secretaria paroquial e o secretário de conselho.

---

## 4. Fluxos Operacionais e Casos de Uso
1. **Importação em Lote:** o usuário baixa o modelo do tipo, preenche, envia para validação, corrige os erros apontados e confirma.
2. **Exportações (nos módulos de origem):**
   - **Calendário:** exportação da grade e das celebrações em PowerPoint, PDF, Excel e CSV (`calendario.md`).
   - **Atas:** Word e PDF, em layout estruturado ou narrativo, com o cabeçalho institucional da Paróquia (`conselho.md`, RN-CON-08).
   - **Relatórios:** relatórios do painel gerados na interface em PDF e CSV (`painel.md`).

---

## 5. Integrações com outros Bounded Contexts
- **`organizacao` e `membro`:** destino das cargas de organizações e de vínculos.
- **`calendario`:** destino da carga de eventos.
- **`iam`:** o vínculo de membro parte de um usuário já existente.
