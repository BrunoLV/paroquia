package br.com.nsfatima.gestao.calendario.application.usecase.metrics;

import br.com.nsfatima.gestao.calendario.api.v1.dto.metrics.AuditoriaOperacaoResponse;
import br.com.nsfatima.gestao.calendario.domain.policy.PeriodoOperacionalPolicy;
import br.com.nsfatima.gestao.iam.infrastructure.security.UsuarioDetails;
import br.com.nsfatima.gestao.observabilidade.domain.service.AuditoriaQueryService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ListAuditTrailUseCase {

    private final AuditoriaQueryService auditLogQueryService;
    private final PeriodoOperacionalPolicy periodoOperacionalPolicy;

    public ListAuditTrailUseCase(
            AuditoriaQueryService auditLogQueryService,
            PeriodoOperacionalPolicy periodoOperacionalPolicy) {
        this.auditLogQueryService = auditLogQueryService;
        this.periodoOperacionalPolicy = periodoOperacionalPolicy;
    }

    /**
     * Provides a detailed and filtered list of system operations, enabling comprehensive security reviews and
     * activity tracking across the calendar domain.
     * <p>
     * Usage Example:
     * {@code
     * AuditoriaOperacaoResponse response = useCase.execute(orgId, "mensal", null, null, null, "patch", "success",
     * null);
     * }
     */
    public AuditoriaOperacaoResponse execute(
            UUID grupoParoquialId,
            String granularidade,
            Instant inicio,
            Instant fim,
            String ator,
            String acao,
            String resultado,
            String correlationId) {
        assertAccessToOrganization(grupoParoquialId);
        PeriodoOperacionalPolicy.ResolvedPeriod periodo = periodoOperacionalPolicy.resolve(granularidade, inicio, fim);

        List<AuditoriaOperacaoResponse.RegistroAuditavelItem> items = auditLogQueryService.findAuditTrail(
                grupoParoquialId,
                periodo.inicio(),
                periodo.fim(),
                ator,
                acao,
                resultado,
                correlationId);

        return new AuditoriaOperacaoResponse(grupoParoquialId, periodo.toResponse(), items);
    }

    private void assertAccessToOrganization(UUID grupoParoquialId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UsuarioDetails usuarioDetails)) {
            throw new AccessDeniedException("Usuário autenticado é obrigatório");
        }
        if (usuarioDetails.findVinculoPorGrupoParoquial(grupoParoquialId).isEmpty()) {
            throw new AccessDeniedException(
                    "Usuário não pode consultar dados de auditoria fora do escopo da organização responsável");
        }
    }
}
