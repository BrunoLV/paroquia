/**
 * Contexto de Projetos paroquiais: um projeto agrupa um conjunto de eventos de calendário sob uma
 * organização responsável, com ciclo de vida próprio e regras de vínculo/desvinculação de eventos
 * ({@code ProjetoVincularPolicy}). Autorização segue o mesmo padrão de papel+escopo de organização
 * do resto do app ({@code ProjetoAuthorizationService}).
 *
 * <p>Cancelamento de projeto segue a mesma regra de desvio para aprovação do contexto
 * {@code aprovacao}: atores de alto nível ({@code ProjetoAuthorizationService#isAtorDeAltoNivel})
 * cancelam imediatamente; os demais criam uma {@code ProjetoSolicitacaoAprovacaoEntity} pendente,
 * decidida depois via {@code TransicionarProjetoUseCase#decidir}.
 *
 * <p>Ciclo de vida de status do projeto ({@code ProjetoStatus}):
 *
 * <pre>{@code
 * stateDiagram-v2
 *     [*] --> ABERTO: projeto criado (imediatamente, ou após aprovação de solicitação de criação)
 *     ABERTO --> EM_ANDAMENTO: início manual, ou automático via ProjetoAutoActivationScheduler
 *                               quando a data de início é atingida
 *     EM_ANDAMENTO --> CONCLUIDO: conclusão manual
 *     ABERTO --> CANCELADO: cancelamento (imediato para ator de alta alçada, ou após aprovação)
 *     EM_ANDAMENTO --> CANCELADO: idem
 *     CONCLUIDO --> [*]
 *     CANCELADO --> [*]
 * }</pre>
 *
 * {@code CANCELADO} e {@code CONCLUIDO} são estados terminais: não aceitam novos eventos vinculados
 * nem novas transições.
 */
package br.com.nsfatima.gestao.projeto;
