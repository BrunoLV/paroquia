package br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface OrganizacaoJpaRepository extends JpaRepository<OrganizacaoEntity, UUID> {
}
