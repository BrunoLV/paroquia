package br.com.nsfatima.gestao.aprovacao.application.usecase;

import br.com.nsfatima.gestao.aprovacao.api.v1.dto.AprovacaoResponse;
import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.mapper.AprovacaoMapper;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Consulta e lista as solicitações de aprovação pendentes ou filtradas por evento/autor.
 *
 * REGRA DE NEGÓCIO:
 * Alimenta a fila de governança pastoral para párocos, vigários e administradores paroquiais.
 */
@Service
public class ListarAprovacoesUseCase {

    private final AprovacaoJpaRepository repository;
    private final AprovacaoMapper mapper;

    public ListarAprovacoesUseCase(AprovacaoJpaRepository repository, AprovacaoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /**
     * Recupera uma listagem paginada de solicitações de aprovação para dar suporte a componentes de interface
     * que exibem histórico de governança e tarefas pendentes.
     * <p>
     * Exemplo de uso:
     * {@code
     * useCase.execute(eventoId, "PENDENTE", PageRequest.of(0, 20));
     * }
     */
    @Transactional(readOnly = true)
    public Page<AprovacaoResponse> execute(UUID eventoId, String status, Pageable pageable) {
        return repository.findByFilter(eventoId, AprovacaoStatus.fromJson(status), pageable)
                .map(entity -> {
                    long count = repository.countByAprovacaoPaiId(entity.getId());
                    return mapper.toResponse(entity, count > 0 ? (int) count : null);
                });
    }

    /**
     * Lista todas as ocorrências filhas associadas a uma aprovação mestre.
     *
     * @param aprovacaoPaiId ID da aprovação mestre
     * @return lista de respostas de aprovação filhas ordenadas pela data da ocorrência
     */
    @Transactional(readOnly = true)
    public List<AprovacaoResponse> listarOcorrencias(UUID aprovacaoPaiId) {
        return repository.findByAprovacaoPaiIdOrderByDataOcorrenciaUtcAsc(aprovacaoPaiId).stream()
                .map(mapper::toResponse)
                .toList();
    }
}
