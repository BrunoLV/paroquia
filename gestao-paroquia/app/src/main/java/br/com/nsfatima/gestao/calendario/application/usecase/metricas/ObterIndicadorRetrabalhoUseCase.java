package br.com.nsfatima.gestao.calendario.application.usecase.metricas;

import br.com.nsfatima.gestao.calendario.api.v1.dto.metricas.IndicadorRetrabalhoResponse;
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

/**
 * Mede o indicador de retrabalho baseado em eventos devolvidos ou alterados após submissão.
 */
@Service
public class ObterIndicadorRetrabalhoUseCase {

    private final PeriodoOperacionalPolicy periodoOperacionalPolicy;
    private final CalculadoraTaxaRetrabalho calculadoraTaxaRetrabalho;

    public ObterIndicadorRetrabalhoUseCase(
            PeriodoOperacionalPolicy periodoOperacionalPolicy,
            CalculadoraTaxaRetrabalho calculadoraTaxaRetrabalho) {
        this.periodoOperacionalPolicy = periodoOperacionalPolicy;
        this.calculadoraTaxaRetrabalho = calculadoraTaxaRetrabalho;
    }

    /**
     * Calcula o indicador de ineficiência operacional para uma organização específica, ajudando lideranças a
     * entender a frequência de mudanças pós-planejamento.
     * <p>
     * Exemplo de uso:
     * {@code
     * IndicadorRetrabalhoResponse response = useCase.execute(orgId, "anual", null, null);
     * }
     */
    @Transactional(readOnly = true)
    public IndicadorRetrabalhoResponse execute(
            UUID organizacaoId, String granularidade, Instant inicio, Instant fim) {
        assertAccessToOrganization(organizacaoId);
        PeriodoOperacionalPolicy.ResolvedPeriod periodo = periodoOperacionalPolicy.resolve(granularidade, inicio, fim);
        return calculadoraTaxaRetrabalho.calcularParaOrganizacao(organizacaoId, periodo);
    }

    private void assertAccessToOrganization(UUID organizacaoId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UsuarioDetails usuarioDetails)) {
            throw new AccessDeniedException("Usuário autenticado é obrigatório");
        }
        if (usuarioDetails.findVinculoPorOrganizacao(organizacaoId).isEmpty()) {
            throw new AccessDeniedException(
                    "User cannot query operational data outside the responsible organization scope");
        }
    }
}
