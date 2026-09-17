import {render, screen} from '@testing-library/react';
import {ThemeProvider} from '@mui/material/styles';
import PainelResumoPeriodo from '@/contextos/painel/presentation/components/painel/PainelResumoPeriodo';
import theme from '@/shared/presentation/theme/theme';
import type {GeneralSummary} from '@/shared/domain/types/types';

const resumo: GeneralSummary = {
  totalEventosPeriodo: 12,
  projetosAtivos: 3,
  solicitacoesPendentes: 2,
  totalFieisAtivos: 40,
  novosFieisMes: 3,
  fieisEngajadosPastoral: 25,
  fieisSemVinculoPastoral: 15,
};

describe('PainelResumoPeriodo', () => {
  test('apresenta os indicadores prioritários em linguagem de gestão', () => {
    render(
      <ThemeProvider theme={theme}>
        <PainelResumoPeriodo resumo={resumo} />
      </ThemeProvider>,
    );

    expect(screen.getByText('Eventos no período')).toBeInTheDocument();
    expect(screen.getByText('Projetos ativos')).toBeInTheDocument();
    expect(screen.getByText('Aprovações pendentes')).toBeInTheDocument();
    expect(screen.getByText('Engajamento pastoral')).toBeInTheDocument();
    expect(screen.getByText('25 de 40 fiéis vinculados')).toBeInTheDocument();
  });
});
