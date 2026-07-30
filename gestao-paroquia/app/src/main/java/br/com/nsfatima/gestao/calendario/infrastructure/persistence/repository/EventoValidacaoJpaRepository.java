package br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository;

import org.springframework.data.repository.NoRepositoryBean;

import java.time.Instant;
import java.util.UUID;

@NoRepositoryBean
public interface EventoValidacaoJpaRepository {

    boolean existsByInicioUtcLessThanAndFimUtcGreaterThan(Instant fimUtcExclusive, Instant inicioUtcExclusive);

    boolean existsByLocalId(UUID localId);

    boolean existsByOrganizacaoResponsavelId(UUID organizacaoId);

    boolean existsByRecorrenciaIdAndInicioUtc(UUID recorrenciaId, Instant inicioUtc);
}
