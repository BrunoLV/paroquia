# Regras de Negócio e Fluxos: Módulo IAM - Identidade e Acesso (`iam`)

O módulo de IAM gerencia a autenticação, segurança, autorização canônica baseada em papéis (Role-Based Access Control - RBAC / ABAC) e emissão de tokens de acesso à API.

---

## 1. Visão Geral e Propósito
Garantir que apenas usuários autenticados e formalmente autorizados acessem os recursos do sistema, aplicando os princípios do Código de Direito Canônico (Cân. 515 §1, Cân. 536 §2 e Cân. 537), o princípio do menor privilégio e protegendo dados pessoais segundo a LGPD.

---

## 2. Taxonomia dos 14 Papéis Canônicos Ortogonais

O sistema adota estritamente 14 papéis ortogonais distribuídos em 3 camadas hierárquicas, sem aliases legados ou redundâncias:

### Camada A: Papéis Globais do Sistema (IAM Transversal)
| Papel | Código Armazenado | Descrição |
| :--- | :--- | :--- |
| **`ADMIN`** | `admin` | Técnico de infraestrutura, logs e segurança. Estritamente impedido de emitir registros de fé pública ou homologar atas eclesiais. |
| **`BISPO_DIOCESANO`** | `bispo-diocesano` | Autoridade ordinária da diocese; instância recursal canônica máxima. |
| **`PAROCO`** | `paroco` | Autoridade executiva e canônica máxima da paróquia. Fé pública e homologação final exclusiva (unifica Pároco e Administrador Paroquial nos termos do Cân. 540 §1). |
| **`VIGARIO`** | `vigario` | Vigário paroquial cooperador em culto e pastoral. |
| **`DIACONO_PAROQUIAL`** | `diacono-paroquial` | Ministro ordenado com encargo litúrgico e pastoral designado. |
| **`SECRETARIA_PAROQUIAL`** | `secretaria-paroquial` | Operador notarial, pré-cadastro civil e sacramental, agendamentos e atendimento. |
| **`FIEL`** | `fiel` | Usuário leigo comum. Consulta de dados próprios, inscrições e agenda pública. |

### Camada B: Papéis de Conselhos Paroquiais (Colegiados Consultivos)
| Papel | Código Armazenado | Descrição |
| :--- | :--- | :--- |
| **`SECRETARIO_CONSELHO`** | `secretario-conselho` | Pauta reuniões e redige minutas de atas do CPP/CPC. Sem voto deliberativo. |
| **`MEMBRO_CONSELHO_PASTORAL`** | `membro-conselho-pastoral` | Representante pastoral no CPP (Matriz) ou CPC (Comunidade). Voto consultivo. |
| **`MEMBRO_CONSELHO_ECONOMICO`** | `membro-conselho-economico` | Membro do CAE/CAEP. Emite pareceres financeiros e auditoria patrimonial. |

### Camada C: Papéis Organizacionais de Pastoral e Movimento
| Papel | Código Armazenado | Descrição |
| :--- | :--- | :--- |
| **`COORDENADOR`** | `coordenador` | Gestão de agentes, escalas e proposição de projetos da pastoral ou movimento. |
| **`SECRETARIO_GRUPO`** | `secretario-grupo` | Apoio operacional, registro de presenças e comunicações internas do grupo. |
| **`MEMBRO_ATIVO`** | `membro-ativo` | Participante regular de atividades e escalas do grupo. |
| **`VOLUNTARIO`** | `voluntario` | Fiel de apoio operacional pontual e serviços comunitários. |

---

## 3. Regras de Negócio Invariantes

### RN-IAM-01: Controle de Acesso Declarativo (@PreAuthorize)
- Todos os endpoints sensíveis no backend Java **DEVEM conter anotações declarativas `@PreAuthorize`** baseadas nos 14 papéis canônicos formais (`ROLE_GLOBAL_<PAPEL>` ou `ROLE_<TIPO>_<PAPEL>`).

### RN-IAM-02: Separação Canônica entre Ordem Sacramental e Ofício
- O sacramento da Ordem (`PADRE`, `DIACONO`, `BISPO`) reside no perfil ontológico do ministro. Prerrogativas administrativas exigem **provisão canônica ativa** (`PAROCO`, `VIGARIO`, `DIACONO_PAROQUIAL`). Sem provisão ativa ou no caso de `EMERITO`, o acesso reverte à base de `FIEL`.

### RN-IAM-03: Princípio da Proposição vs. Homologação
- Órgãos consultivos (conselhos e secretarias) elaboram pareceres, propostas e minutas. Somente o **Pároco** detém o poder de decisão deliberativa e homologação jurídica de atos e atas.

### RN-IAM-04: Isolamento Técnico do Administrador
- O papel `ADMIN` tem alçada estritamente restrita a infraestrutura, suporte técnico, auditoria e gestão de usuários. É **terminantemente proibido** ao `ADMIN` aprovar eventos, fechar o ano paroquial ou assinar atos de governo eclesial.

### RN-IAM-05: Alçada de Criação e Homologação de Usuários
- **ADMIN e PAROCO:** Possuem alçada para criar contas de usuário vinculadas tanto a clérigos (`clerigoId`) quanto a fiéis leigos (`fielId`). As contas criadas por Administrador ou Pároco nascem ativas (`enabled = true`).
- **SECRETARIA_PAROQUIAL:** Possui permissão para criar contas de acesso exclusivamente para fiéis leigos (`fielId`). É estritamente vedada à Secretaria Paroquial a criação de contas associadas a clérigos.
- **Pendência e Notificação de Homologação:** As contas criadas pela Secretaria Paroquial nascem desabilitadas (`enabled = false`). O sistema emite automaticamente uma notificação persistente (`tipo = "USUARIO_APROVACAO_PENDENTE"`) para todos os Párocos e Administradores ativos da paróquia.
- **Papel Base e Acesso Inicial:** Todo usuário associado a um Fiel leigo nasce com a autoridade `ROLE_FIEL` atribuída. Usuários fiéis que ainda não possuam vínculo organizacional ativo em pastorais/movimentos navegam em escopo paroquial padrão (`PAROQUIA`), garantindo acesso ao dashboard pessoal sem falhas de escopo (`ROLE_SCOPE_INVALID`).
- **Aprovação Privativa:** A deliberação e habilitação da conta (`PUT /api/v1/usuarios/{id}/aprovar`) é restrita privativamente a `ADMIN`, `PAROCO` e `BISPO_DIOCESANO`.
- **Remoção de Vínculo:** A remoção (`DELETE /api/v1/usuarios/{id}/membros/{vinculoId}`) só vale para um vínculo que pertença ao fiel do usuário da rota — vínculo alheio é tratado como inexistente — e exige que o ator seja admin/Pároco ou coordenador **da organização daquele vínculo**; coordenar outra organização do mesmo usuário não basta.
- **Alçada no Vínculo Administrativo:** em `POST /api/v1/usuarios/{id}/membros`, atribuir `COORDENADOR` é privativo do Pároco (ou Bispo) e vincular membros a conselhos cabe ao Pároco ou Admin; ter qualquer papel global (como Secretaria Paroquial) não basta.
- **Interoperabilidade de Vínculos:** O endpoint de vinculação a grupos (`POST /api/v1/usuarios/{id}/membros`) aceita `organizacaoId` via DTO `AdicionarVinculoRequest`, prevenindo erros de validação Jackson de campo desconhecido.

- **Sem papel, sem acesso:** um usuário autenticado cujo papel não possa ser reconhecido é negado; nunca é tratado por omissão como coordenador ou qualquer outro perfil com privilégio.

### RN-IAM-06: Pureza do Bounded Context IAM e Segregação de Dados Pastorais
- As APIs e telas do módulo IAM (`/api/v1/usuarios`, `Configuracoes.tsx`) lidam estritamente com **dados de conta e autenticação** (username, status ativo/inativo, roles, id da conta e vínculo de entidade).
- **Expurgo de Dados Pastorais e Sacramentais:** Com exceção do `nome` da pessoa física vinculada (usado exclusivamente como rótulo legível de identificação), é **estritamente vedado** expor ou trafegar dados pessoais, civis, de contato ou sacramentais do fiel/clérigo (como telefone, data de nascimento, comunidade/igreja, batismo, primeira comunhão, crisma ou matrimônio) na API de usuários ou na tela de gerenciamento de usuários. Tais atributos pertencem aos Bounded Contexts de `fiel`, `clero` ou `sacramento`.

### RN-IAM-07: Alçada Exclusiva de Edição e Inativação de Usuários
- **ADMIN e PAROCO:** São as **únicas autoridades** autorizadas a editar dados de contas de usuários (`PATCH /api/v1/usuarios/{id}`) e alterar seu status operacional de ativação/inativação (`PATCH /api/v1/usuarios/{id}/status`).
- **Vedação à Secretaria Paroquial:** A Secretaria Paroquial tem prerrogativa apenas para criação inicial de contas leigas com aprovação pendente. É **terminantemente vedado à Secretaria Paroquial** editar contas existentes, alterar permissões ou inativar/reativar usuários no sistema (tanto via API protegida por `@PreAuthorize("hasAnyRole('ADMIN', 'GLOBAL_ADMIN', 'PAROCO', 'GLOBAL_PAROCO')")` quanto na interface gráfica, onde as ações não são exibidas).

### RN-IAM-08: Fluxos de Redefinição e Autoatendimento de Senhas
- **Alteração de Senha por Usuário Autenticado:** Qualquer usuário logado pode alterar sua própria senha através da aba "Minha Senha" no módulo de configurações (`POST /api/v1/autenticacao/alterar-senha`), exigindo validação prévia da senha atual antes de gravar a nova credencial criptografada com BCrypt.
- **Redefinição Administrativa de Senha:** Redefinir a senha de outro usuário (`POST /api/v1/usuarios/{id}/redefinir-senha`) equivale a assumir a conta dele, por isso a alçada é a mais restrita: `ADMIN` redefine qualquer conta; `PAROCO` redefine apenas contas **sem papel global** (um usuário com papel global — Pároco, Bispo, Vigário, Diácono, Secretaria ou Admin — só tem a senha redefinida pelo `ADMIN`). Coordenadores de pastoral/movimento e demais papéis **não** redefinem senhas, ainda que o alvo seja membro de sua organização.
- **Solicitação Pública de Redefinição de Acesso:** Usuários não autenticados ou impossibilitados de logar podem registrar uma solicitação pública de redefinição na tela de login (`POST /api/v1/autenticacao/solicitar-reset-senha`), informando seu username ou e-mail. A solicitação fica registrada para liberação controlada pela administração paroquial.

---

## 4. Fluxos Operacionais e Casos de Uso
1. **Autenticação:** Login com credenciais -> Validação de hash -> Sessão/Authorities unificadas (globais + vínculos ativos).
2. **Resolução Dinâmica de Contexto de Atores:** O ator é resolvido com tipagem canônica estrita (`EventoActorContextResolver`).
3. **Provisão Canônica de Liderança:** Elevação de fiel a coordenador mediante provisão canônica privativa do Pároco.
4. **Autoatendimento de Credenciais:** Troca de senha pelo próprio usuário autenticado e solicitação anônima de redefinição de acesso na tela inicial.

---

## 5. Integrações com outros Bounded Contexts
- Todos os módulos do monorepo derivam autorizações e auditoria a partir da matriz de 14 papéis canônicos do `iam`.
