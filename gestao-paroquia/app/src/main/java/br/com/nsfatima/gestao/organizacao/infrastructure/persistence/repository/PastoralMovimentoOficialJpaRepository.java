package br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.PastoralMovimentoOficialEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface PastoralMovimentoOficialJpaRepository extends JpaRepository<PastoralMovimentoOficialEntity, UUID> {
    boolean existsByNome(String nome);
}
