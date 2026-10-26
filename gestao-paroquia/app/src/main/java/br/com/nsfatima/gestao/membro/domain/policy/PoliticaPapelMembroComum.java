package br.com.nsfatima.gestao.membro.domain.policy;

import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;

import java.util.Set;

/**
 * O cadastro corrente de membros ({@code /membros}) serve à participação comum: voluntário, membro ativo e
 * secretário de grupo. Papéis de liderança (coordenador) e de conselho conferem acesso administrativo e
 * dependem de provisão do Pároco, idoneidade comprovada e limite de coordenação, que só a promoção
 * ({@code PromoverACoordenadorUseCase}) e o vínculo administrativo ({@code AdicionarVinculoUseCase}) verificam.
 * Por isso o cadastro corrente não pode conferi-los, sob pena de contornar essas garantias.
 *
 * <p>Stateless e sem I/O.</p>
 */
public final class PoliticaPapelMembroComum {

    private static final Set<PapelOrganizacional> PAPEIS_COMUNS = Set.of(
            PapelOrganizacional.VOLUNTARIO, PapelOrganizacional.MEMBRO_ATIVO);

    private PoliticaPapelMembroComum() {
    }

    /**
     * Indica se o papel pode ser conferido pelo cadastro corrente de membros.
     *
     * @param papel papel pretendido
     * @return {@code true} para voluntário e membro ativo
     */
    public static boolean ehComum(PapelOrganizacional papel) {
        return papel != null && PAPEIS_COMUNS.contains(papel);
    }

    /**
     * Exige que o papel seja de participação comum.
     *
     * @param papel papel pretendido
     * @throws IllegalArgumentException se o papel for de liderança ou de conselho
     */
    public static void exigirComum(PapelOrganizacional papel) {
        if (!ehComum(papel)) {
            throw new IllegalArgumentException("O papel " + (papel == null ? "informado" : papel.getDescricao())
                    + " não pode ser atribuído no "
                    + "cadastro de membros; use a promoção pelo Pároco ou o vínculo administrativo da organização.");
        }
    }
}
