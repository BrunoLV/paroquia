import React from 'react';
import { fireEvent, render, screen } from '@testing-library/react';
import { ThemeProvider } from '@mui/material/styles';
import theme from '@/shared/presentation/theme/theme';
import { Evento } from '@/shared/domain/types/types';
import TimelineDiaCalendario from './TimelineDiaCalendario';

const mockGetOrgName = (orgId: string) => (orgId === 'org-1' ? 'Pastoral da Catequese' : 'Paróquia Central');
const mockGetLocalName = (localId: string | null) => (localId === 'loc-1' ? 'Salão São José' : null);

const renderComponent = (props: Partial<React.ComponentProps<typeof TimelineDiaCalendario>> = {}) => {
  const defaultProps = {
    date: new Date(2026, 8, 21), // 21 de Setembro de 2026
    events: [],
    onEventClick: vi.fn(),
    onDayClick: vi.fn(),
    getOrgName: mockGetOrgName,
    getLocalName: mockGetLocalName,
    ...props,
  };

  return render(
    <ThemeProvider theme={theme}>
      <TimelineDiaCalendario {...defaultProps} />
    </ThemeProvider>
  );
};

describe('TimelineDiaCalendario', () => {
  it('renderiza régua de horários quando não há eventos no dia e permite agendamento por clique em horário', () => {
    const onDayClick = vi.fn();
    renderComponent({ events: [], onDayClick });

    expect(screen.getByText('07:00')).toBeInTheDocument();
    expect(screen.getByText('22:00')).toBeInTheDocument();

    const agendarSlot07 = screen.getByText(/Agendar às 07:00/i);
    fireEvent.click(agendarSlot07);
    expect(onDayClick).toHaveBeenCalled();
  });

  it('segrega e renderiza eventos de dia inteiro no topo da timeline', () => {
    const allDayEvent: Evento = {
      id: 'evt-all-day',
      titulo: 'Festa de São Mateus Apóstolo',
      descricao: 'Solenidade Litúrgica',
      categoria: 'LITURGICO',
      grupoParoquialResponsavelId: 'org-1',
      projetoId: null,
      nomeProjeto: null,
      inicio: '2026-09-21T00:00:00.000Z',
      fim: '2026-09-21T23:59:59.000Z',
      status: 'CONFIRMADO',
      estadoConflito: null,
      motivoConflito: null,
    };

    renderComponent({ events: [allDayEvent] });

    expect(screen.getByText(/Celebrações e Eventos de Dia Inteiro/i)).toBeInTheDocument();
    expect(screen.getByText('Festa de São Mateus Apóstolo')).toBeInTheDocument();
  });

  it('renderiza eventos com horário marcado com nós cronológicos e duração', () => {
    const onEventClick = vi.fn();
    const timedEvent: Evento = {
      id: 'evt-timed',
      titulo: 'Missa das Crianças',
      descricao: 'Celebração comunitária',
      categoria: 'LITURGICO',
      grupoParoquialResponsavelId: 'org-1',
      localId: 'loc-1',
      projetoId: null,
      nomeProjeto: null,
      inicio: new Date(2026, 8, 21, 9, 0).toISOString(),
      fim: new Date(2026, 8, 21, 10, 30).toISOString(),
      status: 'CONFIRMADO',
      estadoConflito: null,
      motivoConflito: null,
    };

    renderComponent({ events: [timedEvent], onEventClick });

    expect(screen.getByText('Missa das Crianças')).toBeInTheDocument();
    expect(screen.getByText('09:00 às 10:30')).toBeInTheDocument();
    expect(screen.getByText('Salão São José')).toBeInTheDocument();
    expect(screen.getByText('Pastoral da Catequese')).toBeInTheDocument();

    const detalhesBtn = screen.getByRole('button', { name: /Detalhes/i });
    fireEvent.click(detalhesBtn);
    expect(onEventClick).toHaveBeenCalledWith(timedEvent);
  });

  it('exibe indicador de momento atual (Agora) quando a data visualizada for o dia de hoje', () => {
    const today = new Date();
    const futureTimedEvent: Evento = {
      id: 'evt-today',
      titulo: 'Reunião do CPP',
      categoria: 'PASTORAL',
      grupoParoquialResponsavelId: 'org-1',
      projetoId: null,
      nomeProjeto: null,
      inicio: new Date(today.getFullYear(), today.getMonth(), today.getDate(), 23, 0).toISOString(),
      fim: new Date(today.getFullYear(), today.getMonth(), today.getDate(), 23, 45).toISOString(),
      status: 'CONFIRMADO',
      estadoConflito: null,
      motivoConflito: null,
    };

    renderComponent({ date: today, events: [futureTimedEvent] });

    expect(screen.getByText('Reunião do CPP')).toBeInTheDocument();
    expect(screen.getByText('Agora')).toBeInTheDocument();
  });
});
