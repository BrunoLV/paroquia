package br.com.nsfatima.gestao.calendario.domain.service;

import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoFiltroRequest;
import br.com.nsfatima.gestao.calendario.domain.exception.EventoNotFoundException;
import br.com.nsfatima.gestao.calendario.domain.type.CategoriaEvento;
import br.com.nsfatima.gestao.calendario.domain.exception.ForbiddenOperationException;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Serviço de domínio para consulta de eventos de calendário, responsável por
 * aplicar a regra de visibilidade baseada em papel e escopo organizacional: Clero
 * e Conselho Pastoral enxergam todos os eventos; demais atores só enxergam eventos
 * da própria organização responsável.
 */
@Service
public class EventoService {

    private final EventoJpaRepository eventoJpaRepository;

    public EventoService(EventoJpaRepository eventoJpaRepository) {
        this.eventoJpaRepository = eventoJpaRepository;
    }

    /**
     * Busca um evento por identificador, garantindo que o ator autenticado tem
     * permissão para visualizá-lo.
     *
     * @param id identificador do evento
     * @param actorContext contexto de papel/organização do ator autenticado
     * @return evento encontrado
     * @throws EventoNotFoundException se nenhum evento existir com o identificador informado
     * @throws ForbiddenOperationException se o ator não tiver permissão para visualizar o evento
     */
    public EventoEntity findById(UUID id, EventoActorContext actorContext) {
        EventoEntity evento = eventoJpaRepository.findById(id)
                .orElseThrow(() -> new EventoNotFoundException(id));

        assertCanView(actorContext, evento.getGrupoParoquialResponsavelId());

        return evento;
    }

    /**
     * Lista eventos de calendário paginados, aplicando os filtros opcionais
     * informados (período, organizações, projeto, envolvido, categoria e status).
     *
     * @param filters filtros de busca opcionais
     * @param pageable parâmetros de paginação e ordenação
     * @return página de eventos que atendem aos filtros informados
     */
    public Page<EventoEntity> list(EventoFiltroRequest filters, Pageable pageable) {
        List<CategoriaEvento> categories = filters.categoria();

        List<String> statuses = Optional.ofNullable(filters.status())
                .map(list -> list.stream().map(Enum::name).toList())
                .orElse(null);

        return eventoJpaRepository.findAllWithFilters(
                filters.dataInicio(),
                filters.dataInicio() != null,
                filters.dataFim(),
                filters.dataFim() != null,
                filters.grupoParoquialIds(),
                filters.grupoParoquialIds() != null && !filters.grupoParoquialIds().isEmpty(),
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

    private void assertCanView(EventoActorContext actorContext, UUID grupoParoquialResponsavelId) {
        PapelOrganizacional role = PapelOrganizacional.fromStoredValue(actorContext.role());
        TipoGrupo orgType = TipoGrupo.fromStoredValue(actorContext.organizationType());

        // Clero (Paroco/Vigario) pode ver tudo — não tem mais TipoGrupo (virou UnidadeTerritorial /
        // papel global, track linguagem_ubiqua_dominio), o papel já garante o acesso irrestrito
        if (role == PapelOrganizacional.PAROCO || role == PapelOrganizacional.VIGARIO
                || role == PapelOrganizacional.ADMIN) {
            return;
        }

        // Conselho pode ver tudo
        if (orgType == TipoGrupo.CONSELHO) {
            return;
        }

        // Outros podem ver eventos da sua própria organização
        if (actorContext.organizationId() != null && actorContext.organizationId().equals(grupoParoquialResponsavelId)) {
            return;
        }

        throw new ForbiddenOperationException("Usuario nao tem permissao para visualizar este evento");
    }
}
