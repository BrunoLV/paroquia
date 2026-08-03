package br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.calendario.domain.model.TipoCicloLiturgico;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.CicloLiturgicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositório de persistência do ciclo litúrgico ({@link CicloLiturgicoEntity}), usado para
 * resolver qual ciclo dominical/ferial (ex.: A, B, C) se aplica a um determinado ano litúrgico.
 */
@Repository
public interface CicloLiturgicoRepository extends JpaRepository<CicloLiturgicoEntity, UUID> {

    /**
     * Localiza o primeiro ciclo litúrgico cadastrado de um determinado tipo, independentemente
     * do ano litúrgico a que está associado.
     *
     * @param tipoCiclo tipo do ciclo litúrgico buscado
     * @return o ciclo litúrgico correspondente, ou {@link Optional#empty()} caso não exista
     *     nenhum registro para o tipo informado
     */
    Optional<CicloLiturgicoEntity> findFirstByTipoCiclo(TipoCicloLiturgico tipoCiclo);

    /**
     * Localiza o ciclo litúrgico associado a um tipo e a um ano litúrgico de referência
     * específicos.
     *
     * @param tipoCiclo tipo do ciclo litúrgico buscado
     * @param anoLiturgicoRef ano litúrgico de referência ao qual o ciclo pertence
     * @return o ciclo litúrgico correspondente, ou {@link Optional#empty()} caso não exista
     *     combinação cadastrada para o tipo e ano informados
     */
    Optional<CicloLiturgicoEntity> findByTipoCicloAndAnoLiturgicoRef(TipoCicloLiturgico tipoCiclo, Integer anoLiturgicoRef);
}
