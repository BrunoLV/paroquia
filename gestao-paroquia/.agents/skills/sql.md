# Skill do Agente de IA: Engenharia SQL e Modelagem Relacional (Especialista Multi-Dialeto)

## 1. Visão Geral e Contexto

Esta skill capacita o Agente de IA a projetar, refatorar, otimizar e auditar esquemas relacionais (DDL), scripts de migração e consultas analíticas/transacionais (DML) com nível sênior de engenharia de dados. O agente atua como um DBA e Arquiteto de Dados Multi-Dialeto, assegurando que todo artefato gerado apresente integridade referencial estrita, alto desempenho em planos de execução (`EXPLAIN`), segurança contra injeção e conformidade dinâmica entre o padrão **ANSI SQL** e os motores **PostgreSQL**, **MySQL** e **H2 (em memória/testes)**.

O código SQL deve ser tratado como um artefato de primeira classe na arquitetura da aplicação, equilibrando portabilidade, controle de concorrência, uso estrito de tipos e documentação integrada dos modelos de dados.

---

## 2. O Banco de Dados como Documentação Viva (*Living Schema & Documentation*)

A estrutura do banco de dados deve expressar com clareza a intenção do negócio e as restrições operacionais diretamente no catálogo do SGBD e nos scripts de versionamento.

* **Comentários de Domínio Obrigatórios via DDL (`COMMENT ON`):** Toda tabela e coluna de significado não trivial DEVE conter comentários registrados no próprio catálogo do banco, documentando a regra de negócio e as restrições canônicas:
```sql
-- Documentação nativa da tabela no catálogo do SGBD
COMMENT ON TABLE lancamento_dizimo IS 
    'Registro financeiro de contribuições paroquiais (RN-DIZ-01). Cada registro compõe a receita ordinária da comunidade.';

COMMENT ON COLUMN lancamento_dizimo.status IS 
    'Estado transacional da contribuição: PENDENTE, CONFIRMADO, ESTORNADO ou CONCILIADO.';

```


* **Cabeçalhos Explicativos em Migrações e Scripts DDL:** Scripts devem justificar decisões de modelagem, particionamento e índices no topo do arquivo:
```sql
/**
 * Tabela: fidelidade_paroquial_historico
 * 
 * DECISÃO DE DESIGN (Índice Composto):
 * Criação de índice em (fiel_id, data_contribuicao DESC) para acelerar
 * a verificação de regularidade fiscal nos fluxos de emissão de certidões.
 */

```


* **Proibição de Comentários Triviais:** Comentários como `-- Tabela de usuários` ou `-- Coluna de chave primária` são terminantemente proibidos.

---

## 3. Diretrizes de Sintaxe, Estilo e Convenções SQL

O agente deve gerar código SQL padronizado, legível e determinístico:

* **Palavras-Chave e Funções em Caixa Alta:** Instruções DDL/DML e funções reservadas devem estar em MAIÚSCULO (`SELECT`, `INSERT`, `UPDATE`, `DELETE`, `JOIN`, `ON`, `WHERE`, `GROUP BY`, `COALESCE`, `CURRENT_TIMESTAMP`).
* **Identificadores em Caixa Baixa (`snake_case`):** Schemas, tabelas, colunas, visões, triggers e constraints devem usar letras minúsculas separadas por sublinhado (ex.: `fiel_id`, `data_nascimento`, `valor_contribuicao`).
* **Qualificação e Aliases Significativos:** Em qualquer consulta envolvendo duas ou mais tabelas, todas as colunas devem ser qualificadas com seus respectivos aliases. Os aliases devem ser mnemônicos claros derivados do nome da tabela (ex.: `fiel f`, `sacramento_registro sr`), nunca letras genéricas aleatórias (`a`, `b`, `t1`).
* **Proibição Absoluta de `SELECT *`:** Consultas de produção devem declarar nominalmente as colunas projetadas, eliminando desperdício de I/O de rede, quebra de contratos após migrações e viabilizando o uso de índices de cobertura (*covering index*).
* **Junções Explícitas:** É expressamente proibido o uso de junções implícitas separadas por vírgula (`FROM usuario u, pedido p WHERE u.id = p.usuario_id`). Utilize sempre a sintaxe explícita `INNER JOIN`, `LEFT JOIN`, etc., com a cláusula `ON`.

---

## 4. Performance, Planos de Execução e Otimização

Ao projetar consultas e índices, o agente deve visar a minimização de custos de I/O e varreduras completas (*Full Table Scans* / *Seq Scans*):

### 4.1. Estratégia e Nomenclatura de Índices

* **Convenção Padronizada de Nomes de Constraints e Índices:**
* Chaves Primárias: `pk_<tabela>`
* Chaves Estrangeiras: `fk_<tabela_origem>_<tabela_destino>`
* Unicidade: `uk_<tabela>_<coluna(s)>`
* Índices Secundários: `idx_<tabela>_<coluna(s)>`


* **Índices em Chaves Estrangeiras:** Toda coluna que atue como `FOREIGN KEY` DEVE possuir um índice secundário correspondente para evitar travamento de tabela (*table lock*) em operações de exclusão/atualização em cascata e otimizar junções reversas.
* **Evite Indexação Cega:** Não crie índices em colunas com cardinalidade extremamente baixa (ex.: flags booleanas) a menos que utilize índices parciais (*Partial/Filtered Index*) específicos para a minoria dos registros (ex.: `WHERE ativo = FALSE`).

### 4.2. Sargabilidade Estrita (*Sargable Queries*)

* As cláusulas `WHERE` e `JOIN` devem permitir que o otimizador use buscas por intervalo ou buscas binárias (*Index Seek / Bitmap Index Scan*).
* **Nunca aplique funções ou transformações na coluna indexada:**
```sql
-- INCORRETO (Não sargável - força varredura completa):
WHERE EXTRACT(YEAR FROM data_evento) = 2026
WHERE DATE(criado_em) = '2026-09-07'
WHERE LOWER(email) = 'fiel@paroquia.org'

-- CORRETO (Sargável - viabiliza busca em índice):
WHERE data_evento >= '2026-01-01 00:00:00' AND data_evento < '2027-01-01 00:00:00'
WHERE criado_em >= '2026-09-07 00:00:00' AND criado_em < '2026-09-08 00:00:00'
WHERE email = 'fiel@paroquia.org' -- (ou crie índice funcional em LOWER(email) se a busca case-insensitive for mandatória)

```



### 4.3. Expressões de Tabela Comuns (CTEs) e Subconsultas

* Elimine subqueries correlacionadas em laço (*row-by-row / N+1 em SQL*).
* Utilize **CTEs (`WITH`)** ou junções derivadas para isolar agregações complexas antes de cruzá-las com a tabela principal, preservando legibilidade e facilitando materializações pelo otimizador.

---

## 5. Segurança, Integridade e Concorrência

* **Prevenção Inegociável contra SQL Injection:** Toda consulta gerada para consumo em código de aplicação deve ser projetada para parametrização estrita (`?` ou `:param`). Concatenações literais de strings de entrada do usuário em queries são categoricamente proibidas.
* **Integridade Referencial Forte:**
* Todas as relações devem ter `FOREIGN KEY` explícita.
* Escolha a estratégia de deleção consciente: prefira `ON DELETE RESTRICT` ou `NO ACTION` para dados fiscais e cadastros mestres; use `ON DELETE CASCADE` estritamente para itens dependentes de ciclo de vida atômico (ex.: itens de uma fatura).


* **Bloqueios Otimistas e Pessimistas:**
* Para concorrência crítica de agendamentos, saldos ou emissão de recibos sequenciais, projete seleções com `FOR UPDATE` (pessimista) ou colunas de controle de versão (`versao INT NOT NULL DEFAULT 0` para concorrência otimista).



---

## 6. Versionamento de Esquema com Flyway

O agente está terminantemente proibido de emitir alterações DDL manuais ou fora de migrações controladas.

* **Localização Canônica:** `src/main/resources/db/migration/`
* **Convenção de Nomenclatura:** `V{NNN}__{descricao_em_snake_case}.sql` (ex.: `V001__criar_tabela_fiel.sql`, `V002__criar_tabela_lancamento_dizimo.sql`).
* **Regras de Governança Flyway:**
1. **Imutabilidade:** Scripts já commitados nunca devem ser alterados. Toda evolução ou correção exige uma nova versão sequencial.
2. **Transacionalidade:** Cada script deve ser idempotente e totalmente contido em transação.
3. **Inclusão de DDL e Comentários:** Toda tabela criada via Flyway deve conter constraints nomeadas e os respectivos `COMMENT ON`.



---

## 7. Matriz de Compatibilidade e Blueprints Multi-Dialeto

O agente deve adaptar sintaxes específicas conforme o banco de dados configurado no projeto:

### 7.1. Tipagem Numérica e Monetária

* **Regra Universal:** Use estritamente `NUMERIC(p, s)` ou `DECIMAL(p, s)` com precisão e escala adequadas (ex.: `DECIMAL(15, 2)` para BRL). Tipos `FLOAT`, `DOUBLE` ou `REAL` são terminantemente proibidos para valores financeiros.

### 7.2. Identificadores e UUIDs

* **PostgreSQL:** Utilize o tipo nativo `UUID NOT NULL DEFAULT gen_random_uuid()` para identificadores distribuídos.
* **MySQL 8+:** Utilize `CHAR(36)` ou `BINARY(16)` para UUIDs. Para identificadores sequenciais, `BIGINT AUTO_INCREMENT`.
* **H2 Database:** Suporta nativamente `UUID DEFAULT RANDOM_UUID()` ou `BIGINT GENERATED BY DEFAULT AS IDENTITY`.

### 7.3. Blueprint DDL Comparativo Completo

#### PostgreSQL (Produção Robusta)

```sql
CREATE TABLE fiel (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(150) NOT NULL,
    cpf VARCHAR(11) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_fiel_cpf UNIQUE (cpf)
);

CREATE TABLE lancamento_dizimo (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fiel_id UUID NOT NULL,
    valor DECIMAL(12, 2) NOT NULL,
    data_competencia DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMADO',
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lancamento_fiel FOREIGN KEY (fiel_id) 
        REFERENCES fiel (id) ON DELETE RESTRICT,
    CONSTRAINT ck_dizimo_valor_positivo CHECK (valor > 0.00)
);

-- Índices de suporte a desempenho
CREATE INDEX idx_lancamento_fiel_id ON lancamento_dizimo (fiel_id);
CREATE INDEX idx_lancamento_competencia ON lancamento_dizimo (data_competencia);

COMMENT ON TABLE lancamento_dizimo IS 'Registros de arrecadação e dízimo paroquial (RN-DIZ-01).';
COMMENT ON COLUMN lancamento_dizimo.valor IS 'Valor monetário recebido, estritamente superior a zero.';

```

#### MySQL (Dialeto 8.0+)

```sql
CREATE TABLE fiel (
    id CHAR(36) PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    cpf VARCHAR(11) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_fiel_cpf UNIQUE (cpf)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE lancamento_dizimo (
    id CHAR(36) PRIMARY KEY,
    fiel_id CHAR(36) NOT NULL,
    valor DECIMAL(12, 2) NOT NULL,
    data_competencia DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMADO',
    criado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lancamento_fiel FOREIGN KEY (fiel_id) 
        REFERENCES fiel (id) ON DELETE RESTRICT,
    CONSTRAINT ck_dizimo_valor_positivo CHECK (valor > 0.00)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_lancamento_fiel_id ON lancamento_dizimo (fiel_id);
CREATE INDEX idx_lancamento_competencia ON lancamento_dizimo (data_competencia);

```

#### H2 Database (Testes e Integração em Memória)

```sql
CREATE TABLE fiel (
    id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    cpf VARCHAR(11) NOT NULL,
    ativo BOOLEAN DEFAULT TRUE NOT NULL,
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uk_fiel_cpf UNIQUE (cpf)
);

CREATE TABLE lancamento_dizimo (
    id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    fiel_id UUID NOT NULL,
    valor DECIMAL(12, 2) NOT NULL,
    data_competencia DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'CONFIRMADO' NOT NULL,
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_lancamento_fiel FOREIGN KEY (fiel_id) 
        REFERENCES fiel (id) ON DELETE RESTRICT,
    CONSTRAINT ck_dizimo_valor_positivo CHECK (valor > 0.00)
);

CREATE INDEX idx_lancamento_fiel_id ON lancamento_dizimo (fiel_id);
CREATE INDEX idx_lancamento_competencia ON lancamento_dizimo (data_competencia);

```

### 7.4. Operações de Upsert Idempotentes

Ao persistir registros com reconciliação em conflito de chaves:

* **PostgreSQL / H2 (v2.0+):**
```sql
INSERT INTO parametro_sistema (chave, valor, atualizado_em)
VALUES ('limite_emolumento', '150.00', CURRENT_TIMESTAMP)
ON CONFLICT (chave) DO UPDATE SET 
    valor = EXCLUDED.valor,
    atualizado_em = EXCLUDED.atualizado_em;

```


* **MySQL:**
```sql
INSERT INTO parametro_sistema (chave, valor, atualizado_em)
VALUES ('limite_emolumento', '150.00', CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE 
    valor = VALUES(valor),
    atualizado_em = VALUES(atualizado_em);

```



### 7.5. Paginação Determinística

Toda paginação com `LIMIT` e `OFFSET` DEVE conter obrigatoriamente uma cláusula `ORDER BY` determinística (incluindo a chave primária como critério de desempate), garantindo que os registros não mudem de página arbitrariamente:

```sql
SELECT 
    f.id,
    f.nome,
    f.cpf
FROM fiel f
WHERE f.ativo = TRUE
ORDER BY f.nome ASC, f.id ASC
LIMIT 20 OFFSET 0;

```

---

## 8. Blueprint DML: Relatório Analítico Coeso com CTE

Exemplo de consulta complexa projetada para alta performance, utilizando aliases qualificados, formatação sargável e expressões comuns de tabela:

```sql
WITH metricas_dizimistas AS (
    SELECT 
        ld.fiel_id,
        COUNT(ld.id) AS total_contribuicoes,
        SUM(ld.valor) AS valor_acumulado,
        MAX(ld.data_competencia) AS ultima_competencia
    FROM lancamento_dizimo ld
    WHERE ld.status = 'CONFIRMADO'
      AND ld.data_competencia >= '2026-01-01'
    GROUP BY ld.fiel_id
)
SELECT 
    f.id AS fiel_id,
    f.nome AS nome_fiel,
    f.cpf AS cpf_fiel,
    COALESCE(md.total_contribuicoes, 0) AS quantidade_dizimos_ano,
    COALESCE(md.valor_acumulado, 0.00) AS total_contribuido_ano,
    md.ultima_competencia
FROM fiel f
INNER JOIN metricas_dizimistas md ON f.id = md.fiel_id
WHERE f.ativo = TRUE
ORDER BY md.valor_acumulado DESC, f.id ASC
LIMIT 50;

```

---

## 9. Anti-Patterns em SQL e Modelagem a Evitar

* **Junções sem Índices de Suporte:** Executar `JOIN` em colunas que não sejam chave primária nem possuam índices criados (provoca *Hash Joins* caros e *Nested Loops* degradados).
* **Datas Armazenadas como String:** Armazenar instantes de tempo em `VARCHAR` em vez de `TIMESTAMP` ou `TIMESTAMP WITH TIME ZONE`, impossibilitando cálculos de intervalo e ordenações eficientes.
* **Paginação com Offset Profundo (*Deep Paging*):** Usar `OFFSET 100000` em tabelas massivas. Quando o volume for crítico, prefira paginação por cursor/keyset (`WHERE id > :ultimo_id ORDER BY id ASC LIMIT 20`).
* **Incompatibilidade H2 em Testes:** Usar funções proprietárias do banco de produção (como `DATE_PART()` do Postgres ou `IFNULL()` do MySQL) sem manter a sintaxe compatível com o ANSI SQL suportado pelo H2 (`COALESCE()`, `EXTRACT()`).
* **Queries Não Parametrizadas em Logs:** Exibir valores literais de dados confidenciais nos scripts ou queries de depuração, violando preceitos de segurança e LGPD.

---

## 10. Checklist de Auditoria SQL para o Agente IA

Antes de considerar uma entrega de DDL, migração ou consulta finalizada, valide:

1. [ ] **Documentação Viva:** As tabelas e colunas centrais possuem `COMMENT ON` descrevendo seu papel de negócio? O DDL contém justificativa no cabeçalho?
2. [ ] **Padrão Flyway:** Toda evolução de schema está formatada no padrão `V{NNN}__{descricao}.sql` imutável em `src/main/resources/db/migration/`?
3. [ ] **Qualificação Total e Proibição de Asterisco:** Não há `SELECT *` no script? Todas as colunas em consultas com joins estão qualificadas com o alias da tabela?
4. [ ] **Sargabilidade:** Os filtros no `WHERE` preservam as colunas indexadas livres de funções como `DATE()`, `LOWER()` ou operações aritméticas?
5. [ ] **Integridade Referencial e Nomenclatura:** Todas as relações possuem `FOREIGN KEY` com nomes explícitos (`fk_...`) e índices secundários (`idx_...`) criados na coluna de ligação?
6. [ ] **Precisão Monetária:** Todos os valores financeiros utilizam estritamente `NUMERIC`/`DECIMAL`, banindo completamente `FLOAT` e `DOUBLE`?
7. [ ] **Conformidade de Dialeto:** A sintaxe gerada (UUID, auto-incremento, upsert) é 100% compatível com o motor especificado (PostgreSQL, MySQL ou H2)?
8. [ ] **Paginação Determinística:** As consultas paginadas contêm `ORDER BY` com critério de desempate por chave primária?