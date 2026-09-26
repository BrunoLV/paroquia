import React from 'react';
import { render, screen } from '@testing-library/react';
import { ThemeProvider } from '@mui/material/styles';
import theme from '@/shared/presentation/theme/theme';
import ProjetosPorStatusCard from './ProjetosPorStatusCard';
import type { ProjetosPorStatus } from '@/shared/domain/types/types';

describe('ProjetosPorStatusCard', () => {
  it('renderiza os status com descrições e contagens', () => {
    const data: ProjetosPorStatus = {
      planejamento: 3,
      emExecucao: 5,
      concluidos: 8,
      atrasados: 0,
    };

    render(
      <ThemeProvider theme={theme}>
        <ProjetosPorStatusCard data={data} />
      </ThemeProvider>
    );

    expect(screen.getByText('Projetos por Status')).toBeInTheDocument();
    expect(screen.getByText('16 projetos')).toBeInTheDocument();

    expect(screen.getByText('Planejamento')).toBeInTheDocument();
    expect(screen.getByText('Em estruturação e alinhamento inicial')).toBeInTheDocument();
    expect(screen.getByText('3')).toBeInTheDocument();

    expect(screen.getByText('Em Execução')).toBeInTheDocument();
    expect(screen.getByText('Cronograma ativo com eventos em andamento')).toBeInTheDocument();
    expect(screen.getByText('5')).toBeInTheDocument();

    expect(screen.getByText('Concluídos')).toBeInTheDocument();
    expect(screen.getByText('8')).toBeInTheDocument();

    expect(screen.queryByText(/atraso crítico/i)).not.toBeInTheDocument();
  });

  it('exibe alerta de atenção em vermelho quando há projetos atrasados', () => {
    const data: ProjetosPorStatus = {
      planejamento: 1,
      emExecucao: 2,
      concluidos: 3,
      atrasados: 2,
    };

    render(
      <ThemeProvider theme={theme}>
        <ProjetosPorStatusCard data={data} />
      </ThemeProvider>
    );

    expect(screen.getByText('2 projetos com atraso crítico identificados')).toBeInTheDocument();
  });
});
