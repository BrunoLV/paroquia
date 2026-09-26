package br.com.nsfatima.gestao.clero.application.usecase;

import br.com.nsfatima.gestao.clero.api.v1.dto.DioceseRequest;
import br.com.nsfatima.gestao.clero.api.v1.dto.DioceseResponse;
import br.com.nsfatima.gestao.clero.domain.model.Diocese;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.entity.DioceseEntity;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.repository.DioceseJpaRepository;
import br.com.nsfatima.gestao.observabilidade.application.service.AuditoriaPersistenceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class GerenciarDioceseUseCase {

    private final DioceseJpaRepository repository;
    private final AuditoriaPersistenceService auditLogService;

    public GerenciarDioceseUseCase(DioceseJpaRepository repository, AuditoriaPersistenceService auditLogService) {
        this.repository = repository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<DioceseResponse> listar() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<DioceseResponse> obterPorId(UUID id) {
        return repository.findById(id).map(this::toResponse);
    }

    public DioceseResponse criar(DioceseRequest request) {
        repository.findByNomeIgnoreCase(request.nome()).ifPresent(d -> {
            throw new IllegalArgumentException("Já existe uma diocese cadastrada com o nome: " + request.nome());
        });

        Diocese domain = new Diocese(
                UUID.randomUUID(),
                request.nome(),
                request.bispoDiocesano(),
                request.cnpjMitra(),
                request.cidade(),
                request.uf(),
                request.contato(),
                request.ativo()
        );

        DioceseEntity entity = toEntity(domain);
        repository.save(entity);

        auditLogService.log("admin", "criar-diocese", "diocese", "sucesso", Map.of(
                "dioceseId", entity.getId(),
                "nome", entity.getNome()
        ));

        return toResponse(entity);
    }

    public DioceseResponse atualizar(UUID id, DioceseRequest request) {
        DioceseEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Diocese não encontrada com ID: " + id));

        repository.findByNomeIgnoreCase(request.nome()).ifPresent(d -> {
            if (!d.getId().equals(id)) {
                throw new IllegalArgumentException("Já existe outra diocese cadastrada com o nome: " + request.nome());
            }
        });

        Diocese domain = new Diocese(
                id,
                request.nome(),
                request.bispoDiocesano(),
                request.cnpjMitra(),
                request.cidade(),
                request.uf(),
                request.contato(),
                request.ativo()
        );

        applyDomainToEntity(domain, entity);
        repository.save(entity);

        auditLogService.log("admin", "atualizar-diocese", "diocese", "sucesso", Map.of(
                "dioceseId", id,
                "nome", entity.getNome()
        ));

        return toResponse(entity);
    }

    public void excluir(UUID id) {
        DioceseEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Diocese não encontrada com ID: " + id));

        repository.delete(entity);

        auditLogService.log("admin", "excluir-diocese", "diocese", "sucesso", Map.of(
                "dioceseId", id
        ));
    }

    private DioceseEntity toEntity(Diocese domain) {
        DioceseEntity entity = new DioceseEntity();
        applyDomainToEntity(domain, entity);
        return entity;
    }

    private void applyDomainToEntity(Diocese domain, DioceseEntity entity) {
        entity.setId(domain.getId());
        entity.setNome(domain.getNome());
        entity.setBispoDiocesano(domain.getBispoDiocesano());
        entity.setCnpjMitra(domain.getCnpjMitra());
        entity.setCidade(domain.getCidade());
        entity.setUf(domain.getUf());
        entity.setContato(domain.getContato());
        entity.setAtivo(domain.isAtivo());
    }

    private DioceseResponse toResponse(DioceseEntity entity) {
        return new DioceseResponse(
                entity.getId(),
                entity.getNome(),
                entity.getBispoDiocesano(),
                entity.getCnpjMitra(),
                entity.getCidade(),
                entity.getUf(),
                entity.getContato(),
                entity.isAtivo()
        );
    }
}
