/**
 * Projeções somente-leitura (records) devolvidas pelas consultas agregadas do dashboard, que evitam
 * carregar entidades inteiras e consultas por projeto (N+1).
 */
package br.com.nsfatima.gestao.dashboard.infrastructure.persistence.projection;
