package br.com.nsfatima.gestao.organizacao.domain.model;

import java.util.UUID;

/**
 * Representa um item do catálogo oficial de Pastorais e Movimentos da paróquia (ex.: "Pastoral da Criança",
 * "Renovação Carismática"). Diferente de {@link Organizacao}, que modela a instância concreta de uma pastoral
 * ou movimento numa comunidade específica, esta classe é o registro-mestre único ao qual cada
 * {@code Organizacao} do tipo {@code PASTORAL}/{@code MOVIMENTO} deve se vincular via
 * {@code pastoralOficialId}, garantindo que o nome/tipo oficial seja consistente em todas as comunidades.
 */
public class PastoralMovimentoOficial {
    private final UUID id;
    private String nome;
    private TipoOrganizacao tipo;
    private AmbitoOrganismo ambito;
    private ReconhecimentoEstatuto reconhecimento;

    public PastoralMovimentoOficial(UUID id, String nome, TipoOrganizacao tipo) {
        this(id, nome, tipo, null, null);
    }

    public PastoralMovimentoOficial(UUID id, String nome, TipoOrganizacao tipo,
                                    AmbitoOrganismo ambito, ReconhecimentoEstatuto reconhecimento) {
        this.id = id;
        this.nome = nome;
        this.tipo = tipo;
        this.ambito = ambito;
        this.reconhecimento = reconhecimento;
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

    /**
     * Atualiza o nome e o tipo deste item do catálogo oficial. A unicidade do nome e a restrição de tipo
     * (deve ser {@code PASTORAL} ou {@code MOVIMENTO}) são validadas por
     * {@code PastoralMovimentoOficialService}, não por este método.
     *
     * @param nome novo nome oficial da pastoral/movimento
     * @param tipo novo tipo ({@code PASTORAL} ou {@code MOVIMENTO})
     */
    public void atualizar(String nome, TipoOrganizacao tipo) {
        this.nome = nome;
        this.tipo = tipo;
    }

    public AmbitoOrganismo getAmbito() {
        return ambito;
    }

    public ReconhecimentoEstatuto getReconhecimento() {
        return reconhecimento;
    }

    /**
     * Registra o âmbito e o reconhecimento formal dos estatutos pela autoridade eclesiástica (Cân. 299 e 304).
     * Passar {@code null} em {@code reconhecimento} remove o reconhecimento.
     *
     * @param ambito âmbito de origem do organismo
     * @param reconhecimento reconhecimento dos estatutos, ou {@code null}
     */
    public void registrarReconhecimento(AmbitoOrganismo ambito, ReconhecimentoEstatuto reconhecimento) {
        this.ambito = ambito;
        this.reconhecimento = reconhecimento;
    }

    /**
     * Indica se os estatutos deste organismo têm reconhecimento formal registrado.
     *
     * @return {@code true} quando há reconhecimento
     */
    public boolean possuiEstatutoReconhecido() {
        return reconhecimento != null;
    }
}
