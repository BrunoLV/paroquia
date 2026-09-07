import {render, screen} from '@testing-library/react';
import {ThemeProvider} from '@mui/material/styles';
import PainelResumoPeriodo from '@/components/dashboard/PainelResumoPeriodo';
import theme from '@/theme';
import type {GeneralSummary} from '@/api/types';

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
