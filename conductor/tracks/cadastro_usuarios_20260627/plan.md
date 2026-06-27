# Implementation Plan: Cadastro e Vínculo de Usuários

## Phase 1: DB Migration, Security Converters & Public Sign-Up (TDD)
- [x] Task: Escrever migração de banco de dados Flyway `V037__add_cadastro_fields_to_usuarios.sql` [9e3079c]
    - [x] Adicionar colunas `nome` (VARCHAR), `telefone` (VARCHAR), `data_nascimento` (VARCHAR - para suportar valor criptografado) à tabela `calendario.usuarios`
- [x] Task: Desenvolver o mecanismo de criptografia de dados JPA (`AttributeConverter`) [deef3fa]
    - [x] Criar classe helper de criptografia simétrica (AES) utilizando uma chave secreta injetada por propriedade
    - [x] Criar conversores JPA para criptografia de String (`EncryptedStringConverter`) e Data (`EncryptedLocalDateConverter`)
- [x] Task: Atualizar o modelo de domínio `Usuario` e entidade `UsuarioEntity` no backend [ce48d9b]
    - [x] Adicionar os campos anotados com `@Convert` para aplicar os conversores de criptografia nas colunas correspondentes
- [x] Task: Desenvolver o endpoint de registro público `/api/v1/usuarios/registrar` (permitAll) [3755126]
    - [x] Criar DTO `RegistrarUsuarioRequest` (com campos: nome, username, password, telefone, dataNascimento e solicitações de organizações id opcionais)
    - [x] Permitir requisição POST sem autenticação no `SecurityConfig.java`
    - [x] Implementar caso de uso `RegistrarUsuarioUseCase` para criar o usuário desabilitado e salvar na tabela de usuários
- [ ] Task: Escrever testes unitários e de integração no backend e rodar `./gradlew test`
    - [ ] Testes em `UsuarioServiceTest` e `UsuarioControllerTest` validando cadastro desabilitado e liberação de endpoint sem autenticação
- [ ] Task: Conductor - User Manual Verification 'Phase 1: DB Migration, Security Converters & Public Sign-Up (TDD)' (Protocol in workflow.md)

## Phase 2: User Approval, Member Linking & Role Governance (TDD)
- [ ] Task: Implementar endpoint de aprovação de usuários `PUT /api/v1/usuarios/{id}/approve`
    - [ ] Restringir acesso a `ADMIN`, `CLERO_PAROCO` e `CONSELHO_COORDENADOR`
    - [ ] Ativar usuário (`enabled = true`) e associar papel básico `ROLE_USER`
- [ ] Task: Implementar regras de vínculo e unicidade de coordenação em `membros_organizacao`
    - [ ] Coordenador de pastoral/movimento comum pode associar membros apenas para a pastoral que coordena
    - [ ] Ao adicionar um membro com papel `ROLE_PASTORAL_COORDENADOR` ou `ROLE_PASTORAL_VICE_COORDENADOR`, verificar a unicidade (máximo 1 ativo por organização) e inativar/substituir o anterior se aplicável
    - [ ] Apenas `CLERO_PAROCO` pode definir `COORDENADOR_CONSELHO` e `VICE_COORDENADOR_CONSELHO`
    - [ ] Apenas `ADMIN` pode definir `CLERO_PAROCO`
- [ ] Task: Expor na API de Organizações o Coordenador e Vice-Coordenador resolvidos
    - [ ] No `OrganizacaoResponse`, carregar dinamicamente o nome dos usuários vinculados à organização com os papéis de coordenação correspondentes
- [ ] Task: Escrever testes unitários e de integração para governança de papéis e vínculos
    - [ ] Validar as regras de unicidade de coordenação e delegação hierárquica (pároco define conselho, admin define pároco)
- [ ] Task: Conductor - User Manual Verification 'Phase 2: User Approval, Member Linking & Role Governance (TDD)' (Protocol in workflow.md)

## Phase 3: UI Implementation (Sign Up, User Management Aba & Vínculos)
- [ ] Task: Implementar opção "Cadastre-se" na tela de login
    - [ ] Criar formulário público de cadastro no `Login.tsx` (ou redirecionar para `/cadastro`) com campos Nome, E-mail, Senha, Telefone, Data de Nascimento
    - [ ] Permitir seleção de pastorais/movimentos sugeridos
- [ ] Task: Criar aba "Membros / Usuários" no painel de configurações (`Settings.tsx`)
    - [ ] Listar usuários e exibir status "Pendente" para usuários desabilitados
    - [ ] Disponibilizar botões de aprovação e de vinculação a pastorais
- [ ] Task: Integrar gerenciamento de membros por Pastoral na UI
    - [ ] Adicionar visualização de membros da pastoral selecionada na aba de Pastorais/Movimentos
    - [ ] Permitir que o coordenador adicione fiéis à sua pastoral local
- [ ] Task: Executar build e testes do frontend
    - [ ] Rodar `npm run build` e `CI=true npm test` para validação geral
- [ ] Task: Conductor - User Manual Verification 'Phase 3: UI Implementation (Sign Up, User Management Aba & Vínculos)' (Protocol in workflow.md)
