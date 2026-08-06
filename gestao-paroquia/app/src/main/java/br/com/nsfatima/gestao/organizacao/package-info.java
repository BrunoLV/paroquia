/**
 * Contexto de organização: modela a hierarquia paroquial (Matriz, Comunidades, Pastorais,
 * Movimentos, Conselhos, Clero) via {@code GrupoParoquialEntity}/{@code TipoGrupo} e o vínculo
 * usuário-organização com papel ({@code MembroGrupoParoquialEntity}/{@code PapelOrganizacional}).
 *
 * <p>É o contexto que drive a autorização de escopo usada pela maioria dos demais contextos
 * (calendario, projeto, patrimonio, conselho) — a maior parte das checagens de permissão no app é
 * papel-do-ator + escopo-de-organização, não posse simples do registro
 * ({@code EscopoUsuarioAuthorizationService}). Também expõe o organograma (árvore de organizações
 * e membros) consumido pelo frontend.
 */
package br.com.nsfatima.gestao.grupoParoquial;
