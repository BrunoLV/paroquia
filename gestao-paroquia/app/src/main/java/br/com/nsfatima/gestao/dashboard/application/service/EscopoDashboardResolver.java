package br.com.nsfatima.gestao.dashboard.application.service;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Decide sobre qual organização uma consulta de dashboard é calculada, fechando o vetor de IDOR
 * entre comunidades: o filtro global permite escolher uma comunidade, mas um ator restrito a uma
 * pastoral não pode espiar métricas de outra.
 *
 * <p>Regra: um ator de escopo global (sem organização no contexto — pároco/administrador) pode
 * consultar qualquer organização, ou a visão global quando nada é pedido. Um ator vinculado a uma
 * organização só pode consultar a própria; pedir outra resulta em {@link AccessDeniedException}, e
 * não passar filtro nenhum recai no escopo do próprio vínculo.
 */
@Service
public class EscopoDashboardResolver {

    /**
     * Resolve a organização efetiva sobre a qual as métricas serão calculadas, aplicando a regra
     * de visibilidade descrita na documentação da classe.
     *
     * @param actorContext contexto do ator autenticado
     * @param organizacaoIdSolicitado organização escolhida no filtro, ou {@code null}
     * @return organização efetiva do cálculo ({@link Optional#empty()} = visão global)
     * @throws AccessDeniedException se o ator não tiver acesso à organização solicitada
     */
    public Optional<UUID> resolver(EventoActorContext actorContext, UUID organizacaoIdSolicitado) {
        UUID escopoDoAtor = actorContext.organizationId();
        if (escopoDoAtor == null) {
            return Optional.ofNullable(organizacaoIdSolicitado);
        }
        if (organizacaoIdSolicitado != null && !organizacaoIdSolicitado.equals(escopoDoAtor)) {
            throw new AccessDeniedException("Ator sem acesso à organização solicitada no filtro do dashboard");
        }
        return Optional.of(escopoDoAtor);
    }
}
