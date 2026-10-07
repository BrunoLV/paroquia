package br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.organizacao.domain.model.StatusMandato;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.MandatoCoordenacaoEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositório Spring Data de {@link MandatoCoordenacaoEntity}.
 */
public interface MandatoCoordenacaoJpaRepository extends JpaRepository<MandatoCoordenacaoEntity, UUID> {

    List<MandatoCoordenacaoEntity> findByOrganizacaoIdOrderByInicioDesc(UUID organizacaoId);

    List<MandatoCoordenacaoEntity> findByStatusAndFimBefore(StatusMandato status, LocalDate data);

    boolean existsByOrganizacaoIdAndStatusAndInicioLessThanEqualAndFimGreaterThanEqual(
            UUID organizacaoId, StatusMandato status, LocalDate inicio, LocalDate fim);
}
