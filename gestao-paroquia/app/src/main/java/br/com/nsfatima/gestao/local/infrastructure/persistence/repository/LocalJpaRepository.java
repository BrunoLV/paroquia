package br.com.nsfatima.gestao.local.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.local.infrastructure.persistence.entity.LocalEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
     * Busca o local não excluído com bloqueio de escrita na linha. Serializa pedidos de reserva concorrentes
     * do mesmo espaço: quem obtém o bloqueio primeiro consulta os conflitos e grava antes do próximo.
     * Exige transação ativa.
     *
     * @param id identificador do local
     * @return o Local bloqueado, ou {@link Optional#empty()} se não existir ou estiver excluído
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM LocalEntity l WHERE l.id = :id AND l.deletedAt IS NULL")
    Optional<LocalEntity> findByIdComBloqueio(@Param("id") UUID id);

    /**
     * Retorna todos os locais não excluídos.
     *
     * @return lista de locais ativos/inativos que não sofreram soft delete
     */
    List<LocalEntity> findAllByDeletedAtIsNull();

    /**
     * Busca os Locais não excluídos vinculados a uma organização (Matriz ou Comunidade) específica.
     *
     * @param organizacaoId ID da organização de subordinação
     * @return lista de Locais vinculados à organização informada
     */
    List<LocalEntity> findByOrganizacaoIdAndDeletedAtIsNull(UUID organizacaoId);

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

    List<LocalEntity> findByOrganizacaoId(UUID organizacaoId);

    long countByAtivoTrue();

    List<LocalEntity> findByAtivoTrue();
}
