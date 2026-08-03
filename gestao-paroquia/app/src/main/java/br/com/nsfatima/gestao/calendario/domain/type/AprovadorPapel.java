package br.com.nsfatima.gestao.calendario.domain.type;

import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;

import java.util.Locale;

/**
 * Papel que qualifica quem pode decidir uma solicitação de aprovação de evento
 * (ver {@code ValidateAprovacaoUseCase}/{@code AprovacaoObrigatoriaException}).
 * Apenas usuários que ocupam um destes papéis, na organização correta, têm
 * autoridade para aprovar/reprovar a solicitação vinculada a um evento.
 */
public enum AprovadorPapel {
    /** Pároco (ou Bispo Diocesano, tratado como equivalente) — aprovador de maior hierarquia. */
    PAROCO("paroco", "Pároco"),
    /** Coordenador do conselho pastoral responsável pela organização do evento. */
    COORD_CONSELHO("conselho-coordenador", "Coordenador do Conselho"),
    /** Vice-coordenador ou membro do conselho pastoral, quando não há coordenador titular disponível. */
    VICE_COORD_CONSELHO("conselho-vice-coordenador", "Vice-Coordenador do Conselho");

    private final String storedCode;
    private final String descricao;

    AprovadorPapel(String storedCode, String descricao) {
        this.storedCode = storedCode;
        this.descricao = descricao;
    }

    /**
     * Resolve o código persistido (ou o nome da constante) para o papel de aprovador
     * correspondente, tolerando variações de separador (hífen ou sublinhado) e caixa.
     *
     * @param rawValue código bruto persistido ou informado
     * @return papel de aprovador correspondente, ou {@code null} se não reconhecido ou vazio
     */
    public static AprovadorPapel fromStoredValue(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }

        String normalized = rawValue.trim().toLowerCase(Locale.ROOT).replace("_", "-");

        if ("conselho-coordenador".equals(normalized) || "conselho_coordenador".equalsIgnoreCase(rawValue.trim())) {
            return COORD_CONSELHO;
        }
        if ("conselho-vice-coordenador".equals(normalized) || "conselho_vice_coordenador".equalsIgnoreCase(rawValue.trim())) {
            return VICE_COORD_CONSELHO;
        }

        for (AprovadorPapel value : values()) {
            if (value.storedCode.equals(normalized) || value.name().equalsIgnoreCase(rawValue.trim())) {
                return value;
            }
        }
        return null;
    }

    /**
     * Determina qual papel de aprovador corresponde a um usuário, a partir do seu
     * papel organizacional e do tipo de organização à qual pertence. Pároco e Bispo
     * Diocesano sempre resolvem para {@code PAROCO}; membros/vice-coordenadores de
     * conselho pastoral (CPP/CPC) resolvem para {@code VICE_COORD_CONSELHO}; todos os
     * demais casos assumem {@code COORD_CONSELHO} como papel padrão.
     *
     * @param role código do papel organizacional do usuário
     * @param organizationType código do tipo de organização à qual o usuário pertence
     * @return papel de aprovador correspondente à combinação de papel e tipo de organização
     */
    public static AprovadorPapel resolveForAprovacao(String role, String organizationType) {
        PapelOrganizacional papel = PapelOrganizacional.fromStoredValue(role);
        TipoOrganizacao tipoOrganizacao = TipoOrganizacao.fromStoredValue(organizationType);

        if (papel == PapelOrganizacional.PAROCO || papel == PapelOrganizacional.BISPO_DIOCESANO) {
            return PAROCO;
        }
        boolean isViceOrMembroCpp = papel == PapelOrganizacional.MEMBRO_CPP
                || papel == PapelOrganizacional.MEMBRO_CPC
                || papel == PapelOrganizacional.MESA_CPP;
        if ((tipoOrganizacao == TipoOrganizacao.CONSELHO_PASTORAL || tipoOrganizacao == TipoOrganizacao.CPP || tipoOrganizacao == TipoOrganizacao.CPC)
                && isViceOrMembroCpp) {
            return VICE_COORD_CONSELHO;
        }

        return COORD_CONSELHO;
    }

    /**
     * Retorna o código persistido correspondente ao papel de aprovador.
     *
     * @return código persistido do papel
     */
    public String storedValue() {
        return storedCode;
    }

    /**
     * Retorna a descrição legível do papel de aprovador.
     *
     * @return descrição legível do papel
     */
    public String getDescricao() {
        return descricao;
    }
}
