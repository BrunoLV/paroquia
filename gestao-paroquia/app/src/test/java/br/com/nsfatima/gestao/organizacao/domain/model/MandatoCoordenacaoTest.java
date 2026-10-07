package br.com.nsfatima.gestao.organizacao.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MandatoCoordenacaoTest {

    private static final LocalDate INICIO = LocalDate.of(2026, 1, 1);
    private static final LocalDate FIM = LocalDate.of(2027, 12, 31);
    private static final Instant AGORA = Instant.parse("2026-06-01T12:00:00Z");

    private MandatoCoordenacao proposto() {
        return MandatoCoordenacao.propor(UUID.randomUUID(), UUID.randomUUID(), FuncaoCoordenacao.COORDENADOR,
                INICIO, FIM);
    }

    @Test
    void nasceProposto() {
        MandatoCoordenacao m = proposto();
        assertThat(m.getStatus()).isEqualTo(StatusMandato.PROPOSTO);
        assertThat(m.vigenteEm(LocalDate.of(2026, 6, 1))).isFalse();
        assertThat(m.ocupaCargo()).isTrue();
    }

    @Test
    void homologadoTornaSeVigenteSomenteDentroDoPeriodo() {
        MandatoCoordenacao m = proposto();
        m.homologar("paroco", LocalDate.of(2026, 6, 1), AGORA);
        assertThat(m.getStatus()).isEqualTo(StatusMandato.HOMOLOGADO);
        assertThat(m.vigenteEm(LocalDate.of(2026, 6, 1))).isTrue();
        assertThat(m.vigenteEm(LocalDate.of(2028, 1, 1))).isFalse();
    }

    @Test
    void naoHomologaPeriodoJaVencidoNemRehomologa() {
        assertThatThrownBy(() -> proposto().homologar("paroco", LocalDate.of(2030, 1, 1), AGORA))
                .isInstanceOf(IllegalStateException.class);
        MandatoCoordenacao m = proposto();
        m.homologar("paroco", LocalDate.of(2026, 6, 1), AGORA);
        assertThatThrownBy(() -> m.homologar("paroco", LocalDate.of(2026, 6, 1), AGORA))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void vetoExigeMotivoESoVaiAPropostos() {
        assertThatThrownBy(() -> proposto().vetar("paroco", " ", AGORA))
                .isInstanceOf(IllegalArgumentException.class);
        MandatoCoordenacao m = proposto();
        m.vetar("paroco", "Sem formação básica", AGORA);
        assertThat(m.getStatus()).isEqualTo(StatusMandato.VETADO);
        assertThat(m.ocupaCargo()).isFalse();
        assertThatThrownBy(() -> m.vetar("paroco", "outro", AGORA)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void soEncerraMandatoHomologado() {
        MandatoCoordenacao m = proposto();
        assertThatThrownBy(() -> m.encerrar(AGORA)).isInstanceOf(IllegalStateException.class);
        m.homologar("paroco", LocalDate.of(2026, 6, 1), AGORA);
        m.encerrar(AGORA);
        assertThat(m.getStatus()).isEqualTo(StatusMandato.ENCERRADO);
    }

    @Test
    void duracaoRespeitaPoliticaDoMandatoTemporario() {
        UUID org = UUID.randomUUID();
        UUID fiel = UUID.randomUUID();
        assertThatThrownBy(() -> MandatoCoordenacao.propor(org, fiel, FuncaoCoordenacao.COORDENADOR,
                INICIO, INICIO.plusYears(3).plusDays(1))).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("3 anos");
        assertThatThrownBy(() -> MandatoCoordenacao.propor(org, fiel, FuncaoCoordenacao.COORDENADOR, INICIO, INICIO))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(MandatoCoordenacao.propor(org, fiel, FuncaoCoordenacao.VICE_COORDENADOR, INICIO,
                INICIO.plusYears(3)).getFuncao().papelEquivalente()).isEqualTo(PapelOrganizacional.SECRETARIO_GRUPO);
    }

    @Test
    void detectaSobreposicaoDePeriodos() {
        MandatoCoordenacao m = proposto();
        assertThat(m.sobrepoe(LocalDate.of(2027, 12, 31), LocalDate.of(2028, 12, 31))).isTrue();
        assertThat(m.sobrepoe(LocalDate.of(2028, 1, 1), LocalDate.of(2029, 1, 1))).isFalse();
    }
}
