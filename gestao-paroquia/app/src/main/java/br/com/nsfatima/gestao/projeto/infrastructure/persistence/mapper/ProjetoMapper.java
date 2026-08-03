package br.com.nsfatima.gestao.projeto.infrastructure.persistence.mapper;

import br.com.nsfatima.gestao.projeto.api.v1.dto.ProjetoAprovacaoResponse;
import br.com.nsfatima.gestao.projeto.api.v1.dto.ProjetoResponse;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoEventoEntity;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoSolicitacaoAprovacaoEntity;
import org.springframework.stereotype.Component;

/**
 * Converte entidades JPA do contexto {@code projeto} ({@code ProjetoEventoEntity},
 * {@code ProjetoSolicitacaoAprovacaoEntity}) para os DTOs de resposta da API
 * ({@code ProjetoResponse}, {@code ProjetoAprovacaoResponse}), isolando o restante da aplicação
 * dos detalhes de persistência.
 */
@Component
public class ProjetoMapper {

    /**
     * Converte a entidade para {@code ProjetoResponse}, marcando {@code updated} como
     * {@code false} (uso padrão para respostas de leitura/criação).
     *
     * @param entity entidade do projeto a converter; pode ser {@code null}.
     * @return o DTO de resposta correspondente, ou {@code null} se {@code entity} for
     *         {@code null}.
     */
    public ProjetoResponse toResponse(ProjetoEventoEntity entity) {
        return toResponse(entity, false);
    }

    /**
     * Converte a entidade para {@code ProjetoResponse}, permitindo indicar explicitamente se a
     * resposta representa uma atualização (usado após operações de patch/transição).
     *
     * @param entity  entidade do projeto a converter; pode ser {@code null}.
     * @param updated indica se a resposta deve sinalizar que o projeto foi atualizado.
     * @return o DTO de resposta correspondente, ou {@code null} se {@code entity} for
     *         {@code null}.
     */
    public ProjetoResponse toResponse(ProjetoEventoEntity entity, boolean updated) {
        if (entity == null) {
            return null;
        }
        return new ProjetoResponse(
                entity.getId(),
                entity.getNome(),
                entity.getDescricao(),
                entity.getOrganizacaoResponsavelId(),
                entity.getInicioUtc(),
                entity.getFimUtc(),
                entity.getStatus(),
                updated
        );
    }

    /**
     * Converte a entidade de solicitação de aprovação para o DTO de resposta correspondente.
     *
     * @param entity entidade da solicitação de aprovação a converter; pode ser {@code null}.
     * @return o DTO de resposta correspondente, ou {@code null} se {@code entity} for
     *         {@code null}.
     */
    public ProjetoAprovacaoResponse toAprovacaoResponse(ProjetoSolicitacaoAprovacaoEntity entity) {
        if (entity == null) {
            return null;
        }
        return new ProjetoAprovacaoResponse(
                entity.getId(),
                entity.getProjetoId(),
                entity.getTipoSolicitacao(),
                entity.getStatus().name(),
                entity.getSolicitanteId(),
                entity.getCriadoEmUtc(),
                entity.getDecididoEmUtc(),
                entity.getActionPayloadJson(),
                entity.getCancelarEventosVinculados()
        );
    }
}

