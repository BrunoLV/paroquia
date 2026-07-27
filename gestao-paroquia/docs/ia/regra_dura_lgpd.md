# MANUAL DE REGRAS DURAS DE COMPLIANCE E PRIVACIDADE
### Diretrizes Mandatórias de Engenharia de Software e Tratamento de Dados sob a LGPD (Lei nº 13.709/2018)

| | |
| :--- | :--- |
| **Documento:** | Norma Operacional de Desenvolvimento - ERP-PAROQUIA-V1 |
| **Status:** | Aprovado / Mandatório |
| **Escopo:** | Sistema Gerencial Interno (Acúmulo de Papéis: Controladora e Operadora) |
| **Aplicação:** | Engenharia de Software, DevOps, Administradores de Banco de Dados |

---

## 1. OBJETIVO E DIRETRIZ ESTRUTURAL
Esta aplicação é o sistema gerencial oficial da Paróquia, que centraliza as operações de secretaria, sacramentos, dízimos e pastorais. A instituição acumula os papéis de **Controladora** (decide a finalidade do tratamento) e **Operadora** (desenvolve e roda a infraestrutura técnica).

O sistema opera **estritamente em ambiente interno corporativo**. O objetivo deste documento é impor barreiras técnicas rígidas no código e no banco de dados para garantir compliance total com a LGPD, mitigando vazamentos e acessos indevidos.

---

## 2. REGRAS DURAS: ENTRADA E ARMAZENAMENTO (INPUT & DB ARCHITECTURE)

### 🚨 REGRA DURA 01: Segregação e Bloqueio de Acesso a Dados Pessoais Sensíveis
* **Diretriz:** Informações ligadas a aspectos íntimos (como registros de confissão, pedidos de assistência social/vulnerabilidade ou restrições de saúde em acampamentos/pastorais) são classificados como **Dados Sensíveis**. O banco de dados e a API devem aplicar criptografia em repouso nesses campos e restringir o acesso na tela apenas a perfis com autorização explícita (ex: Pároco ou assistente social responsável).
* **Fundamentação Legal:** Art. 11 (Tratamento de Dados Pessoais Sensíveis) da LGPD.

### 🚨 REGRA DURA 02: Vinculação de Finalidade no Cadastro de Paroquianos
* **Diretriz:** A aplicação não pode permitir a coleta de dados genéricos ("salvar por salvar"). Cada formulário de cadastro (seja para Catequese, Dízimo ou Eventos) deve registrar e vincular no banco de dados a **finalidade específica** e a base legal utilizada (ex: Consentimento para envio de informativos ou Execução de Contrato/Legítimo Interesse para gestão do dízimo).
* **Fundamentação Legal:** Art. 6º, I (Princípio da Finalidade) da LGPD.

### 🚨 REGRA DURA 03: Proibição de Compartilhamento Externo via Integrações não Auditadas
* **Diretriz:** Sendo o sistema de uso exclusivamente interno, nenhuma funcionalidade ou webhook pode exportar listas de paroquianos, contatos ou históricos financeiros para plataformas terceiras (redes sociais, ferramentas de disparo em massa ou serviços de nuvem) sem que os contratos dessas plataformas passem por análise jurídica prévia do DPO/Encarregado.
* **Fundamentação Legal:** Art. 7º, §4º e Art. 46 da LGPD.

---

## 3. REGRAS DURAS: PROCESSAMENTO E ACESSO (PERMISSOES & SEGURANÇA)

### 🚨 REGRA DURA 04: Controle de Acesso Baseado em Relacionamento Rígido (RBAC)
* **Diretriz:** O sistema deve implementar regras duras de autorização baseadas na função estrita do usuário. Voluntários de uma pastoral específica **não podem** ter acesso aos dados financeiros do dízimo geral ou fichas de batismo de outras pessoas. O acesso deve seguir o princípio do privilégio mínimo.
* **Fundamentação Legal:** Art. 6º, III (Princípio da Necessidade) e Art. 46 da LGPD.

### 🚨 REGRA DURA 05: Log de Auditoria Imutável para Ações Críticas
* **Diretriz:** Toda ação de inserção, alteração, exclusão ou visualização em lote de dados de paroquianos deve gerar um log de auditoria automático, contendo timestamp, ID do usuário e IP. Esses logs devem ser protegidos contra exclusão (inclusive por administradores do sistema).
* **Fundamentação Legal:** Art. 50 (Boas Práticas e Governança) da LGPD.

---

## 4. CICLO DE VIDA E DIREITOS DOS PAROQUIANOS (RETENÇÃO E EXCLUSÃO)

### 🚨 REGRA DURA 06: Mecanismo de Exclusão e Anonimização Automática
* **Diretriz:** Se um paroquiano solicitar a exclusão de seus dados do sistema (Direito de Eliminação), a aplicação deve fornecer uma rotina técnica capaz de apagar permanentemente seus dados identificáveis. Caso o dado financeiro (histórico de dízimos/doações) precise ser mantido por questões contábeis/fiscais, o sistema deve **anonimizar** o cadastro associado de forma irreversível.
* **Fundamentação Legal:** Art. 16 e Art. 18, VI (Direitos do Titular) da LGPD.

### 🚨 REGRA DURA 07: Retenção Parcial de Dados Sacramentais vs. Dados Administrativos
* **Diretriz:** O sistema deve separar o ciclo de vida dos dados: registros sacramentais (como Batismo e Matrimônio), que possuem valor histórico e canônico perene, podem ter regras diferenciadas de arquivamento. Já dados puramente administrativos ou de eventos sazonais devem ter rotinas de purga automáticas após o término da finalidade.
* **Fundamentação Legal:** Art. 16 (Término do Tratamento) da LGPD.

### ⚠️ REGRA DURA 08: Preparação Arquitetural para Tecnologias Futuras (Privacy by Design)
* **Diretriz:** Qualquer nova funcionalidade adicionada ao sistema — incluindo possíveis automações algorítmicas ou rotinas inteligentes no futuro — deve nascer adaptada a este ecossistema blindado. É proibido criar módulos que burlem o pipeline central de autorização e auditoria aqui estabelecido.
* **Fundamentação Legal:** Art. 46, §2º (Segurança desde a concepção) da LGPD.

---

> 🚨 **AVISO DE RESPONSABILIDADE**
> Por gerenciar dados de uma comunidade e acumular as funções de Controladora e Operadora, a Paróquia responde diretamente por falhas na segurança do software. O não cumprimento destas regras por parte da equipe de desenvolvimento constitui falha técnica grave sujeita a penalidades contratuais.