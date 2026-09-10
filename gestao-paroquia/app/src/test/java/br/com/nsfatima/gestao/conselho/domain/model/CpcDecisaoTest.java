package br.com.nsfatima.gestao.conselho.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpcDecisaoTest {

    private static final Instant AGORA = Instant.parse("2026-06-01T12:00:00Z");

    private CpcDecisao pendente() {
        return CpcDecisao.registrar(UUID.randomUUID(), "Reforma", "Orçamento de pintura",
                LocalDate.of(2026, 5, 1), UUID.randomUUID(), "ator-1", "ADMIN", AGORA);
    }

    @Test
    void registrarNasceoPendente() {
        assertThat(pendente().getStatusHomologacao()).isEqualTo(StatusHomologacao.PENDENTE_HOMOLOGACAO);
    }

    @Test
    void homologarRegistraHomologadorEData() {
        CpcDecisao d = pendente();
        d.homologar("homologador-1", AGORA);
        assertThat(d.getStatusHomologacao()).isEqualTo(StatusHomologacao.HOMOLOGADO);
        assertThat(d.getHomologadorId()).isEqualTo("homologador-1");
        assertThat(d.getDecididoEmUtc()).isEqualTo(AGORA);
    }

    @Test
    void vetarExigeMotivo() {
        assertThatThrownBy(() -> pendente().vetar("h", "  ", AGORA))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void naoRedecideDecisaoJaHomologada() {
        CpcDecisao d = pendente();
        d.homologar("h", AGORA);
        assertThatThrownBy(() -> d.vetar("h2", "arrependimento", AGORA))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void homologarLimpaMotivoVetoAnterior() {
        CpcDecisao d = new CpcDecisao(UUID.randomUUID(), "t", "p", LocalDate.of(2026, 5, 1), UUID.randomUUID(),
                "CPC", StatusHomologacao.PENDENTE_HOMOLOGACAO, "s", "ADMIN", null, "rascunho de veto", AGORA, null);
        d.homologar("h", AGORA);
        assertThat(d.getMotivoVeto()).isNull();
    }
}
