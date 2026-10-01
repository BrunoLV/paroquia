package br.com.nsfatima.gestao.sacramento.domain;

import br.com.nsfatima.gestao.sacramento.domain.exception.AssentoImutavelException;
import br.com.nsfatima.gestao.sacramento.domain.exception.AssentoInvalidoException;
import br.com.nsfatima.gestao.sacramento.domain.model.AssentoSacramental;
import br.com.nsfatima.gestao.sacramento.domain.model.AssentoSacramentalId;
import br.com.nsfatima.gestao.sacramento.domain.model.Averbacao;
import br.com.nsfatima.gestao.sacramento.domain.model.PapelParticipante;
import br.com.nsfatima.gestao.sacramento.domain.model.Participante;
import br.com.nsfatima.gestao.sacramento.domain.model.ReferenciaLivro;
import br.com.nsfatima.gestao.sacramento.domain.model.StatusAssento;
import br.com.nsfatima.gestao.sacramento.domain.model.TeorAssento;
import br.com.nsfatima.gestao.sacramento.domain.model.TipoEventoAverbacao;
import br.com.nsfatima.gestao.sacramento.domain.model.TipoSacramento;
import br.com.nsfatima.gestao.sacramento.domain.model.Visibilidade;
import br.com.nsfatima.gestao.sacramento.domain.policy.BatismoRequisitosPolicy;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AssentoSacramentalTest {

    private static final Instant AGORA = Instant.parse("2026-01-01T12:00:00Z");
    private final BatismoRequisitosPolicy policy = new BatismoRequisitosPolicy();

    private static Participante pessoa(PapelParticipante papel, String nome) {
        return new Participante(UUID.randomUUID(), papel, nome, "doc", "Cidade/UF/BR", true, true, 30,
                papel.aceitaClerigo() ? "PRESBITERO" : null, null, null);
    }

    private static TeorAssento teorBatismoValido(Participante... extras) {
        List<Participante> participantes = new ArrayList<>(List.of(
                pessoa(PapelParticipante.BATIZANDO, "Maria da Silva"),
                pessoa(PapelParticipante.MAE, "Ana da Silva"),
                pessoa(PapelParticipante.MINISTRO_CELEBRANTE, "Pe. João")));
        participantes.addAll(List.of(extras));
        return new TeorAssento(new ReferenciaLivro("1", "10", "100"), "Paróquia N. Sra. de Fátima",
                LocalDateTime.of(2026, 1, 1, 10, 0), "Matriz", participantes,
                Map.of("certidaoNascimentoCartorio", "1º Ofício", "certidaoNascimentoTermo", "55",
                        "estadoFiliacao", "NAO_CASADOS"), false);
    }

    private AssentoSacramental rascunho(TeorAssento teor) {
        return AssentoSacramental.lavrar(AssentoSacramentalId.gerar(), TipoSacramento.BATISMO, Visibilidade.COMUM,
                teor, "secretaria", AGORA);
    }

    @Test
    void homologaTeorValidoESelaOTermo() {
        AssentoSacramental assento = rascunho(teorBatismoValido());
        assento.homologar(policy, "paroco", AGORA);
        assertThat(assento.getStatus()).isEqualTo(StatusAssento.SEALED);
        assertThat(assento.getHomologadoPor()).isEqualTo("paroco");
    }

    @Test
    void teorSelado_naoPodeSerEditado() {
        AssentoSacramental assento = rascunho(teorBatismoValido());
        assento.homologar(policy, "paroco", AGORA);
        assertThatThrownBy(() -> assento.substituirTeor(teorBatismoValido()))
                .isInstanceOf(AssentoImutavelException.class);
    }

    @Test
    void homologacaoRejeitaPadrinhoNaoCrismado() {
        Participante padrinho = new Participante(UUID.randomUUID(), PapelParticipante.PADRINHO, "Carlos", null, null,
                true, false, 40, null, null, null);
        AssentoSacramental assento = rascunho(teorBatismoValido(padrinho));
        assertThatThrownBy(() -> assento.homologar(policy, "paroco", AGORA))
                .isInstanceOf(AssentoInvalidoException.class)
                .hasMessageContaining("crismado");
    }

    @Test
    void padrinhoNaoCatolicoEExigidoComoTestemunha() {
        Participante padrinho = new Participante(UUID.randomUUID(), PapelParticipante.PADRINHO, "Carlos", null, null,
                false, false, 40, null, null, null);
        AssentoSacramental assento = rascunho(teorBatismoValido(padrinho));
        assertThatThrownBy(() -> assento.homologar(policy, "paroco", AGORA))
                .hasMessageContaining("testemunha");
    }

    @Test
    void pendenciasListamAusenciaDeLivroEFiliacao() {
        TeorAssento vazio = new TeorAssento(null, null, null, null, List.of(), Map.of(), false);
        assertThat(policy.pendencias(vazio)).hasSizeGreaterThan(4);
    }

    @Test
    void vincularFiel_preservaSnapshotTextual() {
        Participante batizando = pessoa(PapelParticipante.BATIZANDO, "Maria da Silva");
        AssentoSacramental assento = rascunho(teorBatismoValido(batizando));
        UUID fielId = UUID.randomUUID();
        assento.vincularFiel(batizando.id(), fielId);
        Participante vinculado = assento.getTeor().participantes().stream()
                .filter(p -> p.id().equals(batizando.id())).findFirst().orElseThrow();
        assertThat(vinculado.fielId()).isEqualTo(fielId);
        assertThat(vinculado.nomeHistorico()).isEqualTo("Maria da Silva");
        assento.desvincular(batizando.id());
        assertThat(assento.getTeor().participantes()).filteredOn(p -> p.id().equals(batizando.id()))
                .allMatch(p -> p.fielId() == null && p.nomeHistorico().equals("Maria da Silva"));
    }

    @Test
    void ministroNaoVinculaAFiel() {
        Participante ministro = pessoa(PapelParticipante.MINISTRO_CELEBRANTE, "Pe. João");
        assertThatThrownBy(() -> ministro.comFiel(UUID.randomUUID())).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void averbacaoExigeBatismoSelado_eHomologadaPeloParoco() {
        AssentoSacramental assento = rascunho(teorBatismoValido());
        Averbacao nota = Averbacao.digitar(TipoEventoAverbacao.CONFIRMACAO, LocalDate.of(2026, 5, 1), "N-1",
                "Crismado em outra paróquia", false, "secretaria");
        assertThatThrownBy(() -> assento.inserirAverbacao(nota)).isInstanceOf(AssentoImutavelException.class);

        assento.homologar(policy, "paroco", AGORA);
        assento.inserirAverbacao(nota);
        assento.homologarAverbacao(nota.id(), "paroco", AGORA);
        assertThat(assento.getAverbacoes()).singleElement()
                .satisfies(a -> assertThat(a.homologadaPor()).isEqualTo("paroco"));
    }

    @Test
    void retificacaoNaoSobrescreveTeorOriginal() {
        AssentoSacramental assento = rascunho(teorBatismoValido());
        assento.homologar(policy, "paroco", AGORA);
        TeorAssento original = assento.getTeor();
        assento.retificar("nomeHistorico", "Maria da Silva", "Maria da Silva Souza", "Erro de grafia", "paroco", AGORA);
        assertThat(assento.getTeor()).isSameAs(original);
        assertThat(assento.getRessalvas()).hasSize(1);
        assertThatThrownBy(() -> assento.retificar("x", "a", "b", " ", "paroco", AGORA))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cancelamentoExigeMotivoENaoSeRepete() {
        AssentoSacramental assento = rascunho(teorBatismoValido());
        assertThatThrownBy(() -> assento.cancelar(" ", "paroco", AGORA)).isInstanceOf(IllegalArgumentException.class);
        assento.cancelar("Duplicidade", "paroco", AGORA);
        assertThat(assento.getStatus()).isEqualTo(StatusAssento.CANCELADO);
        assertThatThrownBy(() -> assento.cancelar("outra", "paroco",
                AGORA)).isInstanceOf(AssentoImutavelException.class);
    }
}
