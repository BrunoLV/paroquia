package br.com.nsfatima.gestao.dashboard.application.usecase;

import br.com.nsfatima.gestao.dashboard.api.v1.dto.AniversarianteDTO;
import br.com.nsfatima.gestao.dashboard.domain.model.PeriodoDashboard;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.support.domain.time.RelogioNegocio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListarAniversariantesUseCaseTest {

    private static final ZoneId BRT = ZoneId.of("America/Sao_Paulo");

    private final FielJpaRepository fielRepository = mock(FielJpaRepository.class);

    private ListarAniversariantesUseCase useCaseEm(String agoraUtc) {
        return new ListarAniversariantesUseCase(fielRepository,
                new RelogioNegocio(Clock.fixed(Instant.parse(agoraUtc), ZoneOffset.UTC), BRT));
    }

    private FielEntity fiel(String nome, LocalDate nascimento, boolean ativo, boolean divulga) {
        FielEntity fiel = new FielEntity();
        fiel.setNome(nome);
        fiel.setDataNascimento(nascimento);
        fiel.setAtivo(ativo);
        fiel.setPermitirDivulgacaoAniversario(divulga);
        return fiel;
    }

    @Test
    void deveIncluirAniversarianteDeHojeExpondoApenasPrimeiroNomeEDiaMes() {
        when(fielRepository.findByAtivoTrue())
                .thenReturn(List.of(fiel("Maria Aparecida Souza", LocalDate.of(1990, 6, 10), true, true)));

        List<AniversarianteDTO> resultado = useCaseEm("2026-06-10T15:00:00Z").execute(PeriodoDashboard.HOJE);

        assertThat(resultado).singleElement().satisfies(dto -> {
            assertThat(dto.primeiroNome()).isEqualTo("Maria");
            assertThat(dto.dia()).isEqualTo(10);
            assertThat(dto.mes()).isEqualTo(6);
        });
    }

    @Test
    void deveExcluirQuemNaoAutorizouDivulgacaoEQuemEstaInativo() {
        when(fielRepository.findByAtivoTrue()).thenReturn(List.of(
                fiel("Sem Consentimento", LocalDate.of(2000, 6, 10), true, false),
                fiel("Inativo Aniversariante", LocalDate.of(1980, 6, 10), false, true)));

        assertThat(useCaseEm("2026-06-10T15:00:00Z").execute(PeriodoDashboard.HOJE)).isEmpty();
    }

    @Test
    void mesDeveIncluirAniversarioNoMesMasForaDosProximosSeteDias() {
        when(fielRepository.findByAtivoTrue())
                .thenReturn(List.of(fiel("Joao Batista", LocalDate.of(1970, 6, 30), true, true)));
        ListarAniversariantesUseCase useCase = useCaseEm("2026-06-10T15:00:00Z");

        assertThat(useCase.execute(PeriodoDashboard.MES)).hasSize(1);
        assertThat(useCase.execute(PeriodoDashboard.SEMANA)).isEmpty();
    }

    @Test
    @DisplayName("21h-24h BRT do ultimo dia do mes: hoje ainda e o dia local, nao o dia seguinte em UTC")
    void hojeNoUltimoDiaDoMesApos21hUsaODiaLocal() {
        when(fielRepository.findByAtivoTrue()).thenReturn(List.of(
                fiel("Aniversariante de 30/06", LocalDate.of(1990, 6, 30), true, true),
                fiel("Aniversariante de 01/07", LocalDate.of(1990, 7, 1), true, true)));

        List<AniversarianteDTO> hoje = useCaseEm("2026-07-01T01:30:00Z").execute(PeriodoDashboard.HOJE);

        assertThat(hoje).singleElement().satisfies(dto -> {
            assertThat(dto.dia()).isEqualTo(30);
            assertThat(dto.mes()).isEqualTo(6);
        });
    }

    @Test
    @DisplayName("31/12 22h BRT: a semana cruza a virada do ano local")
    void semanaCruzaAViradaDoAno() {
        when(fielRepository.findByAtivoTrue()).thenReturn(List.of(
                fiel("Ano Novo", LocalDate.of(1990, 1, 1), true, true)));

        assertThat(useCaseEm("2027-01-01T01:00:00Z").execute(PeriodoDashboard.SEMANA)).hasSize(1);
    }

    @Test
    @DisplayName("Quem nasceu em 29/02 comemora em 28/02 em ano nao bissexto")
    void nascidoEm29DeFevereiroComemoraEm28EmAnoNaoBissexto() {
        when(fielRepository.findByAtivoTrue()).thenReturn(List.of(
                fiel("Bissexto", LocalDate.of(2000, 2, 29), true, true)));

        assertThat(useCaseEm("2027-02-28T15:00:00Z").execute(PeriodoDashboard.HOJE)).hasSize(1);
        assertThat(useCaseEm("2027-03-01T15:00:00Z").execute(PeriodoDashboard.HOJE)).isEmpty();
        assertThat(useCaseEm("2027-02-10T15:00:00Z").execute(PeriodoDashboard.MES)).hasSize(1);
        assertThat(useCaseEm("2027-02-25T15:00:00Z").execute(PeriodoDashboard.SEMANA)).hasSize(1);
    }

    @Test
    @DisplayName("Em ano bissexto o nascido em 29/02 comemora em 29/02 e nao em 28/02")
    void nascidoEm29DeFevereiroComemoraEm29EmAnoBissexto() {
        when(fielRepository.findByAtivoTrue()).thenReturn(List.of(
                fiel("Bissexto", LocalDate.of(2000, 2, 29), true, true)));

        assertThat(useCaseEm("2028-02-29T15:00:00Z").execute(PeriodoDashboard.HOJE)).hasSize(1);
        assertThat(useCaseEm("2028-02-28T15:00:00Z").execute(PeriodoDashboard.HOJE)).isEmpty();
    }
}
