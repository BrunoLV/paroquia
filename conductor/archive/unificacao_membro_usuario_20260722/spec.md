# Specification: Unificação do Conceito de Membro e Usuário (Membro como Vínculo Organizacional)

## Overview
Esta especificação define a unificação e refatoração arquitetural dos conceitos de **Usuário** e **Membro** no sistema de gestão paroquial. O `Usuario` passa a ser a entidade central representativa da pessoa/fiel no sistema (armazenando dados de identidade, contato e indicadores sacramentais). O `Membro` deixa de duplicar dados pessoais e passa a representar estritamente a **vinculação funcional de um Usuário a uma Organização** (Pastoral, Movimento ou Conselho) com o seu respectivo **Papel/Função** (ex: `COORDENADOR`, `VICE_COORDENADOR`, `MEMBRO`).

## Functional & Technical Requirements

1. **Refatoração do Modelo de Dados (Backend Java)**:
   - **UsuarioEntity / Usuario**: Centraliza os atributos pessoais (`nome`, `username`, `email`, `telefone`, `dataNascimento`, `endereco`) e os indicadores sacramentais (booleanos):
     - `batizado` (boolean): Indica se é batizado na Igreja Católica.
     - `primeiraComunhao` (boolean): Indica se fez a Primeira Comunhão Eucarística.
     - `crismado` (boolean): Indica se recebeu o Sacramento da Crisma.
     - `casadoIgrejaCatolica` (boolean): Indica se é casado na Igreja Católica.
   - **MembroEntity / Membro**: Torna-se estritamente uma associação contendo `id`, `usuarioId` (obrigatório, FK `usuarios.id`), `organizacaoId` (obrigatório, FK `organizacoes.id`), `papel` (Enum: `COORDENADOR`, `VICE_COORDENADOR`, `MEMBRO`), `dataEntrada`, `ativo` e `igrejaId`.
   - Remoção dos atributos pessoais duplicados da tabela/entidade `Membro`.

2. **Regras de Negócio e Validações**:
   - Impedir duplicidade de vínculo ativo do mesmo `usuarioId` na mesma `organizacaoId`.
   - Sincronização automática de liderança: definir/atualizar um membro como `COORDENADOR` ou `VICE_COORDENADOR` atualiza os campos de coordenação correspondentes na `Organizacao` de forma consistente.
   - Script/Migration de Seed: Criar automaticamente um `Usuario` para qualquer registro de membro legado no banco de dados/seed que ainda não possua `usuarioId`.

3. **Importação em Lote via Arquivo (CSV/Excel)**:
   - **Importação de USUÁRIOS**: Trata do cadastro de usuários com dados pessoais e sacramentais booleanos no sistema (`nome,username,email,telefone,dataNascimento,batizado,primeiraComunhao,crismado,casadoIgrejaCatolica`).
   - **Importação de MEMBROS**: Trata exclusivamente do vínculo de usuários existentes com organizações (`usuarioUsername,organizacaoNome,papel,igrejaId`). Se o Usuário não existir na base, a linha exibe erro de validação ("Usuário não encontrado").

4. **Telas e Interface Frontend (`gestao-paroquia-ui`)**:
   - **Tela/Modal de Usuários (`UsuarioFormModal` / `Settings`)**: Formulário para cadastro/edição do fiel com campos pessoais e os checkboxes dos 4 sacramentos (`batizado`, `primeiraComunhao`, `crismado`, `casadoIgrejaCatolica`).
   - **Tela de Cadastro Público na Página de Login (`Login.tsx`)**: Formulário de autocadastro alinhado estritamente ao cadastro gerencial de Usuários (sem solicitação inicial de pastorais/movimentos), incluindo dados pessoais, seleção obrigatória da Igreja/Comunidade (`igrejaId`) e os 4 checkboxes de indicadores sacramentais (`batizado`, `primeiraComunhao`, `crismado`, `casadoIgrejaCatolica`). Vínculos com pastorais e movimentos passam a ser gerenciados exclusivamente de forma administrativa.
   - **Tela/Modal de Organizações (`OrganizacaoFormModal` / `Organograma`)**: Exibe membros vinculados e permite selecionar Coordenador e Vice-Coordenador entre os membros vinculados.
   - **Modal de Membro (`MembroFormModal` / `MembroList`)**: Formulário para seleção de `Usuario` existente via autocompletar + seleção de `Organizacao` + seleção de `Papel`.


## Non-Functional Requirements
- **Manutenibilidade & SOLID**: Eliminação de código duplicado e cumprimento do Princípio de Responsabilidade Única (SRP).
- **Integridade Referencial**: Restrições de chave estrangeira (FK) garantidas no PostgreSQL.
- **Zero Regressão**: Manter 100% dos testes unitários e de integração backend (`./gradlew test`) e frontend (`npm test` e `npm run build`) passando sem erros.

## Acceptance Criteria
- Todo registro de `Membro` possui obrigatoriamente um `usuarioId` e `organizacaoId` válidos.
- Os 4 indicadores sacramentais booleanos (`batizado`, `primeiraComunhao`, `crismado`, `casadoIgrejaCatolica`) e dados pessoais residem exclusivamente em `Usuario`.
- A importação em lote de Membros vincula apenas Usuários previamente existentes a Organizações.
- O build de produção (`npm run build`) e os testes unitários (`./gradlew test` e `npm test`) finalizam com 100% de sucesso.
