package br.com.nsfatima.gestao.projeto.domain.policy;

import br.com.nsfatima.gestao.projeto.domain.model.ProjetoStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Especifica o período de vínculo de eventos a um projeto pastoral. */
@DisplayName("Policy: vínculo de projeto")
class PeriodoProjetoVincularPolicyTest {

    private final PeriodoProjetoVincularPolicy policy = new PeriodoProjetoVincularPolicy();

    @Test
    @DisplayName("aceita evento contido no período de projeto aberto")
    void aceitaEventoContidoNoPeriodo() {
        ProjetoVinculo projeto = projeto(ProjetoStatus.ABERTO);
        assertThatCode(() -> policy.validarEvento(projeto,
                Instant.parse("2026-02-01T00:00:00Z"), Instant.parse("2026-02-02T00:00:00Z")))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("rejeita vínculo com projeto terminal")
    void rejeitaVinculoComProjetoTerminal() {
        assertThatThrownBy(() -> policy.validarEvento(projeto(ProjetoStatus.CONCLUIDO),
                Instant.parse("2026-02-01T00:00:00Z"), Instant.parse("2026-02-02T00:00:00Z")))
                .isInstanceOf(IllegalStateException.class);
    }

    private ProjetoVinculo projeto(ProjetoStatus status) {
        return new ProjetoVinculo("Missão", status,
                Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-12-31T23:59:59Z"));
    }
}
