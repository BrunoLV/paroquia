package br.com.nsfatima.gestao.membro.application.usecase;

import br.com.nsfatima.gestao.membro.api.v1.dto.ParticipacaoResponse;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.mapper.MembroMapper;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.ParticipanteOrganizacaoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Lista as participações ativas de um membro em organizações adicionais, para mapear seu engajamento em
 * pastorais/movimentos além do vínculo usuário-organização principal.
 */
@Service
public class ListParticipacoesUseCase {

    private final ParticipanteOrganizacaoJpaRepository repository;
    private final MembroMapper mapper;

    public ListParticipacoesUseCase(ParticipanteOrganizacaoJpaRepository repository, MembroMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /**
     * Recupera todas as participações ainda ativas (sem data de fim) do membro informado.
     *
     * @param membroId identificador do membro (vínculo usuário-organização)
     * @return participações ativas encontradas; lista vazia se não houver nenhuma
     */
    @Transactional(readOnly = true)
    public List<ParticipacaoResponse> execute(UUID membroId) {
        return repository.findByMembroIdAndAtivoTrue(membroId).stream()
                .map(mapper::toParticipacaoResponse)
                .toList();
    }
}
