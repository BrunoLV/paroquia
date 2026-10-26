# Regras de Negócio e Fluxos: Módulo Fiel (`fiel`)

O módulo de Fiel gerencia os dados de pessoas físicas católicas vinculadas à Paróquia: fiéis leigos, famílias, catequizandos e voluntários, sob conformidade rigorosa com a LGPD (Lei 13.709/2018).

---

## 1. Visão Geral e Propósito
Manter o livro de registro pastoral, sacramental e cadastral dos membros da comunidade paroquial, protegendo a intimidade e a privacidade de dados civis e de convicção religiosa.

---

## 2. Agregados, Entidades e Value Objects Principais
- **`Fiel` (Raiz de Agregado):** identificado por UUID. É a pessoa física (o paroquiano), independente de qualquer credencial de acesso. Possui dados civis (nome, CPF ou documento estrangeiro, data de nascimento, sexo, estado civil, contato, endereço), a vocação (`Vocacao`), as preferências de comunicação, o consentimento LGPD e, para menor de idade, o nome e o CPF do responsável.
- **`VidaSacramental` (Value Object):** o registro de Batismo, Primeira Comunhão, Crisma e Matrimônio, com as referências aos livros paroquiais. Os assentos com fé pública pertencem ao módulo `sacramento`.
- **`ConsentimentoLgpd` (Value Object):** termo de consentimento ou base legal (legítimo interesse pastoral, cumprimento de obrigação legal ou execução de contrato), data do aceite e finalidade.
- **`Dependente` e `TipoRelacionamentoFamiliar`:** os vínculos familiares entre fiéis, com `Parentesco` (`FILHO`, `ENTEADO`, `TUTELADO` e `OUTRO`) e o cônjuge.
- **`EstadoCivil` (Enum):** `SOLTEIRO`, `CASADO_IGREJA`, `CASADO_CIVIL`, `VIUVO`, `DIVORCIADO` e outros.
- **`Vocacao` (Enum):** `LEIGO`, `SACERDOTE`, `DIACONO` e `RELIGIOSO`.

---

## 3. Regras de Negócio Invariantes

### RN-FIE-01: Proteção e Blindagem de PII (LGPD Privacy-by-Design)
- **Zero PII em Logs:** É estritamente proibido imprimir nomes completos, CPF, e-mail, telefone ou endereço de fiéis em logs de aplicação (`INFO`, `DEBUG`, `WARN`, `ERROR`). Utilize apenas identificadores opacos (`fielId`).
- **Criptografia em Repouso:** Dados civis identificadores (como CPF e endereços) devem ser protegidos contra acesso direto desautorizado.
- **Prevenção contra IDOR:** Todo acesso a dados de fiel exige validação de permissão no backend antes de expor os dados.

### RN-FIE-02: Paridade Clérigo/Fiel
- Clérigos (padres, diáconos, bispos) e leigos recebem **exatamente o mesmo nível de proteção de privacidade**. Dados civis de clérigos não podem ser expostos publicamente em listagens de pastoral ou governo, ficando restritos a modais de governança/RH autorizados.

### RN-FIE-03: Unicidade Cadastral e Documento Civil
- Não existe mais de um fiel com o mesmo CPF: a unicidade é garantida no banco pelo hash do CPF, sem decifrar a base.
- O brasileiro tem **CPF obrigatório e válido** (dígito verificador); o estrangeiro informa RNE/RNM ou passaporte. O país de origem segue a norma ISO-3166 (três letras).

### RN-FIE-04: Relacionamentos Familiares
- Uma pessoa não se relaciona consigo mesma, os cônjuges têm sexos distintos e os dois envolvidos têm sexo definido.
- Para o menor de idade, o cadastro guarda o nome e o CPF do responsável.

---

## 4. Fluxos Operacionais e Casos de Uso
1. **Cadastro Pastoral de Fiel:** registro civil, sacramental e termo de consentimento.
2. **Relacionamentos Familiares:** vínculo entre fiéis (filiação, tutela e cônjuge).
3. **Ajuste Cadastral:** o fiel solicita a correção de seus dados por meio de uma solicitação, analisada e aplicada se aceita; uma solicitação já analisada não se analisa de novo.
4. **Ciclo de Vida:** desativação, reativação e anonimização do cadastro (LGPD), que limpa os dados pessoais em um único ponto.

---

## 5. Integrações com outros Bounded Contexts
- **`membro`:** o fiel é vinculado como membro em uma ou mais organizações.
- **`clero`:** o clérigo tem cadastro **próprio**, não é um fiel; os dois cadastros não se confundem.
- **`sacramento`:** os assentos sacramentais (livro paroquial com fé pública) pertencem ao módulo `sacramento`; o `Fiel` é apenas um ponteiro opcional e tardio dos participantes, sem sincronização com `VidaSacramental`.
- **`conselho`:** a presença das atas identifica as pessoas pelo cadastro de fiéis, e a busca de fiéis para a presença devolve só identificador e nome.
- **`painel`:** cálculo do indicador executivo "Fiéis ativos" e contagem de "novos fiéis no mês".
