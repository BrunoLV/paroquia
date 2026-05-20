package br.com.nsfatima.gestao.local.infrastructure.persistence.repository;

import java.util.UUID;
import br.com.nsfatima.gestao.local.infrastructure.persistence.entity.LocalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocalJpaRepository extends JpaRepository<LocalEntity, UUID> {
}
