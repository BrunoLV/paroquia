package br.com.nsfatima.gestao.organizacao.domain.service;

import br.com.nsfatima.gestao.observabilidade.domain.service.AuditoriaPersistenceService;
import br.com.nsfatima.gestao.organizacao.domain.exception.OrganizacaoNegocioException;
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

/**
 * Orquestra o ciclo de vida de {@link Organizacao} (criação, atualização, exclusão e consulta), aplicando a
 * regra de hierarquia organizacional da paróquia — qual {@code TipoOrganizacao} pode ter qual tipo de
 * organização-mãe, e qual vínculo obrigatório com o catálogo oficial de Pastorais/Movimentos — e registrando
 * cada mutação na auditoria via {@code AuditoriaPersistenceService}. É o único ponto de entrada de escrita
 * para o agregado {@code Organizacao}; nenhuma outra classe deve mutar organizações diretamente.
 */
@Service
@Transactional
public class OrganizacaoService {

    private static final String AUDIT_ACTOR = "admin";
    private static final String AUDIT_ACTION = "acao-admin";
    private static final String AUDIT_TARGET = "organizacao";
    private static final String AUDIT_RESULT = "sucesso";
    private static final String KEY_RESOURCE_TYPE = "resourceType";
    private static final String VALUE_RESOURCE_TYPE = "ORGANIZACAO";
    private static final String KEY_RESOURCE_ID = "resourceId";
    private static final String KEY_ORGANIZACAO_ID = "organizacaoId";
    private static final String KEY_ACTION = "action";
    private final OrganizacaoRepository organizacaoRepository;
    private final AuditoriaPersistenceService auditLogService;
    private final PastoralMovimentoOficialRepository pastoralOficialRepository;
    public OrganizacaoService(
            OrganizacaoRepository organizacaoRepository,
            AuditoriaPersistenceService auditLogService,
            PastoralMovimentoOficialRepository pastoralOficialRepository) {
        this.organizacaoRepository = organizacaoRepository;
        this.auditLogService = auditLogService;
        this.pastoralOficialRepository = pastoralOficialRepository;
    }

    /**
     * Cria uma nova organização, validando previamente a hierarquia (tipo compatível com o tipo da
     * organização-mãe, vínculo obrigatório com o catálogo oficial para Pastoral/Movimento) e registrando a
     * criação na auditoria.
     *
     * @param nome nome da organização
     * @param tipo tipo organizacional
     * @param contato informação de contato
     * @param coordenador nome do coordenador
     * @param viceCoordenador nome do vice-coordenador
     * @param parentId identificador da organização-mãe, exigido para todos os tipos exceto {@code CLERO}
     * @param pastoralOficialId vínculo com o catálogo oficial, obrigatório para {@code PASTORAL}/{@code MOVIMENTO}
     * @return a organização recém-criada
     * @throws OrganizacaoNegocioException se a hierarquia informada violar as regras de tipo/organização-mãe
     */
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
                KEY_ACTION, "CRIAR",
                "nome", nome
        ));

        return org;
    }

    /**
     * Atualiza os dados de uma organização existente, revalidando a hierarquia com os novos valores e
     * registrando a atualização na auditoria.
     *
     * @param id identificador da organização a atualizar
     * @param nome novo nome
     * @param tipo novo tipo organizacional
     * @param contato novo contato
     * @param coordenador novo nome do coordenador
     * @param viceCoordenador novo nome do vice-coordenador
     * @param ativo novo estado de atividade
     * @param parentId novo identificador da organização-mãe
     * @param pastoralOficialId novo vínculo com o catálogo oficial, quando aplicável
     * @return a organização atualizada
     * @throws OrganizacaoNegocioException se a organização não existir ou a nova hierarquia for inválida
     */
    @Transactional
    public Organizacao updateOrganization(UUID id,
                                          String nome, TipoOrganizacao tipo, String contato, String coordenador,
                                          String viceCoordenador,
                                          boolean ativo, UUID parentId, UUID pastoralOficialId) {

        Organizacao org = organizacaoRepository.findById(id)
                .orElseThrow(() -> OrganizacaoNegocioException.notFound(id));

        validateHierarchy(id, tipo, parentId, pastoralOficialId);

        org.update(nome, tipo, contato, coordenador, viceCoordenador, ativo, parentId, pastoralOficialId);
        organizacaoRepository.save(org);

        auditLogService.log(AUDIT_ACTOR, AUDIT_ACTION, AUDIT_TARGET, AUDIT_RESULT, Map.of(
                KEY_RESOURCE_TYPE, VALUE_RESOURCE_TYPE,
                KEY_RESOURCE_ID, id,
                KEY_ORGANIZACAO_ID, id,
                KEY_ACTION, "ATUALIZAR"
        ));

        return org;
    }

    /**
     * Exclui definitivamente uma organização, impedindo a exclusão caso existam dependências ativas
     * (membros vinculados ou eventos associados) que ficariam órfãs, e registrando a exclusão na auditoria.
     *
     * @param id identificador da organização a excluir
     * @throws OrganizacaoNegocioException se a organização não existir ou possuir dependências ativas
     */
    @Transactional
    public void deleteOrganization(UUID id) {
        if (organizacaoRepository.findById(id).isEmpty()) {
            throw OrganizacaoNegocioException.notFound(id);
        }

        if (organizacaoRepository.hasDependencies(id)) {
            throw OrganizacaoNegocioException.inUse(id);
        }

        organizacaoRepository.delete(id);

        auditLogService.log(AUDIT_ACTOR, AUDIT_ACTION, AUDIT_TARGET, AUDIT_RESULT, Map.of(
                KEY_RESOURCE_TYPE, VALUE_RESOURCE_TYPE,
                KEY_RESOURCE_ID, id,
                KEY_ORGANIZACAO_ID, id,
                KEY_ACTION, "EXCLUIR"
        ));
    }

    /**
     * Busca uma organização pelo seu identificador.
     *
     * @param id identificador da organização
     * @return a organização encontrada, ou {@link Optional#empty()} se não existir
     */
    @Transactional(readOnly = true)
    public Optional<Organizacao> getOrganization(UUID id) {
        return organizacaoRepository.findById(id);
    }

    /**
     * Lista todas as organizações cadastradas, ativas ou inativas.
     *
     * @return lista de todas as organizações
     */
    @Transactional(readOnly = true)
    public List<Organizacao> listOrganizations() {
        return organizacaoRepository.findAll();
    }

    private void validateHierarchy(UUID id, TipoOrganizacao tipo, UUID parentId, UUID pastoralOficialId) {
        if (parentId != null && parentId.equals(id)) {
            throw new OrganizacaoNegocioException("Uma organização não pode ser sua própria organização-mãe.");
        }

        if (tipo == TipoOrganizacao.CLERO) {
            if (parentId != null) {
                throw new OrganizacaoNegocioException("Organização do tipo Clero não pode ter organização-mãe.");
            }
            return;
        }

        // All other canonical types MUST have a parent organization
        if (tipo == TipoOrganizacao.MATRIZ || tipo == TipoOrganizacao.COMUNIDADE ||
                tipo == TipoOrganizacao.CONSELHO_PASTORAL || tipo == TipoOrganizacao.CONSELHO_ECONOMICO ||
                tipo == TipoOrganizacao.PASTORAL || tipo == TipoOrganizacao.MOVIMENTO) {

            if (parentId == null) {
                throw new OrganizacaoNegocioException(tipo.name() + " deve ter uma organização-mãe.");
            }
        }

        // Validate mandatory link to Pastoral/Movimento Oficial
        if (tipo == TipoOrganizacao.PASTORAL || tipo == TipoOrganizacao.MOVIMENTO) {
            if (pastoralOficialId == null) {
                throw new OrganizacaoNegocioException("Vínculo com Pastoral ou Movimento Oficial é obrigatório.");
            }
            PastoralMovimentoOficial oficial = pastoralOficialRepository.findById(pastoralOficialId)
                    .orElseThrow(() -> new OrganizacaoNegocioException(
                            "Pastoral ou Movimento Oficial não encontrado com ID: "
                                    + pastoralOficialId));
            if (oficial.getTipo() != tipo) {
                throw new OrganizacaoNegocioException(
                        "O tipo da Pastoral/Movimento Oficial (" + oficial.getTipo()
                                + ") não coincide com o tipo da organização (" + tipo + ").");
            }
        }

        if (parentId != null) {
            Organizacao parent = organizacaoRepository.findById(parentId)
                    .orElseThrow(() -> new OrganizacaoNegocioException(
                            "Organização-mãe não encontrada com ID: " + parentId));

            if (!parent.isAtivo()) {
                throw new OrganizacaoNegocioException("Organização-mãe não está ativa.");
            }

            TipoOrganizacao parentTipo = parent.getTipo();

            switch (tipo) {
                case MATRIZ:
                case COMUNIDADE:
                    if (parentTipo != TipoOrganizacao.CLERO) {
                        throw new OrganizacaoNegocioException(
                                tipo.name() + " deve ter uma organização-mãe do tipo CLERO.");
                    }
                    break;
                case CONSELHO_PASTORAL:
                case CONSELHO_ECONOMICO:
                    if (parentTipo != TipoOrganizacao.MATRIZ && parentTipo != TipoOrganizacao.COMUNIDADE) {
                        throw new OrganizacaoNegocioException(
                                tipo.name()
                                        + " deve ter uma organização-mãe do tipo MATRIZ ou COMUNIDADE.");
                    }
                    break;
                case PASTORAL:
                case MOVIMENTO:
                    if (parentTipo != TipoOrganizacao.CONSELHO_PASTORAL) {
                        throw new OrganizacaoNegocioException(
                                tipo.name()
                                        + " deve ter uma organização-mãe do tipo CONSELHO_PASTORAL.");
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
