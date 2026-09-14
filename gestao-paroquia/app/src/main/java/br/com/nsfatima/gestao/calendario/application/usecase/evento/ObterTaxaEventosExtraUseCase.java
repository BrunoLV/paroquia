package br.com.nsfatima.gestao.calendario.application.usecase.evento;

import br.com.nsfatima.gestao.calendario.api.v1.dto.metrics.TaxaEventosExtraResponse;
import br.com.nsfatima.gestao.calendario.domain.policy.PeriodoOperacionalPolicy;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.iam.infrastructure.security.UsuarioDetails;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

/**
 * Calcula a proporção de eventos extraordinários em relação à programação ordinária.
 */
@Service
public class ObterTaxaEventosExtraUseCase {

    private static final int TAXA_SCALE = 4;

    private final EventoJpaRepository eventoJpaRepository;
    private final PeriodoOperacionalPolicy periodoOperacionalPolicy;

    public ObterTaxaEventosExtraUseCase(
            EventoJpaRepository eventoJpaRepository,
            PeriodoOperacionalPolicy periodoOperacionalPolicy) {
        this.eventoJpaRepository = eventoJpaRepository;
        this.periodoOperacionalPolicy = periodoOperacionalPolicy;
    }

    /**
     * Provides a quantitative measure of planning quality by calculating the ratio of unplanned events within a
     * specific organizational context and timeframe.
     * <p>
     * Usage Example:
     * {@code
     * TaxaEventosExtraResponse response = useCase.execute(orgId, "mensal", null, null);
     * }
     */
    @Transactional(readOnly = true)
    public TaxaEventosExtraResponse execute(
            UUID grupoParoquialId,
            String granularidade,
            Instant inicio,
            Instant fim) {
        assertAccessToOrganization(grupoParoquialId);
        PeriodoOperacionalPolicy.ResolvedPeriod periodo = periodoOperacionalPolicy.resolve(granularidade, inicio, fim);

        long extras = eventoJpaRepository.countByGrupoParoquialAndStatusAndPeriod(
                grupoParoquialId,
                "ADICIONADO_EXTRA",
                periodo.inicio(),
                periodo.fim());
        long total = eventoJpaRepository.countByGrupoParoquialAndPeriod(
                grupoParoquialId,
                periodo.inicio(),
                periodo.fim());

        BigDecimal taxa = calcularTaxa(extras, total);

        return new TaxaEventosExtraResponse(
                periodo.granularidade() != null ? periodo.granularidade() : "custom",
                taxa);
    }

    /** Percentual numerador/denominador com precisão decimal exata; {@code ZERO} se não houver denominador. */
    private BigDecimal calcularTaxa(long numerador, long denominador) {
        return denominador == 0L
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(numerador)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(denominador), TAXA_SCALE, RoundingMode.HALF_UP);
    }

    private void assertAccessToOrganization(UUID grupoParoquialId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UsuarioDetails usuarioDetails)) {
            throw new AccessDeniedException("Usuário autenticado é obrigatório");
        }
        if (usuarioDetails.findVinculoPorGrupoParoquial(grupoParoquialId).isEmpty()) {
            throw new AccessDeniedException(
                    "Usuário não pode consultar métricas fora do escopo da organização responsável");
        }
    }
}
