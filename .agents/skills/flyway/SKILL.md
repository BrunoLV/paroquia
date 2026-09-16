---
name: flyway
description: "Migrações e evolução de esquema relacional com Flyway: versionamento semântico, padrão Expand & Contract (Zero-Downtime), imutabilidade de scripts, idempotência e comentários DDL (COMMENT ON)."
---

# Skill do Agente de IA: Migrações de Banco de Dados com Flyway (Especialista em Evolução Contínua e Zero-Downtime)

## 1. Visão Geral e Contexto

Esta skill capacita o Agente de IA a projetar, versionar, validar e orquestrar migrações de banco de dados relacionais utilizando o **Flyway**, perfeitamente integrado a ecossistemas **Spring Boot 3+** e **Java 25**. O agente atua como um Engenheiro de Dados e Arquiteto de Software Sênior, garantindo que toda evolução de esquema (DDL) e carga de dados de semente (DML) siga princípios rigorosos de **imutabilidade**, **idempotência**, **retrocompatibilidade (*Zero-Downtime Database Deployment*)** e governança documental de regras de negócio.

O agente deve adaptar sintaxes para os motores configurados no projeto (**PostgreSQL**, **MySQL** e **H2**), preservando a coerência com as diretrizes de DDD, SOLID, SQL sargável e testes unitários/integrados.

---

## 2. O Script Flyway como Documentação Viva (*Living Schema & History*)

Os scripts de migração do Flyway constituem a trilha canônica e histórica de como o domínio do negócio evoluiu ao longo do tempo.

* **Cabeçalho Conceitual Obrigatório em Todo Script:** Cada arquivo `V...` ou `R...` deve iniciar com um bloco descritivo justificando o motivo de negócio da alteração, a regra do domínio atendida e os cuidados de performance/concorrência:
```sql
/**
 * MIGRAÇÃO: V004__adicionar_vigencia_sacramento_crisma.sql
 * CONTEXTO / REGRA DE NEGÓCIO (RN-SAC-03):
 * Introduz a coluna 'data_expiracao_certidao' para averbação automática
 * de certidões de crisma emitidas para fins matrimoniais (validade canônica de 180 dias).
 *
 * ESTRATÉGIA ZERO-DOWNTIME:
 * Coluna criada inicialmente como NULLable para não bloquear instâncias ativas
 * da aplicação rodando com a versão anterior do código.
 */

```


* **Metadados Nativos no Catálogo (`COMMENT ON`):** Toda nova tabela, coluna ou alteração significativa aplicada via Flyway DEVE conter instruções `COMMENT ON TABLE` e `COMMENT ON COLUMN` para que o catálogo do SGBD permaneça autoexplicativo para novos engenheiros e ferramentas de auditoria:
```sql
COMMENT ON COLUMN fiel.estado_civil IS 
    'Estado civil canônico/civil: SOLTEIRO, CASADO_RELIGIOSO, VIUVO ou SEPARADO (RN-FIE-02).';

```


* **Proibição de Comentários Triviais:** Comentários operacionais redundantes (como `-- Criação da tabela` ou `-- Adicionando campo`) são terminantemente proibidos.

---

## 3. Princípios Fundamentais e Engenharia de Zero-Downtime

O Spring Boot aplica o Flyway durante o *startup* da aplicação antes da inicialização do `EntityManagerFactory` do Hibernate. O agente deve assegurar que a execução nunca cause indisponibilidade ou travamento de tabelas em produção:

### 3.1. Imutabilidade Absoluta e Checksum

* Uma vez que um arquivo `V...` foi commitado ou aplicado em qualquer ambiente compartilhado, **ele é sagrado e imutável**.
* **Proibição:** Nunca altere o conteúdo de uma migração antiga. O Flyway calcula o checksum SHA-256 e falhará imediatamente a inicialização do Spring Boot se detectar divergência na tabela `flyway_schema_history`.
* **Correções:** Falhas em migrações anteriores devem ser corrigidas exclusivamente por uma **migração corretiva subsequente** (ex.: `V005__corrigir_tipo_coluna_telefone.sql`).

### 3.2. Padrão de Evolução em Fases (*Expand and Contract Pattern*)

Alterações estruturais destrutivas nunca devem ocorrer em uma única migração:

1. **Fase 1 (Expand):** Adicione novas tabelas, novas colunas como `NULLable` (ou com `DEFAULT`), novas constraints sem validação estrita imediata (`NOT VALID` no Postgres).
2. **Fase 2 (Migrate):** Popule e sincronize dados novos gradualmente em segundo plano ou via DML controlado.
3. **Fase 3 (Contract):** Após todas as instâncias da aplicação estarem rodando com o código novo que já lê/grava na nova estrutura, publique uma nova migração Flyway para aplicar `NOT NULL` ou descartar colunas obsoletas (`DROP COLUMN`).

### 3.3. Travamento de Tabelas (*Table Locks*) e DDL Seguro

* **Índices Concorrentes em Grandes Tabelas:** Em bancos massivos (PostgreSQL), prefira a criação de índices concorrentes (`CREATE INDEX CONCURRENTLY`) fora de transações de DDL quando a tabela tiver milhões de registros, declarando `spring.flyway.mixed=true` apenas se explicitamente autorizado pelo time de infraestrutura.
* **Adição de Colunas com Default:** No PostgreSQL 11+ e MySQL 8.0.12+, `ALTER TABLE ... ADD COLUMN ... DEFAULT ...` é uma operação rápida de metadados. Em motores mais antigos, certifique-se de que a adição com default não reescreva a tabela inteira bloqueando leituras e escritas.

---

## 4. Convenções de Nomenclatura e Estrutura de Diretórios

O agente deve respeitar estritamente o layout de pastas reconhecido pelo Spring Boot:

```
src/main/resources/
└── db/
    └── migration/
        ├── V001__criar_esquema_inicial_fiel.sql
        ├── V002__criar_tabela_sacramento_batismo.sql
        ├── V003__adicionar_documento_estrangeiro_fiel.sql
        └── R__001_view_metricas_contribuicoes.sql

```

### 4.1. Migrações Versionadas (`V`)

* **Padrão Obrigatório:** `V{Versao}__{descricao_em_snake_case}.sql`
* Prefixo `V` maiúsculo.
* Versão com numeração sequencial de 3 dígitos (`001`, `002`, `010`) para microsserviços e monólitos com esteira controlada, ou padrão timestamp (`YYYYMMDDHHMMSS`) para equipes distribuídas.
* **Dois underscores (`__`) obrigatórios** como separador de controle. Um único underscore quebrará o parser do Flyway.
* Descrição em `snake_case` sem caracteres especiais, acentos ou espaços.



### 4.2. Migrações Repetíveis (`R`)

* **Padrão Obrigatório:** `R__{descricao_em_snake_case}.sql` ou `R__{Ordem}_{descricao}.sql`
* Destinado **exclusivamente** a objetos de banco de dados idempotentes que podem ser descartados e recriados a qualquer momento (`CREATE OR REPLACE VIEW`, funções analíticas, triggers de auditoria).
* Executam sempre que o seu checksum for alterado, após a execução de todas as migrações `V` pendentes.

---

## 5. Configuração de Produção no Spring Boot (`application.yml`)

O Agente deve gerar configurações centralizadas, seguras e com *fail-fast* ativado:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:gestao_paroquia}
    username: ${DB_USER:app_user}
    password: ${DB_PASSWORD:secret}
  
  flyway:
    enabled: true
    # Garante que a aplicação falhe imediatamente se faltar alguma migração no histórico
    ignore-missing-migrations: false
    # Bloqueio de segurança crítico: impede comandos de 'flyway clean' em qualquer ambiente
    clean-disabled: true
    # Aplica baseline automático caso o Flyway seja inserido em um banco de dados já existente
    baseline-on-migrate: true
    baseline-version: "0"
    # Localização canônica dos scripts
    locations:
      - classpath:db/migration
    # Suporte a transações por script
    group: true

```

---

## 6. Blueprints Estruturais de Migração Multi-Dialeto

### 6.1. Exemplo de Migração DDL Inicial (`V001__criar_tabela_fiel.sql`)

```sql
/**
 * MIGRAÇÃO: V001__criar_tabela_fiel.sql
 * CONTEXTO: Estruturação cadastral central de fiéis e paroquianos (RN-FIE-01).
 */

CREATE TABLE fiel (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(150) NOT NULL,
    cpf VARCHAR(11) NOT NULL,
    email VARCHAR(150),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_fiel_cpf UNIQUE (cpf)
);

CREATE INDEX idx_fiel_cpf ON fiel (cpf);
CREATE INDEX idx_fiel_ativo ON fiel (ativo);

COMMENT ON TABLE fiel IS 'Cadastro mestre de fiéis vinculados à administração paroquial.';
COMMENT ON COLUMN fiel.cpf IS 'Número de CPF com 11 dígitos, sem pontuação, unívoco.';

```

### 6.2. Exemplo de Expansão Não Destrutiva (`V002__adicionar_telefone_principal_fiel.sql`)

```sql
/**
 * MIGRAÇÃO: V002__adicionar_telefone_principal_fiel.sql
 * CONTEXTO: Inclusão de canal de contato direto para notificações canônicas.
 * ESTRATÉGIA: Adicionado como NULLable para preservar zero-downtime da aplicação v1.
 */

ALTER TABLE fiel 
    ADD COLUMN telefone_principal VARCHAR(20);

COMMENT ON COLUMN fiel.telefone_principal IS 'Telefone principal no formato E.164 ou nacional (apenas dígitos).';

```

### 6.3. Exemplo de Migração de Dados Idempotente (DML / Seeds) (`V003__popular_configuracoes_sistema.sql`)

Cargas de dados obrigatórios nunca devem falhar se executadas em bases restauradas ou ambientes concorrentes:

```sql
/**
 * MIGRAÇÃO: V003__popular_configuracoes_sistema.sql
 * CONTEXTO: Parâmetros operacionais mandatórios de emolumentos canônicos.
 * IDEMPOTÊNCIA: Uso de ON CONFLICT / WHERE NOT EXISTS para evitar duplicações.
 */

-- [POSTGRESQL / H2 v2+]
INSERT INTO parametro_sistema (chave, valor, descricao, atualizado_em)
VALUES 
    ('LIMITE_MESES_CERTIDAO_MATRIMONIO', '6', 'Validade máxima em meses para certidão de batismo averbada', CURRENT_TIMESTAMP),
    ('VALOR_TAXA_EXPEDIENTE_PADRAO', '35.00', 'Taxa administrativa padrão de expediente e emissão', CURRENT_TIMESTAMP)
ON CONFLICT (chave) DO NOTHING;

```

### 6.4. Exemplo de Migração Repetível (`R__001_view_resumo_dizimo_anual.sql`)

```sql
/**
 * MIGRAÇÃO REPETÍVEL: R__001_view_resumo_dizimo_anual.sql
 * CONTEXTO: Relatório consolidado analítico de arrecadação por fiel.
 */

CREATE OR REPLACE VIEW vw_resumo_dizimo_anual AS
SELECT 
    f.id AS fiel_id,
    f.nome AS nome_fiel,
    f.cpf AS cpf_fiel,
    COUNT(ld.id) AS total_lancamentos,
    COALESCE(SUM(ld.valor), 0.00) AS total_arrecadado_ano,
    MAX(ld.data_competencia) AS ultima_competencia
FROM fiel f
LEFT JOIN lancamento_dizimo ld ON f.id = ld.fiel_id AND ld.status = 'CONFIRMADO'
WHERE f.ativo = TRUE
GROUP BY f.id, f.nome, f.cpf;

```

---

## 7. Estratégia de Teste e Validação do Flyway no Pipeline

Para garantir que os scripts Flyway não quebrem a aplicação ao subir:

* **Teste de Integração de Migração Automática:**
O agente deve manter um teste de integração de inicialização validando que todas as migrações sobem sem erros a partir de uma base vazia:
```java
package com.empresa.gestaoparoquia.infrastructure.db;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.flywaydb.core.Flyway;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureTestDatabase
@ActiveProfiles("test")
@DisplayName("Infraestrutura: Validação de Migrações Flyway")
class FlywayMigrationIntegrationTest {

    @Autowired
    private Flyway flyway;

    @Test
    @DisplayName("Deve aplicar com sucesso todos os scripts de migração pendentes sem quebras de integridade")
    void deveAplicarMigracoesComSucesso() {
        var info = flyway.info();

        assertThat(info.pending()).isEmpty();
        assertThat(info.current()).isNotNull();
        assertThat(info.current().getState().isApplied()).isTrue();
    }
}

```



---

## 8. Anti-Patterns em Migrações Flyway a Evitar

* **Edição de Scripts Históricos:** Alterar qualquer caractere em um script `V` já aplicado. Se o ambiente de homologação ou produção acusar erro de checksum, a única ação tolerada é criar uma nova versão sequencial.
* **Separador de Arquivo Inválido:** Criar arquivos com apenas um underscore (`V1_criar_tabela.sql`). O Flyway ignora silenciosamente o arquivo ou quebra a ordem sequencial.
* **Inserção Destrutiva Sem Fases:** Executar `ALTER TABLE DROP COLUMN` enquanto as instâncias do microsserviço com a versão anterior do código ainda estiverem ativas e tentando consultar a coluna.
* **Falta de Transacionalidade DDL no MySQL:** O MySQL não suporta DDL transacional nativo (um `CREATE TABLE` gera commit implícito). Se um script com 5 instruções falhar na 4ª, as 3 primeiras já persistirão no banco, deixando a base inconsistente. O agente deve manter **uma única instrução estrutural importante por script** em ambientes MySQL.
* **Scripts Dependentes de Ambiente:** Inserir dados de teste (massa de mocks ou dados de desenvolvimento) em migrações `V`. Dados de ambiente de desenvolvimento devem ser injetados via perfil específico do Spring (`spring.flyway.locations=classpath:db/migration,classpath:db/dev-seeds`).
* **`clean-disabled: false` em Produção:** Permitir a execução acidental de comandos de `clean`, o que expõe o banco de dados a desastres irreversíveis de perda de dados.

---

## 9. Checklist de Auditoria Flyway para o Agente IA

Antes de considerar uma migração concluída e aprovada, valide:

1. [ ] **Nomenclatura Estrita:** O arquivo está em `src/main/resources/db/migration/` e segue o padrão `V{NNN}__{descricao_snake_case}.sql` com **dois** underscores?
2. [ ] **Cabeçalho e Documentação Viva:** O topo do script possui Javadoc descrevendo a regra de negócio atendida e as instruções contêm comandos `COMMENT ON` para as tabelas/colunas criadas?
3. [ ] **Garantia de Zero-Downtime:** Novas colunas adicionadas a tabelas existentes foram criadas como `NULLable` ou com valor `DEFAULT` seguro para não quebrar instâncias ativas do sistema?
4. [ ] **Idempotência em DML:** Comandos `INSERT` ou cargas estáticas utilizam `ON CONFLICT DO NOTHING`, `ON DUPLICATE KEY UPDATE` ou checagens de existência?
5. [ ] **Imutabilidade:** O script em questão é estritamente novo, sem adulteração de migrações anteriores commitadas?
6. [ ] **Compatibilidade com H2 e Motores Reais:** A sintaxe utilizada não quebra a suíte de testes de integração executada em H2 nem o ambiente de produção (PostgreSQL/MySQL)?
7. [ ] **Segurança no `application.yml`:** A propriedade `clean-disabled: true` está explicitamente configurada para proteção de dados em tempo de execução?