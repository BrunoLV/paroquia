package br.com.nsfatima.gestao.iam.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;
import java.util.UUID;

/**
 * Segrega consultas ligadas ao perfil do usuário do {@link UsuarioJpaRepository}, seguindo o padrão de
 * segregação de repositórios adotado no monorepo. {@code Usuario} não carrega mais dados pessoais (ver
 * {@code FielJpaRepository} para busca por nome via o Fiel vinculado).
 */
@NoRepositoryBean
public interface UsuarioPerfilJpaRepository {

    /**
     * Busca o {@code Usuario} (se houver) já vinculado a um {@code Fiel}, usado por
     * {@code PromoverACoordenadorUseCase} para decidir se deve criar um novo login ou reaproveitar o
     * existente.
     */
    Optional<UsuarioEntity> findByFielId(UUID fielId);

    /** Busca o {@code Usuario} vinculado a um {@code Clerigo}. */
    Optional<UsuarioEntity> findByClerigoId(UUID clerigoId);
}

