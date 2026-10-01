package br.com.nsfatima.gestao.iam.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
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

    /**
     * Busca usuários ativos que possuem competência deliberativa para homologação de novos acessos
     * (administradores do sistema e párocos/bispos diocesanos).
     *
     * @return lista de usuários aprovadores ativos
     */
    @Query("SELECT u FROM UsuarioEntity u WHERE u.enabled = true AND (u.roles LIKE '%ADMIN%' OR u.roles LIKE '%PAROCO%')")
    List<UsuarioEntity> findAprovadoresAtivos();
}


