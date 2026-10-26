# Regras de Negócio e Fluxos: Módulo Conselhos e Atas (`conselho`)

O módulo de Conselhos é o registro oficial das reuniões da paróquia e das comunidades: guarda as atas, as deliberações e os pareceres dos colegiados e dos demais encontros pastorais, conduz a homologação do Pároco e produz o documento formal em Word e PDF. Tem como base o Código de Direito Canônico (Cânn. 536 e 537) e o Documento 100 da CNBB, *Comunidade de comunidades: uma nova paróquia* (nn. 291-294), que pede o funcionamento do Conselho de Pastoral Paroquial e do Conselho de Assuntos Econômicos e a concordância entre eles.

---

## 1. Visão Geral e Propósito

Assegurar que o que foi tratado e deliberado nas reuniões fique registrado de forma fidedigna, auditável e imutável depois de selado, respeitando a natureza **consultiva** dos conselhos e a presidência nata do Pároco. O módulo resolve:

- o registro da ata de cada reunião, por **tipo de reunião**, com pauta, deliberações, presença e, onde cabe, quórum e votação;
- a separação entre quem redige a ata e quem a homologa;
- a correção de ata já selada por **ata retificadora**, sem jamais alterar a original;
- a exportação da ata, ou de uma compilação delas, em Word e PDF, com a identidade institucional da paróquia;
- a trilha de auditoria completa de tudo o que é feito com cada ata, inclusive cada exportação.

---

## 2. Agregados, Entidades e Value Objects Principais

- **`AtaReuniao` (Raiz de Agregado):** identificada por `AtaReuniaoId`. Representa a ata de uma reunião: tipo, órgão convocante (e órgão associado, na reunião conjunta), caráter, modalidade, título, data, local, presença, ata anterior, itens de pauta, situação, número, redator, homologador e motivo de veto. As organizações e os fiéis são referenciados apenas por identificador.
- **`TipoReuniao`:** catálogo de 12 tipos (seção 3).
- **`StatusAta`:** `RASCUNHO`, `PENDENTE_HOMOLOGACAO`, `HOMOLOGADA`, `VETADA` e `REGISTRADA`. *Homologada* e *registrada* são estados **selados**.
- **`ItemPauta`:** assunto da ordem do dia, com título, relato resumido da discussão e as deliberações dele decorrentes.
- **`Deliberacao`:** texto, **resultado** (`PARECER_FAVORAVEL`, `PARECER_CONTRARIO`, `ADIADA`, `INFORMATIVA`), votação opcional e declarações de conflito de interesses.
- **`Votacao`:** votos a favor, contra e abstenções; consultiva.
- **`ConflitoInteresse`:** declaração de um participante sobre uma deliberação, com descrição.
- **`Presenca`:** pessoa (por identificador do fiel), função na reunião e situação (`PRESENTE`, `AUSENTE_JUSTIFICADA`, `AUSENTE`); ou, nas assembleias, apenas o **total de participantes**.
- **`AtaAnterior`:** referência à ata da reunião anterior e o resultado da sua leitura (`APROVADA`, `APROVADA_COM_RESSALVAS` ou `NAO_APLICAVEL`).
- **`PapelOrganizacional` (conselhos):** `SECRETARIO_CONSELHO` (redige e submete as atas), `MEMBRO_CONSELHO_PASTORAL` e `MEMBRO_CONSELHO_ECONOMICO` (conselheiros, sem poder de redação), além de `COORDENADOR` e do vice-coordenador (`SECRETARIO_GRUPO`), que são membros do conselho (`organizacao.md`, RN-ORG-07).

---

## 3. Tipos de Reunião

Os colegiados têm base explícita na CNBB e no Direito Canônico. Os demais tipos refletem a prática pastoral e a organização da paróquia em rede de comunidades; o Documento 100 não os define formalmente.

| Tipo | Convocante | Homologa | Quórum | Presença | Votação | Redator |
| :--- | :--- | :---: | :---: | :--- | :---: | :--- |
| `REUNIAO_CPP` — Conselho Pastoral Paroquial | Conselho CPP | sim | sim | nominal, membros | sim | Secretário do Conselho |
| `REUNIAO_CPC` — Conselho Pastoral Comunitário | Conselho CPC | sim | sim | nominal, membros | sim | Secretário do Conselho |
| `REUNIAO_CAEP` — Conselho de Assuntos Econômicos | Conselho CAEP | sim | sim | nominal, membros | sim | Secretário do Conselho |
| `REUNIAO_CONJUNTA_CPP_CAEP` — Reunião conjunta | Conselho CPP + Conselho CAEP associado | sim | sim | nominal, membros dos dois | sim | Secretário do Conselho |
| `REUNIAO_PASTORAL` — Conselho e coordenadores | Conselho CPP ou CPC | sim | não | nominal, livre | sim | Secretário do Conselho |
| `REUNIAO_COORDENADORES` — Coordenadores | Conselho CPP ou CPC | não | não | nominal, livre | não | Secretário do Conselho |
| `ASSEMBLEIA_PAROQUIAL` | Conselho CPP | sim | não | total de participantes | não | Secretário do Conselho |
| `ASSEMBLEIA_COMUNITARIA` | Conselho CPC | não | não | total de participantes | não | Secretário do Conselho |
| `REUNIAO_ORGANISMO` — Pastoral, movimento ou serviço | Pastoral, movimento ou serviço paroquial | não | não | nominal, membros | não | Secretário de Grupo ou Coordenador |
| `PLANEJAMENTO_PASTORAL` | Conselho CPP | sim | não | nominal, livre | não | Secretário do Conselho |
| `COMISSAO_ESPECIAL` | Qualquer organização | não | não | nominal, livre | não | Secretário de Grupo, Coordenador ou Secretário do Conselho |
| `REUNIAO_ANIMADORES` — Animadores de comunidades | Conselho CPP ou CPC | não | não | nominal, livre | não | Secretário do Conselho |

*Caráter* (`ORDINARIA` ou `EXTRAORDINARIA`) e *modalidade* (`PRESENCIAL`, `VIRTUAL` ou `HIBRIDA`) são atributos da reunião, independentes do tipo.

---

## 4. Regras de Negócio Invariantes

### RN-CON-01: Presidência Canônica Nativa e Voto Consultivo (Cân. 536 §2 e Cân. 537)
- O Pároco (ou Administrador Paroquial) preside nativamente os conselhos da paróquia. Os conselhos têm **estritamente voto consultivo**: a votação registrada é a posição do colegiado para orientar o Pároco, e o resultado de cada deliberação é um **parecer**, não uma decisão com força própria.

### RN-CON-02: Segregação entre Redator e Homologador
- A redação da ata, o registro das deliberações e a submissão competem ao **redator do tipo de reunião**, no órgão convocante. O Pároco não redige.
- A homologação e o veto são **ato privativo e indelegável do Pároco** (ou do Bispo Diocesano). O papel `ADMIN` não homologa, não veta e não lê o conteúdo das atas.
- Um redator de outro órgão não pode redigir nem submeter atas deste órgão.
- O papel de redator vale **por órgão**, pelo vínculo da pessoa com ele, e não pelo perfil global do login: quem é secretário do CPP não redige atas do CAEP. O sistema informa a cada usuário os órgãos pelos quais ele pode redigir cada tipo, e o formulário só oferece esses tipos e órgãos; a recusa informa o papel exigido.

### RN-CON-03: Selagem, Imutabilidade e Ata Retificadora
- Uma ata nasce em `RASCUNHO` e só é editável nesse estado. A submissão leva à homologação do Pároco nos tipos que a exigem; nos demais, a ata é **registrada** e selada de imediato.
- Ata **homologada** ou **registrada** é **estritamente imutável** e nunca é excluída. A correção é feita por **ata retificadora**: uma nova ata, ligada à original, aberta em rascunho com justificativa obrigatória e que percorre o fluxo normal do seu tipo. A original permanece intacta.
- Uma ata submetida aguarda a decisão do Pároco e não pode ser editada. O veto exige **motivo**; a ata vetada é devolvida ao redator por reabertura, com o motivo do veto à vista até a nova submissão.

### RN-CON-04: Regras por Tipo de Reunião
- Cada tipo define quais órgãos o convocam, se exige homologação, se apura quórum, como se registra a presença, se admite votação e quais papéis redigem (tabela da seção 3). Um tipo só pode ser convocado por órgão compatível; a reunião conjunta exige o Conselho de Assuntos Econômicos (CAEP) como órgão associado.
- Os tipos que não exigem homologação terminam em `REGISTRADA`, selados e imutáveis, sem passar pelo Pároco.
- **Consistência entre tipo e órgão:** a coluna "Convocante" da tabela da seção 3 é a regra, e ela é conferida em quatro pontos: na criação da ata; de novo ao editar o rascunho, ao submeter e ao homologar, porque o órgão pode ter sido desativado ou ter mudado de natureza desde o rascunho (o veto continua possível); no formulário, que só oferece os tipos e órgãos pelos quais o usuário redige; e por um gatilho do banco, que impede gravar ata de tipo incompatível com o órgão mesmo por carga direta.
- **Reunião conjunta:** o CAEP associado precisa ser da **mesma unidade** (mesma organização-mãe) do CPP convocante. Assim o quórum e os membros dos dois conselhos dizem respeito à mesma comunidade de fiéis. Um tipo que não é conjunta não admite órgão associado.
- A "Reunião do CAEP" aceita o conselho econômico da paróquia e o das comunidades, pois não há outro tipo para as reuniões deste último.

### RN-CON-05: Presença e Quórum
- A presença é **nominal** (lista de pessoas, identificadas pelo cadastro de fiéis ou, no caso do clero, de clérigos ativos, nunca digitadas) ou **por total de participantes**, conforme o tipo.
- **Presença só de membros** (CPP, CPC, CAEP, reunião conjunta e organismo): só constam membros ativos do órgão (na reunião conjunta, dos dois órgãos).
- **Presença livre** (reunião de pastoral, de coordenadores, de animadores, planejamento pastoral e comissão especial): consta qualquer fiel **ativo** do cadastro. O formulário oferece, além dos membros do órgão convocante, os membros ativos das pastorais, movimentos e serviços articulados a ele, identificados pelo nome do organismo; o coordenador do organismo é sugerido como "Coordenador(a) de pastoral ou organismo" e o vice como "Representante de pastoral ou movimento". "Incluir todos os membros" inclui apenas os do órgão convocante.
- **Busca de fiéis** (só na presença livre): o redator busca qualquer fiel ativo pelo nome, com ao menos 3 letras, no máximo 20 resultados, sem diferenciar maiúsculas nem acentos. A busca devolve só identificador e nome, é privativa de quem redige o tipo no órgão e é recusada nos tipos de presença só de membros. Quem não está cadastrado como fiel não pode constar, pois o nome nunca é digitado.
- **O Pároco é o presidente dos conselhos** (RN-CON-01); o conselho tem ainda o seu coordenador e vice-coordenador, funções distintas da presidência e dos coordenadores de pastoral. O coordenador e o vice-coordenador **não deixam de ser membros**: cada um tem vínculo de membro com o conselho, com o papel de coordenador e de vice (o mesmo papel de secretário de grupo das pastorais), e o formulário da ata sugere a função a partir desse vínculo: nas reuniões nominais convocadas por conselho ele consta na lista, já incluído pelo formulário como Presidente (a situação é editável; ausente, fica sem função). Os demais clérigos (vigário, diácono) podem constar na lista com a função que o encargo sugere. O clero é escolhido entre os clérigos ativos, pois não é cadastrado como fiel, e fica fora da regra "só membros do órgão".
- O **quórum** é a maioria absoluta dos membros ativos convocados, **mais o Pároco**, que preside por direito próprio nos tipos com quórum (metade mais um); no quórum contam os membros presentes e o Pároco presente como Presidente, e o outro clero presente apenas assiste; na reunião conjunta, a maioria absoluta do total de membros dos dois conselhos. O total de convocados é **congelado na submissão** para que o quórum de uma ata não mude quando a composição do órgão mudar. O quórum é **informativo**: consta da ata e dos documentos, mas não impede a lavratura.
- Ausência justificada não conta como presença.
- **Quem redige consta na lista:** o redator da ata é incluído como **presente**, com a função do seu papel (Secretário(a) para secretário de conselho ou de grupo; Coordenador(a) de pastoral ou organismo para coordenador). O servidor recusa a ata nominal em que ele falte, esteja ausente ou tenha outra função.
- **Sem conflitos de função:** a mesma pessoa não consta duas vezes; as funções de presidência, coordenação do conselho e secretaria (Pároco, Vigário paroquial, Presidente, Coordenador(a) do conselho, Vice-coordenador(a) do conselho, Secretário(a) e Tesoureiro(a)) só podem ser exercidas por uma pessoa por reunião; e só quem esteve presente exerce função.
- A **função** de cada pessoa na reunião é escolhida de uma lista fixa, validada também no servidor (Pároco, Vigário paroquial, Diácono, Presidente, Coordenador(a) do conselho, Vice-coordenador(a) do conselho, Secretário(a), Tesoureiro(a), Coordenador(a) de pastoral ou organismo, Representante de pastoral ou movimento, Membro, Convidado(a)); o **local** da reunião é escolhido entre os locais ativos cadastrados.

### RN-CON-06: Modelo da Ata
- A ata é **estruturada**: cabeçalho da reunião, presença, leitura e aprovação da ata anterior, **itens de pauta** numerados com relato resumido e as **deliberações** de cada item.
- **Ata anterior:** quando informada, deve ser ata selada do mesmo órgão e de data anterior; se aprovada com ressalvas, as ressalvas são obrigatórias.
- **Votação:** só existe nos tipos que votam; os votos não podem exceder os presentes aptos a votar.
- **Conflito de interesses:** qualquer deliberação, de qualquer tipo, pode ter declaração de conflito. O declarante deve constar como presente e seu voto não entra na contagem daquela deliberação.
- Para submeter, a ata precisa ter ao menos um item de pauta e a presença informada; nos tipos com quórum, o órgão precisa ter membros ativos.

### RN-CON-07: Numeração
- A ata recebe seu **número** (`003/2026`) ao ser selada, em sequência por órgão convocante e por ano civil (fuso de Brasília). Antes disso é **minuta** e não tem número. Dois selamentos simultâneos nunca recebem o mesmo número.

### RN-CON-08: Exportação em Word e PDF
- Qualquer ata que o usuário possa ler pode ser exportada em **PDF** ou **Word (.docx)**, em dois layouts do mesmo registro: **estruturado** (seções e tabelas) e **narrativo** (texto corrido de Livro de Atas, gerado dos mesmos campos). Pode-se exportar a ata completa ou apenas o **quadro de decisões**.
- O documento traz o cabeçalho institucional (paróquia, diocese, endereço e logotipo), o título conforme o tipo, o número da ata, a presença e o quórum, a ata anterior, a pauta com as deliberações, os votos e conflitos, as linhas de assinatura (redator e, se o tipo homologa, Pároco) e a data de emissão.
- Ata **selada** sai como documento oficial, com o carimbo de homologação ou de registro. Ata **não selada** sai como **minuta**, com a marca "MINUTA — SEM EFEITO" e sem carimbo.
- A **compilação** (livro de atas e decisões) reúne as atas seladas de um órgão e período em um único arquivo, em ordem cronológica, e se limita a 200 atas por arquivo.

### RN-CON-09: Trilha de Auditoria
- Cada ação sobre a ata (abrir rascunho, editar, submeter, homologar, vetar, reabrir, retificar) grava uma entrada **imutável** com autor, perfil, endereço de origem e o estado antes e depois. **Cada exportação também é gravada**, com formato, layout, conteúdo e situação da ata no momento.
- A trilha é consultada pelo Pároco, pelo Bispo Diocesano e pela secretaria paroquial. O log global de auditoria recebe somente identificadores opacos, sem conteúdo da ata nem nomes.

### RN-CON-10: Escopo de Leitura
- Pároco, Bispo Diocesano, Vigário e secretaria paroquial leem as atas de todos os órgãos. Os demais usuários leem apenas as atas dos órgãos a que estão vinculados (na reunião conjunta, também as do órgão associado). Quem não pode ler uma ata recebe "não encontrada", para não revelar a existência de atas de outros órgãos.
- As ações oferecidas ao usuário (editar, submeter, homologar, vetar, reabrir, retificar, exportar, ver trilha) dependem do seu papel, do tipo de reunião e da situação da ata.

---

## 5. Fluxos Operacionais e Casos de Uso
1. **Redação:** o redator abre a ata em rascunho escolhendo o tipo e o órgão convocante, informa dados da reunião, presença, ata anterior e itens de pauta com suas deliberações, e grava quantas vezes precisar.
2. **Submissão:** o redator conclui a ata. Nos tipos com homologação, ela segue ao Pároco; nos demais, é registrada, numerada e selada.
3. **Decisão do Pároco:** homologa (sela e numera) ou veta com motivo. A ata vetada volta ao redator, que a reabre, corrige e submete de novo.
4. **Retificação:** sobre ata selada, o redator abre a ata retificadora com justificativa; a original não muda.
5. **Exportação:** em qualquer momento, o usuário baixa a ata (ou a compilação do livro de atas) em Word ou PDF; cada exportação fica na trilha.

---

## 6. Integrações com outros Bounded Contexts
- **`organizacao`:** órgão convocante e órgão associado, tipo e subtipo do conselho e membros ativos para presença e quórum.
- **`fiel`:** identificação das pessoas na presença e nos conflitos de interesses, e busca de fiéis ativos pelo nome nas reuniões de presença livre; os nomes são resolvidos na leitura e na exportação, nunca guardados na ata.
- **`iam`:** papéis globais e vínculos por organização que definem leitura e redação.
- **`governanca` / `calendario`:** identidade institucional (paróquia, diocese, endereço e logotipo) e log global de auditoria.

---

## 7. Privacidade & Segurança (LGPD e RBAC)
- A ata guarda **apenas identificadores** das pessoas; nomes aparecem só na leitura e nos documentos, para quem tem permissão de ler a ata.
- Clérigos e leigos recebem o mesmo tratamento de privacidade na presença e nos conflitos de interesses.
- A busca de fiéis para a presença expõe apenas identificador e nome, nunca contato ou documento, e só a quem redige o tipo no órgão.
- O mínimo privilégio vale também para o `ADMIN`, que não acessa o conteúdo das atas.
- O endereço de origem e os estados completos da trilha ficam apenas no banco, para perícia; a consulta mostra autor, perfil, ação e data.
- Os documentos exportados não ficam armazenados nem em cache: são gerados a cada pedido e cada pedido é auditado.
