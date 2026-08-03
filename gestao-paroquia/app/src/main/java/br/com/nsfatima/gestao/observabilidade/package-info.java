/**
 * Contexto de observabilidade: trilha de auditoria transversal ({@code AuditoriaPersistenceService}/
 * {@code AuditoriaQueryService}), consumida por publishers de praticamente todos os demais
 * contextos ao invés de logging ad hoc.
 *
 * <p>Define um vocabulário fechado de "ação"/"resultado" (em português, ex.: {@code criar},
 * {@code atualizar}, {@code cancelar}, {@code aprovacao-decisao}) e convenções de chave de
 * metadata ({@code usuarioId}, {@code organizacaoId}) que todo publisher deve respeitar — um
 * publisher que usa uma ação fora da whitelist é silenciosamente descartado, não persistido (ver
 * Javadoc de {@code AuditoriaPersistenceService}). Também expõe o filtro de correlation-id
 * ({@code CorrelationIdFilter}) e o mecanismo de lock distribuído para jobs agendados
 * ({@code JobLockEntity}/{@code JobLockJpaRepository}).
 */
package br.com.nsfatima.gestao.observabilidade;
