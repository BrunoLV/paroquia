package br.com.nsfatima.gestao.calendario.application.usecase.metrics;

import br.com.nsfatima.gestao.calendario.api.v1.dto.metrics.IndicadorRetrabalhoResponse;
import br.com.nsfatima.gestao.calendario.application.service.CalculadoraTaxaRetrabalho;
import br.com.nsfatima.gestao.calendario.domain.policy.PeriodoOperacionalPolicy;
import br.com.nsfatima.gestao.iam.infrastructure.security.UsuarioDetails;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class GetIndicadorRetrabalhoUseCase {

    private final PeriodoOperacionalPolicy periodoOperacionalPolicy;
    private final CalculadoraTaxaRetrabalho reworkRateCalculator;

    public GetIndicadorRetrabalhoUseCase(
            PeriodoOperacionalPolicy periodoOperacionalPolicy,
            CalculadoraTaxaRetrabalho reworkRateCalculator) {
        this.periodoOperacionalPolicy = periodoOperacionalPolicy;
        this.reworkRateCalculator = reworkRateCalculator;
    }

    /**
     * Calculates the operational inefficiency metric for a specific organization, helping leaders understand the
     * frequency of post-planning changes.
     * <p>
     * Usage Example:
     * {@code
     * IndicadorRetrabalhoResponse response = useCase.execute(orgId, "anual", null, null);
     * }
     */
    @Transactional(readOnly = true)
    public IndicadorRetrabalhoResponse execute(UUID grupoParoquialId, String granularidade, Instant inicio, Instant fim) {
        assertAccessToOrganization(grupoParoquialId);
        PeriodoOperacionalPolicy.ResolvedPeriod periodo = periodoOperacionalPolicy.resolve(granularidade, inicio, fim);
        return reworkRateCalculator.calculateForOrganization(grupoParoquialId, periodo);
    }

    private void assertAccessToOrganization(UUID grupoParoquialId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UsuarioDetails usuarioDetails)) {
            throw new AccessDeniedException("Usuário autenticado é obrigatório");
        }
        if (usuarioDetails.findVinculoPorGrupoParoquial(grupoParoquialId).isEmpty()) {
            throw new AccessDeniedException(
                    "User cannot query operational data outside the responsible organization scope");
        }
    }
}
