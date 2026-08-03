package br.com.nsfatima.gestao.patrimonio.application.usecase;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.AtivoPatrimonialResponse;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.IniciarManutencaoRequest;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoManutencaoInvalidaException;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoPatrimonialNotFoundException;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoAlocacaoStatus;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoAlocacaoTipo;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoStatus;
import br.com.nsfatima.gestao.patrimonio.domain.service.AtivoAlocacaoConflitoPolicy;
import br.com.nsfatima.gestao.patrimonio.domain.service.PatrimonioAuthorizationService;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoAlocacaoEntity;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoPatrimonialEntity;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.AtivoAlocacaoJpaRepository;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.AtivoPatrimonialJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Registra um período de manutenção para um ativo patrimonial, checando conflito com outras
 * ocupações já agendadas. O status do ativo só é imediatamente alterado para
 * {@code EM_MANUTENCAO} quando o período já começou (data de início não futura); manutenções
 * agendadas para o futuro ficam apenas reservadas, mantendo o ativo disponível até lá.
 */
@Service
public class IniciarManutencaoAtivoUseCase {

    private final AtivoPatrimonialJpaRepository ativoRepository;
    private final AtivoAlocacaoJpaRepository alocacaoRepository;
    private final AtivoAlocacaoConflitoPolicy conflitoPolicy;
    private final EventoActorContextResolver actorContextResolver;
    private final PatrimonioAuthorizationService authorizationService;

    public IniciarManutencaoAtivoUseCase(AtivoPatrimonialJpaRepository ativoRepository,
                                          AtivoAlocacaoJpaRepository alocacaoRepository,
                                          AtivoAlocacaoConflitoPolicy conflitoPolicy,
                                          EventoActorContextResolver actorContextResolver,
                                          PatrimonioAuthorizationService authorizationService) {
        this.ativoRepository = ativoRepository;
        this.alocacaoRepository = alocacaoRepository;
        this.conflitoPolicy = conflitoPolicy;
        this.actorContextResolver = actorContextResolver;
        this.authorizationService = authorizationService;
    }

    /**
     * Cria uma ocupação de manutenção para o período informado, validando que o ativo não está
     * baixado, que o período é consistente e que não conflita com outra ocupação já registrada.
     *
     * @param ativoId ativo a colocar em manutenção
     * @param request período e motivo da manutenção
     * @return o ativo, com status {@code EM_MANUTENCAO} se o período já começou, ou inalterado se
     *     a manutenção for apenas uma reserva futura
     * @throws AtivoPatrimonialNotFoundException se o ativo não existir
     * @throws AtivoManutencaoInvalidaException  se o ativo estiver baixado ou o período for inválido
     */
    @Transactional
    public AtivoPatrimonialResponse execute(UUID ativoId, IniciarManutencaoRequest request) {
        AtivoPatrimonialEntity ativo = ativoRepository.findById(ativoId)
                .orElseThrow(() -> new AtivoPatrimonialNotFoundException(
                        "Ativo patrimonial não encontrado: " + ativoId));

        if (ativo.getStatus() == AtivoStatus.BAIXADO) {
            throw new AtivoManutencaoInvalidaException("Não é possível iniciar manutenção de um ativo BAIXADO");
        }
        if (request.dataFim().isBefore(request.dataInicio())) {
            throw new AtivoManutencaoInvalidaException(
                    "A data final da manutenção não pode ser anterior à data inicial");
        }

        EventoActorContext actorContext = actorContextResolver.resolveRequired();
        authorizationService.assertCanIniciarManutencao(actorContext, ativo.getOrganizacaoId());

        conflitoPolicy.assertPeriodoDisponivel(ativoId, request.dataInicio(), request.dataFim());

        AtivoAlocacaoEntity ocupacao = new AtivoAlocacaoEntity();
        ocupacao.setId(UUID.randomUUID());
        ocupacao.setAtivoId(ativoId);
        ocupacao.setTipo(AtivoAlocacaoTipo.MANUTENCAO);
        ocupacao.setOrganizacaoOrigemId(ativo.getOrganizacaoId());
        ocupacao.setDataInicio(request.dataInicio());
        ocupacao.setDataFim(request.dataFim());
        ocupacao.setStatus(AtivoAlocacaoStatus.CONFIRMADA);
        ocupacao.setJustificativa(request.motivo());
        ocupacao.setCriadoEmUtc(Instant.now());
        alocacaoRepository.save(ocupacao);

        if (!request.dataInicio().isAfter(LocalDate.now())) {
            ativo.setStatus(AtivoStatus.EM_MANUTENCAO);
            ativo = ativoRepository.save(ativo);
        }

        return CreateAtivoPatrimonialUseCase.toResponse(ativo);
    }
}
