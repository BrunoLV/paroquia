# Regras de Negócio e Fluxos: Módulo Importação (`importacao`)

O módulo de Importação viabiliza a carga em lote de dados paroquiais a partir de planilhas, em três passos, com isolamento dos erros por linha. As exportações de documentos pertencem aos módulos de cada assunto (seção 4).

---

## 1. Visão Geral e Propósito
Possibilitar a carga segura de dados prévios (vínculos de membros, organizações e eventos) a partir de arquivos CSV ou XLSX, sem gravar nada antes de o usuário corrigir os erros apontados.

---

## 2. Agregados, Entidades e Value Objects Principais
O módulo não tem agregado próprio: é um fluxo de orquestração sobre os contextos de destino.
- **`TipoEntidadeImportacao` (Enum):** `MEMBRO` (vínculo de um usuário já existente a uma organização, com papel organizacional), `ORGANIZACAO` (cadastro na hierarquia paroquial) e `EVENTO` (criado pela esteira do calendário). Cada tipo tem seu conjunto de colunas, suas regras de validação por linha e sua lógica de gravação.
- **Resultado de validação e de item:** o sumário com os totais e o detalhe de cada linha (válida ou com o problema apontado).
- **Resultado de confirmação:** total gravado, total enviado para aprovação e erros por linha.

---

## 3. Regras de Negócio Invariantes

### RN-IMP-01: Três Passos, Validação sem Gravação e Isolamento por Linha
- A importação segue **modelo → validação → confirmação**. O modelo é um arquivo de exemplo, com cabeçalhos e linhas de amostra, para cada tipo. A **validação** (dry-run) lê o arquivo e valida cada linha isoladamente, **sem gravar nada**. A **confirmação** grava as linhas válidas.
- **Nenhum valor é assumido por omissão.** Campo obrigatório ausente, identificador malformado, valor fora da lista aceita (tipo, setor, subtipo, categoria, papel) ou data ilegível rejeitam a linha com mensagem clara. Não existe "tipo padrão", "primeira organização" nem "data de agora".
- **Validação e confirmação usam a mesma interpretação da linha e as mesmas regras de domínio do destino**: o "válida" do dry-run não pode divergir do que a confirmação grava. A confirmação revalida integralmente o que recebe, pois o cliente reenvia as linhas.
- **Cada linha é gravada em transação própria.** O erro de uma linha (identificador inexistente, papel inválido, violação de regra do destino) desfaz somente essa linha e **não aborta o lote** nem impede a gravação das demais. A resposta da confirmação traz o total gravado, o total enviado para aprovação e a **lista de erros por linha** (número da linha do arquivo e motivo). Falhas inesperadas aparecem com mensagem genérica, sem detalhe interno. O conteúdo das linhas não é registrado em log.
- Linhas em branco, inclusive as do fim de uma planilha, são ignoradas.
- **Limites de entrada:** arquivo de até 2 MB e até 1.000 linhas de dados por importação; acima disso o envio é recusado.

### RN-IMP-02: Regras do Domínio de Destino
- Cada linha confirmada passa pelas regras do contexto de destino, como se fosse cadastrada pela tela, por meio dos mesmos casos de uso:
  - **Vínculo de membro:** `organizacao.md` (RN-ORG-07, tabela de papéis do tipo de organização). Pastorais e movimentos exigem a igreja de vínculo (Matriz ou Comunidade) informada.
  - **Organização:** tipologia e hierarquia (RN-ORG-01 e RN-ORG-02). A organização-mãe, o vínculo com o catálogo oficial, o setor pastoral e o subtipo de conselho vêm da planilha e são validados pelo destino; nada é escolhido automaticamente. A organização importada **não traz coordenador nem vice**: a liderança só muda por mandato (RN-ORG-05).
  - **Evento:** criação pelo caso de uso do calendário (`calendario.md`), que aplica autorização por papel e escopo, justificativa obrigatória do secretário, bloqueio de ano paroquial fechado, detecção de conflito de agenda e **esteira de aprovação** (`aprovacao.md`). Linhas que caem na esteira são contadas como "aguardando aprovação", não como gravadas. O **status do evento não vem da planilha**: é o definido pelo caso de uso. A planilha pode informar a justificativa, exigida de secretarias e secretários.

### RN-IMP-03: Acesso e Papéis Atribuíveis
- Importam o administrador, o Pároco, o Bispo Diocesano, a secretaria paroquial e o secretário de conselho. O usuário autenticado é sempre o ator; não há ator padrão.
- **A importação de membros só atribui os papéis Membro Ativo e Voluntário.** Coordenação, vice-coordenação (secretário de grupo) e papéis de conselho dependem de mandato, idoneidade e provisão canônica do Pároco (RN-ORG-05) e portanto só mudam pela tela; linha com esses papéis é rejeitada. Vínculos em conselhos também ficam fora da importação.

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
