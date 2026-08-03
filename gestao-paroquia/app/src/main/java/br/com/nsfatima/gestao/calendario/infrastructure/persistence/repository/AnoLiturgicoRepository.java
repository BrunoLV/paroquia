package br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.AnoLiturgicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

/**
 * Repositório de persistência do ano litúrgico ({@link AnoLiturgicoEntity}), responsável por
 * localizar o ano litúrgico vigente (ciclo entre o início do Advento e o fim do Tempo Comum) a
 * partir de uma data de referência.
 */
public interface AnoLiturgicoRepository extends JpaRepository<AnoLiturgicoEntity, Integer> {

    /**
     * Localiza o ano litúrgico cujo intervalo (início do Advento até o fim do Tempo Comum)
     * contém a data informada.
     *
     * @param targetDate data de referência a ser verificada contra o intervalo do ano litúrgico
     * @return o ano litúrgico correspondente, ou {@link Optional#empty()} caso nenhum ano
     *     litúrgico cadastrado cubra a data informada
     */
    @Query("""
           SELECT a FROM AnoLiturgicoEntity a
           WHERE :targetDate >= a.dataInicioAdvento AND :targetDate <= a.dataFimComum
           """)
    Optional<AnoLiturgicoEntity> findAnoLiturgicoByData(@Param("targetDate") Instant targetDate);
}
