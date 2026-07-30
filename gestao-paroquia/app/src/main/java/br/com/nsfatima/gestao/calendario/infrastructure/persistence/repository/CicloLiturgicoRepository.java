package br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.calendario.domain.model.TipoCicloLiturgico;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.CicloLiturgicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CicloLiturgicoRepository extends JpaRepository<CicloLiturgicoEntity, UUID> {

    /**
     * Busca ciclo litúrgico pelo seu tipo.
     *
     * @param tipoCiclo Tipo do ciclo litúrgico
     * @return Optional com a entidade encontrada
     */
    Optional<CicloLiturgicoEntity> findByTipoCiclo(TipoCicloLiturgico tipoCiclo);
}
