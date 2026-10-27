package br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielDependenteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/** Repositório JPA dos dependentes de um fiel, usado na eliminação de dados (LGPD). */
@Repository
public interface FielDependenteJpaRepository extends JpaRepository<FielDependenteEntity, UUID> {

    /**
     * Remove definitivamente os dependentes do fiel anonimizado: nome e nascimento de terceiros (inclusive
     * menores) não têm fé pública canônica a preservar.
     *
     * @param fielId identificador do fiel titular
     * @return quantidade de dependentes removidos
     */
    @Modifying
    @Query("DELETE FROM FielDependenteEntity d WHERE d.fiel.id = :fielId")
    int excluirPorFiel(@Param("fielId") UUID fielId);
}
