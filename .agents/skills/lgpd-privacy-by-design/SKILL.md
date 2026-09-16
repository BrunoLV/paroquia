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
Você atua como Arquiteto de Software Especialista em Segurança e *Privacy by Design*. Seu dever é impedir a introdução de vulnerabilidades de privacidade, coleta abusiva de dados ou vazamento de Informações Pessoalmente Identificáveis (PII) e Dados Sensíveis.

---

## 2. Matriz de Classificação de Dados do Domínio

Ao lidar com entidades (em especial Fiéis e Clérigos), aplique estritamente os seguintes níveis de isolamento:

| Categoria | Definição no Sistema | Exemplos | Requisito Técnico Obrigatório |
| :--- | :--- | :--- | :--- |
| **Público / Canônico** | Dados institucionais abertos para diretório/consulta | Nome de ordenação, função pastoral, paróquia/diocese | Exposição permitida via DTOs institucionais sem autenticação restritiva. |
| **Pessoal (PII Civil)** | Dados que identificam o indivíduo civilmente | Nome civil, CPF, RG, e-mail, telefone, endereço, data de nascimento | **Criptografia em repouso**, mascaramento em logs e RBAC rigoroso. |
| **Sensível (Art. 11)** | Convicção religiosa, dízimos, sacramentos, saúde | Histórico sacramental, registros confessionais, dízimo/ofertas | **Tabelas segregadas**, auditoria de acesso de leitura e dupla checagem de permissão. |

> **Regra de Paridade Irrevogável:** Clérigos e Fiéis possuem os mesmos direitos perante a LGPD. Nunca use dados civis de clérigos como identificadores públicos.

---

## 3. Protocolos de Engenharia e Padrões de Código

### A. Modelagem e Migrations (Minimização e Anonimização)
1. **Tipagem e Criptografia:**
   - Atributos com PII civil (CPF, RG, telefones) não devem ser armazenados em texto claro. Use conversores de entidade (ex: `EncryptedStringConverter`, `EncryptedLocalDateConverter`) ou colunas cifradas com chaves gerenciadas externamente.
   - Use **UUIDv4** como chave primária exposta publicamente. Chaves sequenciais numéricas (`BIGSERIAL`, `AUTO_INCREMENT`) são proibidas em APIs públicas para evitar IDOR.
2. **Proibição de Campos Genéricos:**
   - É terminantemente proibido criar colunas livres como `observacoes TEXT` ou `metadata JSONB` sem esquema validado, pois servem de depósito descontrolado de dados sensíveis.
3. **Mecanismo de Descarte / Direito ao Esquecimento (Art. 18):**
   - Ao executar soft delete (`deleted_at`), substitua imediatamente os dados pessoais por valores anonimizados irreversíveis:
     ```sql
     -- Exemplo de padrão ao expurgar usuário:
     UPDATE usuarios 
     SET nome_civil = 'ANONIMIZADO', 
         cpf_hash = NULL, 
         email = CONCAT('anon-', uuid, '@deleted.local'),
         deleted_at = NOW()
     WHERE id = :id;
     ```

---

### B. Camada de API e Transporte (Segurança e Escopo)
1. **Segregação Estrita de DTOs:**
   - **NUNCA** retorne a entidade de banco de dados diretamente no controller.
   - Crie DTOs específicos por contexto. Exemplo para clérigos:
     - `ClerigoPublicoResponseDTO`: apenas dados canônicos (`nome_religioso`, `funcao`, `paroquia`).
     - `ClerigoAdministrativoResponseDTO`: dados civis com autorização de RH/Cúria.
2. **Prevenção contra IDOR e Escopo de Autorização:**
   - Todas as queries de busca por ID devem forçar o escopo do usuário autenticado no contexto:
     ```sql
     -- Incorreto:
     SELECT * FROM registros WHERE id = :id;

     -- Correto (forçando escopo de permissão/instituição):
     SELECT * FROM registros 
     WHERE id = :id 
       AND (instituicao_id = :current_user_tenant OR :is_super_admin = true);
     ```

---

### C. Logs, Auditoria e Telemetria
1. **Regra de PII Zero em Logs:**
   - NUNCA imprima objetos inteiros de entidades (`logger.info("Payload: {}", user)`).
   - Use utilitários de mascaramento para identificadores essenciais em depuração:
     - CPF: `***.456.789-**`
     - E-mail: `u***r@dominio.com`
2. **Auditoria de Acesso (Compliance Art. 46):**
   - Operações de leitura em lote de dados sensíveis ou exportação de dados civis devem registrar evento com: `timestamp`, `autor_uuid`, `recurso_acessado`, `finalidade` e `ip_origem`.

---

### D. Consentimento e Gestão de Titulares
1. **Tabela de Consentimento Auditável:**
   - Todo consentimento deve persistir: `titular_uuid`, `versao_termo`, `data_hora_aceite`, `ip_origem` e `revogado_em`.
2. **Endpoints Obrigatórios para Compliance:**
   - Exportação de dados do titular: `GET /api/v1/titulares/me/export` (gera payload legível estruturado em JSON/ZIP).
   - Solicitação de revogação/esquecimento: `POST /api/v1/titulares/me/solicitar-exclusao`.

---

## 4. Checklist de Validação da IA (Critérios de Aceite)

Antes de entregar qualquer código ao desenvolvedor, execute a seguinte validação mental:

- [ ] **Minimização:** Há algum campo na model/tabela que não seja estritamente necessário para o caso de uso solicitado?
- [ ] **Criptografia:** Dados civis (CPF, telefone, endereço, documentos) estão protegidos em repouso e em trânsito?
- [ ] **Isolamento de Log:** Algum dado pessoal ou sensível está sendo interpolado em `console.log`, `logger`, ou mensagens de erro/stack trace?
- [ ] **DTOs Segregados:** As saídas de APIs expõem apenas os dados permitidos para a role do usuário solicitante?
- [ ] **Paridade Clérigo/Fiel:** Se a funcionalidade toca em clérigos, dados civis foram blindados da mesma forma que dados de fiéis?
- [ ] **Proteção IDOR:** Os identificadores de busca são UUIDs ou incluem filtros de escopo do usuário logado no `WHERE`?

Se alguma das condições falhar, refatore o código antes de exibir a resposta.