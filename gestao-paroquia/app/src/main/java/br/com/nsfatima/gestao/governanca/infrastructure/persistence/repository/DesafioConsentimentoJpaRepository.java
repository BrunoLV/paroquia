package br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.governanca.domain.model.TipoTitularConsentimento;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.entity.DesafioConsentimentoEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/** Repositório JPA para gestão dos desafios OTP de consentimento. */
@Repository
public interface DesafioConsentimentoJpaRepository extends JpaRepository<DesafioConsentimentoEntity, UUID> {

    /**
     * Desafio OTP ainda não validado e não expirado mais recente do titular, com trava pessimista de escrita:
     * requisições concorrentes de validação serializam e o contador de tentativas não pode ser burlado.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<DesafioConsentimentoEntity>
            findTopByTitularTipoAndTitularIdAndValidadoEmUtcIsNullAndExpiraEmUtcAfterOrderByCriadoEmUtcDesc(
                    TipoTitularConsentimento titularTipo, UUID titularId, Instant agora);

    /** Quantidade de códigos pedidos pelo titular desde o instante informado (janela do rate limit). */
    long countByTitularTipoAndTitularIdAndCriadoEmUtcAfter(
            TipoTitularConsentimento titularTipo, UUID titularId, Instant desde);

    /**
     * Consome atomicamente o desafio validado que sustenta um token de consentimento. Devolve 1 apenas na
     * primeira chamada; qualquer reuso, ou id que nunca foi validado, devolve 0.
     */
    @Modifying
    @Query("UPDATE DesafioConsentimentoEntity d SET d.consumidoEmUtc = :agora "
            + "WHERE d.id = :id AND d.validadoEmUtc IS NOT NULL AND d.consumidoEmUtc IS NULL")
    int consumir(@Param("id") UUID id, @Param("agora") Instant agora);

    /**
     * Trava de advisory lock transacional por chave: serializa os pedidos de código do mesmo titular até o
     * fim da transação, tornando a contagem de pedidos e a criação do desafio atômicas.
     */
    @Query(value = "SELECT 1 FROM (SELECT pg_advisory_xact_lock(hashtextextended(:chave, 0))) t",
            nativeQuery = true)
    int travarPedidos(@Param("chave") String chave);

    void deleteByCriadoEmUtcBefore(Instant limite);
}
