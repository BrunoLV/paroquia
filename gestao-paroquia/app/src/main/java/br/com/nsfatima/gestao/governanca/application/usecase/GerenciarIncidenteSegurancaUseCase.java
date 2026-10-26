package br.com.nsfatima.gestao.governanca.application.usecase;

import br.com.nsfatima.gestao.governanca.api.v1.dto.AtualizarStatusIncidenteRequest;
import br.com.nsfatima.gestao.governanca.api.v1.dto.IncidenteSegurancaResponse;
import br.com.nsfatima.gestao.governanca.api.v1.dto.RegistrarIncidenteRequest;
import br.com.nsfatima.gestao.governanca.domain.model.IncidenteSeguranca;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.entity.IncidenteSegurancaEntity;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.mapper.IncidenteSegurancaMapper;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository.IncidenteSegurancaJpaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Caso de uso para gestão do ciclo de vida, documentação e comunicação de incidentes de segurança (Art. 48 LGPD).
 * Orquestra o modelo de domínio {@link IncidenteSeguranca}, que detém as regras (máquina de estados, linha do
 * tempo e prazo de 3 dias úteis); aqui apenas se carrega, delega e persiste.
 */
@Service
public class GerenciarIncidenteSegurancaUseCase {

    private final IncidenteSegurancaJpaRepository repository;
    private final IncidenteSegurancaMapper mapper;

    public GerenciarIncidenteSegurancaUseCase(
            IncidenteSegurancaJpaRepository repository, IncidenteSegurancaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    public IncidenteSegurancaResponse registrar(RegistrarIncidenteRequest request) {
        IncidenteSeguranca incidente = IncidenteSeguranca.registrar(
                UUID.randomUUID(),
                request.titulo().trim(),
                request.descricao().trim(),
                request.tipoDadoAfetado().trim(),
                request.categoriaGravidade(),
                request.dataOcorrencia(),
                request.dataDeteccao(),
                request.medidasMitigacao() != null ? request.medidasMitigacao().trim() : null);
        IncidenteSegurancaEntity salvo = repository.save(mapper.toEntity(incidente));
        return IncidenteSegurancaResponse.de(mapper.toDomain(salvo), salvo.getCriadoEmUtc(), Instant.now());
    }

    @Transactional(readOnly = true)
    public List<IncidenteSegurancaResponse> listarTodos() {
        Instant agora = Instant.now();
        return repository.findAllByOrderByDataDeteccaoDesc().stream()
                .map(e -> IncidenteSegurancaResponse.de(mapper.toDomain(e), e.getCriadoEmUtc(), agora))
                .toList();
    }

    @Transactional
    public IncidenteSegurancaResponse atualizarStatus(UUID id, AtualizarStatusIncidenteRequest request) {
        IncidenteSegurancaEntity entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Incidente de segurança não encontrado."));
        Instant agora = Instant.now();
        IncidenteSeguranca atualizado = mapper.toDomain(entity).atualizar(
                request.status(),
                request.notificadoAnpd(),
                request.dataNotificacaoAnpd(),
                request.protocoloAnpd() != null ? request.protocoloAnpd().trim() : null,
                request.notificadoTitulares(),
                request.dataNotificacaoTitulares(),
                request.medidasMitigacao() != null ? request.medidasMitigacao().trim() : null,
                agora);
        mapper.aplicar(atualizado, entity);
        IncidenteSegurancaEntity salvo = repository.save(entity);
        return IncidenteSegurancaResponse.de(mapper.toDomain(salvo), salvo.getCriadoEmUtc(), agora);
    }
}
