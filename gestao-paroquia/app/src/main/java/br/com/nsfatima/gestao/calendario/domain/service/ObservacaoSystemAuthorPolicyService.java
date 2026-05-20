package br.com.nsfatima.gestao.calendario.domain.service;

import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ObservacaoSystemAuthorPolicyService {

    private static final UUID TECHNICAL_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    public UUID resolveAuthorId(UUID actorUserId) {
        return actorUserId == null ? TECHNICAL_USER_ID : actorUserId;
    }
}
