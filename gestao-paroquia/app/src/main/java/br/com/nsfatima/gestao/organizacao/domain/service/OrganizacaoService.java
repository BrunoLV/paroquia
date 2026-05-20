package br.com.nsfatima.gestao.organizacao.domain.service;

import br.com.nsfatima.gestao.observabilidade.domain.service.AuditLogPersistenceService;
import br.com.nsfatima.gestao.organizacao.domain.exception.OrganizationBusinessException;
import br.com.nsfatima.gestao.organizacao.domain.model.Organizacao;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.domain.repository.OrganizacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class OrganizacaoService {

    private final OrganizacaoRepository organizacaoRepository;
    private final AuditLogPersistenceService auditLogService;

    public OrganizacaoService(OrganizacaoRepository organizacaoRepository, AuditLogPersistenceService auditLogService) {
        this.organizacaoRepository = organizacaoRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public Organizacao createOrganization(String nome, TipoOrganizacao tipo, String contato) {
        Organizacao org = new Organizacao(UUID.randomUUID(), nome, tipo, contato, true);
        organizacaoRepository.save(org);

        auditLogService.log("admin", "admin-action", "organization", "success", Map.of(
                "resourceType", "ORGANIZATION",
                "resourceId", org.getId(),
                "organizacaoId", org.getId(),
                "action", "CREATE",
                "nome", nome
        ));

        return org;
    }

    @Transactional
    public Organizacao updateOrganization(UUID id, String nome, TipoOrganizacao tipo, String contato, boolean ativo) {
        Organizacao org = organizacaoRepository.findById(id)
                .orElseThrow(() -> OrganizationBusinessException.notFound(id));
        
        org.update(nome, tipo, contato, ativo);
        organizacaoRepository.save(org);

        auditLogService.log("admin", "admin-action", "organization", "success", Map.of(
                "resourceType", "ORGANIZATION",
                "resourceId", id,
                "organizacaoId", id,
                "action", "UPDATE"
        ));

        return org;
    }

    @Transactional
    public void deleteOrganization(UUID id) {
        if (organizacaoRepository.hasDependencies(id)) {
            throw OrganizationBusinessException.inUse(id);
        }
        organizacaoRepository.delete(id);

        auditLogService.log("admin", "admin-action", "organization", "success", Map.of(
                "resourceType", "ORGANIZATION",
                "resourceId", id,
                "organizacaoId", id,
                "action", "DELETE"
        ));
    }

    @Transactional(readOnly = true)
    public Optional<Organizacao> getOrganization(UUID id) {
        return organizacaoRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Organizacao> listOrganizations() {
        return organizacaoRepository.findAll();
    }
}
