package br.com.nsfatima.gestao.conselho.domain.model;

import br.com.nsfatima.gestao.conselho.domain.exception.AtaInvalidaException;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Funções na presença da reunião")
class PresencaFuncaoTest {

    private static Presenca presente(String funcao) {
        return new Presenca(UUID.randomUUID(), funcao, SituacaoPresenca.PRESENTE);
    }

    @Test
    @DisplayName("aceita só funções da lista fechada")
    void listaFechada() {
        assertThatCode(() -> presente("Secretário(a)")).doesNotThrowAnyException();
        assertThatThrownBy(() -> presente("Chefe da turma")).isInstanceOf(AtaInvalidaException.class);
    }

    @Test
    @DisplayName("só quem esteve presente exerce função")
    void ausenteNaoExerceFuncao() {
        assertThatThrownBy(() -> new Presenca(UUID.randomUUID(), "Presidente", SituacaoPresenca.AUSENTE))
                .isInstanceOf(AtaInvalidaException.class);
        assertThatCode(() -> new Presenca(UUID.randomUUID(), null, SituacaoPresenca.AUSENTE))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("função única não se repete, e as demais podem se repetir")
    void funcaoUnica() {
        assertThatThrownBy(() -> PresencaReuniao.nominal(List.of(presente("Presidente"), presente("Presidente"))))
                .isInstanceOf(AtaInvalidaException.class).hasMessageContaining("uma pessoa");
        assertThatCode(() -> PresencaReuniao.nominal(List.of(presente("Membro"), presente("Membro"),
                presente("Secretário(a)"), presente("Tesoureiro(a)")))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("o redator consta como Secretário(a), ou Coordenador(a) quando só coordena")
    void funcaoDoRedator() {
        Set<PapelOrganizacional> redatores = Set.of(PapelOrganizacional.SECRETARIO_CONSELHO,
                PapelOrganizacional.COORDENADOR, PapelOrganizacional.SECRETARIO_GRUPO);
        assertThat(FuncaoPresenca.doRedator(Set.of(PapelOrganizacional.SECRETARIO_CONSELHO), redatores))
                .contains(FuncaoPresenca.SECRETARIO);
        assertThat(FuncaoPresenca.doRedator(Set.of(PapelOrganizacional.COORDENADOR), redatores))
                .contains(FuncaoPresenca.COORDENADOR);
        assertThat(FuncaoPresenca.doRedator(Set.of(PapelOrganizacional.COORDENADOR,
                PapelOrganizacional.SECRETARIO_GRUPO), redatores)).contains(FuncaoPresenca.SECRETARIO);
        assertThat(FuncaoPresenca.doRedator(Set.of(PapelOrganizacional.MEMBRO_ATIVO), redatores)).isEmpty();
    }

    @Test
    @DisplayName("no quórum contam os membros e o presidente; o outro clero presente apenas assiste")
    void quorumContaMembrosEPresidente() {
        UUID paroco = UUID.randomUUID();
        UUID vigario = UUID.randomUUID();
        PresencaReuniao presenca = PresencaReuniao.nominal(List.of(
                presente("Membro"),
                new Presenca(UUID.randomUUID(), null, SituacaoPresenca.AUSENTE),
                new Presenca(paroco, true, "Presidente", SituacaoPresenca.PRESENTE),
                new Presenca(vigario, true, "Vigário paroquial", SituacaoPresenca.PRESENTE)));

        assertThat(presenca.presentes()).isEqualTo(3);
        assertThat(presenca.presentesNoQuorum()).isEqualTo(2);
    }
}
