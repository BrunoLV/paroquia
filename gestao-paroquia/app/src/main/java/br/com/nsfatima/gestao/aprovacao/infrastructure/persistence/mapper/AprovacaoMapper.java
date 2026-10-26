package br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.mapper;

import br.com.nsfatima.gestao.aprovacao.api.v1.dto.AprovacaoResponse;
import br.com.nsfatima.gestao.aprovacao.domain.model.Aprovacao;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.calendario.domain.type.TipoSolicitacaoResponse;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.LegacyEnumInconsistencyPublisher;
import org.springframework.stereotype.Component;

/**
 * Converte {@link AprovacaoEntity} (persistência) em {@link AprovacaoResponse} (contrato de API), isolando o
 * modelo JPA do formato exposto externamente. Normaliza o tipo de solicitação legado via
 * {@link LegacyEnumInconsistencyPublisher}, reportando inconsistências de dados legados sem quebrar a resposta.
 */
@Component
public class AprovacaoMapper {

    private final LegacyEnumInconsistencyPublisher legacyEnumInconsistencyPublisher;

    public AprovacaoMapper(LegacyEnumInconsistencyPublisher legacyEnumInconsistencyPublisher) {
        this.legacyEnumInconsistencyPublisher = legacyEnumInconsistencyPublisher;
    }

    /**
     * Converte a entidade de aprovação persistida em seu DTO de resposta para a API.
     *
     * @param entity entidade de aprovação; pode ser {@code null}
     * @return DTO de resposta correspondente, ou {@code null} se a entidade for {@code null}
     */
    public AprovacaoResponse toResponse(AprovacaoEntity entity) {
        return toResponse(entity, null);
    }

    /**
     * Converte a entidade de aprovação persistida em seu DTO de resposta para a API, incluindo a contagem
     * de ocorrências filhas e os detalhes de falha de execução.
     *
     * @param entity entidade de aprovação; pode ser {@code null}
     * @param totalOcorrenciasFilhas total de ocorrências filhas quando for mestre
     * @return DTO de resposta correspondente, ou {@code null} se a entidade for {@code null}
     */
    public AprovacaoResponse toResponse(AprovacaoEntity entity, Integer totalOcorrenciasFilhas) {
        if (entity == null) {
            return null;
        }

        return new AprovacaoResponse(
                entity.getId(),
                entity.getEventoId(),
                TipoSolicitacaoResponse.fromStoredValue(
                        entity.getTipoSolicitacao(),
                        legacyEnumInconsistencyPublisher,
                        entity.getId().toString()),
                entity.getStatus().name(),
                entity.getAprovadorPapel(),
                entity.getCriadoEmUtc(),
                entity.getDecididoEmUtc(),
                entity.getSolicitanteId(),
                entity.getAprovadorId(),
                entity.getMensagemErroExecucao(),
                entity.getTipoFalhaExecucao() != null ? entity.getTipoFalhaExecucao().name() : null,
                entity.getDetalheErroExecucao(),
                entity.getActionPayloadJson(),
                entity.getAprovacaoPaiId(),
                entity.getConflitosJson(),
                entity.getDataOcorrenciaUtc(),
                entity.getJustificativa(),
                totalOcorrenciasFilhas);
    }

    /**
     * Reconstitui o agregado de domínio {@link Aprovacao} a partir da entidade persistida.
     */
    public Aprovacao toDomain(AprovacaoEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Aprovacao(
                entity.getId(),
                entity.getEventoId(),
                entity.getTipoSolicitacao(),
                entity.getAprovadorPapel(),
                entity.getStatus(),
                entity.getCriadoEmUtc(),
                entity.getDecididoEmUtc(),
                entity.getExecutadoEmUtc(),
                entity.getSolicitanteId(),
                entity.getAprovadorId(),
                entity.getDecisionObservacao(),
                entity.getActionPayloadJson(),
                entity.getCorrelationId(),
                entity.getMensagemErroExecucao(),
                entity.getTipoFalhaExecucao(),
                entity.getDetalheErroExecucao(),
                entity.getAprovacaoPaiId(),
                entity.getConflitosJson(),
                entity.getDataOcorrenciaUtc(),
                entity.getJustificativa());
    }

    /**
     * Aplica o estado corrente do agregado sobre a entidade JPA gerenciada, preservando a identidade
     * da instância (necessário para que a transação persista a mesma linha).
     */
    public void aplicar(Aprovacao dominio, AprovacaoEntity entity) {
        entity.setStatus(dominio.getStatus());
        entity.setAprovadorPapel(dominio.getAprovadorPapel());
        entity.setAprovadorId(dominio.getAprovadorId());
        entity.setDecisionObservacao(dominio.getDecisionObservacao());
        entity.setDecididoEmUtc(dominio.getDecididoEmUtc());
        entity.setExecutadoEmUtc(dominio.getExecutadoEmUtc());
        entity.setMensagemErroExecucao(dominio.getMensagemErroExecucao());
        entity.setTipoFalhaExecucao(dominio.getTipoFalhaExecucao());
        entity.setDetalheErroExecucao(dominio.getDetalheErroExecucao());
    }

    /**
     * Cria uma nova entidade JPA a partir de um agregado recém-aberto (fluxo de criação).
     */
    public AprovacaoEntity toEntity(Aprovacao dominio) {
        AprovacaoEntity entity = new AprovacaoEntity();
        entity.setId(dominio.getId());
        entity.setEventoId(dominio.getEventoId());
        entity.setTipoSolicitacao(dominio.getTipoSolicitacao());
        entity.setAprovadorPapel(dominio.getAprovadorPapel());
        entity.setStatus(dominio.getStatus());
        entity.setDecididoEmUtc(dominio.getDecididoEmUtc());
        entity.setSolicitanteId(dominio.getSolicitanteId());
        entity.setActionPayloadJson(dominio.getActionPayloadJson());
        entity.setCorrelationId(dominio.getCorrelationId());
        entity.setAprovacaoPaiId(dominio.getAprovacaoPaiId());
        entity.setConflitosJson(dominio.getConflitosJson());
        entity.setDataOcorrenciaUtc(dominio.getDataOcorrenciaUtc());
        entity.setJustificativa(dominio.getJustificativa());
        entity.setTipoFalhaExecucao(dominio.getTipoFalhaExecucao());
        entity.setDetalheErroExecucao(dominio.getDetalheErroExecucao());
        return entity;
    }
}
