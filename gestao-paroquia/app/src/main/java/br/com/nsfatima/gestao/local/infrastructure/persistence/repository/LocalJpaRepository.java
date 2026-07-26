package br.com.nsfatima.gestao.local.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.local.infrastructure.persistence.entity.LocalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LocalJpaRepository extends JpaRepository<LocalEntity, UUID> {
}
