/**
 * Contexto de importação: fluxo de importação em lote de Membros, Usuários, Organizações e Eventos
 * a partir de arquivo CSV/XLSX, em três passos — baixar template, validar (dry-run, sem persistir)
 * e confirmar ({@code ImportacaoService}) — restrito a papéis com poder administrativo/coordenativo.
 * A separação validar/confirmar existe para que o usuário corrija erros de linha antes de qualquer
 * gravação; erros são isolados por linha, não abortam o lote inteiro.
 */
package br.com.nsfatima.gestao.importacao;
