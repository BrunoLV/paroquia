/**
 * Contexto de membro: apesar do nome, {@code MembroEntity} não modela uma pessoa/fiel — é o vínculo
 * usuário(IAM)-organização com um papel canônico (ex. {@code COORDENADOR}/{@code SECRETARIO_GRUPO}), cujas
 * mudanças de liderança se propagam para {@code GrupoParoquialEntity}. {@code ParticipanteGrupoParoquialEntity}
 * é um registro de participação secundário e independente, também referenciado por esse mesmo id de
 * "membro", em uma organização adicional.
 *
 * <p>Não confundir com o cadastro de conta/credencial do contexto {@code iam}, nem com o vínculo
 * organizacional genérico do contexto {@code grupoParoquial} ({@code MembroGrupoParoquialEntityLEGACY}) — os
 * três modelam relações diferentes que compartilham vocabulário semelhante.
 */
package br.com.nsfatima.gestao.membro;
