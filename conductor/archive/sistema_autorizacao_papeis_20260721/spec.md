# Specification: Sistema de Autorização, Papéis e Alcance de Permissões

## Overview
Esta especificação define o novo sistema de autorização e controle de acesso baseado em papéis (RBAC) e escopo de organização no sistema de Gestão Paroquial.
Serão implementadas regras estritas de permissão no backend (Spring Security / evaluators de autorização) e no frontend (React hooks/helpers de permissão), estruturando a hierarquia entre Administrador Sistêmico, Clero (Pároco/Vigário), Conselhos Pastorais (por comunidade), Pastorais/Movimentos (específicos) e membros/secretários.

## Functional Requirements
1. **Enumeração e Estrutura de Papéis**:
   - **Administrativos Sistêmicos**: `ADMIN` (Administrador do Sistema com permissão total).
   - **Administrativos Gerais**: `PAROCO`, `VIGARIO` (Acesso total para autorizar, criar, editar, deletar, cancelar e visualizar qualquer registro).
   - **Administrativos de Escopo**: `COORDENADOR`, `VICE_COORDENADOR`:
     - *Conselho Pastoral*: Permissão administrativa restrita à sua Comunidade/Matriz e sub-organizações ligadas a ela.
     - *Pastoral / Movimento*: Permissão de inclusão, edição e cancelamento restrita apenas à sua própria Pastoral/Movimento.
   - **Normais de Consulta/Apoio**: `PADRE`, `SECRETARIO` (Acesso de visualização global de todos os dados do sistema. Não podem manipular registros, exceto adicionar Observações onde permitido).
   - **Normais de Membros**: `MEMBRO` (Visualização limitada de dados concretos/aprovados; não visualizam eventos pendentes ou registros administrativos. Podem adicionar observações apenas em eventos da sua Pastoral/Movimento ou onde a pastoral é participante).

2. **Verificação de Permissões por Escopo (Scope Evaluation)**:
   - Implementar avaliadores de permissão no backend que validem o vínculo do usuário logado e sua organização.
   - Para `COORDENADOR` / `VICE_COORDENADOR` de `CONSELHO_PASTORAL`, verificar se a organização-alvo da ação pertence à mesma comunidade/matriz.
   - Para `COORDENADOR` / `VICE_COORDENADOR` de `PASTORAL`/`MOVIMENTO`, verificar se a organização-alvo corresponde exatamente à sua pastoral/movimento.

3. **Fluxo de Aprovação de Eventos**:
   - Manter a regra de que eventos pendentes criados por pastorais exigem aprovação de `ADMIN`, `PAROCO`, `VIGARIO` ou `COORDENADOR`/`VICE_COORDENADOR` do `CONSELHO_PASTORAL` da comunidade associada.

4. **Frontend & Interface**:
   - Atualizar a função/hook de verificação de permissões (`permissions.ts`).
   - Ajustar botões de ação (Criar, Editar, Deletar, Aprovar, Cancelar, Observação) em todas as telas (Calendário, Eventos, Organizações, Projetos, Observações, Configurações) para refletir o alcance do usuário logado.

5. **Migração de Banco de Dados**:
   - Script Flyway V042 para atualizar a tabela/enums de papéis de usuário no banco PostgreSQL.

## Non-Functional Requirements
- **Segurança**: Garantir que as validações ocorram obrigatoriamente no Backend Spring Security (não dependendo unicamente da ocultação de botões na UI).
- **Desempenho**: Avaliação de permissão leve e otimizada baseada nos dados do token JWT e contexto da sessão.

## Acceptance Criteria
- `ADMIN` tem acesso irrestrito a todas as funcionalidades.
- `PAROCO` e `VIGARIO` possuem permissão total administrativa em todas as comunidades e pastorais.
- `COORDENADOR` do Conselho Pastoral de uma Comunidade X não consegue editar/aprovar eventos ou dados da Comunidade Y.
- `COORDENADOR` de Pastoral só consegue criar/editar eventos da sua própria Pastoral.
- `SECRETARIO` e `PADRE` conseguem visualizar tudo, mas só conseguem adicionar observações.
- `MEMBRO` só visualiza eventos aprovados/concretos e só insere observações nos eventos envolvidos com sua Pastoral.

## Out of Scope
- Alterações em schemas de login/autenticação externa (ex: OAuth2/OpenID).
