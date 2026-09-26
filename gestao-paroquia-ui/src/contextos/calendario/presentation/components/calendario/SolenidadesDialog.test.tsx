import React from 'react';
import { fireEvent, render, screen } from '@testing-library/react';
import { ThemeProvider } from '@mui/material/styles';
import theme from '@/shared/presentation/theme/theme';
import SolenidadesDialog from './SolenidadesDialog';
import type { SolemnityInfo } from '@/shared/utils/liturgicalCalendar';

const mockSolemnities: SolemnityInfo[] = [
  {
    chave: 'natal',
    nome: 'Natal do Senhor',
    date: new Date(2026, 11, 25),
    tipo: 'FIXA',
    corLiturgica: 'BRANCO',
    fatorDemanda: 1.5,
  },
  {
    chave: 'pascoa',
    nome: 'Páscoa da Ressurreição',
    date: new Date(2026, 3, 5),
    tipo: 'MOVEL',
    corLiturgica: 'BRANCO',
    fatorDemanda: 1.5,
  },
];

describe('SolenidadesDialog', () => {
  it('renderiza o título no formato canônico Catálogo Litúrgico & Solenidades - Ano A - 2026', () => {
    render(
      <ThemeProvider theme={theme}>
        <SolenidadesDialog
          open={true}
          onClose={() => {}}
          currentYear={2026}
          liturgicalCycle="Ano A (2026)"
          solemnities={mockSolemnities}
        />
      </ThemeProvider>
    );

    expect(screen.getByText('Catálogo Litúrgico & Solenidades - Ano A - 2026')).toBeInTheDocument();
  });

  it('formata adequadamente mesmo recebendo apenas a letra do ciclo', () => {
    render(
      <ThemeProvider theme={theme}>
        <SolenidadesDialog
          open={true}
          onClose={() => {}}
          currentYear={2027}
          liturgicalCycle="B"
          solemnities={mockSolemnities}
        />
      </ThemeProvider>
    );

    expect(screen.getByText('Catálogo Litúrgico & Solenidades - Ano B - 2027')).toBeInTheDocument();
  });

  it('renderiza a lista de solenidades e permite fechar pelo botão', () => {
    const mockOnClose = vi.fn();
    render(
      <ThemeProvider theme={theme}>
        <SolenidadesDialog
          open={true}
          onClose={mockOnClose}
          currentYear={2026}
          liturgicalCycle="Ano A (2026)"
          solemnities={mockSolemnities}
        />
      </ThemeProvider>
    );

    expect(screen.getByText('Natal do Senhor')).toBeInTheDocument();
    expect(screen.getByText('Páscoa da Ressurreição')).toBeInTheDocument();

    const closeBtn = screen.getByRole('button', { name: /Fechar/i });
    expect(closeBtn).toBeInTheDocument();
    fireEvent.click(closeBtn);
    expect(mockOnClose).toHaveBeenCalledTimes(1);
  });
});
