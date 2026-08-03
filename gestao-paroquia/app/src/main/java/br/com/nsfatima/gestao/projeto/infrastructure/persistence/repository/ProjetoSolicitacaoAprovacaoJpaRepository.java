package br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoSolicitacaoAprovacaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

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
    java.util.List<ProjetoSolicitacaoAprovacaoEntity> findByStatus(AprovacaoStatus status);
}
