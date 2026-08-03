package br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.MembroOrganizacaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório Spring Data para vínculos usuário-organização ({@link MembroOrganizacaoEntity}). As consultas
 * "ativo true" filtram apenas vínculos vigentes (histórico de vínculos desativados é preservado, não
 * excluído); as variantes com {@code igrejaVinculoId} tratam o escopo territorial de pastorais/movimentos,
 * que não pertencem diretamente a uma Matriz/Comunidade na hierarquia de {@code parentId}.
 */
public interface MembroOrganizacaoJpaRepository extends JpaRepository<MembroOrganizacaoEntity, UUID> {
    /**
     * Lista os vínculos ativos de um usuário, usados para determinar seu escopo de acesso atual.
     *
     * @param usuarioId identificador do usuário
     * @return vínculos ativos do usuário
     */
    List<MembroOrganizacaoEntity> findByUsuarioIdAndAtivoTrue(UUID usuarioId);

    /**
     * Lista todos os vínculos (ativos e inativos) de um usuário, incluindo histórico.
     *
     * @param usuarioId identificador do usuário
     * @return todos os vínculos do usuário
     */
    List<MembroOrganizacaoEntity> findByUsuarioId(UUID usuarioId);

    /**
     * Busca o vínculo ativo com determinado papel numa organização, usado para localizar o titular atual de
     * um papel único (ex.: coordenador) sem distinção de contexto de igreja.
     *
     * @param organizacaoId identificador da organização
     * @param papel código do papel
     * @return o vínculo ativo encontrado, se houver
     */
    Optional<MembroOrganizacaoEntity> findByOrganizacaoIdAndPapelAndAtivoTrue(UUID organizacaoId, String papel);

    /**
     * Busca o vínculo ativo com determinado papel numa organização, restrito a um contexto de igreja
     * (Matriz/Comunidade) específico — usado para localizar o titular atual de um papel de coordenação
     * quando pastorais/movimentos exigem escopo territorial explícito.
     *
     * @param organizacaoId identificador da organização
     * @param papel código do papel
     * @param igrejaVinculoId identificador da igreja/comunidade de contexto
     * @return o vínculo ativo encontrado, se houver
     */
    Optional<MembroOrganizacaoEntity> findByOrganizacaoIdAndPapelAndIgrejaVinculoIdAndAtivoTrue(UUID organizacaoId,
                                                                                                String papel,
                                                                                                UUID igrejaVinculoId);


    /**
     * Busca o vínculo ativo com determinado papel numa organização sem contexto de igreja associado — usado
     * para organizações que não são pastorais/movimentos (ex.: conselhos), onde não há escopo territorial
     * separado.
     *
     * @param organizacaoId identificador da organização
     * @param papel código do papel
     * @return o vínculo ativo encontrado, se houver
     */
    Optional<MembroOrganizacaoEntity> findByOrganizacaoIdAndPapelAndIgrejaVinculoIdNullAndAtivoTrue(UUID organizacaoId,
                                                                                                    String papel);


    /**
     * Busca um vínculo específico pela combinação exata de usuário, organização, papel e contexto de igreja,
     * usada para detectar duplicidade antes de criar um novo vínculo.
     *
     * @param usuarioId identificador do usuário
     * @param organizacaoId identificador da organização
     * @param papel código do papel
     * @param igrejaVinculoId identificador da igreja/comunidade de contexto, ou {@code null}
     * @return o vínculo encontrado, se houver, ativo ou não
     */
    Optional<MembroOrganizacaoEntity> findByUsuarioIdAndOrganizacaoIdAndPapelAndIgrejaVinculoId(UUID usuarioId,
                                                                                                UUID organizacaoId,
                                                                                                String papel,
                                                                                                UUID igrejaVinculoId);


    /**
     * Lista os vínculos ativos de uma organização, usados para montar a lista de membros exibida no
     * organograma.
     *
     * @param organizacaoId identificador da organização
     * @return vínculos ativos da organização
     */
    List<MembroOrganizacaoEntity> findByOrganizacaoIdAndAtivoTrue(UUID organizacaoId);

    /**
     * Lista os vínculos ativos cujo contexto de igreja seja a organização informada, usado para agregar
     * membros de pastorais/movimentos vinculados a uma Matriz/Comunidade ao consultar seus membros.
     *
     * @param igrejaVinculoId identificador da igreja/comunidade
     * @return vínculos ativos nesse contexto de igreja
     */
    List<MembroOrganizacaoEntity> findByIgrejaVinculoIdAndAtivoTrue(UUID igrejaVinculoId);

    /**
     * Verifica se a organização possui algum vínculo de membro, usado por
     * {@code OrganizacaoRepositoryImpl.hasDependencies} para bloquear a exclusão de organizações com membros.
     *
     * @param organizacaoId identificador da organização
     * @return {@code true} se existir ao menos um vínculo para a organização
     */
    boolean existsByOrganizacaoId(UUID organizacaoId);
}
