package br.com.nsfatima.gestao.sacramento.application.usecase;

import br.com.nsfatima.gestao.sacramento.domain.model.AssentoSacramental;
import br.com.nsfatima.gestao.sacramento.domain.model.AssentoSacramentalId;
import br.com.nsfatima.gestao.sacramento.domain.model.PapelParticipante;
import br.com.nsfatima.gestao.sacramento.domain.model.Participante;
import br.com.nsfatima.gestao.sacramento.domain.model.ReferenciaLivro;
import br.com.nsfatima.gestao.sacramento.domain.model.TeorAssento;
import br.com.nsfatima.gestao.sacramento.domain.model.TipoSacramento;
import br.com.nsfatima.gestao.sacramento.domain.model.Visibilidade;
import br.com.nsfatima.gestao.sacramento.domain.policy.BatismoRequisitosPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** A certidao nao pode repetir como certo o dado que o proprio Paroco ja retificou por ressalva. */
class EmitirCertidaoUseCaseTest {

    private static final Instant AGORA = Instant.parse("2026-01-01T12:00:00Z");

    private static Participante pessoa(PapelParticipante papel, String nome) {
        return new Participante(UUID.randomUUID(), papel, nome, "doc", "Cidade/UF/BR", true, true, 30,
                papel.aceitaClerigo() ? "PRESBITERO" : null, null, null);
    }

    private AssentoSacramental assentoSelado(boolean dadoSensivel) {
        TeorAssento teor = new TeorAssento(new ReferenciaLivro("1", "10", "100"), "Paróquia N. Sra. de Fátima",
                LocalDateTime.of(2026, 1, 1, 10, 0), "Matriz", List.of(
                pessoa(PapelParticipante.BATIZANDO, "Maria da Silva"),
                pessoa(PapelParticipante.MAE, "Ana da Silva"),
                pessoa(PapelParticipante.MINISTRO_CELEBRANTE, "Pe. João")),
                Map.of("certidaoNascimentoCartorio", "1º Ofício", "certidaoNascimentoTermo", "55",
                        "estadoFiliacao", "NAO_CASADOS"), dadoSensivel);
        AssentoSacramental assento = AssentoSacramental.lavrar(AssentoSacramentalId.gerar(), TipoSacramento.BATISMO,
                Visibilidade.COMUM, teor, "secretaria", AGORA);
        assento.homologar(new BatismoRequisitosPolicy(), "paroco", AGORA);
        return assento;
    }

    @Test
    @DisplayName("Sem ressalvas a certidao nao ganha anexo")
    void semRessalvas() {
        assertThat(EmitirCertidaoUseCase.narrativa(assentoSelado(false))).doesNotContain("Ressalva");
    }

    @Test
    @DisplayName("Ressalva atestada pelo Paroco e anexada ao texto da certidao, sem apagar o teor original")
    void anexaRessalvasAoTexto() {
        AssentoSacramental assento = assentoSelado(false);
        assento.retificar("local de nascimento", "Santos/SP", "Santo André/SP", "erro de digitação", "paroco", AGORA);
        assento.retificar("grafia do nome", null, "Maria da Silva Souza", "nome completo", "paroco", AGORA);

        String texto = EmitirCertidaoUseCase.narrativa(assento);

        assertThat(texto).contains("Maria da Silva");
        assertThat(texto).contains("Ressalvas:");
        assertThat(texto).contains("no campo \"local de nascimento\", onde consta \"Santos/SP\", "
                + "leia-se \"Santo André/SP\"");
        assertThat(texto).contains("no campo \"grafia do nome\", leia-se \"Maria da Silva Souza\"");
        assertThat(texto).doesNotContain("erro de digitação");
    }

    @Test
    @DisplayName("Em assento sensivel, ressalva sobre filiacao nao vaza para a certidao simples")
    void ressalvaDeFiliacaoNaoVazaEmAssentoSensivel() {
        AssentoSacramental assento = assentoSelado(true);
        assento.retificar("filiação (mãe)", "Ana", "Joana", "troca", "paroco", AGORA);
        assento.retificar("local de nascimento", "Santos/SP", "Santo André/SP", "erro", "paroco", AGORA);

        String texto = EmitirCertidaoUseCase.narrativa(assento);

        assertThat(texto).contains("Santo André/SP").doesNotContain("Joana").doesNotContain("filiação");
    }
}
