package br.com.nsfatima.gestao.iam.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

/**
 * Segrega as consultas ligadas a autenticação (busca por username) do {@link UsuarioJpaRepository},
 * seguindo o padrão de segregação de repositórios adotado no monorepo para não acumular métodos de
 * responsabilidades distintas numa única interface "gorda".
 */
@NoRepositoryBean
public interface UsuarioAutenticacaoJpaRepository {

    /**
     * Busca um usuário pelo username, ignorando diferenças de maiúsculas/minúsculas — usado tanto para
     * autenticação (login) quanto para validar duplicidade de cadastro (username funciona como e-mail/login
     * único).
     *
     * @param username username a buscar, comparado sem diferenciar caixa
     * @return o usuário correspondente, se existir
     */
    Optional<UsuarioEntity> findByUsernameIgnoreCase(String username);
}
