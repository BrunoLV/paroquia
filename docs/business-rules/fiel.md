# Regras de Negócio e Fluxos: Módulo Fiel (`fiel`)

O módulo de Fiel gerencia os dados de pessoas físicas católicas vinculadas à Paróquia: fiéis leigos, famílias, dizimistas, catequizandos e voluntários, sob conformidade rigorosa com a LGPD (Lei 13.709/2018).

---

## 1. Visão Geral e Propósito
Manter o livro de registro pastoral, sacramental e cadastral dos membros da comunidade paroquial, protegendo a intimidade e a privacidade de dados civis e de convicção religiosa.

---

## 2. Agregados, Entidades e Value Objects Principais
- **`Fiel` (Raiz de Agregado):** Identificado por `FielId`. Possui dados civis (nome completo, CPF, data de nascimento, filiação, contato, endereço), dados sacramentais (batismo, primeira eucaristia, crisma, matrimônio, ordem), registro de dízimo e histórico pastoral.
- **`Cpf` (Value Object):** Documento civil com validação matemática de dígitos verificadores e formato estrito.
- **`DadosSacramentais` (Value Object):** Registro dos sacramentos recebidos (data, paróquia celebrante, livro, folha, termo e ministro celebrante).
- **`ConsentimentoLgpd` (Value Object):** Termo de consentimento ou base legal (legítimo interesse pastoral / cumprimento de obrigação legal / execução de contrato), data do aceite e finalidade.

---

## 3. Regras de Negócio Invariantes

### RN-FIE-01: Proteção e Blindagem de PII (LGPD Privacy-by-Design)
- **Zero PII em Logs:** É estritamente proibido imprimir nomes completos, CPF, e-mail, telefone ou endereço de fiéis em logs de aplicação (`INFO`, `DEBUG`, `WARN`, `ERROR`). Utilize apenas identificadores opacos (`fielId`).
- **Criptografia em Repouso:** Dados civis identificadores (como CPF e endereços) devem ser protegidos contra acesso direto desautorizado.
- **Prevenção contra IDOR:** Todo acesso a dados de fiel exige validação de permissão no backend antes de expor os dados.

### RN-FIE-02: Paridade Clérigo/Fiel
- Clérigos (padres, diáconos, bispos) e leigos recebem **exatamente o mesmo nível de proteção de privacidade**. Dados civis de clérigos não podem ser expostos publicamente em listagens de pastoral ou governo, ficando restritos a modais de governança/RH autorizados.

### RN-FIE-03: Unicidade Cadastral
- Um fiel não pode ter múltiplos cadastros ativos com o mesmo CPF. Tentativas de duplicidade devem disparar alerta de conciliação cadastral.

### RN-FIE-04: Menores de Idade e Consentimento de Responsáveis
- Fiéis com menos de 18 anos completos (ex.: catequizandos) exigem a associação formal com ao menos um responsável legal registrado no sistema.

---

## 4. Fluxos Operacionais e Casos de Uso
1. **Cadastro Pastoral de Fiel:** Registro civil, sacramental e termo de consentimento.
2. **Histórico Sacramental:** Emissão de certidões e comprovantes sacramentais a partir do registro do livro paroquial.
3. **Módulo do Dízimo:** Registro de contribuições periódicas sem exposição de dados financeiros a outros fiéis.

---

## 5. Integrações com outros Bounded Contexts
- **`membro`:** O fiel é vinculado como membro em uma ou mais pastorais.
- **`clero`:** Clérigos também possuem registro de pessoa física/fiel para amarração sacramental canônica.
- **`sacramento`:** Os assentos sacramentais (livro paroquial com fé pública) pertencem ao módulo `sacramento`; o `Fiel` é apenas um ponteiro opcional e tardio dos participantes, sem sincronização com `DadosSacramentais`.
- **`painel`:** Cálculo do indicador executivo "Fiéis ativos" e contagem de "novos fiéis no mês".
