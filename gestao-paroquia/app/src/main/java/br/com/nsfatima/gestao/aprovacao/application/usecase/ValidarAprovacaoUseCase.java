package br.com.nsfatima.gestao.aprovacao.application.usecase;

import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.domain.exception.AprovacaoObrigatoriaException;
import br.com.nsfatima.gestao.calendario.domain.exception.OperacaoProibidaException;
import br.com.nsfatima.gestao.calendario.domain.type.AprovadorPapel;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

/**
 * Executa regras de validação prévia de autoridade antes do despacho de aprovações.
 * Na governança canônica, o Pároco (e o Bispo Diocesano) detém a alçada para decisão final.
 */
@Service
public class ValidarAprovacaoUseCase {

    private static final Set<AprovadorPapel> ALLOWED_APPROVER_ROLES =
            Set.of(AprovadorPapel.PAROCO, AprovadorPapel.BISPO_DIOCESANO);
    private static final String ERROR_MSG = "Aprovacao is required for date change or cancellation";
    private final AprovacaoJpaRepository aprovacaoJpaRepository;

    public ValidarAprovacaoUseCase(AprovacaoJpaRepository aprovacaoJpaRepository) {
        this.aprovacaoJpaRepository = aprovacaoJpaRepository;
    }

    public void validarObrigatorio(UUID eventoId, UUID aprovacaoId) {
        if (aprovacaoId == null) {
            throw new AprovacaoObrigatoriaException(ERROR_MSG);
        }

        AprovacaoEntity aprovacao = aprovacaoJpaRepository
                .findByIdAndEventoId(aprovacaoId, eventoId)
                .orElseThrow(
                        () -> new AprovacaoObrigatoriaException(ERROR_MSG));

        if (aprovacao.getStatus() != AprovacaoStatus.APROVADA) {
            throw new AprovacaoObrigatoriaException(ERROR_MSG);
        }

        AprovadorPapel role = aprovacao.getAprovadorPapelEnum();
        if (role == null || !ALLOWED_APPROVER_ROLES.contains(role)) {
            throw new AprovacaoObrigatoriaException("É necessária a aprovação do pároco ou do bispo diocesano");
        }
    }

    /**
     * Indica se o papel confere alçada canônica para decidir aprovações (e, por consequência, para ver os
     * detalhes técnicos delas). Fonte única da regra: Pároco ou Bispo Diocesano. Conselhos, coordenações e o
     * Admin técnico não decidem.
     *
     * @param role papel do ator
     * @return {@code true} se o papel decide aprovações
     */
    public boolean podeDecidir(String role) {
        if (role == null) {
            return false;
        }
        AprovadorPapel papel = AprovadorPapel.fromStoredValue(role);
        return papel != null && ALLOWED_APPROVER_ROLES.contains(papel);
    }

    /**
     * Valida se o papel do ator confere alçada canônica para decidir aprovações.
     * Exclusivo para Pároco e Bispo Diocesano. Conselhos e coordenações não decidem.
     *
     * @param role papel informado
     * @param organizationType tipo de organização informado
     */
    public void validarPapelDecisaoAprovacao(String role, String organizationType) {
        if (!podeDecidir(role)) {
            throw new OperacaoProibidaException(
                    "Apenas o Pároco ou o Bispo Diocesano têm autoridade canônica para decidir aprovações");
        }
    }
}
