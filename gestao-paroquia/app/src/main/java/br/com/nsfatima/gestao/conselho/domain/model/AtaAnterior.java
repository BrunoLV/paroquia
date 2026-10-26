package br.com.nsfatima.gestao.conselho.domain.model;

import br.com.nsfatima.gestao.conselho.domain.exception.AtaInvalidaException;

import java.util.Objects;
import java.util.UUID;

/**
 * Leitura e aprovação da ata da reunião anterior, primeiro ponto da pauta dos colegiados. Referencia a
 * ata por identificador (agregados se associam apenas por ID). Sem ata anterior, a aprovação é
 * {@link AprovacaoAtaAnterior#NAO_APLICAVEL}; aprovada com ressalvas, as ressalvas são obrigatórias.
 */
public record AtaAnterior(UUID ataId, AprovacaoAtaAnterior aprovacao, String ressalvas) {

    private static final int RESSALVAS_MAX = 2000;

    public AtaAnterior {
        Objects.requireNonNull(aprovacao, "A aprovação da ata anterior é obrigatória.");
        ressalvas = ressalvas == null || ressalvas.isBlank() ? null : ressalvas.trim();
        if (ataId == null) {
            if (aprovacao != AprovacaoAtaAnterior.NAO_APLICAVEL || ressalvas != null) {
                throw new AtaInvalidaException("Sem ata anterior informada, a aprovação não se aplica.");
            }
        } else {
            exigirAprovacaoCoerente(aprovacao, ressalvas);
        }
    }

    public static AtaAnterior nenhuma() {
        return new AtaAnterior(null, AprovacaoAtaAnterior.NAO_APLICAVEL, null);
    }

    private static void exigirAprovacaoCoerente(AprovacaoAtaAnterior aprovacao, String ressalvas) {
        if (aprovacao == AprovacaoAtaAnterior.NAO_APLICAVEL) {
            throw new AtaInvalidaException("Informe se a ata anterior foi aprovada.");
        }
        boolean comRessalvas = aprovacao == AprovacaoAtaAnterior.APROVADA_COM_RESSALVAS;
        if (comRessalvas && ressalvas == null) {
            throw new AtaInvalidaException("Informe as ressalvas feitas à ata anterior.");
        }
        if (!comRessalvas && ressalvas != null) {
            throw new AtaInvalidaException("Ressalvas só se aplicam à aprovação com ressalvas.");
        }
        if (ressalvas != null && ressalvas.length() > RESSALVAS_MAX) {
            throw new AtaInvalidaException("As ressalvas excedem " + RESSALVAS_MAX + " caracteres.");
        }
    }
}
