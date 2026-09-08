package br.com.nsfatima.gestao.reserva.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.reserva.domain.model.Reserva;
import br.com.nsfatima.gestao.reserva.domain.model.ReservaId;
import br.com.nsfatima.gestao.reserva.domain.model.TipoItemReserva;
import br.com.nsfatima.gestao.reserva.domain.repository.ReservaFiltro;
import br.com.nsfatima.gestao.reserva.domain.repository.ReservaRepository;
import br.com.nsfatima.gestao.reserva.infrastructure.persistence.entity.ReservaEntity;
import br.com.nsfatima.gestao.reserva.infrastructure.persistence.mapper.ReservaMapper;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ReservaRepositoryImpl implements ReservaRepository {

    private final ReservaJpaRepository jpaRepository;
    private final ReservaMapper mapper;

    public ReservaRepositoryImpl(ReservaJpaRepository jpaRepository, ReservaMapper mapper) {
        this.jpaRepository = Objects.requireNonNull(jpaRepository, "ReservaJpaRepository é obrigatório.");
        this.mapper = Objects.requireNonNull(mapper, "ReservaMapper é obrigatório.");
    }

    @Override
    public Reserva save(Reserva reserva) {
        ReservaEntity entity = mapper.toEntity(reserva);
        ReservaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public List<Reserva> saveAll(List<Reserva> reservas) {
        List<ReservaEntity> entities = reservas.stream().map(mapper::toEntity).toList();
        List<ReservaEntity> saved = jpaRepository.saveAll(entities);
        return saved.stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Reserva> findById(ReservaId id) {
        return jpaRepository.findById(id.value())
                .filter(e -> e.getDeletedAt() == null)
                .map(mapper::toDomain);
    }

    @Override
    public List<Reserva> findConflitosPotenciaisComLock(
            TipoItemReserva tipoItem,
            UUID itemId,
            Instant inicioLimite,
            Instant fimLimite,
            UUID ignorarReservaId
    ) {
        List<ReservaEntity> entities = jpaRepository.findConflitosPotenciaisComLock(
                tipoItem.name(),
                itemId,
                inicioLimite,
                fimLimite,
                ignorarReservaId
        );
        return entities.stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Reserva> findPendentesExpiracao(Instant limiteDataHoraInicio) {
        return jpaRepository.findPendentesExpiracao(limiteDataHoraInicio)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Reserva> findByEventoId(UUID eventoId) {
        return jpaRepository.findByEventoIdAndDeletedAtIsNull(eventoId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Reserva> findByGrupoRecorrenciaId(UUID grupoRecorrenciaId) {
        return jpaRepository.findByGrupoRecorrenciaIdAndDeletedAtIsNull(grupoRecorrenciaId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Reserva> findAll(ReservaFiltro filtro) {
        Specification<ReservaEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.isNull(root.get("deletedAt")));

            if (filtro != null) {
                if (filtro.tipoItem() != null) {
                    predicates.add(cb.equal(root.get("tipoItem"), filtro.tipoItem().name()));
                }
                if (filtro.itemId() != null) {
                    predicates.add(cb.equal(root.get("itemId"), filtro.itemId()));
                }
                if (filtro.status() != null) {
                    predicates.add(cb.equal(root.get("status"), filtro.status().name()));
                }
                if (filtro.pastoralMovimentoId() != null) {
                    predicates.add(cb.equal(root.get("pastoralMovimentoId"), filtro.pastoralMovimentoId()));
                }
                if (filtro.solicitanteId() != null) {
                    predicates.add(cb.equal(root.get("solicitanteId"), filtro.solicitanteId()));
                }
                if (filtro.eventoId() != null) {
                    predicates.add(cb.equal(root.get("eventoId"), filtro.eventoId()));
                }
                if (filtro.dataInicio() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("dataHoraInicio"), filtro.dataInicio()));
                }
                if (filtro.dataFim() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("dataHoraFim"), filtro.dataFim()));
                }
                if (filtro.termoBusca() != null && !filtro.termoBusca().isBlank()) {
                    String termo = "%" + filtro.termoBusca().trim().toLowerCase() + "%";
                    predicates.add(cb.like(cb.lower(root.get("descricao")), termo));
                }
            }

            if (query != null) {
                query.orderBy(cb.asc(root.get("dataHoraInicio")));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return jpaRepository.findAll(spec)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public long countPendentesAprovacao(UUID pastoralId) {
        return jpaRepository.countPendentesAprovacao(pastoralId);
    }

    @Override
    public long countDevolucoesPendentesHoje(Instant inicioHoje, Instant fimHoje) {
        return jpaRepository.countDevolucoesPendentesHoje(fimHoje);
    }
}
