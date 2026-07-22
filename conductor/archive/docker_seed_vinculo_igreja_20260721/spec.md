# Specification: Atualização dos Arquivos Docker, Validação de Vínculo por Igreja e Seed Data

## Overview
Esta especificação contempla a atualização dos arquivos do ecossistema Docker para os padrões atuais do Compose V2 (removendo tags e opções depreciadas como `version:`), a implementação estrita da regra de negócio que impede usuários de serem vinculados a organizações fora de sua Igreja/Comunidade (no backend e frontend), e a sincronização do script de carga inicial de dados (`docker/seed-data.sql`).

## Functional Requirements

1. **Validação Estrita de Vínculo por Igreja no Backend**:
   - Em `AddMembershipUseCase` e `OrganizacaoController`, ao tentar vincular um usuário a uma organização (como membro, coordenador, vice-coordenador, etc.), o sistema verifica se a `igrejaId` do usuário coincide com a Igreja/Comunidade pai da organização (`igrejaVinculoId`).
   - Se as igrejas divergirem, a operação é rejeitada com mensagem clara de erro de negócio (ex: *"O usuário pertence a outra igreja/comunidade e não pode ser vinculado a esta organização"*).

2. **Restrição e Filtragem de Vínculos no Frontend**:
   - Na interface administrativa de gestão de membros de organizações e gestão de vínculos de usuários (em `Settings.tsx`), as opções de seleção filtram e exibem apenas usuários e organizações pertencentes à mesma Igreja/Comunidade.

3. **Atualização do Docker Compose (Compose V2)**:
   - Remover a propriedade de nível superior `version:` depreciada no `docker-compose.yml` e em quaisquer arquivos Compose relacionados.
   - Atualizar a sintaxe e diretivas dos serviços (PostgreSQL, backend, etc.) para assegurar compatibilidade nativa com o `docker compose` atual sem alertas de depreciação.

4. **Sincronização e Conformidade do Seed Data (`docker/seed-data.sql`)**:
   - Ajustar o script `seed-data.sql` para refletir exatamente a estrutura de tabelas criada pelas migrações V001 a V043.
   - Garantir que todos os registros de `calendario.usuarios` contenham `igreja_id` válido e que todos os registros em `calendario.membros_organizacao` satisfaçam estritamente a regra de mesma igreja/comunidade.
   - Garantir a execução limpa e idempotente do `seed-data.sql` sem erros de chave estrangeira ou violações de integridade.

## Non-Functional Requirements
- **Integridade de Negócio**: Impedir inconsistências de vínculo entre usuários de comunidades distintas.
- **Compatibilidade Docker**: Garantir execução sem avisos (warnings) de depreciação no Docker Compose V2.

## Acceptance Criteria
- Tentar vincular no backend um usuário a uma organização de outra comunidade resulta em erro de validação (HTTP 400).
- Os seletores da UI filtram apenas usuários/organizações da mesma igreja.
- O arquivo `docker-compose.yml` executa via `docker compose config` ou `docker compose up` sem avisos de depreciação.
- O script `docker/seed-data.sql` é executado com sucesso e todos os dados satisfazem a validação de vinculação por igreja.
