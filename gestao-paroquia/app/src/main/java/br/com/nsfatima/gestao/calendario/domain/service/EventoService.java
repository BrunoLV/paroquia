package br.com.nsfatima.gestao.calendario.domain.service;

import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoFiltroRequest;
import br.com.nsfatima.gestao.calendario.domain.exception.EventoNotFoundException;
import br.com.nsfatima.gestao.calendario.domain.exception.ForbiddenOperationException;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class EventoService {

    private final EventoJpaRepository eventoJpaRepository;

    public EventoService(EventoJpaRepository eventoJpaRepository) {
        this.eventoJpaRepository = eventoJpaRepository;
    }

    public EventoEntity findById(UUID id, EventoActorContext actorContext) {
        EventoEntity evento = eventoJpaRepository.findById(id)
                .orElseThrow(() -> new EventoNotFoundException(id));

        assertCanView(actorContext, evento.getOrganizacaoResponsavelId());
        
        return evento;
    }

    public Page<EventoEntity> list(EventoFiltroRequest filters, Pageable pageable) {
        java.util.List<String> categories = java.util.Optional.ofNullable(filters.categoria())
                .map(list -> list.stream().map(Enum::name).toList())
                .orElse(null);
                
        java.util.List<String> statuses = java.util.Optional.ofNullable(filters.status())
                .map(list -> list.stream().map(Enum::name).toList())
                .orElse(null);

        return eventoJpaRepository.findAllWithFilters(
                filters.dataInicio(),
                filters.dataInicio() != null,
                filters.dataFim(),
                filters.dataFim() != null,
                filters.organizacaoId(),
                filters.organizacaoId() != null,
                filters.projetoId(),
                filters.projetoId() != null,
                filters.envolvidoId(),
                filters.envolvidoId() != null,
                categories,
                categories != null,
                statuses,
                statuses != null,
                pageable);
    }

    private void assertCanView(EventoActorContext actorContext, UUID organizacaoResponsavelId) {
        PapelOrganizacional role = PapelOrganizacional.fromStoredValue(actorContext.role());
        TipoOrganizacao orgType = TipoOrganizacao.fromStoredValue(actorContext.organizationType());

        // Clero pode ver tudo
        if (orgType == TipoOrganizacao.CLERO) {
            return;
        }

        // Conselho pode ver tudo
        if (orgType == TipoOrganizacao.CONSELHO) {
            return;
        }

        // Outros podem ver eventos da sua própria organização
        if (actorContext.organizationId() != null && actorContext.organizationId().equals(organizacaoResponsavelId)) {
            return;
        }

        throw new ForbiddenOperationException("Usuario nao tem permissao para visualizar este evento");
    }
}
