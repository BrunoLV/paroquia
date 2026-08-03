/**
 * Contexto de membro: apesar do nome, {@code MembroEntity} não modela uma pessoa/fiel — é o vínculo
 * usuário(IAM)-organização com um papel (ex. {@code COORDENADOR}/{@code VICE_COORDENADOR}), cujas
 * mudanças de liderança se propagam para {@code OrganizacaoEntity}. {@code ParticipanteOrganizacaoEntity}
 * é um registro de participação secundário e independente, também referenciado por esse mesmo id de
 * "membro", em uma organização adicional.
 *
 * <p>Não confundir com o cadastro de conta/credencial do contexto {@code iam}, nem com o vínculo
 * organizacional genérico do contexto {@code organizacao} ({@code MembroOrganizacaoEntity}) — os
 * três modelam relações diferentes que compartilham vocabulário semelhante.
 */
package br.com.nsfatima.gestao.membro;
