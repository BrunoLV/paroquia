# Specification: Escopo Territorial e Vínculo Obrigatório (community_id)

## Overview
Garantir o isolamento de dados paroquiais e a obrigatoriedade de vínculo territorial através da validação estrita do `community_id` tanto na API/Backend quanto na Interface do Usuário (Frontend). Definir o comportamento de acesso e componentes visuais para Pastorais Locais (restritas à capela), Pastorais Globais (multicomunidade) e Movimentos Eclesiais (gerenciados por afinidade carismática ignorando a trava territorial geográfica).

## Functional Requirements

### Backend & API
1. **Obrigatoriedade de Cadastro:** Exigir `community_id` válido e não-nulo (`@NotNull`) em requisições de cadastro e atualização de Usuários, Membros e Eventos.
2. **Isolamento de Pastorais Locais:** Coordenadores Locais (`COORD_PASTORAL_LOCAL`) e Voluntários só acessam dados da sua própria capela/comunidade (`community_id`).
3. **Visão Global de Pastorais:** Coordenadores Globais (`COORD_PASTORAL_GLOBAL`) possuem bypass do `community_id` para a sua pastoral em todas as comunidades.
4. **Afinidade Carismática em Movimentos:** Consultas de membros de Movimentos Eclesiais ignoram o `community_id` geográfico dos fiéis.

### Frontend (UI)
5. **Formulários e Seleção Obrigatória de Comunidade:** Exigir a seleção de comunidade (`community_id` / `igrejaVinculoId`) nos formulários de cadastro e edição de Eventos, Usuários e Membros, com sinalização visual de campo obrigatório e validação antes da submissão.
6. **Seletor de Escopo Conforme Perfil:**
   - Para perfis locais (`COORD_PASTORAL_LOCAL`), pré-selecionar e fixar a comunidade vinculada ao usuário nos formulários e filtros de contexto, impedindo a escolha indevida de outras capelas.
   - Para perfis globais e movimentos (`COORD_PASTORAL_GLOBAL`, `COORD_MOVIMENTO`), manter a seleção multicomunidade habilitada.
7. **Tratamento e Apresentação de Erros de Escopo:** Exibir mensagens amigáveis e alertas visuais padronizados quando ocorrer violação de escopo territorial ou falta do vínculo de comunidade.

## Acceptance Criteria
- [ ] Requisições do backend e formulários da UI sem `community_id` são rejeitados com erro de validação claro.
- [ ] Coordenador pastoral local de uma capela não consegue visualizar, selecionar ou modificar dados de outra capela na UI ou na API.
- [ ] Membros de Movimentos podem ser listados e geridos independentemente da capela de residência na UI.
- [ ] Execução das suítes completas de testes no backend (`./gradlew test`) e frontend (`npm test`) sem regressões.
