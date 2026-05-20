package br.com.nsfatima.gestao.membro.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.ParticipanteOrganizacaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ParticipanteOrganizacaoJpaRepository extends JpaRepository<ParticipanteOrganizacaoEntity, UUID> {
    List<ParticipanteOrganizacaoEntity> findByMembroIdAndAtivoTrue(UUID membroId);
    List<ParticipanteOrganizacaoEntity> findByOrganizacaoIdAndAtivoTrue(UUID organizacaoId);
}
