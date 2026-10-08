package br.com.nsfatima.gestao.organizacao.domain.model;

/**
 * Encargo de coordenação exercido por um fiel numa organização. Cada função corresponde ao papel
 * organizacional que o vínculo do fiel assume enquanto o mandato estiver vigente.
 */
public enum FuncaoCoordenacao {
    COORDENADOR(PapelOrganizacional.COORDENADOR),
    VICE_COORDENADOR(PapelOrganizacional.SECRETARIO_GRUPO);

    private final PapelOrganizacional papel;

    FuncaoCoordenacao(PapelOrganizacional papelEquivalente) {
        this.papel = papelEquivalente;
    }

    /**
     * Papel que o vínculo do fiel assume enquanto exerce esta função.
     *
     * @return o papel organizacional equivalente
     */
    public PapelOrganizacional papelEquivalente() {
        return papel;
    }
}
