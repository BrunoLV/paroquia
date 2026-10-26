package br.com.nsfatima.gestao.organizacao.domain.model;

import java.util.UUID;

/**
 * Representa um nó da hierarquia organizacional da paróquia (Matriz, Comunidade, Pastoral, Movimento,
 * Conselho, Clero etc.), incluindo seu vínculo com a organização-mãe ({@code parentId}) e, quando aplicável,
 * com o catálogo oficial de Pastorais/Movimentos ({@code pastoralOficialId}). Esta é a raiz de agregado do
 * contexto {@code organizacao}: as regras de qual tipo pode ter qual pai são aplicadas em
 * {@code OrganizacaoService}, não nesta classe.
 */
public class Organizacao {
    private final UUID id;
    private String nome;
    private TipoOrganizacao tipo;
    private String contato;
    private String coordenador;
    private String viceCoordenador;
    private boolean ativo;
    private UUID parentId;
    private UUID pastoralOficialId;
    private SetorPastoral setorPastoral;
    private SubtipoConselho subtipoConselho;
    private String email;
    private String telefone;

    /**
     * Cria uma organização já existente (id definido) ou uma nova instância a ser persistida, sem aplicar
     * nenhuma validação de hierarquia — quem chama este construtor (tipicamente {@code OrganizacaoService} ou
     * a camada de persistência ao reidratar a entidade) é responsável por garantir a consistência do vínculo
     * com {@code parentId} e {@code pastoralOficialId}.
     */
    public Organizacao(UUID id,
                       String nome, TipoOrganizacao tipo, String contato, String coordenador, String viceCoordenador,
                       boolean ativo, UUID parentId, UUID pastoralOficialId) {

        this.id = id;
        this.nome = nome;
        this.tipo = tipo;
        this.contato = contato;
        this.coordenador = coordenador;
        this.viceCoordenador = viceCoordenador;
        this.ativo = ativo;
        this.parentId = parentId;
        this.pastoralOficialId = pastoralOficialId;
    }

    /**
     * Cria uma organização informando também a classificação eclesial: o múnus ({@code setorPastoral}) a que
     * se vincula e, para conselhos, a sua natureza ({@code subtipoConselho}).
     */
    public Organizacao(UUID id,
                       String nome, TipoOrganizacao tipo, String contato, String coordenador, String viceCoordenador,
                       boolean ativo, UUID parentId, UUID pastoralOficialId,
                       SetorPastoral setorPastoral, SubtipoConselho subtipoConselho) {
        this(id, nome, tipo, contato, coordenador, viceCoordenador, ativo, parentId, pastoralOficialId);
        this.setorPastoral = setorPastoral;
        this.subtipoConselho = subtipoConselho;
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public TipoOrganizacao getTipo() {
        return tipo;
    }

    public String getContato() {
        return contato;
    }

    public String getCoordenador() {
        return coordenador;
    }

    public String getViceCoordenador() {
        return viceCoordenador;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public UUID getParentId() {
        return parentId;
    }

    public void setParentId(UUID parentId) {
        this.parentId = parentId;
    }

    public UUID getPastoralOficialId() {
        return pastoralOficialId;
    }

    public void setPastoralOficialId(UUID pastoralOficialId) {
        this.pastoralOficialId = pastoralOficialId;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefone() {
        return telefone;
    }

    /**
     * Define o e-mail e o telefone institucionais da organização. O campo legado {@code contato} passa a
     * espelhar o e-mail ou, na falta dele, o telefone, para os consumidores que ainda leem um único contato.
     *
     * @param email e-mail institucional, ou {@code null}
     * @param telefone telefone institucional, ou {@code null}
     */
    public void definirContatos(String email, String telefone) {
        this.email = vazioParaNulo(email);
        this.telefone = vazioParaNulo(telefone);
        this.contato = this.email != null ? this.email : this.telefone;
    }

    private static String vazioParaNulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    public SetorPastoral getSetorPastoral() {
        return setorPastoral;
    }

    public SubtipoConselho getSubtipoConselho() {
        return subtipoConselho;
    }

    /**
     * Define a classificação eclesial desta organização: o múnus (tríplice missão) e, para conselhos, a
     * natureza do conselho. A obrigatoriedade de cada um por tipo é decidida pelas policies de tipologia.
     *
     * @param setorPastoral múnus a que a organização serve, ou {@code null} quando não se aplica
     * @param subtipoConselho natureza do conselho, ou {@code null} quando a organização não é um conselho
     */
    public void classificar(SetorPastoral setorPastoral, SubtipoConselho subtipoConselho) {
        this.setorPastoral = setorPastoral;
        this.subtipoConselho = subtipoConselho;
    }

    /**
     * Substitui os atributos cadastrais desta organização (fluxo de PUT), incluindo reatribuição de
     * organização-mãe e vínculo com Pastoral/Movimento Oficial. A compatibilidade da hierarquia é
     * responsabilidade de {@code HierarquiaOrganizacionalPolicy}, invocada antes deste método.
     *
     * @param nome novo nome da organização
     * @param tipo novo tipo organizacional
     * @param contato novo contato
     * @param ativo novo estado de atividade
     * @param parentId novo identificador da organização-mãe, ou {@code null} se não houver
     * @param pastoralOficialId novo vínculo com o catálogo oficial de Pastorais/Movimentos, quando aplicável
     */
    public void alterarCadastro(String nome, TipoOrganizacao tipo, String contato, boolean ativo,
                       UUID parentId, UUID pastoralOficialId) {

        this.nome = nome;
        this.tipo = tipo;
        this.contato = contato;
        this.ativo = ativo;
        this.parentId = parentId;
        this.pastoralOficialId = pastoralOficialId;
    }
}
