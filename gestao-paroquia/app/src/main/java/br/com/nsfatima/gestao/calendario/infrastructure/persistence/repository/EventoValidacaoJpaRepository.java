package br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

/**
 * Sub-repositório de {@link br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity}
 * dedicado a consultas de existência ({@code existsBy...}) usadas em regras de negócio de
 * validação, como checagem de conflito de horário e verificação de vínculo a local, organização
 * ou recorrência.
 *
 * <p>Faz parte da segregação por responsabilidade do agregado Evento (ver Javadoc de
 * {@link EventoJpaRepository} para o racional completo): esta interface concentra apenas
 * verificações booleanas de existência, mantendo-as separadas das consultas de leitura de
 * calendário e das consultas agregadas/estatísticas.
 */
@NoRepositoryBean
public interface EventoValidacaoJpaRepository {

    /**
     * Verifica se existe algum evento cujo intervalo se sobrepõe ao intervalo informado,
     * comparando o início de um evento existente contra o fim do intervalo candidato e o fim do
     * evento existente contra o início do intervalo candidato.
     *
     * @param fimUtcExclusive fim do intervalo candidato, usado como limite superior exclusivo
     * @param inicioUtcExclusive início do intervalo candidato, usado como limite inferior exclusivo
     * @return {@code true} caso exista sobreposição de horário, {@code false} caso contrário
     */
    boolean existsByInicioUtcLessThanAndFimUtcGreaterThan(Instant fimUtcExclusive, Instant inicioUtcExclusive);

    /**
     * Verifica se existe algum evento vinculado a um determinado local, usado para impedir a
     * remoção/desativação de um local ainda em uso.
     *
     * @param localId identificador do local
     * @return {@code true} caso exista ao menos um evento vinculado ao local, {@code false} caso contrário
     */
    @Query("""
            select (count(e) > 0) from EventoEntity e
            left join e.locais el
            where e.localId = :localId or el.localId = :localId
            """)
    boolean existsByLocalId(@Param("localId") UUID localId);

    /**
     * Verifica se existe algum evento futuro vinculado a um determinado local com status diferente do
     * informado (tipicamente CANCELADO), garantindo integridade referencial ao inativar ou excluir locais.
     *
     * @param localId identificador do local
     * @param fimUtc instante de corte para eventos futuros
     * @param status status a ser desconsiderado (ex.: "CANCELADO")
     * @return {@code true} se houver eventos futuros agendados ativos
     */
    @Query("""
            select (count(e) > 0) from EventoEntity e
            left join e.locais el
            where (e.localId = :localId or el.localId = :localId)
              and e.fimUtc > :fimUtc
              and e.status != :status
            """)
    boolean existsByLocalIdAndFimUtcGreaterThanAndStatusNot(
            @Param("localId") UUID localId,
            @Param("fimUtc") Instant fimUtc,
            @Param("status") String status);

    /**
     * Verifica se existe algum evento vinculado a uma determinada organização como responsável,
     * usado para impedir a remoção/desativação de uma organização ainda em uso.
     *
     * @param organizacaoId identificador da organização responsável
     * @return {@code true} caso exista ao menos um evento da organização, {@code false} caso contrário
     */
    boolean existsByOrganizacaoResponsavelId(UUID organizacaoId);

    /**
     * Verifica se já existe uma ocorrência de evento para uma determinada recorrência em um
     * instante de início específico, usado para evitar geração duplicada de ocorrências de uma
     * série recorrente.
     *
     * @param recorrenciaId identificador da recorrência (série) de eventos
     * @param inicioUtc instante de início da ocorrência a ser verificada
     * @return {@code true} caso já exista uma ocorrência com esse início para a recorrência, {@code false} caso
     *     contrário
     */
    boolean existsByRecorrenciaIdAndInicioUtc(UUID recorrenciaId, Instant inicioUtc);
}
