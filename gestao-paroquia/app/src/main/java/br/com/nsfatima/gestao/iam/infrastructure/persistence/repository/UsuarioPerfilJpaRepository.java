package br.com.nsfatima.gestao.iam.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

@NoRepositoryBean
public interface UsuarioPerfilJpaRepository {
    Optional<UsuarioEntity> findByNomeIgnoreCase(String nome);
}
