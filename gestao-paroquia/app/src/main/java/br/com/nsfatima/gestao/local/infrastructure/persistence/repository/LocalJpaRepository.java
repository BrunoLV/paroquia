package br.com.nsfatima.gestao.local.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.local.infrastructure.persistence.entity.LocalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LocalJpaRepository extends JpaRepository<LocalEntity, UUID> {
    Optional<LocalEntity> findByNomeIgnoreCase(String nome);
    List<LocalEntity> findByOrganizacaoId(UUID organizacaoId);
}
