/**
 * Contexto de identidade e acesso (IAM): cadastro, aprovação, autenticação e autorização de
 * usuários ({@code UsuarioEntity}), incluindo o fluxo de auto-registro pendente de aprovação
 * administrativa e a troca/reset de senha.
 *
 * <p>Distinto do contexto {@code membro}: aqui o foco é a conta/credencial de acesso ao sistema;
 * o vínculo do usuário com uma organização e um papel institucional é modelado no contexto
 * {@code organizacao} ({@code MembroOrganizacaoEntity}), não aqui.
 */
package br.com.nsfatima.gestao.iam;
