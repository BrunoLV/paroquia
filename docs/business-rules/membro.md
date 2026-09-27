# Regras de Negócio e Fluxos: Módulo Membros de Pastorais (`membro`)

O módulo de Membro operacionaliza a relação ativa entre fiéis e as organizações paroquiais (pastorais, movimentos e serviços), registrando papéis de atuação, voluntariado e coordenações.

---

## 1. Visão Geral e Propósito
Permitir a gestão da participação dos leigos nas diversas frentes evangelizadoras da paróquia, gerenciando papéis organizacionais e a provisão de liderança pastoral.

---

## 2. Agregados, Entidades e Value Objects Principais
- **`MembroGrupo` (Raiz de Agregado):** Identificado por `MembroGrupoId`. Faz o vínculo entre um `FielId` e uma `GrupoParoquialId`, contendo status da participação, data de ingresso, data de desligamento e papel canônico.
- **`PapelOrganizacional` (Camada C):**
  - `COORDENADOR` (`coordenador`): Gestão de agentes, escalas e proposição de projetos da pastoral ou movimento.
  - `SECRETARIO_GRUPO` (`secretario-grupo`): Apoio operacional e registro interno de presenças.
  - `MEMBRO_ATIVO` (`membro-ativo`): Participante regular de atividades e escalas do grupo.
  - `VOLUNTARIO` (`voluntario`): Apoio operacional pontual.
- **`StatusMembro`:** `ATIVO`, `INATIVO`.

---

## 3. Regras de Negócio Invariantes

### RN-MEM-01: Provisão Canônica Privativa de Coordenação
- A promoção de um fiel ao papel de `COORDENADOR` constitui provisão canônica local, sendo de **competência exclusiva do Pároco** (`ROLE_GLOBAL_PAROCO` / `ROLE_CLERO_PAROCO`).
- O papel `ADMIN` é expressamente impedido de nomear ou prover coordenadores de pastoral.

### RN-MEM-02: Criação Automatizada de Login
- Ao ser promovido a `COORDENADOR` pelo Pároco, caso o Fiel ainda não possua usuário de sistema (`Usuario`), o login é provisionado automaticamente com credenciais iniciais para acesso às rotas de coordenação.

### RN-MEM-03: Exclusividade de Coordenação Ativa
- Cada organização paroquial deve possuir preferencialmente uma liderança coordenadora ativa.

---

## 4. Fluxos Operacionais e Casos de Uso
1. **Vinculação de Membro:** Inclusão de um fiel como voluntário ou membro ativo da pastoral.
2. **Promoção a Coordenador:** O Pároco designa formalmente o coordenador e gera acesso sistêmico.
3. **Desativação de Vínculo:** Alteração de status para inativo mantendo integridade histórica.

---

## 5. Integrações com outros Bounded Contexts
- **`fiel`:** Associação ao cadastro pessoal do fiel.
- **`organizacao`:** Associação com a pastoral, movimento ou serviço.
- **`iam`:** Criação de usuário e atribuição de autoridade `ROLE_<TIPO>_COORDENADOR`.
