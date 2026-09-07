package br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.NotificacaoEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositório JPA para notificações persistentes. */
public interface NotificacaoJpaRepository extends JpaRepository<NotificacaoEntity, UUID> {

    List<NotificacaoEntity> findByUsuarioIdOrderByCriadoEmUtcDesc(UUID usuarioId);

    Optional<NotificacaoEntity> findByIdAndUsuarioId(UUID id, UUID usuarioId);
}
