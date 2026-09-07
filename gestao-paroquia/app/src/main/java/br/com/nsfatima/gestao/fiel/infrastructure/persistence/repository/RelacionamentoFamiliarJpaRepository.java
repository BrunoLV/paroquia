package br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.RelacionamentoFamiliarEntity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface RelacionamentoFamiliarJpaRepository extends JpaRepository<RelacionamentoFamiliarEntity,UUID>{ List<RelacionamentoFamiliarEntity> findByFielOrigemIdAndDeletedAtIsNull(UUID fielId); List<RelacionamentoFamiliarEntity> findByParIdAndDeletedAtIsNull(UUID parId); }
