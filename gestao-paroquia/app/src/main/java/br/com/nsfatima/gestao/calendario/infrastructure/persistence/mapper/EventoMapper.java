package br.com.nsfatima.gestao.calendario.infrastructure.persistence.mapper;

import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.CriarEventoRequest;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoResponse;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.AtualizarEventoRequest;
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

/**
 * Converte entre {@link EventoEntity} (persistência) e os DTOs da API de eventos
 * ({@link CriarEventoRequest}, {@link AtualizarEventoRequest}, {@link EventoResponse}), isolando o
 * restante da aplicação dos detalhes de mapeamento — incluindo a conversão tolerante de status
 * legado (via {@link EventoStatusResponse#fromStoredValue}), a resolução do nome do projeto
 * vinculado e a extração dos participantes de apoio a partir dos envolvidos do evento.
 */
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

    /**
     * Cria uma nova {@link EventoEntity} a partir dos dados de uma requisição de criação,
     * gerando um novo ID e aplicando o status inicial informado.
     *
     * @param request dados de criação do evento vindos da API
     * @param status status inicial a ser aplicado ao evento (ex.: confirmado ou pendente de
     *     aprovação, conforme decidido pelo fluxo de aprovação)
     * @return nova entidade de evento pronta para persistência
     */
    public EventoEntity toNewEntity(CriarEventoRequest request, EventoStatusInput status) {
        EventoEntity entity = new EventoEntity();
        entity.setId(UUID.randomUUID());
        entity.setTitulo(request.titulo());
        entity.setDescricao(request.descricao());
        entity.setGrupoParoquialResponsavelId(request.grupoParoquialResponsavelId());
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

    /**
     * Converte uma {@link EventoEntity} persistida para o DTO de resposta da API, resolvendo o
     * status de forma tolerante a valores legados/inconsistentes (publicando um alerta via
     * {@link LegacyEnumInconsistencyPublisher} quando necessário), buscando o nome do projeto
     * vinculado (se houver) e extraindo os IDs das organizações que participam como apoio.
     *
     * @param entity entidade de evento persistida
     * @return DTO de resposta com os dados do evento prontos para serialização na API
     */
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
                    .map(EventoEnvolvidoEntity::getGrupoParoquialId)
                    .toList();
        }

        return new EventoResponse(
                entity.getId(),
                entity.getTitulo(),
                entity.getDescricao(),
                entity.getCategoria(),
                entity.getGrupoParoquialResponsavelId(),
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
                entity.getMotivoConflito(),
                entity.getAdicionadoExtraJustificativa(),
                entity.getNivelPrioridade(),
                entity.getAutorizadoPeloCpp(),
                entity.getSetorPastoral());
    }

    /**
     * Aplica, sobre uma {@link EventoEntity} existente, apenas os campos não nulos presentes na
     * requisição de atualização (patch parcial), e sempre atualiza o status para o valor
     * informado, que reflete a decisão do fluxo de aprovação/atualização para esta operação.
     *
     * @param entity entidade de evento a ser atualizada em memória (mutação in-place)
     * @param request campos da requisição de atualização; campos {@code null} são ignorados e
     *     preservam o valor atual da entidade
     * @param status status a ser definido na entidade após aplicar o patch
     */
    public void applyPatch(EventoEntity entity, AtualizarEventoRequest request, EventoStatusInput status) {
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
        if (request.grupoParoquialResponsavelId() != null) {
            entity.setGrupoParoquialResponsavelId(request.grupoParoquialResponsavelId());
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
