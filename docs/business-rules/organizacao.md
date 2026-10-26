# Regras de Negócio e Fluxos: Módulo Organização Pastoral (`organizacao`)

O módulo de Organização modela os organismos eclesiais da paróquia: pastorais, movimentos apostólicos, serviços paroquiais e conselhos, além do território (Matriz e Comunidades) a que se articulam.

---

## 1. Visão Geral e Propósito

A paróquia é uma comunidade de comunidades confiada a um Pároco como pastor próprio (Cân. 515 §1). Pastorais e movimentos não são departamentos autônomos, mas formas orgânicas de participação dos fiéis na tríplice missão de Cristo: **ensinar** (evangelização, catequese e formação), **santificar** (liturgia, oração e sacramentos) e **governar** (caridade organizada, promoção humana e administração eclesial).

O módulo gerencia a comunhão pastoral, a legitimidade canônica e a administração responsável sob a autoridade do Pároco.

---

## 2. Agregados, Entidades e Value Objects Principais

- **`Organizacao` (Raiz de Agregado):** identificada por UUID. Contém nome, contato, tipo, múnus (`SetorPastoral`), subtipo de conselho, vínculo com o catálogo oficial, organização-mãe (`parentId`) e status ativo/inativo. O coordenador e o vice-coordenador **não são gravados na organização**: a leitura os deriva dos vínculos de membro ativos com os papéis de coordenador e de vice (secretário de grupo).
- **`TipoOrganizacao`:**
  - `PASTORAL` — **pastoral orgânica**: ação direta da Igreja local, vinculada ao plano de pastoral da paróquia e da diocese (ex.: Batismo, Catequese, Dízimo).
  - `MOVIMENTO` — **movimento apostólico ou associação de fiéis**: espiritualidade, método e itinerário próprios, de âmbito diocesano, nacional ou internacional (ex.: RCC, ECC, Terço dos Homens). Exige estatuto reconhecido.
  - `SERVICO_PAROQUIAL` — suporte litúrgico e operacional direto (ex.: Coroinhas, Acolhida, Pascom, Ministros Extraordinários da Sagrada Comunhão).
  - `CONSELHO` — órgão colegiado consultivo.
- **`SubtipoConselho`:** `CPP` (Conselho Pastoral Paroquial, Cân. 536), `CPC` (conselho pastoral comunitário) e `CAEP` (conselho para assuntos econômicos, Cân. 537).
- **`SetorPastoral`:** os três múnus — ensinar (`FORMACAO_ENSINAR`), santificar (`SANTIFICACAO_LITURGIA`) e governar (`SOCIAL_CARIDADE`).
- **`PastoralMovimentoOficial` (catálogo):** registro-mestre ao qual pastorais e movimentos se vinculam. Guarda o âmbito (`AmbitoOrganismo`) e o `ReconhecimentoEstatuto` (autoridade, data e referência do decreto).
- **`MandatoCoordenacao` (Raiz de Agregado):** encargo temporário de coordenação ou vice-coordenação exercido por um **Fiel**. Estados: `PROPOSTO`, `HOMOLOGADO`, `VETADO`, `ENCERRADO`.
- **`UnidadeTerritorial`:** Matriz e Comunidades, raiz territorial distinta das organizações.

---

## 3. Regras de Negócio Invariantes

### RN-ORG-01: Articulação Orgânica ao Conselho Pastoral (Cân. 536)
- Pastorais, movimentos e serviços paroquiais têm como organização-mãe um conselho **pastoral** (`CPP` ou `CPC`) ativo, expressão da comunhão pastoral e da corresponsabilidade no plano pastoral.
- O `CAEP` é de natureza temporal e nunca é organização-mãe de um organismo pastoral.
- Um conselho tem como organização-mãe uma Matriz ou Comunidade ativa. Uma organização nunca é sua própria mãe.

### RN-ORG-02: Tipologia e Invariantes por Tipo
- `PASTORAL`: vínculo obrigatório com o catálogo oficial (item do tipo pastoral) e múnus obrigatório.
- `MOVIMENTO`: vínculo obrigatório com o catálogo oficial (item do tipo movimento) **cujo estatuto esteja reconhecido pela autoridade eclesiástica** (Cân. 299 e 304). Movimento sem estatuto reconhecido não pode ser instanciado.
- `SERVICO_PAROQUIAL`: múnus obrigatório.
- `CONSELHO`: subtipo obrigatório.
- As invariantes vivem em policies stateless por tipo (`RegrasTipologiaOrganizacao`), não em condicionais do serviço.

### RN-ORG-03: Desativação Segura
- Uma organização com mandato de coordenação vigente não pode ser desativada nem excluída: os mandatos devem ser encerrados antes. A exclusão também é bloqueada por membros ou eventos vinculados.

### RN-ORG-04: Unidade de Caixa e Personalidade Jurídica Única
- Pastorais, movimentos, serviços e conselhos locais **não possuem personalidade jurídica nem caixa próprios**: qualquer recurso arrecadado em seu nome pertence à pessoa jurídica paroquial, sujeito à prestação de contas e à fiscalização do CAEP (Cân. 537 e 1284).
- Nenhum registro financeiro do sistema pode tratar uma organização como titular de conta ou saldo; no máximo, como centro de custo da paróquia.

### RN-ORG-05: Mandato de Coordenação como Encargo Temporário (Cân. 519 e 317)
- Nenhuma liderança laical tem mandato legítimo sem a **confirmação do Pároco**. A coordenação é um encargo eclesial temporário, e não uma eleição civil autônoma.
- O mandato é **proposto** (Pároco, Bispo ou mesa do CPP), e somente o Pároco o **homologa**, **veta** (com motivo obrigatório) ou **encerra**. O papel `ADMIN` não homologa.
- A duração máxima é de **3 anos** (`PoliticaDuracaoMandato`); a renovação é um novo mandato, sempre homologado.
- Não pode haver dois mandatos proposto/homologado concomitantes para a mesma função e período na mesma organização.
- Só um **membro ativo** da organização pode ser proposto.
- Ao homologar, o vínculo do fiel assume o papel da função (`COORDENADOR` ou `SECRETARIO_GRUPO` para vice) e a organização passa a exibir o nome dele como coordenador ou vice. Ao encerrar (por decisão ou pelo job diário de mandatos vencidos), o vínculo volta ao papel de membro comum do tipo (`MEMBRO_ATIVO`; no conselho, membro do conselho pastoral ou econômico conforme o subtipo) e a organização deixa de exibi-lo na coordenação.
- O coordenador e o vice-coordenador **não deixam de ser membros**: em qualquer organização, conselho inclusive, o fiel precisa ser membro ativo e o seu vínculo é promovido.
- O cadastro de organização (API e importação em lote) não aceita coordenador nem vice-coordenador: a requisição que os envia é recusada, pois a liderança só muda por mandato de um membro existente.

### RN-ORG-06: Idoneidade e Envio dos Agentes
- Ver `membro.md` (RN-MEM-04). A proposta e a homologação de mandato exigem agente idôneo para coordenar.

### RN-ORG-07: Papéis de Vínculo por Tipo de Organização
- Existe **uma única tabela** de quais papéis um vínculo pode ter em cada tipo de organização. Pastoral, movimento e serviço paroquial admitem coordenador, secretário de grupo (que é também o papel do vice-coordenador), membro ativo e voluntário. O conselho admite secretário de conselho, membro do conselho pastoral, membro do conselho econômico, coordenador e vice-coordenador (secretário de grupo).
- Todos os caminhos que criam ou alteram vínculo (cadastro de membros, promoção a coordenador, vínculo por usuário e mandato de coordenação) recusam o papel que o tipo não admite. O contexto do calendário consulta a mesma tabela para decidir autorização, e a interface só oferece os papéis válidos do tipo.
- O banco repete a regra: um vínculo com papel que o tipo da organização não admite não é gravado. Uma tabela de normalização guarda, por vínculo, o papel anterior de quem teve o papel trocado pelo de membro comum do tipo.

---

## 4. Fluxos Operacionais e Casos de Uso
1. **Cadastro de Organismo:** o Pároco cadastra pastoral, movimento, serviço ou conselho, escolhendo tipo, múnus (ou subtipo), vínculo oficial e o conselho a que se articula.
2. **Catálogo Oficial:** o administrador, o Pároco ou o Bispo mantêm o catálogo, registrando âmbito e reconhecimento de estatuto dos movimentos.
3. **Proposta, Homologação e Encerramento de Mandato:** pela ação "Coordenação" da lista de organizações.
4. **Gestão do Organograma:** visualização em árvore das organizações.
5. **Filtros Globais:** filtro de dados por organização ou comunidade.

---

## 5. Integrações com outros Bounded Contexts
- **`membro`:** vínculos Fiel–organização, papéis e idoneidade dos agentes.
- **`conselho`:** decisões e atas dos conselhos (CPP/CPC), com homologação do Pároco.
- **`calendario`:** eventos e missas atribuídos à organização responsável.
- **`patrimonio`:** a organização pode ser responsável por ativos, mas nunca titular patrimonial próprio (RN-ORG-04).
- **`clero`:** vínculo de padres assessores ou diáconos acompanhantes.
- **`iam`:** o acesso é provisionado ao Fiel pelo fluxo de promoção; a pessoa do domínio é sempre o `Fiel`, nunca o `Usuario`.
