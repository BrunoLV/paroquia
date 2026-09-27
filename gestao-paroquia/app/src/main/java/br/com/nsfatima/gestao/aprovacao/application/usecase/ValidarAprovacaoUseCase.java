package br.com.nsfatima.gestao.aprovacao.application.usecase;

import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.domain.exception.AprovacaoObrigatoriaException;
import br.com.nsfatima.gestao.calendario.domain.exception.OperacaoProibidaException;
import br.com.nsfatima.gestao.calendario.domain.type.AprovadorPapel;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Executa regras de validação prévia de autoridade antes do despacho de aprovações.
 * Na governança canônica, o Pároco detém alçada exclusiva para decisão final.
 */
@Service
public class ValidarAprovacaoUseCase {

    private static final Set<AprovadorPapel> ALLOWED_APPROVER_ROLES = Set.of(AprovadorPapel.PAROCO);
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
        if (!ALLOWED_APPROVER_ROLES.contains(role)) {
            throw new AprovacaoObrigatoriaException("É necessária a aprovação exclusiva do pároco");
        }
    }

    /**
     * Valida se o papel do ator confere alçada canônica para decidir aprovações.
     * Exclusivo para Pároco (ou Bispo Diocesano). Conselhos e coordenações não decidem.
     *
     * @param role papel informado
     * @param organizationType tipo de organização informado
     */
    public void validarPapelDecisaoAprovacao(String role, String organizationType) {
        if (role != null) {
            String normalized = role.trim().toLowerCase(Locale.ROOT).replace("_", "-");
            if ("paroco".equals(normalized) || "bispo-diocesano".equals(normalized)) {
                return;
            }
        }
        throw new OperacaoProibidaException("Apenas o Pároco tem autoridade canônica para decidir aprovações");
    }
}
