package br.com.nsfatima.gestao.local.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.local.infrastructure.persistence.entity.LocalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório Spring Data JPA para {@link LocalEntity}, usado pelo adaptador
 * {@code LocalRepositoryImpl} para implementar a porta de domínio {@code LocalRepository}.
 */
public interface LocalJpaRepository extends JpaRepository<LocalEntity, UUID> {

    /**
     * Busca um Local pelo nome, ignorando maiúsculas/minúsculas — espelha a restrição de unicidade
     * do nome definida na coluna {@code nome} da tabela.
     *
     * @param nome nome do local a ser buscado
     * @return o Local encontrado, ou {@link Optional#empty()} se não existir
     */
    Optional<LocalEntity> findByNomeIgnoreCase(String nome);

    /**
     * Busca os Locais vinculados a uma organização (Matriz ou Comunidade) específica.
     *
     * @param grupoParoquialId ID da organização de subordinação
     * @return lista de Locais vinculados à organização informada
     */
    List<LocalEntity> findByGrupoParoquialId(UUID grupoParoquialId);
}
