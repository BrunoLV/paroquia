package br.com.nsfatima.gestao.projeto.domain.policy;

import java.time.ZoneId;
import br.com.nsfatima.gestao.projeto.domain.model.ProjetoStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import br.com.nsfatima.gestao.calendario.domain.type.RegraRecorrencia;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Especifica o período de vínculo de eventos a um projeto pastoral. */
@DisplayName("Policy: vínculo de projeto")
class PeriodoProjetoVincularPolicyTest {

    private final PeriodoProjetoVincularPolicy policy =
            new PeriodoProjetoVincularPolicy(ZoneId.of("America/Sao_Paulo"));

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

    @Test
    @DisplayName("fim do projeto às 22h de 31/12 em Brasília (01h UTC de 01/01): limite de recorrência em 01/01 excede")
    void limiteDaRecorrenciaUsaODiaCivilDeBrasilia() {
        ProjetoVinculo projeto = new ProjetoVinculo("Missão", ProjetoStatus.ABERTO,
                Instant.parse("2026-01-01T03:00:00Z"), Instant.parse("2027-01-01T01:00:00Z"));
        Instant inicio = Instant.parse("2026-12-01T12:00:00Z");
        Instant fim = Instant.parse("2026-12-01T13:00:00Z");

        assertThatCode(() -> policy.validarRecorrencia(projeto, inicio, fim, regraAte(LocalDate.of(2026, 12, 31))))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> policy.validarRecorrencia(projeto, inicio, fim, regraAte(LocalDate.of(2027, 1, 1))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private RegraRecorrencia regraAte(LocalDate limite) {
        return new RegraRecorrencia("SEMANAL", 1, List.of(DayOfWeek.TUESDAY), null, null, null, null, limite, null);
    }

    private ProjetoVinculo projeto(ProjetoStatus status) {
        return new ProjetoVinculo("Missão", status,
                Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-12-31T23:59:59Z"));
    }
}
