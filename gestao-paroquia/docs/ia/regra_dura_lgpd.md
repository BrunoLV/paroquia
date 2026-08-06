# DIRETRIZES DE LGPD PARA AGENTES DE CODIFICAÇÃO IA (LLMs, Copilots, etc.)

## OBJETIVO
Este arquivo de contexto (ideal para ser utilizado como `.cursorrules`, `context.md` ou injetado no prompt de sistema) serve para orientar agentes de Inteligência Artificial na geração, revisão e refatoração de código. O agente DEVE garantir que todos os sistemas, bancos de dados relacionais e APIs desenvolvidos estejam em conformidade "By Design" (desde a concepção) com a Lei Geral de Proteção de Dados (LGPD - Lei nº 13.709/2018).

## REGRAS DE IMPLEMENTAÇÃO E ARQUITETURA (PRIVACY BY DESIGN)

### 1. Minimização e Necessidade (Art. 6º, III)
- **Regra:** Nunca colete, trafegue ou armazene dados pessoais que não tenham uma finalidade clara e estritamente necessária para o domínio da aplicação.
- **Ação no Código:** 
  - Ao desenhar esquemas de banco de dados (ex: modelagem SQL) ou payloads de API, limite os campos ao mínimo indispensável.
  - Não crie colunas genéricas para "observações" que possam acumular dados pessoais não estruturados sem controle.

### 2. Segurança e Proteção de Dados (Art. 46)
- **Regra:** Proteja os dados pessoais contra acessos não autorizados e vazamentos.
- **Ação no Código:** 
  - Senhas devem SEMPRE utilizar algoritmos de hash fortes e salting (ex: bcrypt, Argon2).
  - Dados sensíveis e PII (Informações Pessoalmente Identificáveis, como CPF, e-mail, telefone) devem ser protegidos.
  - **Regra de Log:** NUNCA registre PII em logs de aplicação (ex: evite `console.log(user.cpf)` ou `logger.info("User data: ", user)`). Use mascaramento estruturado antes de enviar para ferramentas de log.

### 3. Controle de Acesso e Permissionamento
- **Regra:** O acesso aos dados no sistema deve ser estritamente controlado pela necessidade da função.
- **Ação no Código:** 
  - Implemente autorização baseada em papéis (RBAC - Role-Based Access Control) robusta nas consultas SQL e lógicas de serviço.
  - Garanta que usuários com papéis específicos (ex: coordenadores, administradores) só acessem dados estritamente atrelados às suas permissões ou departamentos (ex: filtrando por `pastoral_id` ou `departamento_id`).
  - Proteja todas as rotas de API com validação de autenticação e verificação de escopo, evitando vulnerabilidades como IDOR (Insecure Direct Object Reference) — prefira expor UUIDs em vez de IDs numéricos sequenciais.

### 4. Gestão de Consentimento e Direitos do Titular (Art. 8º e 18)
- **Regra:** A arquitetura deve permitir que o titular exerça seus direitos facilmente.
- **Ação no Código:**
  - Projete tabelas/registros de auditoria para salvar quando e como o consentimento foi dado (armazenando timestamp, IP e a versão exata do termo de consentimento aceito).
  - Projete recursos de **Portabilidade/Acesso**: Crie endpoints para exportação de dados (ex: `GET /api/users/{uuid}/export` retornando JSON consolidado).
  - Projete recursos de **Direito ao Esquecimento**: Crie rotas de exclusão (ex: `DELETE /api/users/{uuid}`). Implemente lógica de exclusão física (hard delete) para dados pessoais, a menos que haja base legal para retenção (obrigação regulatória), momento em que a exclusão lógica (`deleted_at`) associada a uma forte anonimização (substituindo dados originais por hashes irrecuperáveis) deve ser aplicada.

### 5. Tratamento de Dados Sensíveis (Art. 11)
- **Regra:** Dados que revelem origem racial ou étnica, convicção religiosa, opinião política, filiação a sindicato, dados de saúde ou vida sexual requerem isolamento e controles máximos.
- **Ação no Código:** 
  - Isole dados sensíveis em tabelas específicas com políticas de acesso ainda mais restritivas.
  - Garanta que qualquer endpoint que trafegue esses dados exija um nível superior de privilégio ou token de consentimento específico.

## INSTRUÇÕES DIRETAS AO AGENTE IA
Ao receber comandos de desenvolvimento do usuário, você deve agir proativamente:
1. Revise silenciosamente as migrations e models propostos; se houver coleta de dados excessiva, alerte o desenvolvedor no comentário do código.
2. Ao gerar queries SQL, adicione cláusulas `WHERE` que reforcem o permissionamento do usuário logado por padrão.
3. Se gerar código de API, inclua automaticamente middlewares ou decorators de sanitização de dados e verificação de autorização baseada em papéis.