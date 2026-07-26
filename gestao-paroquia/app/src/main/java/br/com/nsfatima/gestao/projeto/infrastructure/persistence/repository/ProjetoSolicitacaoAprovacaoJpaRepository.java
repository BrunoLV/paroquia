package br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoSolicitacaoAprovacaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProjetoSolicitacaoAprovacaoJpaRepository extends JpaRepository<ProjetoSolicitacaoAprovacaoEntity,
        UUID> {

    java.util.List<ProjetoSolicitacaoAprovacaoEntity> findByStatus(String status);
}
