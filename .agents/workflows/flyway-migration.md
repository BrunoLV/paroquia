---
name: flyway-migration
description: "Roteiro padronizado para criação de migrações Flyway seguras (Zero-Downtime), com auditoria automática e comentários DDL."
---

# Workflow: Nova Migração Flyway (Zero-Downtime)

Siga este roteiro rigoroso ao introduzir ou alterar o esquema de banco de dados:

## 1. Identificação e Versionamento

1. Localize a última versão de migração em `gestao-paroquia/src/main/resources/db/migration/`.
2. Nomeie o novo arquivo seguindo o padrão:
   `V{NNN}__{descricao_em_snake_case}.sql`
   (ex.: `V015__criar_tabela_intencoes_missa.sql`).
3. **Regra de Ouro:** NUNCA edite um arquivo `V` já commitado. Crie uma nova versão para qualquer correção ou ajuste.

---

## 2. Estrutura Canônica do Script DDL

Todo script de criação de tabela DEVE contemplar:

1. **Cabeçalho Conceitual:** Justificando o motivo de negócio, regra canônica e estratégia de migração.
2. **Padrão de 4 Colunas de Auditoria (`BaseAuditableEntity`):**
   ```sql
   criado_em_utc TIMESTAMP NOT NULL DEFAULT NOW(),
   atualizado_em_utc TIMESTAMP NOT NULL DEFAULT NOW(),
   criado_por VARCHAR(128) NOT NULL DEFAULT 'sistema',
   atualizado_por VARCHAR(128) NOT NULL DEFAULT 'sistema'
   ```
3. **Identificador Primário:** `UUID` (ou `CHAR(36)`/`VARCHAR(36)` conforme motor).
4. **Precisão Monetária:** Valores financeiros com `DECIMAL(15, 2)` ou `NUMERIC(15, 2)`. Proibido `FLOAT`/`DOUBLE`.
5. **Comentários Obrigatórios (`COMMENT ON`):**
   ```sql
   COMMENT ON TABLE nome_tabela IS 'Finalidade de negócio da tabela na Linguagem Ubíqua.';
   COMMENT ON COLUMN nome_tabela.coluna IS 'Descrição e invariantes canônicas da coluna.';
   ```
6. **Estratégia Zero-Downtime:** Novas colunas adicionadas a tabelas existentes DEVEM ser criadas como `NULLable` ou com `DEFAULT`.

---

## 3. Validação e Testes

1. Se houver DML (carga de semente), garanta idempotência (`ON CONFLICT DO NOTHING`).
2. Valide a migração executando os testes de integração do repositório no backend:
   ```bash
   cd gestao-paroquia && ./gradlew test --tests "*RepositoryTest"
   ```
3. Atualize o modelo de entidade JPA correspondente estendendo `BaseAuditableEntity`.
