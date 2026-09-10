package br.com.nsfatima.gestao.calendario.domain.policy;

import java.util.UUID;

/** Usa o usuário técnico canônico somente quando não há ator humano associado ao fato. */
public final class AutorTecnicoObservacaoPolicy implements AutorObservacaoSistemaPolicy {

    private static final UUID USUARIO_TECNICO_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Override
    public UUID resolve(UUID atorId) {
        return atorId == null ? USUARIO_TECNICO_ID : atorId;
    }
}
