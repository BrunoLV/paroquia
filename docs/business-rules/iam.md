# Regras de Negócio e Fluxos: Módulo IAM - Identidade e Acesso (`iam`)

O módulo de IAM gerencia a autenticação, segurança, autorização baseada em papéis (Role-Based Access Control - RBAC) e emissão de tokens de acesso à API.

---

## 1. Visão Geral e Propósito
Garantir que apenas usuários autenticados e formalmente autorizados acessem os recursos do sistema, aplicando o princípio do menor privilégio e protegendo os dados paroquiais.

---

## 2. Agregados, Entidades e Value Objects Principais
- **`Usuario` (Raiz de Agregado):** Identificado por `UsuarioId`. Contém e-mail (username), senha com hash criptográfico forte (BCrypt), status (`ATIVO`, `BLOQUEADO`, `PENDENTE_CONFIRMACAO`), dados de último acesso e papéis associados.
- **`Papel` (Role):** Identificador semântico do perfil de acesso:
  - `ADMIN`: Administrador técnico do sistema.
  - `PAROCO`: Autoridade máxima canônica e executiva da paróquia.
  - `VIGARIO`: Vigário paroquial com amplas permissões sacramentais e pastorais.
  - `DIACONO`: Diácono com acesso a escalas litúrgicas e pastorais.
  - `SECRETARIO`: Operador administrativo da secretaria paroquial.
  - `LIDER_PASTORAL`: Coordenador de pastoral com acesso ao escopo da sua organização.
  - `FIEL`: Leigo com acesso ao portal do dizimista/fiel.
- **`Permissao` (Authority):** Capacidade atômica (ex.: `EVENTO_CRIAR`, `EVENTO_APROVAR`, `FIEL_VISUALIZAR_CIVIL`).

---

## 3. Regras de Negócio Invariantes

### RN-IAM-01: Controle de Acesso Declarativo (@PreAuthorize)
- Todos os endpoints sensíveis no backend Java **DEVEM conter anotações declarativas `@PreAuthorize`** baseadas em papéis ou permissões. Nenhum endpoint de mutação de dados pode permanecer desprotegido.

### RN-IAM-02: Isolamento de Tokens JWT
- A autenticação é stateless via JSON Web Tokens (JWT) assinados criptograficamente. O payload do token carrega apenas o identificador do usuário e suas authorities, **sem expor dados pessoais (PII)**.

### RN-IAM-03: Bloqueio Preventivo por Falhas Consecutivas
- Tentativas sucessivas de login incorreto disparam temporizador de bloqueio e registro na auditoria de segurança.

---

## 4. Fluxos Operacionais e Casos de Uso
1. **Autenticação:** Login com credenciais -> Validação de hash -> Emissão de Access Token e Refresh Token.
2. **Troca de Senha Segura:** Redefinição com verificação de complexidade mínima e e-mail de confirmação.
3. **Delegação de Acessos:** Atribuição e revogação de papéis executada exclusivamente por usuários `ADMIN` ou `PAROCO`.

---

## 5. Integrações com outros Bounded Contexts
- Todos os demais módulos dependem do `iam` para identificação do usuário autenticado (`AuditingEntityListener`) e verificação de alçadas em regras de negócio.
