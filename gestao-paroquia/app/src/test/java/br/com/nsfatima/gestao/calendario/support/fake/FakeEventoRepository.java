package br.com.nsfatima.gestao.calendario.support.fake;

import br.com.nsfatima.gestao.calendario.domain.type.CategoriaEvento;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
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
            List<UUID> grupoParoquialIds, boolean hasGrupoParoquialIds,
            UUID projetoId, boolean hasProjetoId,
            UUID envolvidoId, boolean hasEnvolvidoId,
            List<CategoriaEvento> categorias, boolean hasCategorias,
            List<String> statuses, boolean hasStatuses,
            Pageable pageable) {
        List<EventoEntity> filtered = storage.stream()
                .filter(e -> (!hasInicioUtc || !e.getInicioUtc().isBefore(inicioUtc)) &&
                        (!hasFimUtc || !e.getInicioUtc().isAfter(fimUtc)) &&
                        (!hasGrupoParoquialIds || grupoParoquialIds.contains(e.getGrupoParoquialResponsavelId())) &&
                        (!hasProjetoId || projetoId.equals(e.getProjetoId())) &&
                        (!hasEnvolvidoId
                                || e.getGrupoParoquialResponsavelId().equals(envolvidoId)) && // Simplified for fake
                        (!hasCategorias || categorias.contains(e.getCategoria())) &&
                        (!hasStatuses || statuses.contains(e.getStatus())))
                .toList();
        return new PageImpl<>(filtered, pageable, filtered.size());
    }

    @Override
    public List<EventoEntity> findAllByOrderByInicioUtcAscIdAsc() {
        return storage.stream().sorted(Comparator.comparing(EventoEntity::getInicioUtc)).toList();
    }

    @Override
    public boolean existsByInicioUtcLessThanAndFimUtcGreaterThan(Instant fimUtcExclusive, Instant inicioUtcExclusive) {
        return storage.stream().anyMatch(e ->
                e.getInicioUtc().isBefore(fimUtcExclusive)
                        && e.getFimUtc().isAfter(inicioUtcExclusive));
    }

    @Override
    public long countByGrupoParoquialAndStatusAndPeriod(UUID orgId, String status, Instant start, Instant end) {
        return storage.stream()
                .filter(e -> e.getGrupoParoquialResponsavelId().equals(orgId) && e.getStatus().equals(status) &&
                        !e.getInicioUtc().isBefore(start) && e.getInicioUtc().isBefore(end))
                .count();
    }

    @Override
    public long countByGrupoParoquialAndPeriod(UUID orgId, Instant start, Instant end) {
        return storage.stream()
                .filter(e -> e.getGrupoParoquialResponsavelId().equals(orgId) &&
                        !e.getInicioUtc().isBefore(start) && e.getInicioUtc().isBefore(end))
                .count();
    }

    @Override
    public long countDistinctByGrupoParoquialResponsavelIdAndIdIn(UUID orgId, List<UUID> ids) {
        return storage.stream()
                .filter(e -> e.getGrupoParoquialResponsavelId().equals(orgId) && ids.contains(e.getId()))
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
    public boolean existsByLocalIdAndFimUtcGreaterThanAndStatusNot(UUID localId, Instant fimUtc, String status) {
        return storage.stream().anyMatch(e -> localId.equals(e.getLocalId())
                && e.getFimUtc() != null && e.getFimUtc().isAfter(fimUtc)
                && !status.equalsIgnoreCase(e.getStatus()));
    }

    @Override
    public boolean existsByGrupoParoquialResponsavelId(UUID grupoParoquialId) {
        return storage.stream().anyMatch(e -> grupoParoquialId.equals(e.getGrupoParoquialResponsavelId()));
    }

    @Override
    public List<EventoEntity> findByRecorrenciaId(UUID recorrenciaId) {
        return storage.stream()
                .filter(e -> recorrenciaId.equals(e.getRecorrenciaId()))
                .toList();
    }

    @Override
    public List<EventoEntity> findByProjetoId(UUID projetoId) {
        return storage.stream()
                .filter(e -> projetoId.equals(e.getProjetoId()))
                .toList();
    }

    @Override
    public boolean existsByRecorrenciaIdAndInicioUtc(UUID recorrenciaId, Instant inicioUtc) {
        return storage.stream()
                .anyMatch(e -> recorrenciaId.equals(e.getRecorrenciaId()) && inicioUtc.equals(e.getInicioUtc()));
    }

    @Override
    public List<String> findInvolvedOrganizationNames(UUID projetoId) {
        return List.of(); // Requires cross-repository join, typically mocked or not needed in simple unit tests
    }

    @Override
    public List<EventoEntity> findOverlappingEvents(Instant inicioUtc, Instant fimUtc, UUID excludeId) {
        return storage.stream()
                .filter(e -> !"CANCELADO".equals(e.getStatus()))
                .filter(e -> excludeId == null || !excludeId.equals(e.getId()))
                .filter(e -> e.getInicioUtc().isBefore(fimUtc) && e.getFimUtc().isAfter(inicioUtc))
                .toList();
    }

    @Override
    public List<EventoEntity> findOrfaosNoPeriodo(Instant inicioUtc, Instant fimUtc) {
        return storage.stream()
                .filter(e -> e.getProjetoId() == null)
                .filter(e -> !"CANCELADO".equals(e.getStatus()))
                .filter(e -> !e.getInicioUtc().isBefore(inicioUtc) && e.getInicioUtc().isBefore(fimUtc))
                .sorted(Comparator.comparing(EventoEntity::getInicioUtc))
                .toList();
    }

    @Override
    public List<Object[]> findChoquesHorarioPorLocal(Instant inicioUtc, Instant fimUtc) {
        List<Object[]> pares = new ArrayList<>();
        List<EventoEntity> janela = storage.stream()
                .filter(e -> e.getLocalId() != null && !"CANCELADO".equals(e.getStatus()))
                .filter(e -> !e.getInicioUtc().isBefore(inicioUtc) && e.getInicioUtc().isBefore(fimUtc))
                .sorted(Comparator.comparing(EventoEntity::getInicioUtc))
                .toList();
        for (int i = 0; i < janela.size(); i++) {
            for (int j = i + 1; j < janela.size(); j++) {
                EventoEntity a = janela.get(i);
                EventoEntity b = janela.get(j);
                if (a.getLocalId().equals(b.getLocalId())
                        && a.getInicioUtc().isBefore(b.getFimUtc()) && b.getInicioUtc().isBefore(a.getFimUtc())) {
                    pares.add(new Object[]{a.getLocalId(), a.getTitulo(), b.getTitulo(), a.getInicioUtc()});
                }
            }
        }
        return pares;
    }

    @Override
    public List<Object[]> contarPorResponsavelNoPeriodo(Instant inicio, Instant fim) {
        return storage.stream()
                .filter(e -> !"CANCELADO".equals(e.getStatus()))
                .filter(e -> !e.getInicioUtc().isBefore(inicio) && e.getInicioUtc().isBefore(fim))
                .collect(java.util.stream.Collectors.groupingBy(EventoEntity::getGrupoParoquialResponsavelId,
                        java.util.stream.Collectors.counting()))
                .entrySet().stream()
                .map(en -> new Object[]{en.getKey(), en.getValue()})
                .toList();
    }

    @Override
    public List<EventoEntity> findComLocalNoPeriodo(Instant inicio, Instant fim) {
        return storage.stream()
                .filter(e -> e.getLocalId() != null && !"CANCELADO".equals(e.getStatus()))
                .filter(e -> !e.getInicioUtc().isBefore(inicio) && e.getInicioUtc().isBefore(fim))
                .toList();
    }

    @Override
    public Optional<EventoEntity> findProximoMarco(UUID projetoId, Instant agora) {
        return storage.stream()
                .filter(e -> projetoId.equals(e.getProjetoId()))
                .filter(e -> !"CANCELADO".equals(e.getStatus()))
                .filter(e -> !e.getInicioUtc().isBefore(agora))
                .min(Comparator.<EventoEntity>comparingInt(
                        e -> e.getNivelPrioridade() == null ? 4 : e.getNivelPrioridade())
                        .thenComparing(EventoEntity::getInicioUtc));
    }

    // Default JPA methods
    @Override
    public List<EventoEntity> findAll() {
        return storage;
    }

    @Override
    public long count() {
        return storage.size();
    }

    @Override
    public void deleteById(UUID id) {
        storage.removeIf(e -> e.getId().equals(id));
    }

    @Override
    public void deleteAll() {
        storage.clear();
    }

    @Override
    public boolean existsById(UUID uuid) {
        return findById(uuid).isPresent();
    }

    // Unimplemented methods
    @Override
    public List<EventoEntity> findAll(Sort sort) {
        return storage;
    }

    @Override
    public Page<EventoEntity> findAll(Pageable pageable) {
        return new PageImpl<>(storage);
    }

    @Override
    public List<EventoEntity> findAllById(Iterable<UUID> uuids) {
        return List.of();
    }

    @Override
    public <S extends EventoEntity> List<S> saveAll(Iterable<S> entities) {
        return List.of();
    }

    @Override
    public void flush() {
    }

    @Override
    public <S extends EventoEntity> S saveAndFlush(S entity) {
        return save(entity);
    }

    @Override
    public <S extends EventoEntity> List<S> saveAllAndFlush(Iterable<S> entities) {
        return List.of();
    }

    @Override
    public void delete(EventoEntity entity) {
    }

    @Override
    public void deleteAllById(Iterable<? extends UUID> uuids) {
    }

    @Override
    public void deleteAll(Iterable<? extends EventoEntity> entities) {
    }

    @Override
    public void deleteAllInBatch(Iterable<EventoEntity> entities) {
    }

    @Override
    public void deleteAllByIdInBatch(Iterable<UUID> uuids) {
    }

    @Override
    public void deleteAllInBatch() {
    }

    @Deprecated
    @Override
    public EventoEntity getOne(UUID uuid) {
        return Objects.requireNonNull(findById(uuid).orElse(null));
    }

    @Deprecated
    @Override
    public EventoEntity getById(UUID uuid) {
        return Objects.requireNonNull(findById(uuid).orElse(null));
    }

    @Override
    public EventoEntity getReferenceById(UUID uuid) {
        return Objects.requireNonNull(findById(uuid).orElse(null));
    }

    @Override
    public <S extends EventoEntity> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends EventoEntity> List<S> findAll(Example<S> example) {
        return List.of();
    }

    @Override
    public <S extends EventoEntity> List<S> findAll(Example<S> example, Sort sort) {
        return List.of();
    }

    @Override
    public <S extends EventoEntity> Page<S> findAll(Example<S> example, Pageable pageable) {
        return Page.empty();
    }

    @Override
    public <S extends EventoEntity> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends EventoEntity> boolean exists(Example<S> example) {
        return false;
    }

    @Override
    public <S extends EventoEntity, R> R findBy(
            Example<S> example,
            Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }

    @Override
    public List<EventoEntity> findAllInPeriod(Instant inicioUtc, Instant fimUtc) {
        return storage.stream()
                .filter(e -> !e.getInicioUtc().isBefore(inicioUtc) && !e.getInicioUtc().isAfter(fimUtc))
                .sorted(Comparator.comparing(EventoEntity::getInicioUtc))
                .toList();
    }
}
