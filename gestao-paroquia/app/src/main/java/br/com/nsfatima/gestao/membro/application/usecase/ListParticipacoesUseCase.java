package br.com.nsfatima.gestao.membro.application.usecase;

import br.com.nsfatima.gestao.membro.api.v1.dto.ParticipacaoResponse;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.mapper.MembroMapper;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.ParticipanteOrganizacaoJpaRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListParticipacoesUseCase {

    private final ParticipanteOrganizacaoJpaRepository repository;
    private final MembroMapper mapper;

    public ListParticipacoesUseCase(ParticipanteOrganizacaoJpaRepository repository, MembroMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    /**
     * Obtém todos os vínculos ativos de um membro com organizações para mapear sua atuação e engajamento na paróquia.
     * 
     * Exemplo: useCase.execute(membroId)
     */
    public List<ParticipacaoResponse> execute(UUID membroId) {
        return repository.findByMembroIdAndAtivoTrue(membroId).stream()
                .map(mapper::toParticipacaoResponse)
                .toList();
    }
}
