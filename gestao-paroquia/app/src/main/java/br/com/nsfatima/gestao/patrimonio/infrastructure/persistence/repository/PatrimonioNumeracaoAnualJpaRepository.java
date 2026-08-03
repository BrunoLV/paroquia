package br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.PatrimonioNumeracaoAnualEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Acesso a persistência do contador anual de tombamento patrimonial. {@link #findById} é
 * sobrescrito com lock pessimista de escrita para serializar o incremento concorrente do número
 * sequencial e evitar tombamentos duplicados sob concorrência.
 */
@Repository
public interface PatrimonioNumeracaoAnualJpaRepository extends JpaRepository<PatrimonioNumeracaoAnualEntity, Integer> {

    /**
     * Busca o contador do ano com lock pessimista de escrita, garantindo que dois incrementos
     * concorrentes do mesmo ano não gerem o mesmo número de tombamento.
     *
     * @param ano ano de referência do contador
     * @return o contador do ano, se existir
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PatrimonioNumeracaoAnualEntity> findById(Integer ano);
}
