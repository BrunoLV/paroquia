package br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.governanca.domain.model.AnoParoquialStatus;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.entity.AnoParoquialEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Acesso a persistência dos registros de Ano Paroquial, chaveados pelo próprio ano civil. Fornece a consulta que
 * sustenta a "regra de sucessão" da governança anual: um ano só pode ser fechado/concluído se todos os anos
 * anteriores já estiverem {@code CONCLUIDO}.
 */
public interface AnoParoquialJpaRepository extends JpaRepository<AnoParoquialEntity, Integer> {

    /**
     * Busca o registro de governança de um ano específico.
     *
     * @param ano ano civil a ser consultado
     * @return o {@link AnoParoquialEntity} correspondente, se já existir
     */
    Optional<AnoParoquialEntity> findByAno(Integer ano);

    /**
     * Verifica se existe algum ano anterior ao informado que ainda não esteja no status terminal, condição que
     * bloqueia o fechamento/conclusão do ano corrente (regra de sucessão: anos anteriores devem ser concluídos
     * primeiro).
     *
     * @param ano    ano de referência (exclusivo — verifica anos estritamente menores)
     * @param status status a excluir da verificação (tipicamente {@code CONCLUIDO})
     * @return {@code true} se houver algum ano anterior com status diferente do informado
     */
    boolean existsByAnoLessThanAndStatusNot(Integer ano, AnoParoquialStatus status);
}
