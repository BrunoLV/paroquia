package br.com.nsfatima.gestao.calendario.infrastructure.persistence.mapper;

import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.CreateEventoRequest;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoResponse;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.UpdateEventoRequest;
import br.com.nsfatima.gestao.calendario.domain.type.EventoStatusInput;
import br.com.nsfatima.gestao.calendario.domain.type.EventoStatusResponse;
import br.com.nsfatima.gestao.calendario.domain.type.CategoriaEvento;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.LegacyEnumInconsistencyPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoEventoEntity;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import java.util.UUID;
import org.springframework.stereotype.Component;

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
        if (request.categoria() != null) {
            entity.setCategoria(request.categoria().name());
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

        return new EventoResponse(
                entity.getId(),
                entity.getTitulo(),
                entity.getDescricao(),
                CategoriaEvento.fromValue(entity.getCategoria()),
                entity.getOrganizacaoResponsavelId(),
                entity.getProjetoId(),
                nomeProjeto,
                entity.getInicioUtc(),
                entity.getFimUtc(),
                status,
                entity.getConflictState(),
                entity.getConflictReason());
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
        if (request.categoria() != null) {
            entity.setCategoria(request.categoria().name());
        }
        entity.setStatus(status.name());
    }
}
