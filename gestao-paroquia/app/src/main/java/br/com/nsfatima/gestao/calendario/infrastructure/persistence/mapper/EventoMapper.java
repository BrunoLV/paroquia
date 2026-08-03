package br.com.nsfatima.gestao.calendario.infrastructure.persistence.mapper;

import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.CreateEventoRequest;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoResponse;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.UpdateEventoRequest;
import br.com.nsfatima.gestao.calendario.domain.type.CategoriaEvento;
import br.com.nsfatima.gestao.calendario.domain.type.EventoStatusInput;
import br.com.nsfatima.gestao.calendario.domain.type.EventoStatusResponse;
import br.com.nsfatima.gestao.calendario.domain.type.PapelEnvolvido;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.LegacyEnumInconsistencyPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEnvolvidoEntity;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoEventoEntity;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class EventoMapper {

    private final LegacyEnumInconsistencyPublisher legacyEnumInconsistencyPublisher;
    private final ProjetoEventoJpaRepository projetoRepository;

    public EventoMapper(
            LegacyEnumInconsistencyPublisher legacyEnumInconsistencyPublisher,
            ProjetoEventoJpaRepository projetoRepository) {
        this.legacyEnumInconsistencyPublisher = legacyEnumInconsistencyPublisher;
        this.projetoRepository = projetoRepository;
    }

    public EventoEntity toNewEntity(CreateEventoRequest request, EventoStatusInput status) {
        EventoEntity entity = new EventoEntity();
        entity.setId(UUID.randomUUID());
        entity.setTitulo(request.titulo());
        entity.setDescricao(request.descricao());
        entity.setOrganizacaoResponsavelId(request.organizacaoResponsavelId());
        entity.setProjetoId(request.projetoId());
        entity.setLocalId(request.localId());
        entity.setIgrejaVinculoId(request.igrejaVinculoId());
        if (request.categoria() != null) {
            entity.setCategoria(request.categoria());
        }
        entity.setInicioUtc(request.inicio());
        entity.setFimUtc(request.fim());
        entity.setStatus(status.name());
        entity.setAdicionadoExtraJustificativa(request.adicionadoExtraJustificativa());
        return entity;
    }

    public EventoResponse toResponse(EventoEntity entity) {
        EventoStatusResponse status = EventoStatusResponse.fromStoredValue(
                entity.getStatus(),
                legacyEnumInconsistencyPublisher,
                entity.getId() == null ? "<null>" : entity.getId().toString());

        String nomeProjeto = null;
        if (entity.getProjetoId() != null) {
            nomeProjeto = projetoRepository.findById(entity.getProjetoId())
                    .map(ProjetoEventoEntity::getNome)
                    .orElse(null);
        }

        List<UUID> participantes = List.of();
        if (entity.getEnvolvidos() != null) {
            participantes = entity.getEnvolvidos().stream()
                    .filter(e -> e.getPapelParticipacao() == PapelEnvolvido.APOIO)
                    .map(EventoEnvolvidoEntity::getOrganizacaoId)
                    .toList();
        }

        return new EventoResponse(
                entity.getId(),
                entity.getTitulo(),
                entity.getDescricao(),
                entity.getCategoria(),
                entity.getOrganizacaoResponsavelId(),
                entity.getProjetoId(),
                nomeProjeto,
                entity.getInicioUtc(),
                entity.getFimUtc(),
                status,
                entity.getLocalId(),
                entity.getIgrejaVinculoId(),
                participantes,
                entity.getRecorrenciaId(),
                entity.getEstadoConflito() == null ? null : entity.getEstadoConflito().name(),
                entity.getMotivoConflito());
    }

    public void applyPatch(EventoEntity entity, UpdateEventoRequest request, EventoStatusInput status) {
        if (request.titulo() != null) {
            entity.setTitulo(request.titulo());
        }
        if (request.descricao() != null) {
            entity.setDescricao(request.descricao());
        }
        if (request.inicio() != null) {
            entity.setInicioUtc(request.inicio());
        }
        if (request.fim() != null) {
            entity.setFimUtc(request.fim());
        }
        if (request.canceladoMotivo() != null) {
            entity.setCanceladoMotivo(request.canceladoMotivo());
        }
        if (request.adicionadoExtraJustificativa() != null) {
            entity.setAdicionadoExtraJustificativa(request.adicionadoExtraJustificativa());
        }
        if (request.organizacaoResponsavelId() != null) {
            entity.setOrganizacaoResponsavelId(request.organizacaoResponsavelId());
        }
        if (request.projetoId() != null) {
            entity.setProjetoId(request.projetoId());
        }
        if (request.localId() != null) {
            entity.setLocalId(request.localId());
        }
        if (request.igrejaVinculoId() != null) {
            entity.setIgrejaVinculoId(request.igrejaVinculoId());
        }
        if (request.categoria() != null) {
            entity.setCategoria(request.categoria());
        }
        entity.setStatus(status.name());
    }
}
