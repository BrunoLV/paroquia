package br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoSolicitacaoAprovacaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/**
 * Repositório JPA para as solicitações de aprovação (criação/cancelamento) de projetos
 * ({@code ProjetoSolicitacaoAprovacaoEntity}), usado por {@code TransitionProjetoUseCase} para
 * persistir e decidir o desvio de aprovação.
 */
public interface ProjetoSolicitacaoAprovacaoJpaRepository extends JpaRepository<ProjetoSolicitacaoAprovacaoEntity,
        UUID> {

    /**
     * Lista as solicitações de aprovação de projeto que estão em um determinado status, usado
     * tipicamente para listar solicitações {@code PENDENTE} aguardando decisão.
     *
     * @param status status da solicitação de aprovação.
     * @return lista de solicitações no status informado.
     */
    List<ProjetoSolicitacaoAprovacaoEntity> findByStatus(AprovacaoStatus status);

    /**
     * Conta as solicitações de aprovação de projeto em um status, agrupadas por tipo de
     * solicitação — soma-se ao detalhamento de pendências de homologação do dashboard. Cada linha
     * é {@code [tipoSolicitacao (String), quantidade (long)]}.
     *
     * @param status status a considerar (tipicamente {@link AprovacaoStatus#PENDENTE})
     * @return contagem por tipo de solicitação
     */
    @Query("SELECT s.tipoSolicitacao, count(s) FROM ProjetoSolicitacaoAprovacaoEntity s "
            + "WHERE s.status = :status GROUP BY s.tipoSolicitacao")
    List<Object[]> contarPorTipoComStatus(@Param("status") AprovacaoStatus status);
}
