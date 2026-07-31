# Skill do Agente de IA: Migrações de Banco de Dados com Flyway

## 1. Visão Geral e Contexto
Esta skill capacita o Agente de IA a projetar, estruturar, gerar e validar scripts de migração de banco de dados utilizando o **Flyway**, totalmente integrado a aplicações **Spring Boot**. Todo o código SQL gerado, convenções de nomenclatura e estratégias de ciclo de vida devem seguir rigorosamente as práticas recomendadas pela documentação oficial da Red-Gate, o repositório oficial do Flyway e padrões de mercado estabelecidos (como os guias da Baeldung).

## 2. Princípios Fundamentais do Flyway no Spring Boot
O Agente deve respeitar o fluxo automatizado de inicialização do Spring Boot e garantir a integridade do banco de dados seguindo estas diretrizes:
*   **Imutabilidade dos Scripts:** Uma vez executada uma migração (`V...__*.sql`), ela nunca deve ser alterada. Correções de banco de dados devem ser feitas exclusivamente gerando uma nova migração subsequente.
*   **Idempotência:** As migrações devem ser escritas de forma resiliente, idealmente garantindo que falhas parciais não deixem o banco em estado inconsistente (utilizar transações nativas quando o banco de dados suportar DDL transacional).
*   **Ciclo de Vida Automático:** O Spring Boot executa o Flyway automaticamente no *startup* da aplicação. Portanto, o Agente deve projetar alterações compatíveis com *Zero-Downtime*, garantindo retrocompatibilidade (ex: adicionar colunas como `NULLable` inicialmente).

## 3. Convenção de Nomenclatura e Estrutura de Diretórios
O Agente deve organizar estritamente os arquivos no padrão reconhecido nativamente pelo Spring Boot:
*   **Diretório Padrão:** Os arquivos SQL devem ser criados obrigatoriamente em `src/main/resources/db/migration/`.
*   **Padrão de Nome de Arquivo (Migrações Versioadas):** `V<Versão>__<Descrição>.sql`
    *   *Prefixo:* `V` em maiúsculo.
    *   *Versão:* Numeração sequencial ou baseada em timestamp (ex: `1`, `2`, `20260727094500`). Pontos ou underscores podem ser usados como separadores de versão (ex: `V1_1__`).
    *   *Separador:* Dois underscores obrigatórios (`__`).
    *   *Descrição:* Texto descritivo em snake_case ou separado por underscores (ex: `criar_tabela_usuario`).
*   **Padrão de Nome de Arquivo (Migrações Repetíveis):** `R__<Descrição>.sql` (Usado para views, procedures ou triggers que podem ser recriadas de forma segura a cada alteração).

## 4. Configuração no Spring Boot (`application.yml`)
Ao fornecer ou ajustar configurações da aplicação, o Agente deve centralizar os parâmetros de controle do Flyway no prefixo `spring.flyway`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/meubanco
    username: app_user
    password: app_password
  
  flyway:
    # Ativa ou desativa o Flyway
    enabled: true
    # Garante que o Flyway falhe caso haja migrações faltantes detectadas no histórico
    ignore-missing-migrations: false
    # Permite limpar o banco de dados (CUIDADO: Manter false em produção)
    clean-disabled: true
    # Aplica o baseline automaticamente caso o Flyway seja inserido em um banco já existente
    baseline-on-migrate: true
    baseline-version: 0

```

## 5. Blueprint Estrutural (Exemplo de Fluxo de Migração)

O Agente deve estruturar os scripts SQL focando em padrões ANSI SQL específicos para o dialeto do banco de dados configurado (ex: PostgreSQL/MySQL).

### Arquivo: `src/main/resources/db/migration/V1__criar_tabela_evento.sql`

```sql
CREATE TABLE evento (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    data_inicio TIMESTAMP WITH TIME ZONE NOT NULL,
    data_fim TIMESTAMP WITH TIME ZONE NOT NULL,
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_evento_data_inicio ON evento(data_inicio);

```

### Arquivo: `src/main/resources/db/migration/V2__adicionar_localizacao_evento.sql`

```sql
-- Adicionando coluna como NULLable para garantir compatibilidade se a aplicação já estiver rodando
ALTER TABLE evento ADD COLUMN localizacao VARCHAR(255);

```

## 6. Checklist de Revisão e Anti-Patterns a Evitar

Antes de finalizar qualquer script ou configuração do Flyway, o Agente deve validar:

* **Duplo Underscore:** O arquivo gerado possui exatamente dois underscores (`__`) separando a versão da descrição? *(Se tiver apenas um, o Flyway não reconhecerá o script)*.
* **Divergência de Checksum:** Uma migração antiga foi editada? *(Se sim, alerte que isso quebrará a inicialização da aplicação Spring Boot devido à falha de validação de checksum na tabela `flyway_schema_history`)*.
* **Alterações Destrutivas Diretas:** O script remove uma coluna ou tabela ativa em produção? *(Substitua por uma estratégia em fases: 1. Adicionar nova estrutura, 2. Migrar dados, 3. Remover estrutura antiga em um deploy futuro)*.
* **Dependência de Dados Dinâmicos:** O script depende de dados mutáveis ou de estados externos? *(Garanta que comandos de inserção de dados estáticos/seed utilizem cláusulas de segurança como `ON CONFLICT DO NOTHING` ou `WHERE NOT EXISTS`)*.

```