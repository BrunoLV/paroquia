package br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.entity.JobLockEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Acesso a dados para {@link JobLockEntity}, implementando um lock distribuído via banco de
 * dados para coordenar a execução exclusiva de jobs agendados entre múltiplas instâncias da
 * aplicação. A aquisição segue o padrão "update-then-insert": tenta primeiro renovar um lock
 * expirado (linha já existente) e, se não houver linha para atualizar, tenta inserir a
 * primeira ocorrência do lock — evitando duas instâncias assumirem o mesmo job simultaneamente.
 */
public interface JobLockJpaRepository extends JpaRepository<JobLockEntity, String> {

    /**
     * Tenta renovar/assumir um lock já existente, mas apenas se estiver expirado
     * ({@code locked_until <= :now}). Não cria a linha caso ela não exista — ver
     * {@link #insertLock} para o caso de primeira aquisição.
     *
     * @param name identificador único do job
     * @param until novo instante até quando o lock deve ser considerado válido
     * @param now instante atual, usado para checar expiração e registrar a aquisição
     * @return quantidade de linhas atualizadas (0 significa que o lock está ativo com outro dono
     *     ou ainda não existe)
     */
    @Transactional
    @Modifying
    @Query(value = """
            UPDATE calendario.job_locks
            SET locked_until = :until, locked_at = :now
            WHERE name = :name AND locked_until <= :now
            """, nativeQuery = true)
    int updateLock(@Param("name") String name, @Param("until") Instant until, @Param("now") Instant now);

    /**
     * Insere a primeira ocorrência do lock de um job. Deve ser chamado apenas quando
     * {@link #updateLock} retornar 0, indicando que a linha ainda não existe; uma tentativa de
     * inserir um {@code name} já existente falha por violação de chave primária, sinalizando
     * que outra instância detém o lock.
     *
     * @param name identificador único do job
     * @param until instante até quando o lock deve ser considerado válido
     * @param now instante em que o lock foi adquirido
     */
    @Transactional
    @Modifying
    @Query(value = """
            INSERT INTO calendario.job_locks (name, locked_until, locked_at)
            VALUES (:name, :until, :now)
            """, nativeQuery = true)
    void insertLock(@Param("name") String name, @Param("until") Instant until, @Param("now") Instant now);

    /**
     * Tenta adquirir o lock de um job, combinando renovação e primeira inserção em uma única
     * operação: renova se o lock existir e estiver expirado; caso contrário, tenta criá-lo. Se
     * a inserção falhar (linha já existe e não estava expirada), o lock é considerado detido
     * por outra instância e a aquisição falha silenciosamente, retornando {@code false} para
     * que o chamador simplesmente pule a execução do job nesta instância.
     *
     * @param name identificador único do job
     * @param until instante até quando o lock, se adquirido, será considerado válido
     * @param now instante atual da tentativa de aquisição
     * @return {@code true} se o lock foi adquirido por esta instância; {@code false} se outra
     *     instância já o detém
     */
    @Transactional
    default boolean acquireLock(String name, Instant until, Instant now) {
        if (updateLock(name, until, now) > 0) {
            return true;
        }
        try {
            insertLock(name, until, now);
            return true;
        } catch (Exception e) {
            // Already exists and was not updated (still locked)
            return false;
        }
    }

    /**
     * Libera antecipadamente o lock de um job, ajustando {@code locked_until} para o instante
     * informado (em vez de esperar sua expiração natural), permitindo que outra instância o
     * adquira imediatamente após a conclusão do job atual.
     *
     * @param name identificador único do job
     * @param now instante a partir do qual o lock passa a ser considerado expirado
     */
    @Transactional
    @Modifying
    @Query(value = "UPDATE calendario.job_locks SET locked_until = :now WHERE name = :name", nativeQuery = true)
    void releaseLock(@Param("name") String name, @Param("now") Instant now);

    /**
     * Libera o lock de um job usando o instante atual como referência de expiração. Atalho de
     * conveniência para {@link #releaseLock(String, Instant)}.
     *
     * @param name identificador único do job
     */
    default void releaseLock(String name) {
        releaseLock(name, Instant.now());
    }
}
