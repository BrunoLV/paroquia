package br.com.nsfatima.gestao.patrimonio.domain.service;

import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoAlocacaoInvalidaException;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoStatus;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoPatrimonialEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class AtivoVinculacaoPolicy {

    private static final int JUSTIFICATIVA_MIN_LENGTH = 10;

    private final AtivoAlocacaoConflitoPolicy conflitoPolicy;

    public AtivoVinculacaoPolicy(AtivoAlocacaoConflitoPolicy conflitoPolicy) {
        this.conflitoPolicy = conflitoPolicy;
    }

    public void assertCanAlocar(AtivoPatrimonialEntity ativo, LocalDate dataInicio, LocalDate dataFim,
                                 String justificativaAlocacao) {
        if (ativo.getStatus() == AtivoStatus.BAIXADO) {
            throw new AtivoAlocacaoInvalidaException("Não é possível alocar um ativo com status BAIXADO");
        }

        if (dataFim.isBefore(dataInicio)) {
            throw new AtivoAlocacaoInvalidaException("A data final da alocação não pode ser anterior à data inicial");
        }

        if (justificativaAlocacao == null || justificativaAlocacao.trim().length() < JUSTIFICATIVA_MIN_LENGTH) {
            throw new AtivoAlocacaoInvalidaException(
                    "A alocação de um ativo exige uma nota explicativa de utilização.");
        }

        conflitoPolicy.assertPeriodoDisponivel(ativo.getId(), dataInicio, dataFim);
    }

    public void assertCanReconfirmar(AtivoPatrimonialEntity ativo, UUID alocacaoId, LocalDate dataInicio,
                                      LocalDate dataFim) {
        if (ativo.getStatus() == AtivoStatus.BAIXADO) {
            throw new AtivoAlocacaoInvalidaException("Não é possível alocar um ativo com status BAIXADO");
        }

        conflitoPolicy.assertPeriodoDisponivel(ativo.getId(), dataInicio, dataFim, alocacaoId);
    }
}
