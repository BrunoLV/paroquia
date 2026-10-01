# Regras de Negócio e Fluxos: Módulo Sacramentos (`sacramento`)

O módulo de Sacramentos escritura os assentos de Batismo, Confirmação, Matrimônio e Ordem Sacra, que conferem fé pública perante o ordenamento canônico (Cân. 535 §1) e produzem efeitos perpétuos.

---

## 1. Visão Geral e Propósito
Manter o livro de assentos da paróquia com natureza jurídica e probatória, separando a escrituração preparatória (Secretaria) da fé pública (Pároco). O Assento de Batismo é o registro-mestre da pessoa na Igreja (Cân. 535 §2): eventos que alteram o estado canônico entram como averbações marginais.

- **Penitência jamais é registrada** (Cân. 983): não existe tipo, tabela nem endpoint.
- **Primeira Comunhão** é matéria pastoral (catequese) e não gera assento canônico neste módulo.

---

## 2. Agregados, Entidades e Value Objects Principais
- **`AssentoSacramental` (Raiz de Agregado):** identificado por `AssentoSacramentalId`. Contém `TipoSacramento` (`BATISMO`, `CONFIRMACAO`, `MATRIMONIO`, `ORDEM`), `StatusAssento` (`DRAFT`, `SEALED`, `CANCELADO`), `Visibilidade` (`COMUM`, `RESERVADO`), data de lavratura, `TeorAssento`, ressalvas, averbações e notificações canônicas.
- **`TeorAssento`:** conteúdo documental somente-leitura após selar: `ReferenciaLivro` (livro, folha, termo), paróquia custodiante, data/hora e local da celebração, participantes, dados específicos do sacramento e marca de dado sensível.
- **`Participante`:** snapshot textual da pessoa no dia da celebração (`nomeHistorico`, documento cifrado em repouso, local de nascimento, condição canônica) mais ponteiros opcionais `fielId` / `clerigoId`.
- **`Averbacao`:** nota marginal append-only do Batismo (`PENDENTE_HOMOLOGACAO` → `HOMOLOGADA`).
- **`Ressalva`:** retificação material atestada pelo Pároco; acrescenta-se ao termo sem sobrescrevê-lo.
- **`NotificacaoCanonica`:** comunicação ao pároco do local de batismo (Cânn. 895, 1054, 1122).
- **`HistoricoEntrada`:** linha da trilha estrita do assento.
- **Policies (`domain/policy`, stateless e zero-I/O):** `BatismoRequisitosPolicy`, `ConfirmacaoRequisitosPolicy`, `MatrimonioRequisitosPolicy`, `OrdemRequisitosPolicy` (Strategy por tipo) e `AlcadaSacramentalPolicy` (matriz de papéis).

---

## 3. Regras de Negócio Invariantes

### RN-SAC-01: Ciclo de Vida e Imutabilidade do Teor
- Todo assento nasce `DRAFT` com edição livre. A homologação pelo Pároco o transforma em `SEALED`; o teor passa a ser somente-leitura e qualquer edição responde `409`.
- Correções após a selagem são ressalvas (`RN-SAC-06`), nunca sobrescrita de colunas.

### RN-SAC-02: Desacoplamento Cadastral e Associação Tardia (Late Binding)
- O cadastro de Fiel ou Clérigo **não é pré-requisito** para lavrar o assento.
- Vincular é ação restrita a Secretaria e Pároco. O vínculo do **Pároco vale de imediato**; o da **Secretaria nasce pendente** e só passa a valer quando o Pároco o aprova. Rejeitar remove o ponteiro. Participante com vínculo (aprovado ou pendente) não aceita outro: é preciso desvincular antes.
- Desvincular, aprovar e rejeitar são atos privativos do Pároco. A edição de rascunho pela Secretaria não remove nem troca um vínculo existente.
- Cada participante grava em texto o que foi atestado na celebração. `fielId` (leigos) e `clerigoId` (ministros, ordenado, bispo ordenante) são opcionais; o vínculo pode ser criado ou removido a qualquer tempo, inclusive em termo selado, e **nunca altera o snapshot textual**.
- Ao informar o cadastro na lavratura com nome em branco, o snapshot do nome é preenchido a partir dele.

### RN-SAC-03: Requisitos Canônicos na Homologação
O Pároco só sela o termo quando a policy do sacramento não aponta pendências; todas são devolvidas juntas (`422`).
- **Universais:** livro, folha e termo; paróquia custodiante; local; data e hora da celebração. A combinação livro/folha/termo é única por sacramento entre assentos não cancelados.
- **Batismo (Cânn. 877–878, 873–874):** exatamente um batizando com local de nascimento; cartório e termo da certidão de nascimento; ministro celebrante (BISPO, PRESBITERO ou DIACONO); filiação conforme `estadoFiliacao` — `MATRIMONIAL` (pai ou mãe), `NAO_CASADOS` (mãe obrigatória; pai somente com documento autêntico ou declaração perante o pároco, Cân. 877 §2) ou `ADOTIVA` (adotantes); no máximo um padrinho e uma madrinha.
- **Padrinhos (Cân. 874):** católico, crismado e com ao menos 16 anos. Cristão não católico consta apenas como testemunha.
- **Confirmação (Cânn. 895–896, 893):** exatamente um crismando; referência completa ao assento de batismo (paróquia, diocese, livro, folha, termo); um único padrinho; ministro da crisma.
- **Matrimônio (Cânn. 1121–1123):** dois cônjuges; ao menos duas testemunhas; ministro assistente; número do processo de habilitação.
- **Ordem (Cânn. 1053–1054):** um ordenado; bispo ordenante; grau (`DIACONATO_PERMANENTE`, `DIACONATO_TRANSITORIO` ou `PRESBITERADO`); diocese ou congregação de incardinação.

### RN-SAC-04: Alçadas (Cân. 535 §1)
| Operação | Secretaria | Pároco | Bispo Diocesano | Admin |
| :--- | :---: | :---: | :---: | :---: |
| Ler / listar | sim | sim | sim | não |
| Lavrar e editar rascunho, digitar averbação, registrar notificação, emitir certidão | sim | sim | não | não |
| Vincular a fiel/clérigo | propõe (pendente) | sim (direto) | não | não |
| Aprovar ou rejeitar vínculo proposto, desvincular | não | sim | não | não |
| Homologar assento e averbação, retificar, cancelar, consultar trilha | não | sim | não | não |
| Excluir fisicamente | proibido | proibido | proibido | proibido |

- `ADMIN` nunca atua sobre atos de fé pública (RN-IAM-04).
- Não existe verbo `DELETE`; triggers do banco vedam exclusão em todas as tabelas do livro e alteração de ressalvas e histórico.

### RN-SAC-05: Averbações e Notificações
- Averbação só existe no assento de Batismo **homologado**. A secretaria digita (`PENDENTE_HOMOLOGACAO`); somente o Pároco homologa. Não há edição nem remoção.
- Eventos averbáveis: `CONFIRMACAO`, `MATRIMONIO`, `ORDEM`, `VOTOS_PERPETUOS`, `NULIDADE`, `ANOTACAO_DISCIPLINAR`. Averbação marcada como reservada não aparece em certidão simples.
- Notificação canônica é registrada em Confirmação, Matrimônio e Ordem homologados; o Batismo, assento de origem, não notifica.

### RN-SAC-06: Retificação e Cancelamento
- Retificação material exige assento `SEALED`, campo, valor correto e justificativa; gera `Ressalva` atestada pelo Pároco e preserva o teor original.
- Cancelamento (duplicidade ou erro formal grave) exige motivo, é privativo do Pároco e mantém o termo no livro como `CANCELADO`, liberando livro/folha/termo.

### RN-SAC-07: Livro Reservado (Matrimônio Secreto, Cânn. 1130–1133)
- A visibilidade `RESERVADO` é definida apenas na lavratura e não muda.
- Assento reservado só é lavrado, listado ou lido por Pároco e Bispo Diocesano. Para a Secretaria ele **não existe**: leitura devolve `404`, listagens o omitem e a visão por vínculo não o retorna.
- O controle de licença do Ordinário para o Pároco é administrativo e externo ao sistema.

### RN-SAC-08: Trilha de Auditoria Estrita (Cân. 535 §4)
- Toda mutação grava, na mesma transação, uma linha append-only em `assento_historico`: assento, ação, usuário, perfil, endereço IP, estado anterior e posterior serializados e instante UTC.
- O log global recebe apenas identificadores opacos; nomes e documentos nunca vão para logs (PII zero).
- A trilha é consultada exclusivamente pelo Pároco.

### RN-SAC-09: LGPD e Certidões
- A certidão só é emitida de assento `SEALED`. Em assento marcado como `dadoSensivel` (filiação adotiva) a certidão omite pai, mãe e adotantes; averbações reservadas ou pendentes de homologação também são omitidas.
- O documento de cada participante é cifrado em repouso.
- Visão 360º: `GET /api/v1/sacramentos/vinculados/{pessoaId}` lista os assentos em que o fiel ou clérigo está vinculado, respeitando a visibilidade do ator; cada item informa o participante e o papel que a pessoa ocupa no termo.

### RN-SAC-10: Vida Sacramental nas Fichas de Fiel e Clérigo
- As fichas de Fiel e de Clérigo exibem a seção "Vida sacramental" com os assentos do módulo `sacramento` em que a pessoa está vinculada e o papel que ocupa. Os cadastros continuam módulos separados: nenhum dado sacramental é copiado para `fiel` ou `clero`.
- A partir da ficha, Pároco e Secretaria vinculam a pessoa a uma posição ainda sem cadastro de um assento existente. O vínculo da Secretaria aparece como pendente de aprovação; o Pároco o aprova ou rejeita e é o único que desvincula. Fiéis ocupam posições leigas; clérigos, posições ministeriais (ministro celebrante, ministro assistente, ordenado, bispo ordenante).
- A seção é visível a Pároco, Secretaria e Bispo Diocesano (este só consulta); demais perfis não a veem. Assentos cancelados não são oferecidos para novo vínculo.

---

## 4. Fluxos Operacionais e Casos de Uso
1. **Escrituração:** Secretaria lava o rascunho com os dados em mãos → completa livro/folha/termo → Pároco homologa.
2. **Vínculo tardio:** pessoa cadastrada depois → Secretaria ou Pároco vincula o participante ao `Fiel`/`Clerigo`.
3. **Averbação:** Secretaria digita a nota após documento ou notificação externa → Pároco homologa.
4. **Retificação:** Pároco registra ressalva justificada sobre termo selado.
5. **Certidão:** extração narrativa do assento homologado.

Endpoints em `/api/v1/sacramentos`: `POST` (lavrar), `GET` (listar, `?tipo=`), `GET /{id}`, `PUT /{id}` (rascunho), `POST /{id}/homologacao`, `PATCH /{id}/participantes/{participanteId}/vinculo`, `POST /{id}/participantes/{participanteId}/vinculo/aprovacao`, `POST /{id}/participantes/{participanteId}/vinculo/rejeicao`, `POST /{id}/participantes/{participanteId}/desvinculo`, `POST /{id}/averbacoes`, `POST /{id}/averbacoes/{averbacaoId}/homologacao`, `POST /{id}/retificacoes`, `POST /{id}/cancelamento`, `POST /{id}/notificacoes`, `GET /{id}/certidao`, `GET /{id}/historico`, `GET /vinculados/{pessoaId}`.

---

---

## 5. Integração e Bounded Contexts Relacionados
- **`fiel` / `clero`:** leitura do nome civil por porta (`ConsultaPessoaPort`) apenas para validar o vínculo e preencher o snapshot; o contexto não depende de entidades desses módulos.
- **`iam`:** papéis `PAROCO`, `SECRETARIA_PAROQUIAL` e `BISPO_DIOCESANO` definem as alçadas; `ADMIN` é excluído.
- **`observabilidade`:** ações registradas com identificadores opacos.

---

## 6. Privacidade & Segurança (LGPD e RBAC)
- Todos os endpoints exigem `@PreAuthorize` por papel; as alçadas são reforçadas nos casos de uso pela `AlcadaSacramentalPolicy`.
- Nome e documento de participantes são dados pessoais; ficam restritos às telas e APIs do módulo, acessíveis a Pároco e Secretaria, e jamais em logs.
- Interface: página `/sacramentos` com uma aba por sacramento, visível a Pároco, Secretaria e Bispo Diocesano; ações de fé pública aparecem apenas ao Pároco; a opção de livro reservado aparece apenas a Pároco e Bispo Diocesano.
