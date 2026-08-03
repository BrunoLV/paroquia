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

    @Override
    public boolean supports(TipoSolicitacaoInput tipo) {
        return tipo == TipoSolicitacaoInput.ALOCACAO_ATIVO_EMPRESTIMO;
    }

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
