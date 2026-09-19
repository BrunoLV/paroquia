import { vi } from 'vitest';
import React from 'react';
import { act, fireEvent, render, screen } from '@testing-library/react';
import Ativos from './Ativos';
import { ThemeProvider } from '@mui/material/styles';
import theme from '@/shared/presentation/theme/theme';
import { MemoryRouter } from 'react-router-dom';
import apiClient from '@/shared/infrastructure/http/client';
import { AuthProvider } from '@/contextos/iam/presentation/auth/AuthContext';
import { QueryProvider } from '@/shared/presentation/context/QueryProvider';

vi.mock('@/shared/infrastructure/http/client');
const mockedApiClient = apiClient as jest.Mocked<typeof apiClient>;

const mockShowError = jest.fn();
const mockShowSuccess = jest.fn();
vi.mock('@/shared/presentation/context/NotificationContext', () => ({
  useNotification: () => ({ showError: mockShowError, showSuccess: mockShowSuccess }),
}));

describe('Ativos Page', () => {
  const mockOrgs = [
    { id: 'org-1', nome: 'Comunidade Santo Agostinho', tipo: 'COMUNIDADE', ativo: true },
    { id: 'org-2', nome: 'Matriz', tipo: 'MATRIZ', ativo: true },
    { id: 'org-3', nome: 'Pastoral da Juventude', tipo: 'PASTORAL', ativo: true },
  ];

  const mockAtivos = [
    {
      id: 'ativo-1',
      nome: 'TV 55 polegadas',
      codigoTombamento: 'PAT-2026-0001',
      descricao: 'Tela OLED',
      categoria: 'ELETRONICO',
      status: 'DISPONIVEL',
      isReservavel: true,
      grupoParoquialId: 'org-1',
      dataAquisicao: '2024-01-10',
      valorAquisicao: 3500,
      criadoEmUtc: '2026-01-01T00:00:00Z',
      atualizadoEmUtc: '2026-01-01T00:00:00Z',
    },
    {
      id: 'ativo-2',
      nome: 'Projetor Epson',
      codigoTombamento: 'PAT-2026-0002',
      categoria: 'ELETRONICO',
      status: 'EM_MANUTENCAO',
      isReservavel: false,
      grupoParoquialId: 'org-1',
      dataAquisicao: '2023-05-20',
      valorAquisicao: 2000,
      criadoEmUtc: '2026-01-02T00:00:00Z',
      atualizadoEmUtc: '2026-01-02T00:00:00Z',
    },
  ];

  beforeEach(() => {
    jest.clearAllMocks();
    mockShowError.mockClear();
    mockShowSuccess.mockClear();
  });

  const renderAtivos = async (user: any) => {
    mockedApiClient.get.mockImplementation((url) => {
      if (url === '/autenticacao/meu-perfil') return Promise.resolve({ data: user });
      if (url === '/organizacoes') return Promise.resolve({ data: mockOrgs });
      if (url === '/ativos/organizacao/org-1') return Promise.resolve({ data: mockAtivos });
      return Promise.reject(new Error('Not found'));
    });

    await act(async () => {
      render(
        <MemoryRouter>
          <QueryProvider>
            <AuthProvider>
              <ThemeProvider theme={theme}>
                <Ativos />
              </ThemeProvider>
            </AuthProvider>
          </QueryProvider>
        </MemoryRouter>,
      );
    });
  };

  const adminUser = { id: '1', name: 'Admin', roles: ['ADMIN'], organizations: [] };
  const memberUser = {
    id: '2',
    name: 'Membro',
    roles: ['ROLE_PASTORAL_MEMBRO'],
    organizations: [{ id: 'org-1', nome: 'Comunidade Santo Agostinho', tipo: 'COMUNIDADE', ativo: true }],
  };
  const coordenadorUser = {
    id: '3',
    name: 'Coordenador',
    roles: ['ROLE_PASTORAL_COORDENADOR'],
    organizations: [{ id: 'org-1', nome: 'Comunidade Santo Agostinho', tipo: 'COMUNIDADE', ativo: true }],
  };

  const selecionarOrg = async () => {
    await act(async () => {
      fireEvent.mouseDown(screen.getByLabelText('Organização'));
    });
    await act(async () => {
      screen.getByRole('option', { name: 'Comunidade Santo Agostinho' }).click();
    });
  };

  test('renderiza o cabeçalho da página', async () => {
    await renderAtivos(adminUser);
    expect(screen.getByRole('heading', { name: /Ativos Patrimoniais/i })).toBeInTheDocument();
  });

  test('lista os ativos após selecionar a organização', async () => {
    await renderAtivos(adminUser);
    await selecionarOrg();
    expect(screen.getByText('TV 55 polegadas')).toBeInTheDocument();
    expect(screen.getByText('Projetor Epson')).toBeInTheDocument();
  });

  test('só oferece organizações MATRIZ/COMUNIDADE no seletor', async () => {
    await renderAtivos(adminUser);
    await act(async () => {
      fireEvent.mouseDown(screen.getByLabelText('Organização'));
    });
    const labels = screen.getAllByRole('option').map((o) => o.textContent);
    expect(labels).toContain('Comunidade Santo Agostinho');
    expect(labels).toContain('Matriz');
    expect(labels).not.toContain('Pastoral da Juventude');
  });

  test('botão "Novo Ativo" oculto para usuário sem permissão', async () => {
    await renderAtivos(memberUser);
    expect(screen.queryByRole('button', { name: /Novo Ativo/i })).not.toBeInTheDocument();
  });

  test('botão "Novo Ativo" visível para admin', async () => {
    await renderAtivos(adminUser);
    expect(screen.getByRole('button', { name: /Novo Ativo/i })).toBeInTheDocument();
  });

  test('coluna de valor de aquisição formatada como BRL', async () => {
    await renderAtivos(adminUser);
    await selecionarOrg();
    expect(screen.getByText('R$ 3.500,00')).toBeInTheDocument();
    expect(screen.getByText('R$ 2.000,00')).toBeInTheDocument();
  });

  test('ações da linha por status: manutenção só para DISPONIVEL, concluir só para EM_MANUTENCAO', async () => {
    await renderAtivos(coordenadorUser);
    await selecionarOrg();

    await act(async () => {
      screen.getAllByLabelText('Ações')[0].click();
    });
    expect(screen.getByRole('menuitem', { name: /Enviar para Manutenção/i })).toBeInTheDocument();
    expect(screen.getByRole('menuitem', { name: /^Reservar$/i })).toBeInTheDocument();
    expect(screen.queryByRole('menuitem', { name: /Concluir Manutenção/i })).not.toBeInTheDocument();
    await act(async () => {
      fireEvent.keyDown(document.activeElement || document.body, { key: 'Escape' });
    });

    await act(async () => {
      screen.getAllByLabelText('Ações')[1].click();
    });
    expect(screen.getByRole('menuitem', { name: /Concluir Manutenção/i })).toBeInTheDocument();
    expect(screen.queryByRole('menuitem', { name: /Enviar para Manutenção/i })).not.toBeInTheDocument();
    // Projetor não é reservável
    expect(screen.queryByRole('menuitem', { name: /^Reservar$/i })).not.toBeInTheDocument();
  });

  test('menu da linha oferece Visualizar, Editar e Excluir', async () => {
    await renderAtivos(adminUser);
    await selecionarOrg();
    await act(async () => {
      screen.getAllByLabelText('Ações')[0].click();
    });
    expect(screen.getByRole('menuitem', { name: /^Visualizar$/i })).toBeInTheDocument();
    expect(screen.getByRole('menuitem', { name: /^Editar$/i })).toBeInTheDocument();
    expect(screen.getByRole('menuitem', { name: /^Excluir$/i })).toBeInTheDocument();
  });

  test('abre o modal de detalhes ao clicar na ação Visualizar e não ao clicar na linha da tabela', async () => {
    await renderAtivos(adminUser);
    await selecionarOrg();

    // Clicar na linha não deve abrir o modal
    await act(async () => {
      fireEvent.click(screen.getByText('TV 55 polegadas'));
    });
    expect(screen.queryByText('Dados do ativo')).not.toBeInTheDocument();

    // Clicar na ação "Visualizar" deve abrir o modal de detalhes
    await act(async () => {
      screen.getAllByLabelText('Ações')[0].click();
    });
    await act(async () => {
      screen.getByRole('menuitem', { name: /^Visualizar$/i }).click();
    });
    expect(screen.getByText('Dados do ativo')).toBeInTheDocument();
    expect(screen.getByText('Tombamento: PAT-2026-0001')).toBeInTheDocument();
  });

  test('diálogo de cadastro oferece unidades MATRIZ/COMUNIDADE como dona', async () => {
    await renderAtivos(adminUser);
    await selecionarOrg();
    await act(async () => {
      screen.getByRole('button', { name: /Novo Ativo/i }).click();
    });
    await screen.findByText('Novo Ativo Patrimonial');
    const donaLabel = await screen.findByLabelText(/Organização Dona/i);
    await act(async () => {
      fireEvent.mouseDown(donaLabel);
    });
    const labels = screen.getAllByRole('option').map((o) => o.textContent);
    expect(labels).toContain('Comunidade Santo Agostinho');
    expect(labels).toContain('Matriz');
  });
});
