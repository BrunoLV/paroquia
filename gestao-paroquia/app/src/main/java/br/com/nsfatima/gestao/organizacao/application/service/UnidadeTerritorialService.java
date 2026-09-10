package br.com.nsfatima.gestao.organizacao.application.service;

import br.com.nsfatima.gestao.observabilidade.application.service.AuditoriaPersistenceService;
import br.com.nsfatima.gestao.organizacao.domain.exception.GrupoParoquialNegocioException;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoUnidadeTerritorial;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.UnidadeTerritorialEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.UnidadeTerritorialJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Orquestra o ciclo de vida de {@link UnidadeTerritorialEntity} (criação, atualização, exclusão e consulta)
 * e registra cada mutação na auditoria.
 */
@Service
@Transactional
public class UnidadeTerritorialService {

    private static final String AUDIT_ACTOR = "admin";
    private static final String AUDIT_ACTION = "acao-admin";
    private static final String AUDIT_TARGET = "unidadeTerritorial";
    private static final String AUDIT_RESULT = "sucesso";
    private static final String AUDIT_KEY_RESOURCE_TYPE = "resourceType";
    private static final String AUDIT_KEY_RESOURCE_ID = "resourceId";
    private static final String AUDIT_KEY_ACTION = "action";
    private static final String AUDIT_VALUE_UNIDADE_TERRITORIAL = "UNIDADE_TERRITORIAL";

    private final UnidadeTerritorialJpaRepository repository;
    private final AuditoriaPersistenceService auditLogService;

    public UnidadeTerritorialService(
            UnidadeTerritorialJpaRepository repository,
            AuditoriaPersistenceService auditLogService) {
        this.repository = repository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<UnidadeTerritorialEntity> listAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<UnidadeTerritorialEntity> findById(UUID id) {
        return repository.findById(id);
    }

    @Transactional
    public UnidadeTerritorialEntity create(
            String nome, TipoUnidadeTerritorial tipo, String contato, boolean ativo, UUID parentId) {
        if (parentId != null) {
            var parent = repository.findById(parentId)
                    .orElseThrow(() -> new GrupoParoquialNegocioException(
                            "Unidade territorial mãe não encontrada com ID: " + parentId));
            if (!parent.isAtivo()) {
                throw new GrupoParoquialNegocioException("Unidade territorial mãe não está ativa.");
            }
        }

        UnidadeTerritorialEntity entity = new UnidadeTerritorialEntity();
        entity.setId(UUID.randomUUID());
        entity.setNome(nome);
        entity.setTipo(tipo);
        entity.setContato(contato);
        entity.setAtivo(ativo);
        entity.setParentId(parentId);

        repository.save(entity);

        auditLogService.log(AUDIT_ACTOR, AUDIT_ACTION, AUDIT_TARGET, AUDIT_RESULT, Map.of(
                AUDIT_KEY_RESOURCE_TYPE, AUDIT_VALUE_UNIDADE_TERRITORIAL,
                AUDIT_KEY_RESOURCE_ID, entity.getId(),
                AUDIT_KEY_ACTION, "CRIAR",
                "nome", nome
        ));

        return entity;
    }

    @Transactional
    public UnidadeTerritorialEntity update(
            UUID id, String nome, TipoUnidadeTerritorial tipo, String contato, boolean ativo, UUID parentId) {
        UnidadeTerritorialEntity entity = repository.findById(id)
                .orElseThrow(() -> GrupoParoquialNegocioException.notFound(id));

        if (parentId != null) {
            if (parentId.equals(id)) {
                throw new GrupoParoquialNegocioException("Uma unidade territorial não pode ser mãe de si mesma.");
            }
            var parent = repository.findById(parentId)
                    .orElseThrow(() -> new GrupoParoquialNegocioException(
                            "Unidade territorial mãe não encontrada com ID: " + parentId));
            if (!parent.isAtivo()) {
                throw new GrupoParoquialNegocioException("Unidade territorial mãe não está ativa.");
            }
        }

        entity.setNome(nome);
        entity.setTipo(tipo);
        entity.setContato(contato);
        entity.setAtivo(ativo);
        entity.setParentId(parentId);

        repository.save(entity);

        auditLogService.log(AUDIT_ACTOR, AUDIT_ACTION, AUDIT_TARGET, AUDIT_RESULT, Map.of(
                AUDIT_KEY_RESOURCE_TYPE, AUDIT_VALUE_UNIDADE_TERRITORIAL,
                AUDIT_KEY_RESOURCE_ID, id,
                AUDIT_KEY_ACTION, "ATUALIZAR"
        ));

        return entity;
    }

    @Transactional
    public void delete(UUID id) {
        UnidadeTerritorialEntity entity = repository.findById(id)
                .orElseThrow(() -> GrupoParoquialNegocioException.notFound(id));

        repository.delete(entity);

        auditLogService.log(AUDIT_ACTOR, AUDIT_ACTION, AUDIT_TARGET, AUDIT_RESULT, Map.of(
                AUDIT_KEY_RESOURCE_TYPE, AUDIT_VALUE_UNIDADE_TERRITORIAL,
                AUDIT_KEY_RESOURCE_ID, id,
                AUDIT_KEY_ACTION, "EXCLUIR"
        ));
    }
}
