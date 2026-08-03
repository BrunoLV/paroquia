package br.com.nsfatima.gestao.patrimonio.application.usecase.aprovacao.strategy;

import br.com.nsfatima.gestao.aprovacao.api.v1.dto.AprovacaoDecisionResponse;
import br.com.nsfatima.gestao.aprovacao.application.dto.AprovacaoAcaoPayload;
import br.com.nsfatima.gestao.aprovacao.application.mapper.AprovacaoAcaoPayloadMapper;
import br.com.nsfatima.gestao.aprovacao.application.usecase.strategy.EstrategiaExecucaoAprovacao;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.calendario.domain.exception.ForbiddenOperationException;
import br.com.nsfatima.gestao.calendario.domain.type.TipoSolicitacaoInput;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.patrimonio.application.usecase.AlocarAtivoParaEventoUseCase;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoPatrimonialNotFoundException;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoAlocacaoStatus;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoAlocacaoEntity;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoPatrimonialEntity;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.AtivoAlocacaoJpaRepository;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.AtivoPatrimonialJpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Plugin do fluxo genérico de aprovação (`EstrategiaExecucaoAprovacao`) para o tipo de solicitação
 * {@code ALOCACAO_ATIVO_EMPRESTIMO}: efetiva o empréstimo de ativo patrimonial entre organizações
 * quando aprovado, cancela a ocupação pendente quando rejeitado, e restringe quem pode decidir ao
 * coordenador de conselho da organização dona do ativo ou ao pároco/admin.
 */
@Component
public class AlocacaoAtivoExecutionStrategy implements EstrategiaExecucaoAprovacao {

    private final AlocarAtivoParaEventoUseCase alocarAtivoParaEventoUseCase;
    private final AtivoPatrimonialJpaRepository ativoRepository;
    private final AtivoAlocacaoJpaRepository alocacaoRepository;
    private final AprovacaoAcaoPayloadMapper aprovacaoAcaoPayloadMapper;

    public AlocacaoAtivoExecutionStrategy(AlocarAtivoParaEventoUseCase alocarAtivoParaEventoUseCase,
                                          AtivoPatrimonialJpaRepository ativoRepository,
                                          AtivoAlocacaoJpaRepository alocacaoRepository,
                                          AprovacaoAcaoPayloadMapper aprovacaoAcaoPayloadMapper) {
        this.alocarAtivoParaEventoUseCase = alocarAtivoParaEventoUseCase;
        this.ativoRepository = ativoRepository;
        this.alocacaoRepository = alocacaoRepository;
        this.aprovacaoAcaoPayloadMapper = aprovacaoAcaoPayloadMapper;
    }

    /**
     * Indica que esta estratégia trata apenas solicitações de empréstimo de ativo patrimonial.
     *
     * @param tipo tipo de solicitação da aprovação
     * @return {@code true} somente para {@code ALOCACAO_ATIVO_EMPRESTIMO}
     */
    @Override
    public boolean supports(TipoSolicitacaoInput tipo) {
        return tipo == TipoSolicitacaoInput.ALOCACAO_ATIVO_EMPRESTIMO;
    }

    /**
     * Executa a aprovação de um empréstimo: valida que o ator decisor tem autoridade sobre a
     * organização dona do ativo e delega a efetivação da alocação ao caso de uso de alocação.
     *
     * @param aprovacao   solicitação de aprovação já aceita, com o payload da ação
     * @param actorContext contexto do ator que decide a aprovação
     * @return resultado da execução, com o id/status do ativo já alocado
     * @throws AtivoPatrimonialNotFoundException se o ativo referenciado no payload não existir
     * @throws ForbiddenOperationException       se o ator não tiver autoridade sobre a organização
     *                                            dona do ativo
     */
    @Override
    public AprovacaoDecisionResponse.ActionExecution execute(AprovacaoEntity aprovacao,
                                                             EventoActorContext actorContext) {
        AprovacaoAcaoPayload payload = aprovacaoAcaoPayloadMapper.toPayload(aprovacao.getActionPayloadJson());
        AtivoPatrimonialEntity ativo = ativoRepository.findById(payload.ativoId())
                .orElseThrow(() -> new AtivoPatrimonialNotFoundException(
                        "Ativo patrimonial não encontrado: " + payload.ativoId()));

        assertPodeDecidir(actorContext, ativo.getOrganizacaoId());

        var response = alocarAtivoParaEventoUseCase.executeApprovedAlocacao(
                aprovacao.getId(), actorContext.actor(), actorContext.usuarioId());

        return new AprovacaoDecisionResponse.ActionExecution(
                "EXECUTED", response.id(), response.status().name(), null);
    }

    /**
     * Consulta o status corrente do ativo referenciado pela aprovação, usado para exibir o estado
     * atualizado do recurso mesmo depois de decidida a solicitação.
     *
     * @param aprovacao solicitação de aprovação
     * @return o nome do status do ativo, ou {@code null} se o payload não referenciar um ativo ou
     *     o ativo não existir mais
     */
    @Override
    public String fetchResourceStatus(AprovacaoEntity aprovacao) {
        AprovacaoAcaoPayload payload = aprovacaoAcaoPayloadMapper.toPayload(aprovacao.getActionPayloadJson());
        if (payload.ativoId() == null) {
            return null;
        }
        return ativoRepository.findById(payload.ativoId())
                .map(ativo -> ativo.getStatus().name())
                .orElse(null);
    }

    /**
     * Reverte a ocupação pendente quando a solicitação de empréstimo é rejeitada, marcando-a como
     * {@code CANCELADA} para liberar o ativo (só age se a ocupação ainda estiver
     * {@code PENDENTE_APROVACAO}, evitando sobrescrever um estado já avançado por outro caminho).
     *
     * @param aprovacao solicitação de aprovação rejeitada
     */
    @Override
    public void onRejected(AprovacaoEntity aprovacao) {
        alocacaoRepository.findByAprovacaoId(aprovacao.getId())
                .filter(ocupacao -> ocupacao.getStatus() == AtivoAlocacaoStatus.PENDENTE_APROVACAO)
                .ifPresent(ocupacao -> {
                    ocupacao.setStatus(AtivoAlocacaoStatus.CANCELADA);
                    alocacaoRepository.save(ocupacao);
                });
    }

    private void assertPodeDecidir(EventoActorContext actorContext, UUID ativoOrganizacaoId) {
        PapelOrganizacional papel = PapelOrganizacional.fromStoredValue(actorContext.role());

        if (papel == PapelOrganizacional.ADMIN || papel == PapelOrganizacional.PAROCO) {
            return;
        }

        boolean coordenadorDoConselho = papel == PapelOrganizacional.PRESIDENTE_CPC
                || papel == PapelOrganizacional.MESA_CPP;

        if (coordenadorDoConselho && ativoOrganizacaoId.equals(actorContext.organizationId())) {
            return;
        }

        throw new ForbiddenOperationException(
                "Somente o coordenador de conselho da organização dona do ativo ou o pároco podem autorizar "
                        + "este empréstimo");
    }
}
