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

    /**
     * Substitui todos os atributos mutáveis desta organização, incluindo reatribuição de organização-mãe e
     * vínculo com Pastoral/Movimento Oficial. Não valida a nova hierarquia — a validação de compatibilidade
     * entre tipo e organização-mãe é responsabilidade de {@code OrganizacaoService.validateHierarchy}, que
     * deve ser chamado antes de invocar este método.
     *
     * @param nome novo nome da organização
     * @param tipo novo tipo organizacional
     * @param contato novo contato
     * @param coordenador novo nome do coordenador
     * @param viceCoordenador novo nome do vice-coordenador
     * @param ativo novo estado de atividade
     * @param parentId novo identificador da organização-mãe, ou {@code null} se não houver
     * @param pastoralOficialId novo vínculo com o catálogo oficial de Pastorais/Movimentos, quando aplicável
     */
    public void update(String nome,
                       TipoOrganizacao tipo, String contato, String coordenador, String viceCoordenador, boolean ativo,
                       UUID parentId, UUID pastoralOficialId) {

        this.nome = nome;
        this.tipo = tipo;
        this.contato = contato;
        this.coordenador = coordenador;
        this.viceCoordenador = viceCoordenador;
        this.ativo = ativo;
        this.parentId = parentId;
        this.pastoralOficialId = pastoralOficialId;
    }
}
