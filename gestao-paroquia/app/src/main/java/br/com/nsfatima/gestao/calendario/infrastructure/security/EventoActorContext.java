package br.com.nsfatima.gestao.calendario.infrastructure.security;

import java.util.UUID;

/**
 * Representa o contexto do ator autenticado que está executando uma operação sobre eventos do
 * calendário: quem é o ator, com qual papel organizacional e em qual escopo de organização ele
 * está agindo. A maioria das checagens de autorização neste contexto é baseada em
 * papel-do-ator + escopo-da-organização (e não em uma checagem simples de posse do recurso), então
 * este objeto centraliza essa tripla para que casos de uso e serviços de autorização não precisem
 * re-derivar a mesma informação a partir da {@code Authentication} do Spring Security em cada ponto
 * de decisão.
 *
 * @param actor identificador/login do usuário autenticado que está agindo
 * @param role papel organizacional do ator no escopo resolvido (ex.: valor de
 *     {@code PapelOrganizacional})
 * @param organizationType tipo da organização em que o papel é exercido (ex.: valor de
 *     {@code TipoGrupo})
 * @param organizationId identificador da organização de escopo, ou {@code null} quando o ator atua
 *     sem vínculo a uma organização específica (ex.: administrador global)
 * @param usuarioId identificador interno do usuário autenticado, quando disponível
 * @param igrejaVinculoId identificador do vínculo do usuário com a organização, quando aplicável
 */
public record EventoActorContext(
        String actor,
        String role,
        String organizationType,
        UUID organizationId,
        UUID usuarioId,
        UUID igrejaVinculoId) {
}
