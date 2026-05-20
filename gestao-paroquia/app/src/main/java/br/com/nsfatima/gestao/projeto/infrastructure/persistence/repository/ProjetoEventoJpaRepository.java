package br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository;

import java.util.UUID;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoEventoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjetoEventoJpaRepository extends JpaRepository<ProjetoEventoEntity, UUID> {
}
