package br.com.nsfatima.gestao.patrimonio.application.usecase;

import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.CancelarAlocacaoRequest;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoAlocacaoCancelamentoInvalidoException;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoAlocacaoStatus;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoAlocacaoTipo;
import br.com.nsfatima.gestao.patrimonio.domain.service.PatrimonioAuthorizationService;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoAlocacaoEntity;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.AtivoAlocacaoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Cancela uma reserva/pedido de alocação de ativo (incluindo pedidos de empréstimo pendentes de
 * aprovação) desde que ainda não tenha iniciado seu período. Se a ocupação estiver vinculada a
 * uma solicitação de aprovação pendente, essa solicitação é automaticamente reprovada em
 * conjunto para manter os dois estados consistentes.
 */
@Service
public class CancelarAlocacaoAtivoUseCase {

    private static final List<AtivoAlocacaoStatus> STATUS_CANCELAVEIS = List.of(
            AtivoAlocacaoStatus.PENDENTE_APROVACAO, AtivoAlocacaoStatus.CONFIRMADA);

    private final AtivoAlocacaoJpaRepository alocacaoRepository;
    private final AprovacaoJpaRepository aprovacaoRepository;
    private final EventoActorContextResolver actorContextResolver;
    private final PatrimonioAuthorizationService authorizationService;

    public CancelarAlocacaoAtivoUseCase(AtivoAlocacaoJpaRepository alocacaoRepository,
                                         AprovacaoJpaRepository aprovacaoRepository,
                                         EventoActorContextResolver actorContextResolver,
                                         PatrimonioAuthorizationService authorizationService) {
        this.alocacaoRepository = alocacaoRepository;
        this.aprovacaoRepository = aprovacaoRepository;
        this.actorContextResolver = actorContextResolver;
        this.authorizationService = authorizationService;
    }

    /**
     * Cancela a ocupação (alocação ou manutenção) informada, exigindo que ela esteja em um status
     * cancelável ({@code PENDENTE_APROVACAO} ou {@code CONFIRMADA}) e que seu período ainda não
     * tenha começado. O solicitante original sempre pode cancelar; caso contrário, é exigida
     * autorização equivalente à de iniciar a alocação/manutenção na organização de origem.
     *
     * @param alocacaoId ocupação a cancelar
     * @param request    motivo do cancelamento
     * @throws AtivoAlocacaoCancelamentoInvalidoException se a ocupação não existir, já tiver
     *     iniciado ou não estiver em status cancelável
     */
    @Transactional
    public void execute(UUID alocacaoId, CancelarAlocacaoRequest request) {
        AtivoAlocacaoEntity ocupacao = alocacaoRepository.findById(alocacaoId)
                .orElseThrow(() -> new AtivoAlocacaoCancelamentoInvalidoException(
                        "Ocupação não encontrada: " + alocacaoId));

        if (!STATUS_CANCELAVEIS.contains(ocupacao.getStatus()) || !ocupacao.getDataInicio().isAfter(LocalDate.now())) {
            throw new AtivoAlocacaoCancelamentoInvalidoException(
                    "Só é possível cancelar uma reserva futura ainda não iniciada");
        }

        EventoActorContext actorContext = actorContextResolver.resolveRequired();
        assertPodeCancelar(actorContext, ocupacao);

        ocupacao.setStatus(AtivoAlocacaoStatus.CANCELADA);
        ocupacao.setMotivoCancelamento(request.motivoCancelamento());
        alocacaoRepository.save(ocupacao);

        if (ocupacao.getAprovacaoId() != null) {
            aprovacaoRepository.findById(ocupacao.getAprovacaoId()).ifPresent(aprovacao ->
                    cancelarAprovacaoVinculada(aprovacao, request.motivoCancelamento()));
        }
    }

    private void cancelarAprovacaoVinculada(AprovacaoEntity aprovacao, String motivo) {
        if (aprovacao.getStatus() != AprovacaoStatus.PENDENTE) {
            return;
        }
        aprovacao.setStatus(AprovacaoStatus.REPROVADA);
        aprovacao.setDecididoEmUtc(Instant.now());
        aprovacao.setDecisionObservacao(motivo);
        aprovacaoRepository.save(aprovacao);
    }

    private void assertPodeCancelar(EventoActorContext actorContext, AtivoAlocacaoEntity ocupacao) {
        boolean isSolicitante = ocupacao.getSolicitanteId() != null
                && ocupacao.getSolicitanteId().equals(actorContext.usuarioId());
        if (isSolicitante) {
            return;
        }

        if (ocupacao.getTipo() == AtivoAlocacaoTipo.MANUTENCAO) {
            authorizationService.assertCanIniciarManutencao(actorContext, ocupacao.getGrupoParoquialOrigemId());
        } else {
            authorizationService.assertCanAlocar(actorContext, ocupacao.getGrupoParoquialOrigemId());
        }
    }
}
