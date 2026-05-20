package br.com.nsfatima.gestao.calendario.application.usecase.evento;

import br.com.nsfatima.gestao.calendario.api.v1.dto.metrics.TaxaEventosExtraResponse;
import br.com.nsfatima.gestao.calendario.domain.policy.PeriodoOperacionalPolicy;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.iam.infrastructure.security.UsuarioDetails;
import java.time.Instant;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetTaxaEventosExtraUseCase {

    private final EventoJpaRepository eventoJpaRepository;
    private final PeriodoOperacionalPolicy periodoOperacionalPolicy;

    public GetTaxaEventosExtraUseCase(
            EventoJpaRepository eventoJpaRepository,
            PeriodoOperacionalPolicy periodoOperacionalPolicy) {
        this.eventoJpaRepository = eventoJpaRepository;
        this.periodoOperacionalPolicy = periodoOperacionalPolicy;
    }

    /**
     * Provides a quantitative measure of planning quality by calculating the ratio of unplanned events within a specific organizational context and timeframe.
     * 
     * Usage Example:
     * {@code
     * TaxaEventosExtraResponse response = useCase.execute(orgId, "mensal", null, null);
     * }
     */
    @Transactional(readOnly = true)
    public TaxaEventosExtraResponse execute(
            UUID organizacaoId,
            String granularidade,
            Instant inicio,
            Instant fim) {
        assertAccessToOrganization(organizacaoId);
        PeriodoOperacionalPolicy.ResolvedPeriod periodo = periodoOperacionalPolicy.resolve(granularidade, inicio, fim);

        long extras = eventoJpaRepository.countByOrganizacaoAndStatusAndPeriod(
                organizacaoId,
                "ADICIONADO_EXTRA",
                periodo.inicio(),
                periodo.fim());
        long total = eventoJpaRepository.countByOrganizacaoAndPeriod(
                organizacaoId,
                periodo.inicio(),
                periodo.fim());

        double taxa = total == 0L ? 0.0 : (extras / (double) total) * 100.0;

        return new TaxaEventosExtraResponse(
                periodo.granularidade() != null ? periodo.granularidade() : "custom",
                taxa);
    }

    private void assertAccessToOrganization(UUID organizacaoId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UsuarioDetails usuarioDetails)) {
            throw new AccessDeniedException("Authenticated user required");
        }
        if (usuarioDetails.findMembershipByOrganization(organizacaoId).isEmpty()) {
            throw new AccessDeniedException("User cannot query metrics outside the responsible organization scope");
        }
    }
}
