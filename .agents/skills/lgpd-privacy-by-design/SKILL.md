---
name: lgpd-privacy-by-design
description: Aplica conformidade estrita com a LGPD (Lei 13.709/2018) na criação, refatoração e revisão de bancos de dados, APIs e lógicas de negócio.
triggers:
  - Criação ou alteração de modelos, entidades ou migrations de banco de dados
  - Criação ou alteração de endpoints de API (rotas, controllers, DTOs)
  - Implementação de logs, auditorias ou monitoramento
  - Manipulação de dados de usuários, fiéis ou clérigos
---

# SKILL: Governança e Engenharia de Privacidade (LGPD by Design)

## 1. Perfil e Postura do Agente
Você atua como Arquiteto de Software Especialista em Segurança e *Privacy by Design*. Seu dever é garantir a conformidade irrestrita com a **Lei Geral de Proteção de Dados (Lei 13.709/2018)**, as diretrizes da **ANPD** e as normas técnicas de boas práticas (Cartilha CGE-PR 2025 e Cartilha Fundação Real Grandeza), impedindo qualquer introdução de vulnerabilidade de privacidade, coleta abusiva, falta de consentimento ou vazamento de Informações Pessoalmente Identificáveis (PII) e Dados Pessoais Sensíveis.

---

## 2. Princípios Basilares da LGPD (Art. 6º)

Toda modelagem de banco, caso de uso, endpoint e componente visual DEVE respeitar os 10 princípios:

1. **Finalidade:** Realização do tratamento para propósitos legítimos, específicos, explícitos e informados ao titular, sem possibilidade de tratamento posterior incompatível.
2. **Adequação:** Compatibilidade do tratamento com as finalidades informadas ao titular.
3. **Necessidade (Minimização):** Limitação do tratamento ao mínimo necessário para a realização de suas finalidades, com abrangência dos dados pertinentes, proporcionais e não excessivos. Proibido coletar dados "por precaução".
4. **Livre Acesso:** Garantia aos titulares de consulta facilitada e gratuita sobre a forma e a duração do tratamento, bem como sobre a integralidade de seus dados.
5. **Qualidade dos Dados:** Garantia de exatidão, clareza, relevância e atualização dos dados.
6. **Transparência:** Garantia de informações claras, precisas e facilmente acessíveis sobre o tratamento e os agentes.
7. **Segurança:** Utilização de medidas técnicas e administrativas aptas a proteger os dados de acessos não autorizados e de situações acidentais ou ilícitas.
8. **Prevenção:** Adoção de medidas prévias para evitar a ocorrência de danos em virtude do tratamento de dados.
9. **Não Discriminação:** Impossibilidade de realização do tratamento para fins discriminatórios, ilícitos ou abusivos.
10. **Responsabilização e Prestação de Contas:** Demonstração pelo agente da adoção de medidas eficazes e capazes de comprovar a observância e o cumprimento das normas.

---

## 3. Matriz de Classificação de Dados no Contexto Eclesial

Ao lidar com entidades (em especial Fiéis, Clérigos e Sacramentos), aplique rigorosamente:

| Categoria | Definição no Sistema | Exemplos | Requisito Técnico Obrigatório |
| :--- | :--- | :--- | :--- |
| **Público / Canônico** | Dados institucionais abertos para diretório/consulta | Nome de ordenação/eclesiástico, grau da ordem, ofício/encargo pastoral, paróquia/diocese | Exposição permitida via DTOs institucionais sem autenticação restritiva. |
| **Pessoal (PII Civil)** | Dados que identificam o indivíduo civilmente | Nome civil, CPF, RG, e-mail, telefone, endereço, data de nascimento | **Criptografia em repouso (AES-256)**, blind index (SHA-256), mascaramento em logs e RBAC rigoroso. |
| **Sensível (Art. 5º, II e Art. 11)** | Convicção religiosa, sacramentos, filiação a organização de caráter religioso | Assentos de batismo, confirmação, matrimônio, ordem, vínculos pastorais, dizimistas | **Criptografia em repouso**, tabelas segregadas, auditoria estrita de leitura e dupla checagem de permissão. |

> **Regra de Paridade Irrevogável:** Clérigos e Fiéis leigos possuem exatamente os mesmos direitos de privacidade. Dados civis de clérigos recebem a mesma criptografia e segregação de DTOs que dados de leigos.

> **Harmonização Canônica (Art. 16, I da LGPD):** Os assentos sacramentais paroquiais gozam de fé pública perpétua sob o Código de Direito Canônico. A eliminação do cadastro do fiel anonimiza dados civis cadastrais, mas preserva a integridade histórica dos livros sacramentais (retificações são feitas apenas por averbações canônicas formais).

---

## 4. As 5 Fases do Ciclo de Vida & 20 Ações de Tratamento

O sistema cobre ativamente todas as etapas do tratamento de dados pessoais:

1. **Coleta:** Obtenção e recepção com consentimento explícito e informado (`termoConsentimentoAceito`, IP e timestamp UTC).
2. **Retenção:** Armazenamento seguro e cifrado em banco de dados relacional sob política de expurgo/anonimização.
3. **Processamento:** Classificação, utilização, controle e modificação restrita a agentes autorizados.
4. **Compartilhamento:** Comunicação e difusão interna estritamente controlada por papéis e alçadas canônicas (anti-IDOR); proibida circulação por canais inadequados (WhatsApp pessoal, murais públicos).
5. **Eliminação:** Anonimização irreversível após 5 anos de inatividade ou mediante solicitação justificada do titular, ressalvadas as obrigações canônicas/legais de guarda.

---

## 5. Protocolos de Engenharia e Padrões de Código

### A. Modelagem e Migrations (Minimização e Anonimização)
1. **Criptografia Simétrica em Repouso:**
   - Atributos com PII civil (CPF, RG, telefones, endereço) devem utilizar conversores de entidade JPA (`EncryptedStringConverter`, `EncryptedLocalDateConverter`) baseados em AES-256/CBC/PKCS5Padding com IV aleatório prefixado ao ciphertext.
   - Chave configurada externamente e normalizada via SHA-256.
2. **Blind Indexing para Unicidade e Busca:**
   - O CPF é armazenado também como hash SHA-256 (`cpf_hash`) para viabilizar validação de unicidade e buscas exatas sem decifrar a base.
3. **Identificadores Públicos:**
   - Uso exclusivo de **UUIDv4** como chave primária exposta publicamente. Chaves sequenciais numéricas (`BIGSERIAL`) são proibidas em contratos REST.
4. **Proibição de Campos Genéricos:**
   - Proibido criar colunas livres como `observacoes TEXT` ou `metadata JSONB` sem esquema validado.
5. **Anonimização Irreversível:**
   - Métodos de anonimização (como `fiel.anonimizar()`) devem nulificar todos os atributos civis identificadores e marcar `dados_cadastrais_eliminados = true`.

### B. Camada de API, DTOs e Prevenção contra IDOR
1. **Segregação Estrita de DTOs via Sealed Interfaces:**
   - Proibido expor entidades JPA diretamente nos controllers.
   - Utilizar contratos segregados por alçada:
     - `FielResponse` (acesso pleno para Pároco e Secretaria).
     - `FielContatoResponse` (projeção mínima sem documentos civis para Coordenadores de pastoral).
     - `ClerigoInstitucionalResponse` (nome eclesiástico, grau e ofício) vs `ClerigoResponse` (dados civis protegidos).
2. **Prevenção contra IDOR:**
   - Endpoints de consulta por ID validam escopo de organização e vínculo ativo do solicitante (`ActorContextResolver` e verificação de vínculo pastoral).

### C. Higiene de Logs e Telemetria
1. **Regra de PII Zero em Logs:**
   - NUNCA imprimir entidades, nomes completos, CPF, e-mail, telefone ou endereço em logs (`INFO`, `DEBUG`, `WARN`, `ERROR`).
   - Utilizar exclusivamente identificadores opacos (`UUID`).

### D. Gestão de Consentimento e Direitos do Titular (Art. 18)
1. **Consentimento Auditável:**
   - Registrar: `termoConsentimentoAceito`, `dataHoraConsentimentoUtc`, `ipConsentimento`, `versaoTermoConsentimento`, `usoImagemAutorizado` e `permitirDivulgacaoAniversario`.
2. **Direito à Revogação (Art. 8º, § 5º):**
   - O titular pode revogar a qualquer momento autorizações secundárias (uso de imagem e mural de aniversários).
3. **Direito à Portabilidade (Art. 18, V):**
   - Disponibilizar endpoint `GET /api/v1/ajustes-cadastrais/meu-perfil/portabilidade` gerando payload estruturado JSON com todos os dados pessoais do titular.

### E. Governança do Encarregado (DPO - Art. 41) e Resposta a Incidentes (Art. 48)
1. **Canal Oficial do DPO:**
   - A paróquia deve cadastrar e exibir publicamente o Nome, E-mail institucional e Telefone do Encarregado de Proteção de Dados para contato com titulares e a ANPD.
2. **Registro e Notificação de Incidentes:**
   - Violações de dados pessoais devem ser formalmente registradas em tabela de governança (`governanca.incidentes_seguranca`), contendo data da ocorrência, data de detecção, tipo de dados afetados, gravidade, medidas de mitigação e prazos de comunicação à ANPD e aos titulares (em até 3 dias úteis).

---

## 6. Checklist de Validação da IA (Critérios de Aceite)

- [ ] **Minimização:** Há apenas campos estritamente necessários para a finalidade pastoral informada?
- [ ] **Criptografia:** Dados civis (CPF, RG, telefone, endereço) usam conversores JPA criptográficos AES-256?
- [ ] **Blind Index:** Unicidade de CPF é validada por hash SHA-256 sem decifrar registros em massa?
- [ ] **Isolamento de Log:** Zero PII interpolada em logs; apenas UUIDs opacos?
- [ ] **DTOs Segregados:** Coordenadores e terceiros recebem projeções sem CPF/endereço civil?
- [ ] **Paridade Clérigo/Fiel:** Clérigos possuem o mesmo nível de blindagem em seus dados civis?
- [ ] **Proteção IDOR:** Consultas validam escopo institucional/pastoral do usuário autenticado?
- [ ] **Consentimento & Revogação:** Há captura de termo aceito e opção para revogar imagem/aniversário?
- [ ] **Portabilidade:** O titular consegue exportar seus dados pessoais em formato JSON estruturado?
- [ ] **Canal do DPO:** Há indicação clara dos canais de contato do Encarregado de Dados?
- [ ] **Fé Pública Canônica:** Livros de assento são preservados sem exclusão física sumária?
