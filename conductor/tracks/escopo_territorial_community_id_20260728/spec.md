# Specification: Escopo Territorial e Vínculo Obrigatório (community_id)

## Overview
Garantir o isolamento de dados paroquiais e a obrigatoriedade de vínculo territorial através da validação estrita do `community_id`. Definir o comportamento de acesso para Pastorais Locais (restritas à capela), Pastorais Globais (multicomunidade) e Movimentos Eclesiais (gerenciados por afinidade carismática ignorando a trava territorial geográfica).

## Functional Requirements
1. **Obrigatoriedade de Cadastro:** Exigir `community_id` válido e não-nulo (`@NotNull`) em requisições de cadastro de Usuários, Membros e Eventos.
2. **Isolamento de Pastorais Locais:** Coordenadores Locais (`COORDENADOR_PASTORAL_LOCAL`) e Voluntários só acessam dados da sua própria capela/comunidade (`community_id`).
3. **Visão Global de Pastorais:** Coordenadores Globais (`COORDENADOR_PASTORAL_GLOBAL`) possuem bypass do `community_id` para a sua pastoral em todas as comunidades.
4. **Afinidade Carismática em Movimentos:** Consultas de membros de Movimentos Eclesiais ignoram o `community_id` geográfico dos fiéis.

## Acceptance Criteria
- [ ] Requisições de cadastro sem `community_id` são rejeitadas com erro de validação.
- [ ] Coordenador pastoral local de uma capela não consegue visualizar nem modificar dados de outra capela.
- [ ] Membros de Movimentos podem ser listados e geridos independentemente da capela de residência.
