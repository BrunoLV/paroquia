package br.com.nsfatima.gestao.calendario.support.fake;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

/**
 * Fake implementation of EventoJpaRepository for testing.
 */
public class FakeEventoRepository implements EventoJpaRepository {
    private final List<EventoEntity> storage = new ArrayList<>();

    @Override
    public <S extends EventoEntity> S save(S entity) {
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID());
        }
        storage.removeIf(e -> e.getId().equals(entity.getId()));
        storage.add(entity);
        return entity;
    }

    @Override
    public Optional<EventoEntity> findById(UUID id) {
        return storage.stream().filter(e -> e.getId().equals(id)).findFirst();
    }

    @Override
    public Optional<String> findStatusByIdNoLock(UUID id) {
        return findById(id).map(EventoEntity::getStatus);
    }

    @Override
    public Page<EventoEntity> findAllWithFilters(
            Instant inicioUtc, boolean hasInicioUtc,
            Instant fimUtc, boolean hasFimUtc,
            UUID organizacaoId, boolean hasOrganizacaoId,
            UUID projetoId, boolean hasProjetoId,
            UUID envolvidoId, boolean hasEnvolvidoId,
            List<String> categorias, boolean hasCategorias,
            List<String> statuses, boolean hasStatuses,
            Pageable pageable) {
        List<EventoEntity> filtered = storage.stream()
                .filter(e -> (!hasInicioUtc || !e.getInicioUtc().isBefore(inicioUtc)) &&
                             (!hasFimUtc || !e.getInicioUtc().isAfter(fimUtc)) &&
                             (!hasOrganizacaoId || e.getOrganizacaoResponsavelId().equals(organizacaoId)) &&
                             (!hasProjetoId || projetoId.equals(e.getProjetoId())) &&
                             (!hasEnvolvidoId || e.getOrganizacaoResponsavelId().equals(envolvidoId)) && // Simplified for fake
                             (!hasCategorias || categorias.contains(e.getCategoria())) &&
                             (!hasStatuses || statuses.contains(e.getStatus())))
                .toList();
        return new PageImpl<>(filtered, pageable, filtered.size());
    }

    @Override
    public List<EventoEntity> findAllByOrderByInicioUtcAscIdAsc() {
        return storage.stream().sorted((a, b) -> a.getInicioUtc().compareTo(b.getInicioUtc())).toList();
    }

    @Override
    public boolean existsByInicioUtcLessThanAndFimUtcGreaterThan(Instant fimUtcExclusive, Instant inicioUtcExclusive) {
        return storage.stream().anyMatch(e -> e.getInicioUtc().isBefore(fimUtcExclusive) && e.getFimUtc().isAfter(inicioUtcExclusive));
    }

    @Override
    public long countByOrganizacaoAndStatusAndPeriod(UUID orgId, String status, Instant start, Instant end) {
        return storage.stream()
                .filter(e -> e.getOrganizacaoResponsavelId().equals(orgId) && e.getStatus().equals(status) &&
                             !e.getInicioUtc().isBefore(start) && e.getInicioUtc().isBefore(end))
                .count();
    }

    @Override
    public long countByOrganizacaoAndPeriod(UUID orgId, Instant start, Instant end) {
        return storage.stream()
                .filter(e -> e.getOrganizacaoResponsavelId().equals(orgId) &&
                             !e.getInicioUtc().isBefore(start) && e.getInicioUtc().isBefore(end))
                .count();
    }

    @Override
    public long countDistinctByOrganizacaoResponsavelIdAndIdIn(UUID orgId, List<UUID> ids) {
        return storage.stream()
                .filter(e -> e.getOrganizacaoResponsavelId().equals(orgId) && ids.contains(e.getId()))
                .count();
    }

    @Override
    public long countByProjetoId(UUID projetoId) {
        return storage.stream().filter(e -> projetoId.equals(e.getProjetoId())).count();
    }

    @Override
    public long countByProjetoIdAndStatus(UUID projetoId, String status) {
        return storage.stream().filter(e -> projetoId.equals(e.getProjetoId()) && status.equals(e.getStatus())).count();
    }

    @Override
    public long countByProjetoIdAndFimUtcLessThan(UUID projetoId, Instant now) {
        return storage.stream().filter(e -> projetoId.equals(e.getProjetoId()) && e.getFimUtc().isBefore(now)).count();
    }

    @Override
    public long countByProjetoIdAndFimUtcGreaterThanEqual(UUID projetoId, Instant now) {
        return storage.stream().filter(e -> projetoId.equals(e.getProjetoId()) && !e.getFimUtc().isBefore(now)).count();
    }

    @Override
    public boolean existsByLocalId(UUID localId) {
        return storage.stream().anyMatch(e -> localId.equals(e.getLocalId()));
    }

    @Override
    public boolean existsByOrganizacaoResponsavelId(UUID organizacaoId) {
        return storage.stream().anyMatch(e -> organizacaoId.equals(e.getOrganizacaoResponsavelId()));
    }

    @Override
    public List<String> findInvolvedOrganizationNames(UUID projetoId) {
        return List.of(); // Requires cross-repository join, typically mocked or not needed in simple unit tests
    }

    // Default JPA methods
    @Override public List<EventoEntity> findAll() { return storage; }
    @Override public long count() { return storage.size(); }
    @Override public void deleteById(UUID id) { storage.removeIf(e -> e.getId().equals(id)); }
    @Override public void deleteAll() { storage.clear(); }
    @Override public boolean existsById(UUID uuid) { return findById(uuid).isPresent(); }

    // Unimplemented methods
    @Override public List<EventoEntity> findAll(Sort sort) { return storage; }
    @Override public Page<EventoEntity> findAll(Pageable pageable) { return new PageImpl<>(storage); }
    @Override public List<EventoEntity> findAllById(Iterable<UUID> uuids) { return List.of(); }
    @Override public <S extends EventoEntity> List<S> saveAll(Iterable<S> entities) { return List.of(); }
    @Override public void flush() {}
    @Override public <S extends EventoEntity> S saveAndFlush(S entity) { return save(entity); }
    @Override public <S extends EventoEntity> List<S> saveAllAndFlush(Iterable<S> entities) { return List.of(); }
    @Override public void delete(EventoEntity entity) {}
    @Override public void deleteAllById(Iterable<? extends UUID> uuids) {}
    @Override public void deleteAll(Iterable<? extends EventoEntity> entities) {}
    @Override public void deleteAllInBatch(Iterable<EventoEntity> entities) {}
    @Override public void deleteAllByIdInBatch(Iterable<UUID> uuids) {}
    @Override public void deleteAllInBatch() {}
    @Override public EventoEntity getOne(UUID uuid) { return findById(uuid).orElse(null); }
    @Override public EventoEntity getById(UUID uuid) { return findById(uuid).orElse(null); }
    @Override public EventoEntity getReferenceById(UUID uuid) { return findById(uuid).orElse(null); }
    @Override public <S extends EventoEntity> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
    @Override public <S extends EventoEntity> List<S> findAll(Example<S> example) { return List.of(); }
    @Override public <S extends EventoEntity> List<S> findAll(Example<S> example, Sort sort) { return List.of(); }
    @Override public <S extends EventoEntity> Page<S> findAll(Example<S> example, Pageable pageable) { return Page.empty(); }
    @Override public <S extends EventoEntity> long count(Example<S> example) { return 0; }
    @Override public <S extends EventoEntity> boolean exists(Example<S> example) { return false; }
    @Override public <S extends EventoEntity, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
}
