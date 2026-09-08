package br.com.nsfatima.gestao.local.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.local.infrastructure.persistence.entity.LocalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório Spring Data JPA para {@link LocalEntity}, usado pelo adaptador
 * {@code LocalRepositoryImpl} para implementar a porta de domínio {@code LocalRepository}.
 *
 * <p>Todas as consultas de negócio consideram o filtro {@code deletedAt IS NULL} para garantir
 * que registros excluídos logicamente não poluam listagens operacionais ou regras de agendamento.
 */
public interface LocalJpaRepository extends JpaRepository<LocalEntity, UUID> {

    /**
     * Busca um Local pelo nome, ignorando maiúsculas/minúsculas entre registros não excluídos.
     *
     * @param nome nome do local a ser buscado
     * @return o Local encontrado, ou {@link Optional#empty()} se não existir
     */
    Optional<LocalEntity> findByNomeIgnoreCaseAndDeletedAtIsNull(String nome);

    /**
     * Busca um Local ativo/não excluído pelo identificador.
     *
     * @param id identificador do local
     * @return o Local encontrado, ou {@link Optional#empty()} se não existir ou se estiver excluído
     */
    Optional<LocalEntity> findByIdAndDeletedAtIsNull(UUID id);

    /**
     * Retorna todos os locais não excluídos.
     *
     * @return lista de locais ativos/inativos que não sofreram soft delete
     */
    List<LocalEntity> findAllByDeletedAtIsNull();

    /**
     * Busca os Locais não excluídos vinculados a uma organização (Matriz ou Comunidade) específica.
     *
     * @param grupoParoquialId ID da organização de subordinação
     * @return lista de Locais vinculados à organização informada
     */
    List<LocalEntity> findByGrupoParoquialIdAndDeletedAtIsNull(UUID grupoParoquialId);

    /**
     * Conta os locais ativos não excluídos — capacidade instalada de ambientes.
     *
     * @return quantidade de locais ativos
     */
    long countByAtivoTrueAndDeletedAtIsNull();

    /**
     * Busca os locais ativos não excluídos.
     *
     * @return locais com cadastro ativo e não excluídos
     */
    List<LocalEntity> findByAtivoTrueAndDeletedAtIsNull();

    /**
     * Métodos legados mantidos para compatibilidade com fluxos existentes.
     */
    Optional<LocalEntity> findByNomeIgnoreCase(String nome);

    List<LocalEntity> findByGrupoParoquialId(UUID grupoParoquialId);

    long countByAtivoTrue();

    List<LocalEntity> findByAtivoTrue();
}
