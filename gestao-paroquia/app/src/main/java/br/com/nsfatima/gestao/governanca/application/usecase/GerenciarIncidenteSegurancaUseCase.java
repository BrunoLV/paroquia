package br.com.nsfatima.gestao.governanca.application.usecase;

import br.com.nsfatima.gestao.governanca.api.v1.dto.AtualizarStatusIncidenteRequest;
import br.com.nsfatima.gestao.governanca.api.v1.dto.IncidenteSegurancaResponse;
import br.com.nsfatima.gestao.governanca.api.v1.dto.RegistrarIncidenteRequest;
import br.com.nsfatima.gestao.governanca.domain.model.StatusIncidente;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.entity.IncidenteSegurancaEntity;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository.IncidenteSegurancaJpaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Caso de uso para gestão do ciclo de vida, documentação e comunicação de incidentes de segurança (Art. 48 LGPD).
 */
@Service
public class GerenciarIncidenteSegurancaUseCase {

    private final IncidenteSegurancaJpaRepository repository;

    public GerenciarIncidenteSegurancaUseCase(IncidenteSegurancaJpaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public IncidenteSegurancaResponse registrar(RegistrarIncidenteRequest request) {
        IncidenteSegurancaEntity entity = new IncidenteSegurancaEntity();
        entity.setId(UUID.randomUUID());
        entity.setTitulo(request.titulo().trim());
        entity.setDescricao(request.descricao().trim());
        entity.setTipoDadoAfetado(request.tipoDadoAfetado().trim());
        entity.setCategoriaGravidade(request.categoriaGravidade());
        entity.setDataOcorrencia(request.dataOcorrencia());
        entity.setDataDeteccao(request.dataDeteccao());
        entity.setMedidasMitigacao(request.medidasMitigacao() != null ? request.medidasMitigacao().trim() : null);
        entity.setStatus(StatusIncidente.EM_ANALISE);

        return IncidenteSegurancaResponse.de(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<IncidenteSegurancaResponse> listarTodos() {
        return repository.findAllByOrderByDataDeteccaoDesc().stream()
                .map(IncidenteSegurancaResponse::de)
                .toList();
    }

    @Transactional
    public IncidenteSegurancaResponse atualizarStatus(UUID id, AtualizarStatusIncidenteRequest request) {
        IncidenteSegurancaEntity entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Incidente de segurança não encontrado."));

        entity.setStatus(request.status());

        if (request.notificadoAnpd() != null) {
            entity.setNotificadoAnpd(request.notificadoAnpd());
        }
        if (request.dataNotificacaoAnpd() != null) {
            entity.setDataNotificacaoAnpd(request.dataNotificacaoAnpd());
        }
        if (request.protocoloAnpd() != null) {
            entity.setProtocoloAnpd(request.protocoloAnpd().trim());
        }
        if (request.notificadoTitulares() != null) {
            entity.setNotificadoTitulares(request.notificadoTitulares());
        }
        if (request.dataNotificacaoTitulares() != null) {
            entity.setDataNotificacaoTitulares(request.dataNotificacaoTitulares());
        }
        if (request.medidasMitigacao() != null) {
            entity.setMedidasMitigacao(request.medidasMitigacao().trim());
        }

        return IncidenteSegurancaResponse.de(repository.save(entity));
    }
}
