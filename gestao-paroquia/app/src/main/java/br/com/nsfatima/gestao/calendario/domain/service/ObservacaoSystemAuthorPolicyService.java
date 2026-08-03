package br.com.nsfatima.gestao.calendario.domain.service;

import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Serviço de domínio que resolve o autor a ser registrado em observações de evento
 * geradas automaticamente pelo sistema (justificativa, aprovação, reprovação,
 * cancelamento, ajuste de horário) quando não há um usuário humano identificável
 * disparando a ação — nesses casos, a autoria é atribuída a um usuário técnico
 * fixo, garantindo que toda observação tenha um autor válido.
 */
@Service
public class ObservacaoSystemAuthorPolicyService {

    private static final UUID TECHNICAL_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    /**
     * Resolve o identificador do autor a ser registrado na observação: o próprio
     * ator, quando identificado, ou o usuário técnico fixo do sistema quando a ação
     * não tem um ator humano associado.
     *
     * @param actorUserId identificador do ator que disparou a ação, ou {@code null} se a ação for automática
     * @return identificador do ator, ou o identificador do usuário técnico se o ator for {@code null}
     */
    public UUID resolveAuthorId(UUID actorUserId) {
        return actorUserId == null ? TECHNICAL_USER_ID : actorUserId;
    }
}
