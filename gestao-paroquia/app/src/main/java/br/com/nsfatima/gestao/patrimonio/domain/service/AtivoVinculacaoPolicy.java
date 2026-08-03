package br.com.nsfatima.gestao.patrimonio.domain.service;

import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoAlocacaoInvalidaException;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoStatus;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoPatrimonialEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Valida as pré-condições para vincular um ativo patrimonial a um novo período de ocupação: o
 * ativo não pode estar baixado, o período deve ser consistente, a alocação exige uma justificativa
 * de uso com tamanho mínimo (não é uma anotação opcional), e o período não pode conflitar com
 * outra ocupação já registrada.
 */
@Service
public class AtivoVinculacaoPolicy {

    private static final int JUSTIFICATIVA_MIN_LENGTH = 10;

    private final AtivoAlocacaoConflitoPolicy conflitoPolicy;

    public AtivoVinculacaoPolicy(AtivoAlocacaoConflitoPolicy conflitoPolicy) {
        this.conflitoPolicy = conflitoPolicy;
    }

    /**
     * Valida se um ativo pode ser alocado ao período/justificativa informados: rejeita ativos
     * baixados, períodos invertidos, justificativas ausentes ou muito curtas, e conflitos com
     * outra ocupação já registrada.
     *
     * @param ativo                 ativo a alocar
     * @param dataInicio            início do período de alocação
     * @param dataFim               fim do período de alocação
     * @param justificativaAlocacao nota de uso obrigatória (mínimo de caracteres exigido)
     * @throws AtivoAlocacaoInvalidaException se qualquer uma das pré-condições falhar
     */
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

    /**
     * Valida se uma ocupação já existente (empréstimo aprovado) pode ser reconfirmada no ativo,
     * ignorando a própria ocupação na checagem de conflito de período (ela mesma já ocupa o
     * período que está sendo reconfirmado).
     *
     * @param ativo      ativo cuja ocupação será reconfirmada
     * @param alocacaoId ocupação a reconfirmar, excluída da checagem de conflito
     * @param dataInicio início do período já reservado
     * @param dataFim    fim do período já reservado
     * @throws AtivoAlocacaoInvalidaException se o ativo estiver baixado ou o período conflitar
     *     com outra ocupação
     */
    public void assertCanReconfirmar(AtivoPatrimonialEntity ativo, UUID alocacaoId, LocalDate dataInicio,
                                      LocalDate dataFim) {
        if (ativo.getStatus() == AtivoStatus.BAIXADO) {
            throw new AtivoAlocacaoInvalidaException("Não é possível alocar um ativo com status BAIXADO");
        }

        conflitoPolicy.assertPeriodoDisponivel(ativo.getId(), dataInicio, dataFim, alocacaoId);
    }
}
