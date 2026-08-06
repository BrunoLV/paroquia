package br.com.nsfatima.gestao.aprovacao.application.usecase;

import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.domain.exception.AprovacaoObrigatoriaException;
import br.com.nsfatima.gestao.calendario.domain.exception.ForbiddenOperationException;
import br.com.nsfatima.gestao.calendario.domain.type.AprovadorPapel;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
public class ValidateAprovacaoUseCase {

    private static final Set<AprovadorPapel> ALLOWED_APPROVER_ROLES = Set.of(
            AprovadorPapel.PAROCO,
            AprovadorPapel.COORD_CONSELHO,
            AprovadorPapel.VICE_COORD_CONSELHO);
    private static final String ERROR_MSG = "Aprovacao is required for date change or cancellation";
    private final AprovacaoJpaRepository aprovacaoJpaRepository;

    public ValidateAprovacaoUseCase(AprovacaoJpaRepository aprovacaoJpaRepository) {
        this.aprovacaoJpaRepository = aprovacaoJpaRepository;
    }

    public void validateRequired(UUID eventoId, UUID aprovacaoId) {
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
        if (!ALLOWED_APPROVER_ROLES.contains(role)) {
            throw new AprovacaoObrigatoriaException(
                    "Conselho coordinator, conselho vice-coordinator, or parroco aprovacao is required");
        }
    }

    /**
     * Enforces the organizational hierarchy by restricting decision-making power to specific roles within the
     * Council or the Parish Priest.
     * <p>
     * Usage Example:
     * {@code
     * useCase.validateAprovacaoDecisionRole("COORDENADOR", "CONSELHO");
     * }
     */
    public void validateAprovacaoDecisionRole(String role, String organizationType) {
        PapelOrganizacional normalizedRole = PapelOrganizacional.fromStoredValue(role);
        TipoGrupo normalizedOrgType = TipoGrupo.fromStoredValue(organizationType);
        if (normalizedRole == PapelOrganizacional.PAROCO || normalizedRole == PapelOrganizacional.ADMIN) {
            return;
        }
        if (normalizedOrgType == TipoGrupo.CONSELHO
                && (normalizedRole == PapelOrganizacional.PRESIDENTE_CPC
                || normalizedRole == PapelOrganizacional.MESA_CPP
                || normalizedRole == PapelOrganizacional.COORD_PASTORAL_GLOBAL
                || normalizedRole == PapelOrganizacional.COORD_PASTORAL_LOCAL
                || normalizedRole == PapelOrganizacional.COORD_PASTORAL)) {
            return;
        }
        throw new ForbiddenOperationException("User does not have permission to decide event aprovacaos");
    }
}
