package br.com.nsfatima.gestao.iam.infrastructure.persistence.repository;

import org.springframework.data.repository.NoRepositoryBean;

/**
 * Segrega consultas ligadas ao perfil do usuário do {@link UsuarioJpaRepository}, seguindo o padrão de
 * segregação de repositórios adotado no monorepo. {@code Usuario} não carrega mais dados pessoais (ver
 * {@code FielJpaRepository} para busca por nome via o Fiel vinculado) — esta interface fica vazia até que
 * outra consulta específica de perfil seja necessária.
 */
@NoRepositoryBean
public interface UsuarioPerfilJpaRepository {
}
