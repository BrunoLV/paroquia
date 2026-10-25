package br.com.nsfatima.gestao.organizacao.application.service;

import br.com.nsfatima.gestao.observabilidade.application.service.AuditoriaPersistenceService;
import br.com.nsfatima.gestao.organizacao.domain.exception.OrganizacaoNegocioException;
import br.com.nsfatima.gestao.organizacao.domain.model.Organizacao;
import br.com.nsfatima.gestao.organizacao.domain.model.SetorPastoral;
import br.com.nsfatima.gestao.organizacao.domain.model.SubtipoConselho;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.domain.repository.MandatoCoordenacaoRepository;
import br.com.nsfatima.gestao.organizacao.domain.repository.OrganizacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
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
    private final ValidadorHierarquiaOrganizacional hierarquiaPolicy;
    private final MandatoCoordenacaoRepository mandatoRepository;
    private final Clock clock;

    public OrganizacaoService(
            OrganizacaoRepository organizacaoRepository,
            AuditoriaPersistenceService auditLogService,
            ValidadorHierarquiaOrganizacional hierarquiaPolicy,
            MandatoCoordenacaoRepository mandatoRepository,
            Clock clock) {
        this.organizacaoRepository = organizacaoRepository;
        this.auditLogService = auditLogService;
        this.hierarquiaPolicy = hierarquiaPolicy;
        this.mandatoRepository = mandatoRepository;
        this.clock = clock;
    }

    /**
     * Cria uma nova organização, validando previamente a hierarquia (tipo compatível com o tipo da
     * organização-mãe, vínculo obrigatório com o catálogo oficial para Pastoral/Movimento) e registrando a
     * criação na auditoria.
     *
     * @param nome nome da organização
     * @param tipo tipo organizacional
     * @param contato informação de contato
     * @param parentId identificador da organização-mãe, exigido para todos os tipos exceto {@code CLERO}
     * @param pastoralOficialId vínculo com o catálogo oficial, obrigatório para {@code PASTORAL}/{@code MOVIMENTO}
     * @return a organização recém-criada
     * @throws OrganizacaoNegocioException se a hierarquia informada violar as regras de tipo/organização-mãe
     */
    @Transactional
    public Organizacao criar(String nome, TipoOrganizacao tipo, String contato, UUID parentId,
                                UUID pastoralOficialId) {
        return criar(nome, tipo, contato, parentId, pastoralOficialId, null, null);
    }

    /**
     * Cria uma nova organização com classificação eclesial (múnus e, para conselhos, subtipo). As invariantes
     * da tipologia são avaliadas por {@code RegrasTipologiaOrganizacao} antes da persistência.
     *
     * @param setorPastoral múnus a que a organização serve (obrigatório para pastoral e serviço paroquial)
     * @param subtipoConselho natureza do conselho (obrigatória quando {@code tipo} é {@code CONSELHO})
     * @return a organização recém-criada
     * @throws OrganizacaoNegocioException se a tipologia ou a hierarquia forem violadas
     */
    @Transactional
    public Organizacao criar(String nome, TipoOrganizacao tipo, String contato, UUID parentId,
                                UUID pastoralOficialId, SetorPastoral setorPastoral,
                                SubtipoConselho subtipoConselho) {
        return criar(nome, tipo, contato, parentId, pastoralOficialId, setorPastoral, subtipoConselho, null, null);
    }

    /**
     * Cria uma nova organização com e-mail e telefone institucionais separados.
     *
     * @param email e-mail institucional; quando ambos forem nulos, {@code contato} legado é classificado
     * @param telefone telefone institucional
     * @return a organização recém-criada
     * @throws OrganizacaoNegocioException se a tipologia ou a hierarquia forem violadas
     */
    @Transactional
    public Organizacao criar(String nome, TipoOrganizacao tipo, String contato, UUID parentId,
                                UUID pastoralOficialId, SetorPastoral setorPastoral,
                                SubtipoConselho subtipoConselho, String email, String telefone) {

        hierarquiaPolicy.validar(null, tipo, parentId, pastoralOficialId, setorPastoral, subtipoConselho);
        // a coordenação só nasce de mandato homologado: o espelho começa vazio
        Organizacao org = new Organizacao(UUID.randomUUID(),
                nome, tipo, contato, null, null, true, parentId, pastoralOficialId,
                setorPastoral, subtipoConselho);
        aplicarContatos(org, contato, email, telefone);

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
     * @param ativo novo estado de atividade
     * @param parentId novo identificador da organização-mãe
     * @param pastoralOficialId novo vínculo com o catálogo oficial, quando aplicável
     * @return a organização atualizada
     * @throws OrganizacaoNegocioException se a organização não existir ou a nova hierarquia for inválida
     */
    @Transactional
    public Organizacao atualizar(UUID id,
                                   String nome, TipoOrganizacao tipo, String contato,
                                   boolean ativo, UUID parentId, UUID pastoralOficialId) {
        Organizacao atual = organizacaoRepository.findById(id)
                .orElseThrow(() -> OrganizacaoNegocioException.notFound(id));
        return atualizar(id, nome, tipo, contato, ativo, parentId,
                pastoralOficialId, atual.getSetorPastoral(), atual.getSubtipoConselho());
    }

    /**
     * Atualiza os dados de uma organização existente, incluindo a classificação eclesial, revalidando a
     * tipologia e a hierarquia com os novos valores.
     *
     * @param setorPastoral múnus a que a organização serve
     * @param subtipoConselho natureza do conselho, quando aplicável
     * @return a organização atualizada
     * @throws OrganizacaoNegocioException se a organização não existir ou violar a tipologia/hierarquia
     */
    @Transactional
    public Organizacao atualizar(UUID id,
                                   String nome, TipoOrganizacao tipo, String contato,
                                   boolean ativo, UUID parentId, UUID pastoralOficialId,
                                   SetorPastoral setorPastoral, SubtipoConselho subtipoConselho) {
        return atualizar(id, nome, tipo, contato, ativo, parentId,
                pastoralOficialId, setorPastoral, subtipoConselho, null, null);
    }

    /**
     * Atualiza uma organização com e-mail e telefone institucionais separados.
     *
     * @param email e-mail institucional; quando ambos forem nulos, {@code contato} legado é classificado
     * @param telefone telefone institucional
     * @return a organização atualizada
     * @throws OrganizacaoNegocioException se a organização não existir ou violar a tipologia/hierarquia
     */
    @Transactional
    public Organizacao atualizar(UUID id,
                                   String nome, TipoOrganizacao tipo, String contato,
                                   boolean ativo, UUID parentId, UUID pastoralOficialId,
                                   SetorPastoral setorPastoral, SubtipoConselho subtipoConselho,
                                   String email, String telefone) {

        Organizacao org = organizacaoRepository.findById(id)
                .orElseThrow(() -> OrganizacaoNegocioException.notFound(id));

        hierarquiaPolicy.validar(id, tipo, parentId, pastoralOficialId, setorPastoral, subtipoConselho);
        if (org.isAtivo() && !ativo && mandatoRepository.existeVigente(id, LocalDate.now(clock))) {
            throw new OrganizacaoNegocioException(
                    "Encerre os mandatos de coordenação vigentes antes de desativar a organização.");
        }

        org.alterarCadastro(nome, tipo, contato, ativo, parentId, pastoralOficialId);
        org.classificar(setorPastoral, subtipoConselho);
        aplicarContatos(org, contato, email, telefone);
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
    public void excluir(UUID id) {
        if (organizacaoRepository.findById(id).isEmpty()) {
            throw OrganizacaoNegocioException.notFound(id);
        }

        if (organizacaoRepository.hasDependencies(id) || !mandatoRepository.findByOrganizacaoId(id).isEmpty()) {
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
    public Optional<Organizacao> obterPorId(UUID id) {
        return organizacaoRepository.findById(id);
    }

    /**
     * Lista todas as organizações cadastradas, ativas ou inativas.
     *
     * @return lista de todas as organizações
     */
    @Transactional(readOnly = true)
    public List<Organizacao> listar() {
        return organizacaoRepository.findAll();
    }

    /**
     * Define e-mail e telefone; sem nenhum dos dois, classifica o {@code contato} legado (com {@code @} é
     * e-mail, caso contrário telefone).
     */
    private void aplicarContatos(Organizacao org, String contato, String email, String telefone) {
        boolean semContatos = (email == null || email.isBlank()) && (telefone == null || telefone.isBlank());
        if (semContatos && contato != null && !contato.isBlank()) {
            boolean ehEmail = contato.contains("@");
            org.definirContatos(ehEmail ? contato : null, ehEmail ? null : contato);
        } else {
            org.definirContatos(email, telefone);
        }
    }
}
