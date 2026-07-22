# Specification: Cadastros do Sistema via Importação de Arquivos CSV e Excel (XLS/XLSX)

## Overview
Esta especificação define a funcionalidade de cadastro em lote de entidades da paróquia (Membros/Féis Leigos, Usuários, Organizações/Pastorais e Eventos) por meio de importação de arquivos nos formatos CSV (`.csv`) e Excel (`.xls`, `.xlsx`). O sistema disponibilizará modelos para download (templates CSV/Excel), uma tela/modal de upload com pré-visualização (preview), validação linha a linha exibindo relatórios de inconsistência e confirmação para gravação final no banco de dados. Esta funcionalidade é estritamente restrita a perfis administrativos (ex: `ADMIN`, `COORDENADOR_CLERO`, `COORDENADOR_CONSELHO`).

## Functional & Technical Requirements

1. **Entidades Suportadas & Downloads de Arquivos de Exemplo (Templates CSV/Excel)**:
   - Disponibilizar botões e atalhos para download de arquivos de exemplo (templates CSV e Excel `.xlsx`) com cabeçalhos padrão e dados de exemplo preenchidos para orientar o usuário nos cadastros:
     - **Membros**: `nomeCompleto,email,telefone,dataNascimento,rua,numero,bairro,cidade`
     - **Usuários**: `nome,username,telefone,dataNascimento,igrejaId,roles`
     - **Organizações**: `nome,tipo,contato,coordenador,viceCoordenador,parentId,pastoralOficialId`
     - **Eventos**: `nome,descricao,dataInicio,dataFim,categoria,localId,igrejaId,organizacaoId`

2. **Parsing, Validação & Controle de Acesso (Backend Java)**:
   - Restringir endpoints de importação via Spring Security (`@PreAuthorize("hasRole('ADMIN') or hasRole('COORDENADOR_CLERO') or hasRole('COORDENADOR_CONSELHO')")`). Perfis de pastorais/movimentos leigos (`MEMBRO_PASTORAL`, `COORDENADOR_PASTORAL`) têm acesso negado (HTTP 403 Forbidden).
   - Suporte a parsing de arquivos `.csv` e planilhas Excel `.xls` / `.xlsx` (Apache POI).
   - Endpoint `POST /api/v1/importacao/validar?tipo={MEMBRO|USUARIO|ORGANIZACAO|EVENTO}`: Faz o parse do arquivo enviado, valida os campos (formato de e-mail, formato de datas `yyyy-MM-dd HH:mm` ou `dd/MM/yyyy HH:mm`, IDs válidos e campos obrigatórios) e retorna uma lista de registros válidos e um relatório de erros detalhado por número de linha.
   - Endpoint `POST /api/v1/importacao/confirmar?tipo={MEMBRO|USUARIO|ORGANIZACAO|EVENTO}`: Efetiva a gravação em lote dos registros previamente validados.

3. **Interface Frontend (React / Multi-Step Modal)**:
   - O botão/ação "Importar de Arquivo" é visível apenas para usuários administrativos.
   - **Passo 1 (Upload & Download de Template)**: Seleção do arquivo (`.csv`, `.xls`, `.xlsx`) e atalhos para baixar modelos de dados.
   - **Passo 2 (Pré-visualização & Erros)**: Tabela comparativa com linhas válidas (verde) e linhas com divergência (vermelho com mensagem do erro).
   - **Passo 3 (Confirmação)**: Botão para importar as linhas válidas ou cancelar o processo.

4. **Regras de Negócio e Segurança**:
   - Validação de escopo por igreja (respeitando a regra de `igrejaId` para usuários, organizações e eventos).
   - Impedir a inserção de registros duplicados com base em e-mail ou username.

## Non-Functional Requirements
- **Desempenho**: Processamento performático com Apache POI / Streaming de arquivos até 5.000 linhas.
- **Zero Regressão**: Manter 100% dos testes backend (`./gradlew test`) e frontend (`npm test` e `npm run build`) passando.

## Acceptance Criteria
- O build de produção (`npm run build`) e os testes unitários (`./gradlew test` e `npm test`) finalizam com 100% de sucesso.
- Apenas perfis administrativos conseguem importar arquivos para Membros, Usuários, Organizações e Eventos nos formatos CSV, XLS e XLSX.
- Linhas com erros de validação são identificadas no relatório de pré-visualização.
