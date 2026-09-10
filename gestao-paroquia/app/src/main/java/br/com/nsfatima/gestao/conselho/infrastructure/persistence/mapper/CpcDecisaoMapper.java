package br.com.nsfatima.gestao.conselho.infrastructure.persistence.mapper;

import br.com.nsfatima.gestao.conselho.api.v1.dto.CpcDecisaoResponse;
import br.com.nsfatima.gestao.conselho.domain.model.CpcDecisao;
import br.com.nsfatima.gestao.conselho.infrastructure.persistence.entity.CpcDecisaoEntity;
import org.springframework.stereotype.Component;

/**
 * Converte entre o agregado {@link CpcDecisao} (domínio puro), a {@link CpcDecisaoEntity} JPA e o
 * DTO {@link CpcDecisaoResponse} da API.
 */
@Component
public class CpcDecisaoMapper {

    public CpcDecisao toDomain(CpcDecisaoEntity e) {
        if (e == null) {
            return null;
        }
        return new CpcDecisao(
                e.getId(),
                e.getTitulo(),
                e.getPautaDescricao(),
                e.getDataReuniao(),
                e.getComunidadeId(),
                e.getConselhoTipo(),
                e.getStatusHomologacao(),
                e.getSolicitanteId(),
                e.getSolicitanteNome(),
                e.getHomologadorId(),
                e.getMotivoVeto(),
                e.getCriadoEmUtc(),
                e.getDecididoEmUtc());
    }

    /** Nova entidade a partir de um agregado recém-registrado. */
    public CpcDecisaoEntity toEntity(CpcDecisao d) {
        CpcDecisaoEntity e = new CpcDecisaoEntity();
        e.setId(d.getId());
        e.setTitulo(d.getTitulo());
        e.setPautaDescricao(d.getPautaDescricao());
        e.setDataReuniao(d.getDataReuniao());
        e.setComunidadeId(d.getComunidadeId());
        e.setConselhoTipo(d.getConselhoTipo());
        e.setStatusHomologacao(d.getStatusHomologacao());
        e.setSolicitanteId(d.getSolicitanteId());
        e.setSolicitanteNome(d.getSolicitantePapel());
        e.setHomologadorId(d.getHomologadorId());
        e.setMotivoVeto(d.getMotivoVeto());
        e.setCriadoEmUtc(d.getCriadoEmUtc());
        e.setDecididoEmUtc(d.getDecididoEmUtc());
        return e;
    }

    /** Aplica o estado do agregado sobre a entidade JPA gerenciada. */
    public void aplicar(CpcDecisao d, CpcDecisaoEntity e) {
        e.setStatusHomologacao(d.getStatusHomologacao());
        e.setHomologadorId(d.getHomologadorId());
        e.setMotivoVeto(d.getMotivoVeto());
        e.setDecididoEmUtc(d.getDecididoEmUtc());
    }

    public CpcDecisaoResponse toResponse(CpcDecisao d) {
        return new CpcDecisaoResponse(
                d.getId(),
                d.getTitulo(),
                d.getPautaDescricao(),
                d.getDataReuniao(),
                d.getComunidadeId(),
                d.getConselhoTipo(),
                d.getStatusHomologacao(),
                d.getSolicitanteId(),
                d.getSolicitantePapel(),
                d.getHomologadorId(),
                d.getMotivoVeto(),
                d.getCriadoEmUtc(),
                d.getDecididoEmUtc());
    }
}
