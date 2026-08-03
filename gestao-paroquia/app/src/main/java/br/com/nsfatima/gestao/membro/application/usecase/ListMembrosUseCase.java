package br.com.nsfatima.gestao.membro.application.usecase;

import br.com.nsfatima.gestao.membro.api.v1.dto.MembroResponse;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.mapper.MembroMapper;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Lista vínculos usuário-organização (membros) de forma paginada, com filtros opcionais por organização,
 * usuário e situação, para suportar a tela administrativa de gestão de membros.
 */
@Service
public class ListMembrosUseCase {

    private final MembroJpaRepository repository;
    private final MembroMapper mapper;

    public ListMembrosUseCase(MembroJpaRepository repository, MembroMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /**
     * Executa a busca paginada aplicando os filtros informados (qualquer parâmetro {@code null} é ignorado).
     *
     * @param organizacaoId filtro por organização, ou {@code null} para não filtrar
     * @param usuarioId     filtro por usuário, ou {@code null} para não filtrar
     * @param ativo         filtro por situação do vínculo, ou {@code null} para não filtrar
     * @param pageable      paginação/ordenação solicitada
     * @return página de vínculos que atendem aos filtros informados
     */
    @Transactional(readOnly = true)
    public Page<MembroResponse> execute(UUID organizacaoId, UUID usuarioId, Boolean ativo, Pageable pageable) {
        return repository.findByFiltros(organizacaoId, usuarioId, ativo, pageable)
                .map(mapper::toResponse);
    }
}
