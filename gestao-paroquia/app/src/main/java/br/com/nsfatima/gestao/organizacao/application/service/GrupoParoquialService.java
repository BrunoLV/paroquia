package br.com.nsfatima.gestao.organizacao.application.service;

import br.com.nsfatima.gestao.observabilidade.application.service.AuditoriaPersistenceService;
import br.com.nsfatima.gestao.organizacao.domain.exception.GrupoParoquialNegocioException;
import br.com.nsfatima.gestao.organizacao.domain.model.GrupoParoquial;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
import br.com.nsfatima.gestao.organizacao.domain.repository.GrupoParoquialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Orquestra o ciclo de vida de {@link GrupoParoquial} (criação, atualização, exclusão e consulta), aplicando a
 * regra de hierarquia organizacional da paróquia — qual {@code TipoGrupo} pode ter qual tipo de
 * organização-mãe, e qual vínculo obrigatório com o catálogo oficial de Pastorais/Movimentos — e registrando
 * cada mutação na auditoria via {@code AuditoriaPersistenceService}. É o único ponto de entrada de escrita
 * para o agregado {@code GrupoParoquial}; nenhuma outra classe deve mutar organizações diretamente.
 */
@Service
@Transactional
public class GrupoParoquialService {

    private static final String AUDIT_ACTOR = "admin";
    private static final String AUDIT_ACTION = "acao-admin";
    private static final String AUDIT_TARGET = "grupoParoquial";
    private static final String AUDIT_RESULT = "sucesso";
    private static final String KEY_RESOURCE_TYPE = "resourceType";
    private static final String VALUE_RESOURCE_TYPE = "GRUPO_PAROQUIAL";
    private static final String KEY_RESOURCE_ID = "resourceId";
    private static final String KEY_GRUPO_PAROQUIAL_ID = "grupoParoquialId";
    private static final String KEY_ACTION = "action";
    private final GrupoParoquialRepository grupoParoquialRepository;
    private final AuditoriaPersistenceService auditLogService;
    private final ValidadorHierarquiaOrganizacional hierarquiaPolicy;

    public GrupoParoquialService(
            GrupoParoquialRepository grupoParoquialRepository,
            AuditoriaPersistenceService auditLogService,
            ValidadorHierarquiaOrganizacional hierarquiaPolicy) {
        this.grupoParoquialRepository = grupoParoquialRepository;
        this.auditLogService = auditLogService;
        this.hierarquiaPolicy = hierarquiaPolicy;
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
     * @throws GrupoParoquialNegocioException se a hierarquia informada violar as regras de tipo/organização-mãe
     */
    @Transactional
    public GrupoParoquial criar(String nome,
                                TipoGrupo tipo, String contato, String coordenador,
                                String viceCoordenador, UUID parentId,
                                UUID pastoralOficialId) {

        hierarquiaPolicy.validar(null, tipo, parentId, pastoralOficialId);
        GrupoParoquial org = new GrupoParoquial(UUID.randomUUID(),
                nome, tipo, contato, coordenador, viceCoordenador, true, parentId, pastoralOficialId);

        grupoParoquialRepository.save(org);

        auditLogService.log(AUDIT_ACTOR, AUDIT_ACTION, AUDIT_TARGET, AUDIT_RESULT, Map.of(
                KEY_RESOURCE_TYPE, VALUE_RESOURCE_TYPE,
                KEY_RESOURCE_ID, org.getId(),
                KEY_GRUPO_PAROQUIAL_ID, org.getId(),
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
     * @throws GrupoParoquialNegocioException se a organização não existir ou a nova hierarquia for inválida
     */
    @Transactional
    public GrupoParoquial atualizar(UUID id,
                                   String nome, TipoGrupo tipo, String contato, String coordenador,
                                   String viceCoordenador,
                                   boolean ativo, UUID parentId, UUID pastoralOficialId) {

        GrupoParoquial org = grupoParoquialRepository.findById(id)
                .orElseThrow(() -> GrupoParoquialNegocioException.notFound(id));

        hierarquiaPolicy.validar(id, tipo, parentId, pastoralOficialId);

        org.alterarCadastro(nome, tipo, contato, coordenador, viceCoordenador, ativo, parentId, pastoralOficialId);
        grupoParoquialRepository.save(org);

        auditLogService.log(AUDIT_ACTOR, AUDIT_ACTION, AUDIT_TARGET, AUDIT_RESULT, Map.of(
                KEY_RESOURCE_TYPE, VALUE_RESOURCE_TYPE,
                KEY_RESOURCE_ID, id,
                KEY_GRUPO_PAROQUIAL_ID, id,
                KEY_ACTION, "ATUALIZAR"
        ));

        return org;
    }

    /**
     * Exclui definitivamente uma organização, impedindo a exclusão caso existam dependências ativas
     * (membros vinculados ou eventos associados) que ficariam órfãs, e registrando a exclusão na auditoria.
     *
     * @param id identificador da organização a excluir
     * @throws GrupoParoquialNegocioException se a organização não existir ou possuir dependências ativas
     */
    @Transactional
    public void excluir(UUID id) {
        if (grupoParoquialRepository.findById(id).isEmpty()) {
            throw GrupoParoquialNegocioException.notFound(id);
        }

        if (grupoParoquialRepository.hasDependencies(id)) {
            throw GrupoParoquialNegocioException.inUse(id);
        }

        grupoParoquialRepository.delete(id);

        auditLogService.log(AUDIT_ACTOR, AUDIT_ACTION, AUDIT_TARGET, AUDIT_RESULT, Map.of(
                KEY_RESOURCE_TYPE, VALUE_RESOURCE_TYPE,
                KEY_RESOURCE_ID, id,
                KEY_GRUPO_PAROQUIAL_ID, id,
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
    public Optional<GrupoParoquial> obterPorId(UUID id) {
        return grupoParoquialRepository.findById(id);
    }

    /**
     * Lista todas as organizações cadastradas, ativas ou inativas.
     *
     * @return lista de todas as organizações
     */
    @Transactional(readOnly = true)
    public List<GrupoParoquial> listar() {
        return grupoParoquialRepository.findAll();
    }
}
