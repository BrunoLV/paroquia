/**
 * Contexto de dashboard: agrega métricas de leitura de outros contextos (eventos, projetos,
 * aprovações pendentes) em uma única consulta escopada por organização
 * ({@code ObterDashboardMetricasUseCase}), sem possuir dados próprios — é somente leitura/agregação
 * sobre entidades de {@code calendario}, {@code projeto} e {@code aprovacao}.
 */
package br.com.nsfatima.gestao.dashboard;
