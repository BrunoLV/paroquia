import {vi} from "vitest";
import React from 'react';
import {act, fireEvent, render, screen, waitFor} from '@testing-library/react';
import Calendario from './Calendario';
import {BrowserRouter} from 'react-router-dom';
import {AuthProvider} from '@/contextos/iam/presentation/auth/AuthContext';
import {ThemeProvider} from '@mui/material/styles';
import theme from '@/shared/presentation/theme/theme';
import apiClient from '@/shared/infrastructure/http/client';
import { QueryProvider } from '@/shared/presentation/context/QueryProvider';

vi.mock('@/shared/infrastructure/http/client');
const mockedApiClient = apiClient as jest.Mocked<typeof apiClient>;

const mockShowError = vi.fn();
const mockShowSuccess = vi.fn();
vi.mock('@/shared/presentation/context/NotificationContext', () => ({
  useNotification: () => ({
    showError: mockShowError,
    showSuccess: mockShowSuccess,
  }),
}));

const mockUser = { id: '1', name: 'Test User', username: 'testuser', roles: ['ADMIN'] };

const mockOrgs = [
  { id: 'org-1', nome: 'Pastoral da Juventude', tipo: 'PASTORAL', contato: null, ativo: true },
  { id: 'org-2', nome: 'Pastoral da Comunicação', tipo: 'PASTORAL', contato: null, ativo: true },
];

const mockEvents = [
  {
    id: 'event-1',
    titulo: 'Missa Paroquial',
    descricao: 'Missa de Domingo',
    categoria: 'LITURGICO',
    grupoParoquialResponsavelId: 'org-1',
    projetoId: null,
    nomeProjeto: null,
    inicio: new Date().toISOString(),
    fim: new Date(Date.now() + 3600000).toISOString(),
    status: 'CONFIRMADO',
    estadoConflito: null,
    motivoConflito: null,
  }
];

const setupApiMock = (user = mockUser, approvals: any[] = []) => {
  mockedApiClient.get.mockImplementation((url) => {
    if (url === '/autenticacao/meu-perfil') {
      return Promise.resolve({ data: user });
    }
    if (url === '/organizacoes') {
      return Promise.resolve({ data: mockOrgs });
    }
    if (url === '/eventos') {
      return Promise.resolve({ data: { content: mockEvents } });
    }
    if (url === '/locais') {
      return Promise.resolve({ data: [{ id: 'loc-1', nome: 'Salão Paroquial', ativo: true }] });
    }
    if (url === '/projetos') {
      return Promise.resolve({ data: [] });
    }
    if (url === '/aprovacoes') {
      return Promise.resolve({ data: { content: approvals } });
    }
    if (url === '/solenidades-liturgicas/ativas') {
      return Promise.resolve({ data: [] });
    }
    return Promise.reject(new Error('Not found'));
  });

  mockedApiClient.post.mockImplementation(() => Promise.resolve({ data: {} }));
  mockedApiClient.put.mockImplementation(() => Promise.resolve({ data: {} }));
};

// Helper wrapper to render the calendar with mocks
const renderCalendar = async (user = mockUser, approvals: any[] = [], skipSetupMock = false) => {
  if (!skipSetupMock) {
    setupApiMock(user, approvals);
  }

  await act(async () => {
    render(
      <BrowserRouter>
        <AuthProvider>
          <ThemeProvider theme={theme}>
            <QueryProvider>
              <Calendario />
            </QueryProvider>
          </ThemeProvider>
        </AuthProvider>
      </BrowserRouter>
    );
  });
};

beforeEach(() => {
  jest.clearAllMocks();
  mockShowError.mockClear();
  mockShowSuccess.mockClear();
});

test('renders calendar controls', async () => {
  await renderCalendar();

  expect(screen.getByRole('button', { name: /Novo Evento/i })).toBeInTheDocument();
  expect(screen.getByRole('button', { name: /Ir para Mês Atual/i })).toBeInTheDocument();
});

test('loads events and displays them in the monthly grid', async () => {
  await renderCalendar();

  // It should request the events endpoint
  expect(mockedApiClient.get).toHaveBeenCalledWith(
    '/eventos',
    expect.objectContaining({
      params: expect.objectContaining({
        size: 200,
      }),
    })
  );

  // It should display the mock event title on the screen
  const eventPills = await screen.findAllByText(/Missa Paroquial/i);
  expect(eventPills.length).toBeGreaterThan(0);
});

test('restricts organization filter and events for non-ADMIN users', async () => {
  const nonAdminUser = { 
    id: '2', 
    name: 'Member User', 
    username: 'member', 
    roles: ['MEMBER'], 
    organizations: [mockOrgs[0]] 
  };
  
  await renderCalendar(nonAdminUser);

  // It should request events with implicit organization filter
  expect(mockedApiClient.get).toHaveBeenCalledWith(
    '/eventos',
    expect.objectContaining({
      params: expect.objectContaining({
        organizacaoIds: [mockOrgs[0].id]
      })
    })
  );

  // Open organization filter dropdown
  const orgFilter = screen.getByLabelText(/Organização/i);
  fireEvent.mouseDown(orgFilter);

  // Should see "Todas" and only the user's organization "Pastoral da Juventude"
  expect(screen.getByText('Pastoral da Juventude')).toBeInTheDocument();
  expect(screen.queryByText('Pastoral da Comunicação')).not.toBeInTheDocument();
});

test('can switch to agenda list view', async () => {
  await renderCalendar();

  const agendaButton = screen.getByRole('button', { name: /Visualização em Agenda/i });
  await act(async () => {
    fireEvent.click(agendaButton);
  });

  // Verify elements inside the Agenda card are visible
  expect(await screen.findByText(/Missa Paroquial/i)).toBeInTheDocument();
  expect(screen.getByText(/Missa de Domingo/i)).toBeInTheDocument();
});

test('opens event details modal when clicking an event', async () => {
  await renderCalendar();

  const eventPill = (await screen.findAllByText(/Missa Paroquial/i))[0];
  await act(async () => {
    fireEvent.click(eventPill);
  });

  // Verify details modal header
  expect(screen.getByText(/detalhes do evento/i)).toBeInTheDocument();
  expect(screen.getAllByTestId('ChurchIcon').length).toBeGreaterThan(0);
});

test('event details modal displays Local and Supporting Organizations', async () => {
  const eventWithExtraInfo = {
    ...mockEvents[0],
    localId: 'loc-1',
    participantes: ['org-2']
  };
  
  mockedApiClient.get.mockImplementation((url) => {
    if (url === '/autenticacao/meu-perfil') return Promise.resolve({ data: mockUser });
    if (url === '/organizacoes') return Promise.resolve({ data: mockOrgs });
    if (url === '/eventos') return Promise.resolve({ data: { content: [eventWithExtraInfo] } });
    if (url === '/locais') return Promise.resolve({ data: [{ id: 'loc-1', nome: 'Salão Principal', ativo: true }] });
    if (url === '/projetos') return Promise.resolve({ data: [] });
    if (url === '/solenidades-liturgicas/ativas') return Promise.resolve({ data: [] });
    return Promise.reject(new Error('Not found'));
  });

  await act(async () => {
    render(
      <BrowserRouter>
        <AuthProvider>
          <ThemeProvider theme={theme}>
            <QueryProvider>
              <Calendario />
            </QueryProvider>
          </ThemeProvider>
        </AuthProvider>
      </BrowserRouter>
    );
  });

  const eventPill = await screen.findByText(/Missa Paroquial/i);
  await act(async () => {
    fireEvent.click(eventPill);
  });

  expect(screen.getByText('Salão Principal')).toBeInTheDocument();
  expect(screen.getByText('Pastoral da Comunicação')).toBeInTheDocument();
});

test('opens and submits event creation form', async () => {
  mockedApiClient.post.mockResolvedValueOnce({ data: { id: 'new-event-uuid' } });
  await renderCalendar();

  const openFormButton = screen.getByRole('button', { name: /Novo Evento/i });
  await act(async () => {
    fireEvent.click(openFormButton);
  });

  // Form title should be visible
  expect(screen.getByText('Criar Novo Evento')).toBeInTheDocument();

  // Fill in the title field
  const titleInput = screen.getByLabelText(/Título do Evento/i);
  fireEvent.change(titleInput, { target: { value: 'Novo Evento Teste' } });

  // Select first organization from dropdown - mockOrgs has Pastoral da Juventude
  const submitButton = screen.getByRole('button', { name: /Salvar/i });

  await act(async () => {
    fireEvent.click(submitButton);
  });

  // Verify post API call
  expect(mockedApiClient.post).toHaveBeenCalledWith(
    '/eventos',
    expect.objectContaining({
      titulo: 'Novo Evento Teste',
      categoria: 'PASTORAL',
      grupoParoquialResponsavelId: 'org-1',
      status: 'CONFIRMADO',
    }),
    expect.objectContaining({
      headers: expect.objectContaining({
        'Idempotency-Key': expect.any(String),
      }),
    })
  );
}, 15000);

test('displays events with pending actions with reduced opacity and indicator', async () => {
  const aprovacoesPendentes = [
    {
      id: 'approval-1',
      eventoId: 'event-1',
      tipoSolicitacao: 'CANCELAMENTO',
      status: 'PENDENTE',
      solicitanteId: 'user-1',
      criadoEmUtc: new Date().toISOString(),
      actionPayloadJson: '{"motivo": "Teste"}'
    }
  ];

  await renderCalendar(mockUser, aprovacoesPendentes);

  // It should render the event with [Pendente] indicator
  const eventPill = await screen.findByText(/\[Pendente\] Missa Paroquial/i);
  expect(eventPill).toBeInTheDocument();

  // Click on the event to see details dialog warning
  await act(async () => {
    fireEvent.click(eventPill);
  });

  expect(screen.getByText('Solicitação de Cancelamento Pendente')).toBeInTheDocument();
});

test('parses and renders transient events from pending creation requests', async () => {
  const aprovacoesPendentes = [
    {
      id: 'approval-2',
      eventoId: null,
      tipoSolicitacao: 'CRIACAO_EVENTO',
      status: 'PENDENTE',
      solicitanteId: 'user-2',
      criadoEmUtc: new Date().toISOString(),
      actionPayloadJson: JSON.stringify({
        titulo: 'Missa das Crianças',
        descricao: 'Missa infantil',
        categoria: 'LITURGICO',
        inicio: new Date().toISOString(),
        fim: new Date(Date.now() + 3600000).toISOString(),
        grupoParoquialResponsavelId: 'org-1',
        localId: null,
        status: 'CONFIRMADO'
      })
    }
  ];

  await renderCalendar(mockUser, aprovacoesPendentes);

  // The transient event should be rendered on the calendar grid
  expect(screen.getByText(/\[Pendente\] Missa das Crianças/i)).toBeInTheDocument();

  // Clicking it should show the creation details dialog
  const eventPill = screen.getByText(/\[Pendente\] Missa das Crianças/i);
  await act(async () => {
    fireEvent.click(eventPill);
  });

  expect(screen.getByText('Solicitação de Criação Pendente')).toBeInTheDocument();
  expect(screen.getByText('Missa infantil')).toBeInTheDocument();
});

test('shows error notification when calendar data fetch fails', async () => {
  mockedApiClient.get.mockImplementation((url) => {
    if (url === '/autenticacao/meu-perfil') return Promise.resolve({ data: mockUser });
    if (url === '/solenidades-liturgicas/ativas') return Promise.resolve({ data: [] });
    return Promise.reject(new Error('API failure'));
  });
  await renderCalendar(mockUser, [], true);
  await waitFor(() => {
    expect(mockShowError).toHaveBeenCalledWith('Não foi possível carregar as informações do calendário.');
  });
});

test('toggles calendar view between Grid and List mode', async () => {
  await renderCalendar();

  const listBtn = screen.getByRole('button', { name: /Visualização em Agenda/i });
  await act(async () => {
    fireEvent.click(listBtn);
  });

  await waitFor(() => {
    expect(screen.getByText('Missa Paroquial')).toBeInTheDocument();
  });

  const gridBtn = screen.getByRole('button', { name: /^Mês$/i });
  await act(async () => {
    fireEvent.click(gridBtn);
  });

  const monthNames = [
    'Janeiro',
    'Fevereiro',
    'Março',
    'Abril',
    'Maio',
    'Junho',
    'Julho',
    'Agosto',
    'Setembro',
    'Outubro',
    'Novembro',
    'Dezembro',
  ];
  const currentMonthName = monthNames[new Date().getMonth()];
  expect(screen.getByRole('heading', { name: new RegExp(currentMonthName, 'i') })).toBeInTheDocument();
});

test('renders liturgical season background colors and season labels on calendar day cells', async () => {
  await renderCalendar();
  // July 2026 is in TEMPO COMUM
  expect(screen.getAllByText(/TEMPO COMUM/i).length).toBeGreaterThan(0);
});

test('renders Solenidades and Exportar buttons with their semantic icon/color', async () => {
  await renderCalendar();
  const solemnitiesButton = screen.getByRole('button', { name: /Solenidades & Santoral/i });
  const exportButton = screen.getByRole('button', { name: /^Exportar$/i });
  expect(solemnitiesButton.querySelector('[data-testid="EventIcon"]')).toBeInTheDocument();
  expect(exportButton.className).toContain('MuiButton-colorInfo');
});

test('opens solemnities dialog when clicking Solenidades & Santoral button', async () => {
  await renderCalendar();
  const btn = screen.getByRole('button', { name: /Solenidades & Santoral/i });
  await act(async () => {
    fireEvent.click(btn);
  });
  expect(screen.getByText(/Catálogo Litúrgico & Solenidades/i)).toBeInTheDocument();
  expect(screen.getByText(/Santa Maria, Mãe de Deus/i)).toBeInTheDocument();
  expect(screen.getByText(/Natal do Senhor/i)).toBeInTheDocument();
});

test('toggles to DIA (daily) view and displays daily banner and timeline', async () => {
  await renderCalendar();

  const diaBtn = screen.getByRole('button', { name: /^Dia$/i });
  await act(async () => {
    fireEvent.click(diaBtn);
  });

  // Verify daily banner elements
  expect(screen.getByRole('button', { name: /Novo Evento neste Dia/i })).toBeInTheDocument();
  expect(screen.getByText('Missa Paroquial')).toBeInTheDocument();

  // Verify timeline mode switch buttons
  const timelineBtn = screen.getByRole('button', { name: /Exibir Linha do Tempo/i });
  const listaBtn = screen.getByRole('button', { name: /Exibir Lista de Cartões/i });
  expect(timelineBtn).toBeInTheDocument();
  expect(listaBtn).toBeInTheDocument();

  // Switch to Lista mode
  await act(async () => {
    fireEvent.click(listaBtn);
  });
  expect(screen.getByText('Missa Paroquial')).toBeInTheDocument();
});

test('toggles to SEMANA (weekly) view and displays 7 days columns', async () => {
  await renderCalendar();

  const semanaBtn = screen.getByRole('button', { name: /^Semana$/i });
  await act(async () => {
    fireEvent.click(semanaBtn);
  });

  // Verify weekday headers (Dom, Seg, Ter, Qua, Qui, Sex, Sáb)
  expect(screen.getByText('Dom')).toBeInTheDocument();
  expect(screen.getByText('Seg')).toBeInTheDocument();
  expect(screen.getByText('Ter')).toBeInTheDocument();
  expect(screen.getByText('Qua')).toBeInTheDocument();
  expect(screen.getByText('Qui')).toBeInTheDocument();
  expect(screen.getByText('Sex')).toBeInTheDocument();
  expect(screen.getByText('Sáb')).toBeInTheDocument();
  expect(screen.getByText('Missa Paroquial')).toBeInTheDocument();
});

test('toggles to ANO (annual) view and displays 12 months overview', async () => {
  await renderCalendar();

  const anoBtn = screen.getByRole('button', { name: /^Ano$/i });
  await act(async () => {
    fireEvent.click(anoBtn);
  });

  // Verify annual overview card and months
  expect(screen.getByText(new RegExp(`Visão Geral do Ano ${new Date().getFullYear()}`, 'i'))).toBeInTheDocument();
  expect(screen.getByText('Janeiro')).toBeInTheDocument();
  expect(screen.getByText('Dezembro')).toBeInTheDocument();
  expect(screen.getAllByRole('button', { name: /Ver Mês/i }).length).toBe(12);
});

test('toggles to DIAS ESPECÍFICOS view and displays date inputs and shortcuts', async () => {
  await renderCalendar();

  const diasEspBtn = screen.getByRole('button', { name: /Dias Específicos/i });
  await act(async () => {
    fireEvent.click(diasEspBtn);
  });

  // Verify inputs and shortcut buttons
  expect(screen.getByLabelText(/Data Inicial/i)).toBeInTheDocument();
  expect(screen.getByLabelText(/Data Final/i)).toBeInTheDocument();
  expect(screen.getByRole('button', { name: /7 dias/i })).toBeInTheDocument();
  expect(screen.getByRole('button', { name: /15 dias/i })).toBeInTheDocument();
  expect(screen.getByRole('button', { name: /30 dias/i })).toBeInTheDocument();
  await waitFor(() => {
    expect(screen.getByText('Missa Paroquial')).toBeInTheDocument();
  });
});
