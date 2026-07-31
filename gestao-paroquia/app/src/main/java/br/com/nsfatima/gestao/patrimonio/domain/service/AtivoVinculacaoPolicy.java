package br.com.nsfatima.gestao.patrimonio.domain.service;

import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoAlocacaoInvalidaException;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoStatus;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoPatrimonialEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AtivoVinculacaoPolicy {

    private static final int JUSTIFICATIVA_MIN_LENGTH = 10;

    public void assertCanAlocar(AtivoPatrimonialEntity ativo, UUID eventoLocalOrganizacaoId,
                                 String justificativaAlocacao) {
        if (ativo.getStatus() != AtivoStatus.DISPONIVEL) {
            throw new AtivoAlocacaoInvalidaException("Somente ativos com status DISPONIVEL podem ser alocados");
        }

        if (!ativo.getOrganizacaoId().equals(eventoLocalOrganizacaoId)) {
            throw new AtivoAlocacaoInvalidaException(
                    "Não é permitido alocar ativos de outras comunidades para este local.");
        }

        if (justificativaAlocacao == null || justificativaAlocacao.trim().length() < JUSTIFICATIVA_MIN_LENGTH) {
            throw new AtivoAlocacaoInvalidaException(
                    "A alocação de um ativo exige uma nota explicativa de utilização.");
        }
    }
}
