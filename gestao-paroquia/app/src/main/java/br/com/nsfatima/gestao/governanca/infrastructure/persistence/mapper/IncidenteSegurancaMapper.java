package br.com.nsfatima.gestao.governanca.infrastructure.persistence.mapper;

import br.com.nsfatima.gestao.governanca.domain.model.IncidenteSeguranca;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.entity.IncidenteSegurancaEntity;
import org.springframework.stereotype.Component;

/**
 * Tradutor entre a entidade JPA e o modelo de domínio {@link IncidenteSeguranca}, mantendo o domínio livre de
 * persistência. A auditoria (criado/atualizado) fica exclusivamente a cargo do Spring Data JPA Auditing.
 */
@Component
public class IncidenteSegurancaMapper {

    public IncidenteSeguranca toDomain(IncidenteSegurancaEntity e) {
        return new IncidenteSeguranca(
                e.getId(), e.getTitulo(), e.getDescricao(), e.getTipoDadoAfetado(), e.getCategoriaGravidade(),
                e.getDataOcorrencia(), e.getDataDeteccao(), e.getMedidasMitigacao(), e.isNotificadoAnpd(),
                e.getDataNotificacaoAnpd(), e.getProtocoloAnpd(), e.isNotificadoTitulares(),
                e.getDataNotificacaoTitulares(), e.getStatus());
    }

    public IncidenteSegurancaEntity toEntity(IncidenteSeguranca d) {
        IncidenteSegurancaEntity e = new IncidenteSegurancaEntity();
        e.setId(d.id());
        aplicar(d, e);
        return e;
    }

    public void aplicar(IncidenteSeguranca d, IncidenteSegurancaEntity e) {
        e.setTitulo(d.titulo());
        e.setDescricao(d.descricao());
        e.setTipoDadoAfetado(d.tipoDadoAfetado());
        e.setCategoriaGravidade(d.categoriaGravidade());
        e.setDataOcorrencia(d.dataOcorrencia());
        e.setDataDeteccao(d.dataDeteccao());
        e.setMedidasMitigacao(d.medidasMitigacao());
        e.setNotificadoAnpd(d.notificadoAnpd());
        e.setDataNotificacaoAnpd(d.dataNotificacaoAnpd());
        e.setProtocoloAnpd(d.protocoloAnpd());
        e.setNotificadoTitulares(d.notificadoTitulares());
        e.setDataNotificacaoTitulares(d.dataNotificacaoTitulares());
        e.setStatus(d.status());
    }
}
