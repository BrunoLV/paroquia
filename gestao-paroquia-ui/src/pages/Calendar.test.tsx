import React from 'react';
import { render, screen, fireEvent, act, waitFor } from '@testing-library/react';
import Calendar from './Calendar';
import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from '../auth/AuthContext';
import { ThemeProvider } from '@mui/material/styles';
import theme from '../theme';
import apiClient from '../api/client';

jest.mock('../api/client');
const mockedApiClient = apiClient as jest.Mocked<typeof apiClient>;

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
    organizacaoResponsavelId: 'org-1',
    projetoId: null,
    nomeProjeto: null,
    inicio: new Date().toISOString(),
    fim: new Date(Date.now() + 3600000).toISOString(),
    status: 'CONFIRMADO',
    conflictState: null,
    conflictReason: null,
  }
];

// Helper wrapper to render the calendar with mocks
const renderCalendar = async (user = mockUser) => {
  mockedApiClient.get.mockImplementation((url) => {
    if (url === '/auth/me') {
      return Promise.resolve({ data: user });
    }
    if (url === '/organizacoes') {
      return Promise.resolve({ data: mockOrgs });
    }
    if (url === '/eventos') {
      return Promise.resolve({ data: { content: mockEvents } });
    }
    if (url === '/locais') {
      return Promise.resolve({ data: [] });
    }
    if (url === '/projetos') {
      return Promise.resolve({ data: [] });
    }
    return Promise.reject(new Error('Not found'));
  });

  await act(async () => {
    render(
      <BrowserRouter>
        <AuthProvider>
          <ThemeProvider theme={theme}>
            <Calendar />
          </ThemeProvider>
        </AuthProvider>
      </BrowserRouter>
    );
  });
};

test('renders calendar controls', async () => {
  await renderCalendar();

  expect(screen.getByRole('button', { name: /Novo Evento/i })).toBeInTheDocument();
  expect(screen.getByText(/Hoje/i)).toBeInTheDocument();
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
  const eventPills = screen.getAllByText(/Missa Paroquial/i);
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

  const agendaButton = screen.getByTitle('Visualização em Agenda');
  await act(async () => {
    fireEvent.click(agendaButton);
  });

  // Verify elements inside the Agenda card are visible
  expect(screen.getByText(/Missa Paroquial/i)).toBeInTheDocument();
  expect(screen.getByText(/Missa de Domingo/i)).toBeInTheDocument();
});

test('opens event details modal when clicking an event', async () => {
  await renderCalendar();

  const eventPill = screen.getAllByText(/Missa Paroquial/i)[0];
  await act(async () => {
    fireEvent.click(eventPill);
  });

  // Verify details modal header
  expect(screen.getByText('Detalhes do Evento')).toBeInTheDocument();
  expect(screen.getAllByText('LITURGICO').length).toBeGreaterThan(0);
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
  const submitButton = screen.getByRole('button', { name: /Criar Evento/i });

  await act(async () => {
    fireEvent.click(submitButton);
  });

  // Verify post API call
  expect(mockedApiClient.post).toHaveBeenCalledWith(
    '/eventos',
    expect.objectContaining({
      titulo: 'Novo Evento Teste',
      categoria: 'PASTORAL',
      organizacaoResponsavelId: 'org-1',
      status: 'CONFIRMADO',
    }),
    expect.objectContaining({
      headers: expect.objectContaining({
        'Idempotency-Key': expect.any(String),
      }),
    })
  );
});
