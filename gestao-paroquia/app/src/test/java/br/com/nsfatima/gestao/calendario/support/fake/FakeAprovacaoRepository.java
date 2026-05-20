package br.com.nsfatima.gestao.calendario.support.fake;

import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import java.util.function.Function;

/**
 * Fake implementation of AprovacaoJpaRepository for testing without a database.
 */
public class FakeAprovacaoRepository implements AprovacaoJpaRepository {
    private final List<AprovacaoEntity> storage = new ArrayList<>();

    @Override
    public <S extends AprovacaoEntity> S save(S entity) {
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID());
        }
        storage.removeIf(e -> e.getId().equals(entity.getId()));
        storage.add(entity);
        return entity;
    }

    @Override
    public Optional<AprovacaoEntity> findById(UUID id) {
        return storage.stream().filter(e -> e.getId().equals(id)).findFirst();
    }

    @Override
    public Optional<AprovacaoEntity> findByIdAndEventoId(UUID id, UUID eventoId) {
        return storage.stream().filter(e -> e.getId().equals(id) && e.getEventoId().equals(eventoId)).findFirst();
    }

    @Override
    public Optional<AprovacaoEntity> findByIdAndStatusIgnoreCase(UUID id, String status) {
        return storage.stream().filter(e -> e.getId().equals(id) && e.getStatus().equalsIgnoreCase(status)).findFirst();
    }

    @Override
    public List<AprovacaoEntity> findByTipoSolicitacaoAndStatusIgnoreCaseOrderByCriadoEmUtcAsc(String type, String status) {
        return storage.stream()
                .filter(e -> e.getTipoSolicitacao().equals(type) && e.getStatus().equalsIgnoreCase(status))
                .toList();
    }

    @Override
    public List<AprovacaoEntity> findByStatusIgnoreCaseOrderByCriadoEmUtcAsc(String status) {
        return storage.stream()
                .filter(e -> e.getStatus().equalsIgnoreCase(status))
                .toList();
    }

    @Override
    public Page<AprovacaoEntity> findByFilter(UUID eventoId, String status, Pageable pageable) {
        List<AprovacaoEntity> filtered = storage.stream()
                .filter(e -> (eventoId == null || e.getEventoId().equals(eventoId)) && (status == null || e.getStatus().equalsIgnoreCase(status)))
                .toList();
        return new PageImpl<>(filtered, pageable, filtered.size());
    }

    @Override
    public boolean existsByEventoIdAndTipoSolicitacaoAndStatusIgnoreCase(UUID eventoId, String tipo, String status) {
        return storage.stream().anyMatch(e -> e.getEventoId().equals(eventoId) && e.getTipoSolicitacao().equals(tipo) && e.getStatus().equalsIgnoreCase(status));
    }

    @Override
    public Optional<AprovacaoEntity> findTopByEventoIdAndTipoSolicitacaoAndStatusIgnoreCaseOrderByCriadoEmUtcDesc(UUID eventoId, String tipo, String status) {
        return storage.stream()
                .filter(e -> e.getEventoId().equals(eventoId) && e.getTipoSolicitacao().equals(tipo) && e.getStatus().equalsIgnoreCase(status))
                .sorted(Comparator.comparing(AprovacaoEntity::getCriadoEmUtc).reversed())
                .findFirst();
    }

    // Default JPA methods (simplified)
    @Override public List<AprovacaoEntity> findAll() { return storage; }
    @Override public long count() { return storage.size(); }
    @Override public void deleteById(UUID id) { storage.removeIf(e -> e.getId().equals(id)); }
    @Override public void deleteAll() { storage.clear(); }

    // Unimplemented methods
    @Override public List<AprovacaoEntity> findAll(Sort sort) { return storage; }
    @Override public Page<AprovacaoEntity> findAll(Pageable pageable) { return new PageImpl<>(storage); }
    @Override public List<AprovacaoEntity> findAllById(Iterable<UUID> uuids) { return List.of(); }
    @Override public void delete(AprovacaoEntity entity) {}
    @Override public void deleteAllById(Iterable<? extends UUID> uuids) {}
    @Override public void deleteAll(Iterable<? extends AprovacaoEntity> entities) {}
    @Override public <S extends AprovacaoEntity> List<S> saveAll(Iterable<S> entities) { return List.of(); }
    @Override public void flush() {}
    @Override public <S extends AprovacaoEntity> S saveAndFlush(S entity) { return save(entity); }
    @Override public <S extends AprovacaoEntity> List<S> saveAllAndFlush(Iterable<S> entities) { return List.of(); }
    @Override public void deleteAllInBatch(Iterable<AprovacaoEntity> entities) {}
    @Override public void deleteAllByIdInBatch(Iterable<UUID> uuids) {}
    @Override public void deleteAllInBatch() {}
    @Override public AprovacaoEntity getOne(UUID uuid) { return findById(uuid).orElse(null); }
    @Override public AprovacaoEntity getById(UUID uuid) { return findById(uuid).orElse(null); }
    @Override public AprovacaoEntity getReferenceById(UUID uuid) { return findById(uuid).orElse(null); }
    @Override public <S extends AprovacaoEntity> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
    @Override public <S extends AprovacaoEntity> List<S> findAll(Example<S> example) { return List.of(); }
    @Override public <S extends AprovacaoEntity> List<S> findAll(Example<S> example, Sort sort) { return List.of(); }
    @Override public <S extends AprovacaoEntity> Page<S> findAll(Example<S> example, Pageable pageable) { return Page.empty(); }
    @Override public <S extends AprovacaoEntity> long count(Example<S> example) { return 0; }
    @Override public <S extends AprovacaoEntity> boolean exists(Example<S> example) { return false; }
    @Override public <S extends AprovacaoEntity, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
    @Override public boolean existsById(UUID uuid) { return findById(uuid).isPresent(); }
}
