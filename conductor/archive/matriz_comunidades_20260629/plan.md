# Plano de Implementação: Matriz, Comunidades e Vínculos

## Fase 1: Banco de Dados e Infraestrutura do Backend [checkpoint: 508e20e]
- [x] Task: Banco de Dados: Migração Flyway [9bc7595]
    - [x] Criar arquivo de migração SQL adicionando parent_id na tabela organizacoes
    - [x] Adicionar coluna igreja_vinculo_id na tabela membros_organizacao
- [x] Task: Mapeamento de Entidades e DTOs [37de79c]
    - [x] Adicionar parentId na entidade OrganizacaoEntity e no modelo de domínio
    - [x] Adicionar igrejaVinculoId na entidade MembroOrganizacaoEntity e no modelo de domínio
    - [x] Atualizar o enum TipoOrganizacao para incluir os tipos MATRIZ e COMUNIDADE
- [x] Task: Conductor - User Manual Verification 'Banco de Dados e Infraestrutura' (Protocol in workflow.md) [508e20e]

## Fase 2: Regras de Negócio e Use Cases do Backend [checkpoint: 1072db7]
- [x] Task: Escrita de Testes Unitários de Regra de Negócio (Red Phase) [8591b53]
    - [x] Escrever testes falhando em AddMembershipUseCaseTest e RegistrarUsuarioUseCaseTest para validar a obrigatoriedade de igrejaVinculoId para pastorais/movimentos
- [x] Task: Implementação das Validações no Backend (Green Phase) [b085fd7]
    - [x] Atualizar AddMembershipUseCase para exigir e validar a igrejaVinculoId
    - [x] Atualizar RegistrarUsuarioUseCase para salvar a igrejaVinculoId de atuações solicitadas
    - [x] Validar que os testes passem com sucesso e verificar cobertura
- [x] Task: Conductor - User Manual Verification 'Regras de Negócio do Backend' (Protocol in workflow.md) [1072db7]

## Fase 3: Interface do Usuário (Frontend)
- [x] Task: Escrita de Testes Unitários de Componentes (Red Phase) [b8d02da]
    - [x] Escrever testes falhando no formulário de cadastro e modal de vinculação para verificar o preenchimento de igreja de atuação
- [x] Task: Implementação no Formulário de Cadastro (Sign Up) [cd69b6e]
    - [x] Adicionar campo de seleção de Matriz/Comunidade ao escolher uma pastoral no Login.tsx
- [x] Task: Implementação do Vínculo e Listagem de Membros [cd69b6e]
    - [x] Atualizar modal de vinculação em Settings.tsx para selecionar a igreja de atuação
    - [x] Exibir a igreja de atuação na lista de membros vinculados
    - [x] Validar que todos os testes passem com sucesso
- [x] Task: Conductor - User Manual Verification 'Interface do Usuário' (Protocol in workflow.md) [085ca0e]
