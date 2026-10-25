package br.com.nsfatima.gestao.conselho.domain.model;

import br.com.nsfatima.gestao.conselho.domain.exception.AtaInvalidaException;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

/**
 * Funções que uma pessoa exerce na reunião, em lista fechada para que a ata e os documentos usem sempre o
 * mesmo vocabulário. As funções {@code unica} (presidência, secretaria e afins) só podem ser exercidas por uma
 * pessoa por reunião; as demais podem se repetir.
 */
public enum FuncaoPresenca {
    PAROCO("Pároco", true),
    VIGARIO_PAROQUIAL("Vigário paroquial", true),
    DIACONO("Diácono", false),
    PRESIDENTE("Presidente", true),
    VICE_PRESIDENTE("Vice-presidente", true),
    SECRETARIO("Secretário(a)", true),
    TESOUREIRO("Tesoureiro(a)", true),
    COORDENADOR("Coordenador(a)", false),
    REPRESENTANTE("Representante de pastoral ou movimento", false),
    MEMBRO("Membro", false),
    CONVIDADO("Convidado(a)", false);

    private final String rotulo;
    private final boolean unica;

    FuncaoPresenca(String rotulo, boolean unica) {
        this.rotulo = rotulo;
        this.unica = unica;
    }

    public String getRotulo() {
        return rotulo;
    }

    /** Indica se só uma pessoa pode exercer a função na mesma reunião. */
    public boolean ehUnica() {
        return unica;
    }

    /**
     * Resolve a função pelo rótulo gravado na ata.
     *
     * @throws AtaInvalidaException se o rótulo não pertence à lista
     */
    public static FuncaoPresenca doRotulo(String rotulo) {
        return Arrays.stream(values()).filter(f -> f.rotulo.equals(rotulo)).findFirst()
                .orElseThrow(() -> new AtaInvalidaException("A função \"" + rotulo + "\" não é uma função válida."));
    }

    /**
     * Função que o redator exerce na reunião que redige: quem é secretário (do conselho ou do grupo) consta como
     * Secretário(a); o coordenador, como Coordenador(a).
     *
     * @param papeisDoRedator papéis do redator no órgão convocante
     * @param papeisRedatores papéis que o tipo de reunião admite como redatores
     * @return a função, ou vazio se o redator não tem papel redator no órgão
     */
    public static Optional<FuncaoPresenca> doRedator(Set<PapelOrganizacional> papeisDoRedator,
                                                     Set<PapelOrganizacional> papeisRedatores) {
        boolean secretario = papeisDoRedator.stream().filter(papeisRedatores::contains)
                .anyMatch(p -> p == PapelOrganizacional.SECRETARIO_CONSELHO
                        || p == PapelOrganizacional.SECRETARIO_GRUPO);
        if (secretario) {
            return Optional.of(SECRETARIO);
        }
        return papeisDoRedator.stream().filter(papeisRedatores::contains)
                .anyMatch(p -> p == PapelOrganizacional.COORDENADOR) ? Optional.of(COORDENADOR) : Optional.empty();
    }
}
