package br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.calendario.domain.model.TipoCicloLiturgico;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.CicloLiturgicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CicloLiturgicoRepository extends JpaRepository<CicloLiturgicoEntity, UUID> {

    Optional<CicloLiturgicoEntity> findFirstByTipoCiclo(TipoCicloLiturgico tipoCiclo);

    Optional<CicloLiturgicoEntity> findByTipoCicloAndAnoLiturgicoRef(TipoCicloLiturgico tipoCiclo, Integer anoLiturgicoRef);
}
