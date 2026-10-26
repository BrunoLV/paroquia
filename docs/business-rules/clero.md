# Regras de Negócio e Fluxos: Módulo Clero e Governança Eclesiástica (`clero`)

O módulo de Clero gerencia as autoridades eclesiásticas, as ordenações, os encargos (funções) clericais, o governo eclesiástico e a vinculação diocesana da Paróquia.

---

## 1. Visão Geral e Propósito
Assegurar o registro formal do clero paroquial e diocesano, mantendo o histórico dos encargos (Pároco, Vigários, Diáconos e Bispo Diocesano) em consonância com o Direito Canônico.

---

## 2. Agregados, Entidades e Value Objects Principais
- **`Clerigo` (Raiz de Agregado):** identificado por UUID. Cadastro pessoal e canônico próprio, **não é um fiel**: nome civil e eclesiástico, data de nascimento, país de origem, documentos (CPF, RG ou documento estrangeiro), contatos, endereço, `GrauOrdem`, diocese de incardinação, as datas de ordenação diaconal, presbiteral e episcopal, e os atributos de consentimento LGPD (`termoConsentimentoAceito`, `dataHoraConsentimentoUtc`, `ipConsentimento`, `versaoTermoConsentimento`, `canalConsentimento`, `hashTermoConsentimento`, `usoImagemAutorizado`, `permitirDivulgacaoAniversario`). Inativar o clérigo é uma transição explícita (exclusão lógica). O CPF possui blind index (HMAC-SHA256 com chave própria) para busca exata sem decifrar a base.
- **`GrauOrdem` (Enum):** `DIACONO_TRANSITORIO`, `DIACONO_PERMANENTE`, `PADRE_DIOCESANO`, `PADRE_RELIGIOSO` e `BISPO`.
- **`FuncaoEclesiastica` (Enum):** `BISPO_DIOCESANO`, `PAROCO`, `ADMINISTRADOR_PAROQUIAL`, `VIGARIO_PAROQUIAL`, `DIACONO_AUXILIAR` e `EMERITO`.
- **Encargo clerical:** a função que o clérigo exerce numa unidade territorial (a Matriz), com a indicação de ser a **atual** e a data de encerramento. O histórico de encargos de cada clérigo é preservado.
- **`GovernoEclesiastico`:** a estrutura de governo canônico da paróquia: a diocese, o Pároco e a data da posse, o decreto de ereção, a data de instalação e o número do livro de tombo.
- **`Diocese`:** a Igreja particular à qual a paróquia pertence (nome, bispo diocesano, CNPJ da mitra, cidade, UF e contato).
- **Solicitação de ajuste clerical:** pedido do próprio clérigo para corrigir seus contatos e endereço, analisado por quem tem alçada.

---

## 3. Regras de Negócio Invariantes

### RN-CLE-01: Coerência da Ordem e das Ordenações
- O grau recebido exige as ordenações correspondentes: todo ministro ordenado tem a diaconal; o padre e o bispo, também a presbiteral; o bispo, ainda a episcopal. O país de origem é normalizado para maiúsculas.

### RN-CLE-02: Encargo Atual
- Cada mudança de função **encerra o encargo atual** do clérigo e abre um novo, atribuído à unidade territorial sede (a Matriz). O encargo anterior fica no histórico com a data de encerramento.
- **Provisão é ato de governo:** conferir ou alterar a função de um clérigo (no cadastro ou na edição) cabe somente ao **Pároco** e ao **Bispo Diocesano**; a secretaria e o Admin mantêm apenas os dados cadastrais. Reenviar a mesma função não encerra nem reabre o encargo (o histórico só muda quando a função muda).
- **Titular único:** só pode haver um Pároco ou Administrador Paroquial ativo na paróquia; para nomear outro é preciso antes encerrar o encargo do titular atual.
- Inativar o clérigo **encerra os encargos vigentes** e desabilita a conta de acesso que ele tiver.

### RN-CLE-03: Governo da Paróquia
- O Pároco do governo eclesiástico é um clérigo cadastrado. A data de posse, o decreto de ereção, a data de instalação e o livro de tombo constam do registro de governo, e a diocese é a da paróquia.

### RN-CLE-04: Paridade de Proteção de Dados e Consentimento (LGPD)
- Aplicação das mesmas restrições de privacidade: os dados civis de padres, diáconos e bispos têm a mesma blindagem criptográfica (AES-256 + blind index SHA-256 de CPF) e segregação de DTOs (`ClerigoInstitucionalResponse` vs `ClerigoResponse`) dispensada aos fiéis leigos.
- O clérigo gerencia suas manifestações e revogações de consentimento tanto via secretaria quanto por autoatendimento no Portal de Consentimento (`/portal/consentimento`, com validação OTP por e-mail), gerando trilha forense imutável em `calendario.historico_consentimentos` visível no modal de detalhes do clérigo (`ClerigoViewModal`).
- O clérigo corrige seus contatos e endereço por **solicitação de ajuste**, analisada e, se aceita, aplicada ao cadastro; uma solicitação já processada não se processa de novo.

---

## 4. Fluxos Operacionais e Casos de Uso
1. **Cadastro e Encargo:** registro do clérigo com sua função atual; cada alteração de função gera novo encargo.
2. **Governo da Paróquia:** registro da diocese e do governo eclesiástico, com o Pároco e a data de posse.
3. **Diretório do Clero:** lista dos clérigos ativos, com função atual, para uso em outros módulos (por exemplo, a presença nas atas do conselho).
4. **Ajuste Cadastral:** o clérigo solicita a correção de seus contatos; a secretaria ou o Pároco analisa.
5. **Anonimização (paridade com o fiel):** cinco anos após a inativação, o cadastro é anonimizado: dados civis, blind index do CPF, nascimento, contatos, endereço e rastro de consentimento são eliminados, a trilha de consentimento tem IP/responsável anonimizados e a conta de acesso é desativada. Nome eclesiástico, grau da ordem, ordenações e encargos permanecem por serem dados canônicos de fé pública (ver `lgpd.md`, RN-LGP-06).

---

## 5. Integrações com outros Bounded Contexts
- **`iam`:** o clérigo pode ter conta de acesso, com papéis de segurança (`PAROCO`, `VIGARIO` e demais).
- **`sacramento`:** o clérigo consta como ministro celebrante nos assentos sacramentais.
- **`conselho`:** o diretório de clérigos ativos alimenta a presença nas atas.
- **`organizacao`:** a unidade territorial a que o encargo se vincula.
