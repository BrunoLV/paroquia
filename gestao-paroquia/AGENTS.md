# AGENTS.md - Backend Engineering Standards & Guidelines

Este documento unifica as diretrizes arquiteturais, regras de domínio e comandos de ferramentas para atuar no backend do "Projeto Paróquia".

## 1. Arquitetura e Contexto do Projeto
- **Escopo do Módulo:** Este diretório (`gestao-paroquia/`) contém um monólito modular desenvolvido em Java 25 com Spring Boot 3, responsável pela API REST do sistema de gestão administrativa e pastoral da Paróquia Nossa Senhora de Fátima[cite: 4].
- **Arquitetura Limpa e DDD:** O backend adota Domain-Driven Design (DDD) e arquitetura Portas e Adaptadores (Hexagonal). O Domínio Core deve ser estritamente isolado da Infraestrutura[cite: 3, 4].
- **Contextos Delimitados (Bounded Contexts):** O sistema é dividido por funcionalidades (ex: `calendario`, `aprovacao`, `local`, `organizacao`, `membro`). É estritamente proibido vazar entidades ou estruturas de banco de dados entre os contextos[cite: 3, 4].
- **Camadas Estratégicas:** Cada contexto segue a estruturação: `domain/model` (zero dependência de frameworks), `domain/service`, `application/usecase` (orquestração transacional) e `infrastructure` (Controllers, Repositories, Adapters)[cite: 3, 4].

## 2. Regras de Negócio e Preocupações Transversais (Cross-cutting)
- **Fluxo de Aprovação:** Modificações de estado (criar/atualizar/cancelar eventos ou projetos) frequentemente passam por uma máquina de estados de aprovação (`AprovacaoEntity`, `DecideSolicitacaoAprovacaoUseCase`) em vez de aplicar o CRUD direto. Verifique essa necessidade antes de implementar mutações[cite: 4].
- **Gestão de Eventos:** O contexto `Evento` utiliza um padrão de segregação de repositórios por responsabilidade. Novas consultas devem seguir esse padrão em vez de sobrecarregar uma única interface de repositório[cite: 4].
- **Autorização Baseada em Organização:** As permissões não são simples verificações de propriedade, mas baseadas no papel do ator e no escopo organizacional da paróquia (Matriz, Comunidades, movimentos)[cite: 4].
- **Ciclo de Vida do Ano Paroquial:** O provisionamento de eventos é governado pelo ciclo de vida do `AnoParoquialEntity` e seu respectivo status[cite: 4].
- **`Fiel` vs. `Usuario` (Contexto Paroquial vs. IAM):** `Fiel` (pacote `fiel/`) representa a pessoa física — nome, CPF, data de nascimento, dados de contato (e-mail, telefone, endereço), flags sacramentais (batizado, primeira comunhão, crismado, casado na Igreja Católica) e `Vocacao` (LEIGO, SACERDOTE, DIACONO, RELIGIOSO). `Usuario` carrega **apenas** credenciais de login e papéis globais (`username`, `passwordHash`, `roles`) — não tem mais nenhum dado pessoal/pastoral embutido; qualquer tela/endpoint que precise de nome/telefone/sacramentos de um Usuario deve resolvê-los via o `Fiel` referenciado por `UsuarioEntity.fielId` (obrigatório, `NOT NULL`). São conceitos **desacoplados**: o e-mail de contato do Fiel é independente do `username` de login, imutável — nunca sincronize os dois, e nunca trate o e-mail de contato do Fiel como credencial. Um `Fiel` pode existir sem nunca ter um `Usuario` associado (ex.: um paroquiano/dizimista comum, criado via `AdicionarMembroUseCase`); o inverso não é permitido — `CreateUsuarioUseCase` exige um `fielId` de um `Fiel` já cadastrado, e rejeita a criação se ele não existir. **Não há mais autocadastro público**: o antigo endpoint `POST /usuarios/registrar` foi removido — todo `Usuario` nasce por criação administrativa (`ROLE_ADMIN`).
- **Vínculo de membro (`MembroGrupo`, pacote `membro/`):** substitui as três tabelas/entidades legadas (`MembroEntity`, `MembroOrganizacaoEntity`, `ParticipanteOrganizacaoEntity`) por um único vínculo `fielId` + `organizacaoId` + `papel` (`calendario.membro_grupo`, sem FK para `organizacoes`). Participação em múltiplas organizações é simplesmente múltiplas linhas de `MembroGrupo` para o mesmo `fielId` — não existe mais um recurso de "participações" separado. Dois casos de uso distintos gerenciam vínculos, com autorização e efeitos colaterais diferentes:
  - `AdicionarMembroUseCase` (`POST /api/v1/membros`): cria vínculo comum a partir de um `fielId` já existente; **nunca lê, cria ou altera `UsuarioEntity`** — é o caminho para cadastrar um paroquiano/dizimista sem login.
  - `PromoverACoordenadorUseCase` (`POST /api/v1/membros/promover-coordenador`): promove um `Fiel` já membro ativo de uma organização a um papel de liderança e, só então, cria um `Usuario` vinculado a ele se ainda não existir um. É o único ponto do sistema em que participação pastoral "vira" acesso administrativo, e é restrito a quem tem papel Pároco, Vigário, Mesa do CPP ou Admin (`UsuarioAuthorizationService.requireParocoVigarioMesaCppOuAdmin()`).
  - `AdicionarVinculoUseCase`/`RemoverVinculoUseCase`/`ListarVinculosUseCase` (usados por `/api/v1/usuarios/{id}/membros`) continuam recebendo `usuarioId` externamente (contrato preservado) mas resolvem o `fielId` internamente antes de operar sobre `MembroGrupo` — o mecanismo de autenticação (`LeitorVinculoExterno`) também resolve `fielId` via `usuarios.fiel_id` antes de consultar `membro_grupo` para montar as authorities do Spring Security.

## 3. Governança de IA e Otimização de Tokens
- **Graphify Obrigatório:** É mandatório consultar os artefatos em `../graphify-out/` antes de explorar classes ou repositórios via buscas manuais[cite: 3, 4].
- **Economia de Tokens:** É proibida a leitura em lote de arquivos inteiros se a estrutura puder ser obtida diretamente via consulta aos artefatos do Graphify[cite: 3].
- **Isolamento de Privacidade (LGPD):** A leitura e aplicação das regras em `docs/ia/regras_duras_lgpd.md` é uma etapa prévia inegociável antes de autorar código, logs ou contratos de dados[cite: 3, 4].
- **Consulta de Habilidades (Skills):** O agente deve aplicar as diretrizes contidas nos guias em `.agents/skills/` (ex: `ddd.md`, `java.md`, `tdd.md`) antes de refatorar o backend[cite: 3, 4].

## 4. Padrões de Código e Tipagem Estrita
- **Idioma de Nomenclatura:** Conceitos de domínio, testes e variáveis locais devem ser nomeados em Português. Termos técnicos/arquiteturais devem permanecer em Inglês[cite: 3].
- **Termos Proibidos:** O uso de sufixos genéricos como `Data`, `Handler`, `Manager`, `Helper` ou `Utility` é estritamente proibido[cite: 3, 4].
- **Imutabilidade e Tipagem:** Utilize Java `record` para Objetos de Valor e DTOs, e `java.util.Optional` em vez de retornos `null`. Evite tipos brutos (raw types) ou strings literais para domínios fixos, preferindo `enum`[cite: 3, 4].
- **Injeção de Dependências:** É exigida a injeção via construtor. O uso de `@Autowired` em atributos (fields) é proibido[cite: 3, 4].

## 5. Qualidade, Testes e Documentação
- **Limites Físicos:** Arquivos não devem ultrapassar 500 linhas e métodos devem conter entre 4 e 20 linhas[cite: 3, 4].
- **Política de Zero Avisos:** Todo código deve passar pelos portões de qualidade do PMD, SpotBugs e Checkstyle sem disparar avisos[cite: 3, 4].
- **Higiene de Importações:** É proibida a utilização de nomes totalmente qualificados inline (FQNs) ou importações com caracteres coringa (star imports/wildcards)[cite: 3, 4].
- **Mandato de TDD:** O ciclo Red-Green-Refactor é obrigatório. Devem ser implementadas as camadas de testes Unitários, de Integração e End-to-End (E2E)[cite: 3, 4].
- **Documentação Exaustiva:** Cada contexto exige um arquivo `package-info.java`. Interfaces públicas e contratos de API (OpenAPI/Swagger) devem ser documentados, e regras de negócio complexas devem utilizar diagramas Mermaid.js[cite: 3].
- **Comentários de Intenção:** Os comentários no código devem explicar o PORQUÊ (razão de negócio), não o O QUÊ (mecânica), e garantir que invariantes estejam declaradas para leitura autônoma por IA[cite: 3].

## 6. Instruções de Ferramentas (Tools) e Comandos
### Exploração via Graphify (A partir do diretório gestao-paroquia/)
- `graphify query "<question>"`: Para perguntas sobre a base de código[cite: 4].
- `graphify path "<A>" "<B>"`: Para relações entre dois símbolos[cite: 4].
- `graphify explain "<concept>"`: Para focar em um conceito específico[cite: 4].
- `graphify update .`: Para atualizar a AST após alterações no código[cite: 4].

### Build e Testes (Gradle Wrapper)
- `./gradlew check`: Executar testes e portões de qualidade (checkstyle, pmd, spotbugs)[cite: 4].
- `./gradlew build`: Build completo com qualidade e testes[cite: 4].
- `./gradlew test`: Executar todas as suítes de testes em memória[cite: 4].
- `./gradlew test --tests "NomeDaClasse"`: Executar classe de teste específica[cite: 4].
- `./gradlew bootRun`: Iniciar a API localmente[cite: 4].
- `./demo_mvp.sh`: Executar script de testes End-to-End para gerar relatório de MVP[cite: 4].