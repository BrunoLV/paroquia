/**
 * Contexto de locais: cadastro dos locais físicos/lógicos (venues/salas) usados por eventos do
 * contexto {@code calendario} ({@code LocalEntity}, nome único, tipo obrigatório, organização
 * opcional). Exclusão é lógica ({@code ativo=false}), não física, para preservar o histórico de
 * eventos que referenciam o local ({@code LocalService#deleteLocal}).
 */
package br.com.nsfatima.gestao.local;
