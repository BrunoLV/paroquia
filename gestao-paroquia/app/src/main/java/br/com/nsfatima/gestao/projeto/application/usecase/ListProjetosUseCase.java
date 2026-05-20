package br.com.nsfatima.gestao.projeto.application.usecase;

import br.com.nsfatima.gestao.projeto.api.v1.dto.ProjetoResponse;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.mapper.ProjetoMapper;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListProjetosUseCase {

    private final ProjetoEventoJpaRepository repository;
    private final ProjetoMapper mapper;

    /**
     * Inicializa o caso de uso com as dependências para consulta e mapeamento de projetos.
     *
     * @param repository Repositório JPA para consulta de projetos
     * @param mapper Mapeador para conversão de entidades em DTOs de resposta
     */
    public ListProjetosUseCase(ProjetoEventoJpaRepository repository, ProjetoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /**
     * Recupera a lista completa de projetos cadastrados para apoiar a visualização e gestão macro das iniciativas paroquiais.
     *
     * <p>Usage Example:
     * {@code List<ProjetoResponse> projetos = listProjetosUseCase.execute()}
     *
     * @return Lista de todos os projetos cadastrados
     */
    @Transactional(readOnly = true)
    public List<ProjetoResponse> execute() {
        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}
