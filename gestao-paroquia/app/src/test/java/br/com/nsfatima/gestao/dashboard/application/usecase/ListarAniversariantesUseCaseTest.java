package br.com.nsfatima.gestao.dashboard.application.usecase;

import br.com.nsfatima.gestao.dashboard.api.v1.dto.AniversarianteDTO;
import br.com.nsfatima.gestao.dashboard.domain.model.PeriodoDashboard;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListarAniversariantesUseCaseTest {

    private final FielJpaRepository fielRepository = mock(FielJpaRepository.class);
    private final ListarAniversariantesUseCase useCase = new ListarAniversariantesUseCase(fielRepository);

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
        LocalDate hoje = LocalDate.now(ZoneOffset.UTC);
        when(fielRepository.findByAtivoTrue())
                .thenReturn(List.of(fiel("Maria Aparecida Souza", hoje.minusYears(30), true, true)));

        List<AniversarianteDTO> resultado = useCase.execute(PeriodoDashboard.HOJE);

        assertThat(resultado).singleElement().satisfies(dto -> {
            assertThat(dto.primeiroNome()).isEqualTo("Maria");
            assertThat(dto.dia()).isEqualTo(hoje.getDayOfMonth());
            assertThat(dto.mes()).isEqualTo(hoje.getMonthValue());
        });
    }

    @Test
    void deveExcluirQuemNaoAutorizouDivulgacaoEQuemEstaInativo() {
        LocalDate hoje = LocalDate.now(ZoneOffset.UTC);
        when(fielRepository.findByAtivoTrue()).thenReturn(List.of(
                fiel("Sem Consentimento", hoje.minusYears(20), true, false),
                fiel("Inativo Aniversariante", hoje.minusYears(40), false, true)));

        assertThat(useCase.execute(PeriodoDashboard.HOJE)).isEmpty();
    }

    @Test
    void mesDeveIncluirAniversarioNoMesMasForaDosProximosSeteDias() {
        LocalDate hoje = LocalDate.now(ZoneOffset.UTC);
        LocalDate fimDoMes = hoje.withDayOfMonth(hoje.lengthOfMonth());
        LocalDate aniversario = hoje.getDayOfMonth() <= 20 ? fimDoMes : hoje.withDayOfMonth(1);
        when(fielRepository.findByAtivoTrue())
                .thenReturn(List.of(fiel("Joao Batista", aniversario.minusYears(50), true, true)));

        assertThat(useCase.execute(PeriodoDashboard.MES)).hasSize(1);
        List<AniversarianteDTO> semana = useCase.execute(PeriodoDashboard.SEMANA);
        assertThat(semana).isEmpty();
    }
}
