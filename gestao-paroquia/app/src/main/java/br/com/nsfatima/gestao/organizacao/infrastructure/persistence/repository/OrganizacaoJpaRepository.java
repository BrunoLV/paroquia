package br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repositório Spring Data para {@link OrganizacaoEntity}, combinando o CRUD genérico com as consultas
 * customizadas de {@link OrganizacaoConsultaJpaRepository}.
 */
@Repository
public interface OrganizacaoJpaRepository extends JpaRepository<OrganizacaoEntity, UUID>,
        OrganizacaoConsultaJpaRepository {
}
