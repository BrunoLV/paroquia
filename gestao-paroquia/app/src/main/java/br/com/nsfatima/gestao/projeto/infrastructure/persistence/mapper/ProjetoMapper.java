package br.com.nsfatima.gestao.projeto.infrastructure.persistence.mapper;

import br.com.nsfatima.gestao.projeto.api.v1.dto.ProjetoAprovacaoResponse;
import br.com.nsfatima.gestao.projeto.api.v1.dto.ProjetoResponse;
import br.com.nsfatima.gestao.projeto.domain.model.Projeto;
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
                entity.getGrupoParoquialResponsavelId(),
                entity.getInicioUtc(),
                entity.getFimUtc(),
                entity.getStatus(),
                updated
        );
    }

    /** Reconstitui o agregado de domínio {@link Projeto} a partir da entidade persistida. */
    public Projeto toDomain(ProjetoEventoEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Projeto(
                entity.getId(),
                entity.getNome(),
                entity.getDescricao(),
                entity.getGrupoParoquialResponsavelId(),
                entity.getInicioUtc(),
                entity.getFimUtc(),
                entity.getStatus());
    }

    /** Nova entidade a partir de um agregado recém-aberto. */
    public ProjetoEventoEntity toEntity(Projeto projeto) {
        ProjetoEventoEntity entity = new ProjetoEventoEntity();
        entity.setId(projeto.getId());
        aplicar(projeto, entity);
        return entity;
    }

    /** Aplica o estado do agregado sobre a entidade JPA gerenciada. */
    public void aplicar(Projeto projeto, ProjetoEventoEntity entity) {
        entity.setNome(projeto.getNome());
        entity.setDescricao(projeto.getDescricao());
        entity.setGrupoParoquialResponsavelId(projeto.getGrupoParoquialResponsavelId());
        entity.setInicioUtc(projeto.getInicio());
        entity.setFimUtc(projeto.getFim());
        entity.setStatus(projeto.getStatus());
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

