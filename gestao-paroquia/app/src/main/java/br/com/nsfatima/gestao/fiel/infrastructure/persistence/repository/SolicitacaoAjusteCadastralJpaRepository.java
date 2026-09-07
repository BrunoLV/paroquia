package br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.SolicitacaoAjusteCadastralEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
public interface SolicitacaoAjusteCadastralJpaRepository
        extends JpaRepository<SolicitacaoAjusteCadastralEntity, UUID> {

    List<SolicitacaoAjusteCadastralEntity> findByStatusOrderByCriadoEmUtcAsc(String status);

    List<SolicitacaoAjusteCadastralEntity> findByFielIdAndStatusOrderByCriadoEmUtcDesc(
            UUID fielId,
            String status);
}
