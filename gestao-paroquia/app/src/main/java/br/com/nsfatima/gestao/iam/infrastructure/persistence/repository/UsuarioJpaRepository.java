package br.com.nsfatima.gestao.iam.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repositório JPA principal de {@link UsuarioEntity}, composto pelas interfaces segregadas de autenticação
 * ({@link UsuarioAutenticacaoJpaRepository}) e perfil ({@link UsuarioPerfilJpaRepository}) além do CRUD
 * padrão do Spring Data — segregação adotada para não concentrar todas as consultas de usuário numa única
 * interface, seguindo o mesmo padrão usado para {@code EventoJpaRepository}.
 */
@Repository
public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, UUID>,
        UsuarioAutenticacaoJpaRepository, UsuarioPerfilJpaRepository {
}
