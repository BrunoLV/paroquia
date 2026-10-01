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
- **Interoperabilidade de Vínculos:** O endpoint de vinculação a grupos (`POST /api/v1/usuarios/{id}/membros`) aceita tanto `grupoParoquialId` quanto o alias `organizacaoId` via DTO `AdicionarVinculoRequest`, prevenindo erros de validação Jackson de campo desconhecido.

---

## 4. Fluxos Operacionais e Casos de Uso
1. **Autenticação:** Login com credenciais -> Validação de hash -> Sessão/Authorities unificadas (globais + vínculos ativos).
2. **Resolução Dinâmica de Contexto de Atores:** O ator é resolvido com tipagem canônica estrita (`EventoActorContextResolver`).
3. **Provisão Canônica de Liderança:** Elevação de fiel a coordenador mediante provisão canônica privativa do Pároco.

---

## 5. Integrações com outros Bounded Contexts
- Todos os módulos do monorepo derivam autorizações e auditoria a partir da matriz de 14 papéis canônicos do `iam`.
