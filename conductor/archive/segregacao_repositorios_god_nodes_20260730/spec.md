# Specification: Otimização de Arquitetura Identificada pelo Grafo (Segregação de God Nodes)

## Overview
Com base na análise de acoplamento do Grafo (`graphify`), identificou-se que `UsuarioJpaRepository`, `EventoJpaRepository` e `OrganizacaoJpaRepository` (assim como seus respectivos serviços) atuam como God Nodes de alta centralidade com baixa coesão interna.
Esta track de refatoração objetiva segregar estes repositórios e serviços em sub-interfaces e sub-serviços focados em casos de uso específicos (ex: `UsuarioAutenticacaoRepository`, `UsuarioPerfilRepository`, `EventoConsultaRepository`, `EventoAgendamentoRepository`), substituindo as injeções de dependência nos controllers e use cases existentes pelas sub-interfaces específicas para eliminar o acoplamento excessivo.

## Architectural & Functional Requirements
1. **Segregação de Repositórios por Caso de Uso:**
   - Dividir `UsuarioJpaRepository` em sub-interfaces (ex: `UsuarioAutenticacaoRepository`, `UsuarioPerfilRepository`).
   - Dividir `EventoJpaRepository` em sub-interfaces (ex: `EventoConsultaRepository`, `EventoAgendamentoRepository`).
   - Dividir `OrganizacaoJpaRepository` em sub-interfaces (ex: `OrganizacaoConsultaRepository`, `OrganizacaoGestaoRepository`).
2. **Substituição de Injeções de Dependência (Usages & Controllers):**
   - Atualizar os Controllers, Use Cases e Services para injetar diretamente as novas sub-interfaces específicas de seu escopo (ex: `AuthService` injeta `UsuarioAutenticacaoRepository` em vez de `UsuarioRepository` monolítico).
3. **Decomposição dos Serviços de Domínio/Aplicação:**
   - Decompor `UsuarioService`, `EventoService` e `OrganizacaoService` em serviços focados por caso de uso.
4. **Validação de Qualidade & TDD:**
   - Atualizar a suíte de testes unitários e de integração correspondentes.
   - Garantir 100% de aprovação na compilação, testes e verificação estática (`./gradlew check`).

## Acceptance Criteria
- Repositórios God Nodes segregados em sub-interfaces coesas por caso de uso.
- Injeções de dependência nos componentes existentes migradas para as sub-interfaces específicas.
- Zero regressão funcional e 100% de aprovação em `./gradlew check`.
