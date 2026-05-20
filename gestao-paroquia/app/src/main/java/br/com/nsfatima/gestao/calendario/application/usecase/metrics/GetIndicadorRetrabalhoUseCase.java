package br.com.nsfatima.gestao.calendario.application.usecase.metrics;

import br.com.nsfatima.gestao.calendario.api.v1.dto.metrics.IndicadorRetrabalhoResponse;
import br.com.nsfatima.gestao.calendario.domain.policy.PeriodoOperacionalPolicy;
import br.com.nsfatima.gestao.iam.infrastructure.security.UsuarioDetails;
import java.time.Instant;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetIndicadorRetrabalhoUseCase {

    private final PeriodoOperacionalPolicy periodoOperacionalPolicy;
    private final ReworkRateCalculator reworkRateCalculator;

    public GetIndicadorRetrabalhoUseCase(
            PeriodoOperacionalPolicy periodoOperacionalPolicy,
            ReworkRateCalculator reworkRateCalculator) {
        this.periodoOperacionalPolicy = periodoOperacionalPolicy;
        this.reworkRateCalculator = reworkRateCalculator;
    }

    /**
     * Calculates the operational inefficiency metric for a specific organization, helping leaders understand the frequency of post-planning changes.
     * 
     * Usage Example:
     * {@code
     * IndicadorRetrabalhoResponse response = useCase.execute(orgId, "anual", null, null);
     * }
     */
    @Transactional(readOnly = true)
    public IndicadorRetrabalhoResponse execute(UUID organizacaoId, String granularidade, Instant inicio, Instant fim) {
        assertAccessToOrganization(organizacaoId);
        PeriodoOperacionalPolicy.ResolvedPeriod periodo = periodoOperacionalPolicy.resolve(granularidade, inicio, fim);
        return reworkRateCalculator.calculateForOrganization(organizacaoId, periodo);
    }

    private void assertAccessToOrganization(UUID organizacaoId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UsuarioDetails usuarioDetails)) {
            throw new AccessDeniedException("Authenticated user required");
        }
        if (usuarioDetails.findMembershipByOrganization(organizacaoId).isEmpty()) {
            throw new AccessDeniedException(
                    "User cannot query operational data outside the responsible organization scope");
        }
    }
}
