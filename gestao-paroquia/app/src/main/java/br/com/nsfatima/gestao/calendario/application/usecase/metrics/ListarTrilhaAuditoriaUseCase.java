package br.com.nsfatima.gestao.calendario.application.usecase.metrics;

import br.com.nsfatima.gestao.calendario.api.v1.dto.metrics.AuditoriaOperacaoResponse;
import br.com.nsfatima.gestao.calendario.api.v1.dto.metrics.PeriodoOperacionalResponse;
import br.com.nsfatima.gestao.calendario.domain.policy.PeriodoOperacionalPolicy;
import br.com.nsfatima.gestao.iam.infrastructure.security.UsuarioDetails;
import br.com.nsfatima.gestao.observabilidade.application.service.AuditoriaQueryService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Consulta e extrai a trilha consolidada de auditoria de eventos para conformidade.
 */
@Service
public class ListarTrilhaAuditoriaUseCase {

    private final AuditoriaQueryService auditLogQueryService;
    private final PeriodoOperacionalPolicy periodoOperacionalPolicy;

    public ListarTrilhaAuditoriaUseCase(
            AuditoriaQueryService auditLogQueryService,
            PeriodoOperacionalPolicy periodoOperacionalPolicy) {
        this.auditLogQueryService = auditLogQueryService;
        this.periodoOperacionalPolicy = periodoOperacionalPolicy;
    }

    /**
     * Fornece uma listagem detalhada e filtrada das operações do sistema, permitindo revisões de segurança
     * abrangentes e rastreamento de atividade em todo o domínio de calendário.
     * <p>
     * Exemplo de uso:
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

        return new AuditoriaOperacaoResponse(grupoParoquialId,
                new PeriodoOperacionalResponse(periodo.granularidade(), periodo.inicio(), periodo.fim()), items);
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
