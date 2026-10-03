# Regras de Negócio e Fluxos: Módulo Governança e Auditoria (`governanca`)

O módulo de Governança garante a rastreabilidade total, auditoria contínua, histórico de modificações e conformidade legal e canônica das operações do sistema.

---

## 1. Visão Geral e Propósito
Assegurar que toda operação de criação, edição ou remoção de dados no ecossistema paroquial seja registrada com carimbo de tempo universal e autoria comprovada, sem brechas manuais ou bypass de aplicação.

---

## 2. Agregados, Entidades e Value Objects Principais
- **`BaseAuditableEntity` (Superclasse Mapeada JPA):** Padrão canônico e obrigatório estendido por **todas as entidades persistidas** do sistema:
  - `criado_em_utc` (`Instant`, UTC): Timestamp automático da criação.
  - `atualizado_em_utc` (`Instant`, UTC): Timestamp automático da última alteração.
  - `criado_por` (`String` / `UUID`): Identificador do usuário autor da criação.
  - `atualizado_por` (`String` / `UUID`): Identificador do usuário autor da atualização.
- **`RegistroAuditoria`:** Log estruturado imutável de eventos de mutação e acessos administrativos.

---

## 3. Regras de Negócio Invariantes

### RN-GOV-01: Auditoria 100% Automática
- A gravação dos 4 campos de auditoria (`criado_em_utc`, `atualizado_em_utc`, `criado_por`, `atualizado_por`) é **estritamente automática** via Spring Data JPA Auditing (`AuditingEntityListener`).
- É **TERMINANTEMENTE PROIBIDO** definir ou alterar valores de auditoria manualmente através de setters em use cases, controllers ou callbacks manuais (`@PrePersist` / `@PreUpdate`).

### RN-GOV-02: Imutabilidade de Histórico
- Registros de auditoria são somente-leitura (*append-only*). Nenhum usuário, inclusive administradores, tem permissão para expurgar ou retificar a trilha de auditoria do sistema.

### RN-GOV-03: Timestamps Estritamente em UTC
- Todas as datas e horas de auditoria e persistência no banco de dados devem ser armazenadas em UTC (`Instant`), convertendo para o fuso local da Paróquia (ex.: `America/Sao_Paulo`) exclusivamente na camada de apresentação/UI.

### RN-GOV-04: Isolamento Canônico do Papel `ADMIN` e Trava do Ano Paroquial
- O papel `ADMIN` é estritamente restrito à infraestrutura técnica, logs e segurança do sistema, sendo expressamente impedido de executar atos de governo eclesial (como homologar atas, aprovar eventos ou alterar o status do Ano Paroquial).
- A abertura, fechamento ou arquivamento do Ano Paroquial (`AnoParoquialStatus`) é prerrogativa exclusiva de governo canônico do **`PAROCO`** (Cân. 515 §1 e Cân. 532), validada por `ConselhoOuParocoAutorizacaoAnoParoquialPolicy`.

### RN-GOV-05: Single Source of Truth (SSOT) dos Dados Institucionais
- Os dados cadastrais da paróquia (Nome, Diocese, CNPJ, Endereço, Telefone e E-mail) **NÃO DEVEM** ser duplicados em arquivos de configuração estática (`application.yml`) nem em tabelas redundantes.
- A fonte única da verdade para dados civis, endereço e contatos é a entidade da Igreja Matriz (`UnidadeTerritorial`), enquanto a Diocese/Arquidiocese de vinculação deriva do Governo Eclesiástico (`GovernoEclesiastico`).
- A tabela `calendario.paroquia_configuracao` armazena exclusivamente os parâmetros de branding e identidade visual (cores primária/secundária e logotipos binarizados).

### RN-GOV-06: Persistência Binarizada de Logotipos e Dupla Variante de Fundo
- Os logotipos oficiais da paróquia são persistidos no banco de dados como binários (`bytea`) com seus respectivos tipos MIME (`logo_fundo_claro_mime_type`, `logo_fundo_escuro_mime_type`).
- O sistema oferece obrigatoriamente duas variantes:
  1. **Logotipo para Fundo Claro (`CLARO`):** Utilizado em cabeçalhos claros, tela de autenticação/login e relatórios/impressões.
  2. **Logotipo para Fundo Escuro (`ESCURO`):** Utilizado em cabeçalhos escuros, modo noturno e superfícies de alto contraste.
- O upload das imagens (multipart) e alteração das cores são prerrogativas exclusivas dos perfis **`PAROCO`** e **`ADMIN`** (além de `BISPO_DIOCESANO`), enquanto a consulta aos dados e logotipos é pública para atender à tela de login e renderização da aplicação.
- Endpoints de streaming de logotipos (`/api/v1/configuracao-paroquia/logo/{variante}`) implementam headers de controle de cache HTTP (`Cache-Control: public, max-age=86400`).

---

## 4. Fluxos Operacionais e Casos de Uso
1. **Auditoria Transacional:** Captura automática do contexto de segurança Spring Security (`SecurityContextHolder`) e persistência atômica.
2. **Trilha de Auditoria:** Painel para o Pároco, Vigário, Conselho Econômico (`MEMBRO_CONSELHO_ECONOMICO`) e Auditoria Diocesana (`BISPO_DIOCESANO`) inspecionarem quem realizou determinada alteração crítica.
3. **Gestão do Ciclo do Ano Paroquial:** Controle de abertura e trava de planejamento anual pelo `PAROCO`.
4. **Gestão da Identidade Visual:** Pároco ou Administrador ajustam cores da paróquia via seletor visual e efetuam upload dos logotipos institucionais com pré-visualização imediata e persistência no banco.

---

## 5. Integrações com outros Bounded Contexts
- Todos os bounded contexts dependem de `BaseAuditableEntity` e da infraestrutura de governança.
- O contexto de calendário/governança integra-se via ACL (`ConsultaInstitucionalAdapter`) aos contextos `organizacao` e `clero` para recuperar dados institucionais agregados sem violar o isolamento hexagonal e as regras de arquitetura.


