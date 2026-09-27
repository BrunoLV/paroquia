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

---

## 4. Fluxos Operacionais e Casos de Uso
1. **Auditoria Transacional:** Captura automática do contexto de segurança Spring Security (`SecurityContextHolder`) e persistência atômica.
2. **Trilha de Auditoria:** Painel para o Pároco e Auditoria Diocesana inspecionarem quem realizou determinada alteração crítica.

---

## 5. Integrações com outros Bounded Contexts
- Todos os bounded contexts dependem de `BaseAuditableEntity` e da infraestrutura de governança.
