# Regras de Negócio e Fluxos: Módulo Membros de Pastorais (`membro`)

O módulo de Membro operacionaliza a relação ativa entre fiéis e as organizações paroquiais (pastorais, movimentos e serviços), registrando papéis de atuação, voluntariado e coordenações.

---

## 1. Visão Geral e Propósito
Permitir a gestão da participação dos leigos nas diversas frentes evangelizadoras da paróquia, gerenciando papéis organizacionais e a provisão de liderança pastoral.

---

## 2. Agregados, Entidades e Value Objects Principais
- **`MembroGrupo` (Raiz de Agregado):** identificado por UUID. Faz o vínculo entre um fiel e uma organização, contendo o tipo da organização, o papel canônico, a igreja de vínculo, a data de entrada, o estado ativo ou inativo e a idoneidade do agente. O vínculo nasce ativo e ativar ou desativar é explícito; o papel nunca é nulo nem é um papel global (`PAROCO`, `VIGARIO`, `ADMIN`), concedidos só pela atribuição de papel global.
- **`PapelOrganizacional` (Camada C):**
  - `COORDENADOR` (`coordenador`): Gestão de agentes, escalas e proposição de projetos da pastoral ou movimento.
  - `SECRETARIO_GRUPO` (`secretario-grupo`): Apoio operacional e registro interno de presenças; é também o papel do vice-coordenador.
  - `MEMBRO_ATIVO` (`membro-ativo`): Participante regular de atividades e escalas do grupo.
  - `VOLUNTARIO` (`voluntario`): Apoio operacional pontual.
  - Nos conselhos valem ainda `SECRETARIO_CONSELHO`, `MEMBRO_CONSELHO_PASTORAL` e `MEMBRO_CONSELHO_ECONOMICO`. Quais papéis cada tipo de organização admite está em `organizacao.md`, RN-ORG-07.
- **`IdoneidadeAgente` (Value Object):** datas de formação doutrinária básica, vivência sacramental atestada pelo Pároco, termo de proteção de menores (assinatura e validade) e envio (missão), com o responsável pelo envio. Guarda somente datas, nunca o conteúdo dos documentos.

---

## 3. Regras de Negócio Invariantes

### RN-MEM-01: Provisão Canônica Privativa de Coordenação
- A promoção de um fiel ao papel de `COORDENADOR` constitui provisão canônica local, sendo de **competência exclusiva do Pároco** (`ROLE_GLOBAL_PAROCO` / `ROLE_CLERO_PAROCO`).
- A coordenação é temporária e nasce de um **mandato homologado** (ver `organizacao.md`, RN-ORG-05); a promoção direta permanece restrita ao Pároco e também exige idoneidade (RN-MEM-04).
- O papel `ADMIN` é expressamente impedido de nomear ou prover coordenadores de pastoral.

- **Cadastro corrente não confere liderança:** o cadastro de membros (`/api/v1/membros`) só atribui papéis de participação comum — voluntário, membro ativo e secretário de grupo. `COORDENADOR` e os papéis de conselho só são conferidos pela promoção do Pároco ou pelo vínculo administrativo da organização, nunca por criação ou edição de membro, nem pela reativação de um vínculo de liderança. Editar dados de um coordenador sem mudar seu papel continua permitido.
- **Quem pode promover:** apenas Pároco (ou Bispo Diocesano). Papéis globais como Secretaria Paroquial, Vigário ou Diácono não promovem coordenadores, e Admin também não.
- **Limite de coordenação:** a organização (por igreja de contexto) tem um único coordenador ativo; a promoção direta é recusada enquanto houver outro. O vínculo administrativo, por sua vez, rebaixa o coordenador anterior a membro comum ao empossar o novo.
- **Idoneidade no vínculo administrativo:** atribuir a coordenação por `POST /api/v1/usuarios/{id}/membros` também exige idoneidade (RN-MEM-04) e que o fiel já seja membro ativo da organização.

### RN-MEM-02: Criação Automatizada de Login
- Ao ser promovido a `COORDENADOR` pelo Pároco, caso o Fiel ainda não possua usuário de sistema (`Usuario`), o login é provisionado automaticamente com credenciais iniciais para acesso às rotas de coordenação.

### RN-MEM-04: Idoneidade e Envio dos Agentes
- O vínculo a uma função exige discernimento pastoral. A `PoliticaIdoneidadeFuncao` define as pendências:
  - **Coordenação (coordenador ou vice):** formação doutrinária básica, vivência sacramental atestada, termo de proteção de menores e vulneráveis vigente e envio (missão) conferido pelo Pároco.
  - **Organismos do múnus de ensinar (catequese e formação):** vivência sacramental atestada e termo de proteção de menores vigente.
  - **Organismos do múnus de santificar (liturgia):** vivência sacramental atestada.
- A proposta e a homologação de mandato e a promoção a coordenador são **bloqueadas** com pendências. Para os demais membros, as pendências são informativas (indicador "Idoneidade" na lista de membros).
- O registro é feito pelo Pároco, Bispo ou secretaria paroquial; a vivência sacramental é **atestada** pelo Pároco (o sistema não deduz sacramentos automaticamente).
- LGPD: apenas datas são armazenadas; o termo e demais documentos permanecem fora do sistema.

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
