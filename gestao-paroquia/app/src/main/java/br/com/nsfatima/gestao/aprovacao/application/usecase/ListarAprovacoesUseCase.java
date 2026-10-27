package br.com.nsfatima.gestao.aprovacao.application.usecase;

import br.com.nsfatima.gestao.aprovacao.api.v1.dto.AprovacaoResponse;
import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.mapper.AprovacaoMapper;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Consulta e lista as solicitações de aprovação.
 *
 * <p>REGRA DE NEGÓCIO: quem decide (Pároco e Bispo Diocesano, a mesma regra de
 * {@link ValidarAprovacaoUseCase#podeDecidir}) enxerga a fila inteira e o diagnóstico técnico das falhas. Quem
 * apenas propõe enxerga somente as próprias solicitações e nunca o detalhamento técnico de falha — a fila
 * carrega dados de outras pastorais e de projetos que não lhe dizem respeito.</p>
 */
@Service
public class ListarAprovacoesUseCase {

    private final AprovacaoJpaRepository repository;
    private final AprovacaoMapper mapper;
    private final EventoActorContextResolver actorContextResolver;
    private final ValidarAprovacaoUseCase validarAprovacaoUseCase;

    public ListarAprovacoesUseCase(AprovacaoJpaRepository repository, AprovacaoMapper mapper,
                                   EventoActorContextResolver actorContextResolver,
                                   ValidarAprovacaoUseCase validarAprovacaoUseCase) {
        this.repository = repository;
        this.mapper = mapper;
        this.actorContextResolver = actorContextResolver;
        this.validarAprovacaoUseCase = validarAprovacaoUseCase;
    }

    /**
     * Recupera uma listagem paginada de solicitações de aprovação para dar suporte a componentes de interface
     * que exibem histórico de governança e tarefas pendentes, conforme a alçada do ator.
     * <p>
     * Exemplo de uso:
     * {@code
     * useCase.execute(eventoId, "PENDENTE", PageRequest.of(0, 20));
     * }
     *
     * @throws AccessDeniedException se o ator não decide e não tem usuário identificado
     */
    @Transactional(readOnly = true)
    public Page<AprovacaoResponse> execute(UUID eventoId, String status, Pageable pageable) {
        EventoActorContext ator = actorContextResolver.resolveRequired();
        boolean decide = validarAprovacaoUseCase.podeDecidir(ator.role());
        String solicitante = decide ? null : identificar(ator);
        return repository.findByFilterDoSolicitante(eventoId, AprovacaoStatus.fromJson(status), solicitante, pageable)
                .map(entity -> {
                    long count = repository.countByAprovacaoPaiId(entity.getId());
                    return visivelPara(decide, mapper.toResponse(entity, count > 0 ? (int) count : null));
                });
    }

    /**
     * Lista todas as ocorrências filhas associadas a uma aprovação mestre.
     *
     * @param aprovacaoPaiId ID da aprovação mestre
     * @return lista de respostas de aprovação filhas ordenadas pela data da ocorrência
     * @throws AccessDeniedException se o ator não decide e a série não é sua
     */
    @Transactional(readOnly = true)
    public List<AprovacaoResponse> listarOcorrencias(UUID aprovacaoPaiId) {
        EventoActorContext ator = actorContextResolver.resolveRequired();
        boolean decide = validarAprovacaoUseCase.podeDecidir(ator.role());
        if (!decide) {
            String proprio = identificar(ator);
            boolean serieDoAtor = repository.findById(aprovacaoPaiId)
                    .map(pai -> proprio.equals(pai.getSolicitanteId()))
                    .orElse(true);
            if (!serieDoAtor) {
                throw new AccessDeniedException("A série de aprovação pertence a outro solicitante");
            }
        }
        return repository.findByAprovacaoPaiIdOrderByDataOcorrenciaUtcAsc(aprovacaoPaiId).stream()
                .map(mapper::toResponse)
                .map(resposta -> visivelPara(decide, resposta))
                .toList();
    }

    private static String identificar(EventoActorContext ator) {
        if (ator.usuarioId() == null) {
            throw new AccessDeniedException("Usuário não identificado");
        }
        return ator.usuarioId().toString();
    }

    private static AprovacaoResponse visivelPara(boolean decide, AprovacaoResponse resposta) {
        return decide ? resposta : resposta.semDetalheTecnico();
    }
}
