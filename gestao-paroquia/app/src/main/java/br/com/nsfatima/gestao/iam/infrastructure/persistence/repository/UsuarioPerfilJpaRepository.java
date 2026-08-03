package br.com.nsfatima.gestao.iam.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

/**
 * Segrega as consultas ligadas ao perfil/dados cadastrais do usuário (busca por nome) do
 * {@link UsuarioJpaRepository}, seguindo o padrão de segregação de repositórios adotado no monorepo.
 */
@NoRepositoryBean
public interface UsuarioPerfilJpaRepository {

    /**
     * Busca um usuário pelo nome cadastrado, ignorando diferenças de maiúsculas/minúsculas. Como
     * {@code nome} é armazenado criptografado (via converter JPA de criptografia), essa comparação depende
     * do comportamento do converter/coluna, não de um índice funcional simples de banco.
     *
     * @param nome nome a buscar, comparado sem diferenciar caixa
     * @return o usuário correspondente, se existir
     */
    Optional<UsuarioEntity> findByNomeIgnoreCase(String nome);
}
