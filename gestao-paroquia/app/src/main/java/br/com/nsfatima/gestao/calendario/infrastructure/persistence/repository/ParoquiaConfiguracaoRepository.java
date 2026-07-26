package br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.ParoquiaConfiguracaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ParoquiaConfiguracaoRepository extends JpaRepository<ParoquiaConfiguracaoEntity, UUID> {
}
