# Specification: Vínculos de Usuários/Membros, Igrejas, Organizações e Papéis

## Overview
Esta especificação estabelece as regras de escopo para associação de Usuários/Membros às suas respectivas Igrejas (Matriz ou Comunidades), restrição de vínculos funcionais com pastorais e movimentos locais, transição automática de papéis ao indicar Coordenadores/Vice-Coordenadores, e restrição de acesso inicial para usuários sem vínculos ativos.

## Functional Requirements

1. **Vínculo Obrigatório Usuário x Igreja (Matriz / Comunidade)**:
   - Todo usuário/membro possui obrigatoriamente um campo `igrejaId` (referenciando uma organização do tipo `MATRIZ` ou `COMUNIDADE`).
   - O campo é obrigatório no formulário de registro/cadastro público, na tela de aprovação de usuários e na edição de perfil/usuário pelas autoridades competentes.

2. **Escopo de Vínculos com Pastorais/Movimentos**:
   - Um usuário só pode ser vinculado (como Coordenador, Vice-Coordenador ou Membro) a uma pastoral, movimento ou conselho que pertença à sua mesma Igreja (`igrejaId`).
   - Os seletores de membros na UI de edição de organizações e no modal de membros filtram estritamente os usuários cuja igreja corresponde à igreja da organização-pai.

3. **Vínculo Automático e Transição de Papel ao Definir Coordenadores**:
   - Ao salvar uma Organização com Coordenador ou Vice-Coordenador selecionado:
     - O sistema cria/ativa o vínculo em `membros_organizacao` com o papel `COORDENADOR` ou `VICE_COORDENADOR`.
     - Caso um Coordenador/Vice-Coordenador anterior seja substituído:
       - O vínculo anterior de coordenação é inativado/atualizado e o coordenador anterior passa a ter vínculo ativo como `MEMBRO`.
       - O novo indicado passa a ter o vínculo ativo com a role de coordenação.

4. **Bloqueio de Acesso para Usuários sem Vínculo (Zero Access State)**:
   - Se um usuário aprovado no sistema ainda não possui nenhum vínculo ativo com pastoral, movimento ou conselho:
     - Ele não visualiza dados de eventos, calendário, projetos ou organizações.
     - A interface exibe uma tela informativa de aviso: *"Sua conta está ativa, porém aguardando vínculo a uma pastoral ou movimento da sua comunidade por um coordenador ou administrador."*

5. **Permissões de Governança de Vínculos**:
   - A alteração de vínculos e indicação de coordenação é restrita a `ADMIN`, `PAROCO`, `VIGARIO` ou Coordenadores do `CONSELHO_PASTORAL` da comunidade correspondente.

## Non-Functional Requirements
- **Integridade de Dados**: Transações no banco de dados devem garantir que ao alterar coordenações a transição de papeis ocorra de forma atômica.
- **Experiência do Usuário (UX)**: A indicação visual e mensagens de bloqueio inicial devem ser claras e profissionais.

## Acceptance Criteria
- Tentar vincular um usuário da Comunidade A a uma Pastoral da Comunidade B retorna erro de validação na API e é impedido na UI.
- Alterar o Coordenador de uma Pastoral de "Usuário A" para "Usuário B" remove o papel de coordenação de "Usuário A" (retornando a MEMBRO) e concede a "Usuário B".
- Usuários recém-aprovados sem vínculo não conseguem acessar menus operacionais ou dados de eventos.
