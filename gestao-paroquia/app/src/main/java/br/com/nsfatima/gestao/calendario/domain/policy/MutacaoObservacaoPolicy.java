package br.com.nsfatima.gestao.calendario.domain.policy;

import br.com.nsfatima.gestao.calendario.domain.type.TipoObservacaoInput;

import java.util.UUID;

/** Define a regra de edição manual de observações sem conhecer persistência. */
public interface MutacaoObservacaoPolicy {

    void assertCriacaoManualPermitida(TipoObservacaoInput tipo);

    void assertEdicaoOuExclusaoPermitida(ObservacaoRegistrada observacao, UUID solicitanteId);
}
