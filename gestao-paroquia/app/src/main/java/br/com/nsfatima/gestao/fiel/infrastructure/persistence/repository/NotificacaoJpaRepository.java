package br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.NotificacaoEntity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface NotificacaoJpaRepository extends JpaRepository<NotificacaoEntity,UUID>{ List<NotificacaoEntity> findByUsuarioIdOrderByCriadoEmUtcDesc(UUID usuarioId); Optional<NotificacaoEntity> findByIdAndUsuarioId(UUID id,UUID usuarioId); }
