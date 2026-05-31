# Especificação: Melhoria de Apresentação e Detalhamento de Erros (API & UI)

## 1. Visão Geral
Este track consiste em padronizar e detalhar as respostas de erros fornecidas pelo backend e aprimorar sua exibição no frontend através de um sistema de notificações flutuantes (Toast/Snackbar). O objetivo é eliminar mensagens genéricas e guiar melhor o usuário sobre o que ocorreu e como corrigir.

## 2. Requisitos Funcionais

### 2.1 Padronização de Erros no Backend (API)
* **Estrutura Unificada de Erros**: Todas as exceções da API devem retornar um JSON estruturado com:
  - `code`: Código único de erro da aplicação (ex: `VALIDATION_ERROR`, `FORBIDDEN_OPERATION`, `CONFLICT_ERROR`).
  - `message`: Mensagem geral amigável sobre o erro.
  - `timestamp`: Instante em que o erro ocorreu.
  - `errors`: Lista opcional de detalhes de erros (campo, mensagem de validação e valor rejeitado).
* **Mapeamento de Exceções Específicas**:
  - **Validação**: Detalhar campos incorretos, limites de caracteres e formatos de data inválidos.
  - **Autorização/Permissão**: Retornar mensagens explicando por que o usuário não pode realizar a ação na organização (ex: "Coordenador não tem permissão para cadastrar projetos em outras pastorais").
  - **Conflito de Recursos**: Tratar violações de unicidade no banco de dados com mensagens amigáveis (ex: "Já existe um evento reservado para este local neste horário").
  - **Erros Operacionais**: Tratar erros genéricos de rede ou falhas inesperadas sem expor o stack trace.

### 2.2 Apresentação de Erros no Frontend (UI)
* **Sistema Global de Feedback (Snackbar/Toast)**:
  - Implementar um provedor/componente de Snackbar com alertas coloridos (sucesso, erro, alerta, info).
  - Integrar os catch blocks de formulários e listagens com o Snackbar para notificar falhas operacionais e de validação de forma flutuante e temporizada.
  - Exibir detalhes específicos do erro (como campos incorretos) de forma inteligível para o usuário final em português.

## 3. Critérios de Aceitação
1. Exceções de segurança (`AccessDeniedException`, `ForbiddenOperationException`) retornam JSON padronizado com código e mensagem amigável.
2. Exceções de violação de chave/integridade no banco são tratadas por um `@RestControllerAdvice` e convertidas em erros de conflito legíveis.
3. Componente de notificação flutuante (Snackbar/Toast) exibe mensagens detalhadas no canto da tela no frontend ao falhar qualquer ação.
4. Nenhuma stack trace interna do Java é vazada no corpo das respostas de erro da API.

## 4. Fora de Escopo
* Alteração de regras de autenticação principal.
* Redesenho completo das telas do sistema.
