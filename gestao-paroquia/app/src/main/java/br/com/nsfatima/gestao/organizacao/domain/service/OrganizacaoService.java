package br.com.nsfatima.gestao.organizacao.domain.service;

import br.com.nsfatima.gestao.observabilidade.domain.service.AuditLogPersistenceService;
import br.com.nsfatima.gestao.organizacao.domain.exception.OrganizationBusinessException;
import br.com.nsfatima.gestao.organizacao.domain.model.Organizacao;
import br.com.nsfatima.gestao.organizacao.domain.model.PastoralMovimentoOficial;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.domain.repository.OrganizacaoRepository;
import br.com.nsfatima.gestao.organizacao.domain.repository.PastoralMovimentoOficialRepository;
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
    private final PastoralMovimentoOficialRepository pastoralOficialRepository;

    public OrganizacaoService(
            OrganizacaoRepository organizacaoRepository,
            AuditLogPersistenceService auditLogService,
            PastoralMovimentoOficialRepository pastoralOficialRepository) {
        this.organizacaoRepository = organizacaoRepository;
        this.auditLogService = auditLogService;
        this.pastoralOficialRepository = pastoralOficialRepository;
    }

    private static final String AUDIT_ACTOR = "admin";
    private static final String AUDIT_ACTION = "admin-action";
    private static final String AUDIT_TARGET = "organization";
    private static final String AUDIT_RESULT = "success";
    private static final String KEY_RESOURCE_TYPE = "resourceType";
    private static final String VALUE_RESOURCE_TYPE = "ORGANIZATION";
    private static final String KEY_RESOURCE_ID = "resourceId";
    private static final String KEY_ORGANIZACAO_ID = "organizacaoId";
    private static final String KEY_ACTION = "action";

    @Transactional
    public Organizacao createOrganization(String nome,
                                          TipoOrganizacao tipo, String contato, String coordenador,
                                          String viceCoordenador, UUID parentId,
                                          UUID pastoralOficialId) {

        validateHierarchy(null, tipo, parentId, pastoralOficialId);
        Organizacao org = new Organizacao(UUID.randomUUID(),
                nome, tipo, contato, coordenador, viceCoordenador, true, parentId, pastoralOficialId);

        organizacaoRepository.save(org);

        auditLogService.log(AUDIT_ACTOR, AUDIT_ACTION, AUDIT_TARGET, AUDIT_RESULT, Map.of(
                KEY_RESOURCE_TYPE, VALUE_RESOURCE_TYPE,
                KEY_RESOURCE_ID, org.getId(),
                KEY_ORGANIZACAO_ID, org.getId(),
                KEY_ACTION, "CREATE",
                "nome", nome
        ));

        return org;
    }

    @Transactional
    public Organizacao updateOrganization(UUID id,
                                          String nome, TipoOrganizacao tipo, String contato, String coordenador,
                                          String viceCoordenador,
                                          boolean ativo, UUID parentId, UUID pastoralOficialId) {

        Organizacao org = organizacaoRepository.findById(id)
                .orElseThrow(() -> OrganizationBusinessException.notFound(id));

        validateHierarchy(id, tipo, parentId, pastoralOficialId);

        org.update(nome, tipo, contato, coordenador, viceCoordenador, ativo, parentId, pastoralOficialId);
        organizacaoRepository.save(org);

        auditLogService.log(AUDIT_ACTOR, AUDIT_ACTION, AUDIT_TARGET, AUDIT_RESULT, Map.of(
                KEY_RESOURCE_TYPE, VALUE_RESOURCE_TYPE,
                KEY_RESOURCE_ID, id,
                KEY_ORGANIZACAO_ID, id,
                KEY_ACTION, "UPDATE"
        ));

        return org;
    }

    @Transactional
    public void deleteOrganization(UUID id) {
        if (!organizacaoRepository.findById(id).isPresent()) {
            throw OrganizationBusinessException.notFound(id);
        }

        if (organizacaoRepository.hasDependencies(id)) {
            throw OrganizationBusinessException.inUse(id);
        }

        organizacaoRepository.delete(id);

        auditLogService.log(AUDIT_ACTOR, AUDIT_ACTION, AUDIT_TARGET, AUDIT_RESULT, Map.of(
                KEY_RESOURCE_TYPE, VALUE_RESOURCE_TYPE,
                KEY_RESOURCE_ID, id,
                KEY_ORGANIZACAO_ID, id,
                KEY_ACTION, "DELETE"
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

    private void validateHierarchy(UUID id, TipoOrganizacao tipo, UUID parentId, UUID pastoralOficialId) {
        if (parentId != null && id != null && parentId.equals(id)) {
            throw new OrganizationBusinessException("An organization cannot be its own parent.");
        }

        if (tipo == TipoOrganizacao.CLERO) {
            if (parentId != null) {
                throw new OrganizationBusinessException("Clero organization cannot have a parent organization.");
            }
            return;
        }

        // All other canonical types MUST have a parent organization
        if (tipo == TipoOrganizacao.MATRIZ || tipo == TipoOrganizacao.COMUNIDADE ||
                tipo == TipoOrganizacao.CONSELHO_PASTORAL || tipo == TipoOrganizacao.CONSELHO_ECONOMICO ||
                tipo == TipoOrganizacao.PASTORAL || tipo == TipoOrganizacao.MOVIMENTO) {

            if (parentId == null) {
                throw new OrganizationBusinessException(tipo.name() + " organization must have a parent organization.");
            }
        }

        // Validate mandatory link to Pastoral/Movimento Oficial
        if (tipo == TipoOrganizacao.PASTORAL || tipo == TipoOrganizacao.MOVIMENTO) {
            if (pastoralOficialId == null) {
                throw new OrganizationBusinessException("Vínculo com Pastoral ou Movimento Oficial é obrigatório.");
            }
            PastoralMovimentoOficial oficial = pastoralOficialRepository.findById(pastoralOficialId)
                    .orElseThrow(() -> new OrganizationBusinessException(
                            "Pastoral ou Movimento Oficial não encontrado com ID: "
                                    + pastoralOficialId));
            if (oficial.getTipo() != tipo) {
                throw new OrganizationBusinessException(
                        "O tipo da Pastoral/Movimento Oficial (" + oficial.getTipo()
                                + ") não coincide com o tipo da organização (" + tipo + ").");
            }
        }

        if (parentId != null) {
            Organizacao parent = organizacaoRepository.findById(parentId)
                    .orElseThrow(() -> new OrganizationBusinessException(
                            "Parent organization not found with ID: " + parentId));

            if (!parent.isAtivo()) {
                throw new OrganizationBusinessException("Parent organization is not active.");
            }

            TipoOrganizacao parentTipo = parent.getTipo();

            switch (tipo) {
                case MATRIZ:
                case COMUNIDADE:
                    if (parentTipo != TipoOrganizacao.CLERO) {
                        throw new OrganizationBusinessException(
                                tipo.name() + " organization parent must be a CLERO organization.");
                    }
                    break;
                case CONSELHO_PASTORAL:
                case CONSELHO_ECONOMICO:
                    if (parentTipo != TipoOrganizacao.MATRIZ && parentTipo != TipoOrganizacao.COMUNIDADE) {
                        throw new OrganizationBusinessException(
                                tipo.name()
                                        + " organization parent must be a MATRIZ or COMUNIDADE organization.");
                    }
                    break;
                case PASTORAL:
                case MOVIMENTO:
                    if (parentTipo != TipoOrganizacao.CONSELHO_PASTORAL) {
                        throw new OrganizationBusinessException(
                                tipo.name()
                                        + " organization parent must be a CONSELHO_PASTORAL organization.");
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
