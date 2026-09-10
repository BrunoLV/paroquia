package br.com.nsfatima.gestao.calendario.domain.policy;

import br.com.nsfatima.gestao.calendario.domain.exception.ObservacaoAutorInvalidoException;
import br.com.nsfatima.gestao.calendario.domain.type.TipoObservacaoInput;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Especifica que somente o autor pode alterar uma nota manual ainda ativa. */
@DisplayName("Policy: mutação de observação manual")
class NotaManualMutacaoPolicyTest {

    private final NotaManualMutacaoPolicy policy = new NotaManualMutacaoPolicy();

    @Test
    @DisplayName("permite nota manual para seu próprio autor")
    void permiteNotaManualParaSeuProprioAutor() {
        UUID autor = UUID.randomUUID();
        assertThatCode(() -> {
            policy.assertCriacaoManualPermitida(TipoObservacaoInput.NOTA);
            policy.assertEdicaoOuExclusaoPermitida(new ObservacaoRegistrada("NOTA", false, autor), autor);
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("rejeita mutação por usuário que não é autor")
    void rejeitaMutacaoPorUsuarioQueNaoEAutor() {
        assertThatThrownBy(() -> policy.assertEdicaoOuExclusaoPermitida(
                new ObservacaoRegistrada("NOTA", false, UUID.randomUUID()), UUID.randomUUID()))
                .isInstanceOf(ObservacaoAutorInvalidoException.class);
    }
}
