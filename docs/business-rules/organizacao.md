# Regras de Negócio e Fluxos: Módulo Organização Pastoral (`organizacao`)

O módulo de Organização modela a estrutura hierárquica e pastoral da paróquia: setores, pastorais, movimentos, serviços, comunidades e territórios eclesiásticos.

---

## 1. Visão Geral e Propósito
Estruturar o organograma da comunidade de fé, permitindo que cada grupo apostólico tenha sua identidade, carisma, lideranças e âmbito de atuação geográfica e pastoral mapeados.

---

## 2. Agregados, Entidades e Value Objects Principais
- **`Organizacao` (Raiz de Agregado):** Identificada por `OrganizacaoId`. Contém nome, sigla, carisma/descrição, tipo de entidade pastoral, data de fundação, organização pai (`ParentId`) e status (`ATIVA`, `INATIVA`).
- **`TipoOrganizacao` (Enum):** `PAROQUIA`, `COMUNIDADE_CAPELA`, `SETOR_PASTORAL`, `PASTORAL`, `MOVIMENTO_ECLESIAL`, `SERVICO_PAROQUIAL`, `MINISTERIO`.
- **`NivelHierarquico` (Value Object):** Nível na árvore organizacional (raiz, setor, grupo base).

---

## 3. Regras de Negócio Invariantes

### RN-ORG-01: Proibição de Ciclos na Hierarquia
- A estrutura de organizações opera como uma árvore estrita direcionada (DAG acíclico). Uma organização não pode ser ancestral ou subordinada de si mesma em nenhum nível de profundidade.

### RN-ORG-02: Comunidade Matriz como Raiz
- A Paróquia Matriz é o nó raiz da governança organizacional local. Todas as capelas, comunidades e setores descendem direta ou indiretamente dela.

### RN-ORG-03: Desativação Segura de Organizações
- A desativação de uma organização exige que não existam eventos ativos futuros vinculados exclusivamente a ela, nem membros com mandatos vigentes de liderança em aberto.

---

## 4. Fluxos Operacionais e Casos de Uso
1. **Gestão do Organograma:** Visualização em árvore e organograma interativo das pastorais e movimentos.
2. **Distribuição Territorial:** Mapeamento de quais pastorais atuam em quais comunidades/capelas.
3. **Filtros Globais:** Capacidade de filtrar dados do sistema por pastoral ou por comunidade.

---

## 5. Integrações com outros Bounded Contexts
- **`membro`:** Associação de fiéis como participantes e coordenadores das pastorais.
- **`calendario`:** Responsabilidade por eventos e missas no calendário.
- **`clero`:** Vínculo de padres assessores ou diáconos acompanhantes de cada pastoral.
