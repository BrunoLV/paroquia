package br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoSolicitacaoAprovacaoEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjetoSolicitacaoAprovacaoJpaRepository extends JpaRepository<ProjetoSolicitacaoAprovacaoEntity, UUID> {
}
