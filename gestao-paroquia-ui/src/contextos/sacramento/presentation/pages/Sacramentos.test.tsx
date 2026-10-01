import { vi } from 'vitest';
import { act, render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { ThemeProvider } from '@mui/material/styles';
import theme from '@/shared/presentation/theme/theme';
import apiClient from '@/shared/infrastructure/http/client';
import { AuthProvider } from '@/contextos/iam/presentation/auth/AuthContext';
import { QueryProvider } from '@/shared/presentation/context/QueryProvider';
import Sacramentos from './Sacramentos';

vi.mock('@/shared/infrastructure/http/client');
const mockedApiClient = apiClient as jest.Mocked<typeof apiClient>;

vi.mock('@/shared/presentation/context/NotificationContext', () => ({
  useNotification: () => ({ showError: vi.fn(), showSuccess: vi.fn() }),
}));

const resumos = [
  { id: 'a1', tipo: 'BATISMO', status: 'DRAFT', visibilidade: 'COMUM', livro: null, folha: null, termo: null, dataCelebracao: null, sujeito: 'Maria da Silva' },
  { id: 'a2', tipo: 'BATISMO', status: 'SEALED', visibilidade: 'COMUM', livro: '7', folha: '3', termo: '9', dataCelebracao: '2026-03-01T10:00:00', sujeito: 'José Souza' },
];

const renderPagina = async (roles: string[]) => {
  mockedApiClient.get.mockImplementation((url: string) => {
    if (url === '/autenticacao/meu-perfil') return Promise.resolve({ data: { id: '1', name: 'Usuário', roles, organizations: [] } });
    if (url === '/sacramentos') return Promise.resolve({ data: resumos });
    return Promise.reject(new Error(`Não mockado: ${url}`));
  });
  await act(async () => {
    render(
      <MemoryRouter>
        <QueryProvider>
          <AuthProvider>
            <ThemeProvider theme={theme}>
              <Sacramentos />
            </ThemeProvider>
          </AuthProvider>
        </QueryProvider>
      </MemoryRouter>,
    );
  });
};

describe('Sacramentos', () => {
  beforeEach(() => vi.clearAllMocks());

  test('lista assentos do sacramento selecionado e traz abas dos quatro sacramentos', async () => {
    await renderPagina(['PAROCO']);
    expect(screen.getByRole('heading', { name: /Sacramentos/i })).toBeInTheDocument();
    ['Batismo', 'Confirmação', 'Matrimônio', 'Ordem Sacra'].forEach((nome) =>
      expect(screen.getByRole('tab', { name: nome })).toBeInTheDocument());
    expect(screen.getByText('Maria da Silva')).toBeInTheDocument();
    expect(screen.getByText('José Souza')).toBeInTheDocument();
  });

  test('pároco pode homologar rascunho; secretaria prepara mas não homologa', async () => {
    const user = userEvent.setup();
    await renderPagina(['PAROCO']);
    await user.click(within(screen.getByText('Maria da Silva').closest('tr') as HTMLElement).getByRole('button', { name: 'Ações' }));
    expect(screen.getByRole('menuitem', { name: /Homologar/ })).toBeInTheDocument();
  });

  test('secretaria vê Novo Assento mas não a ação Homologar', async () => {
    const user = userEvent.setup();
    await renderPagina(['SECRETARIA_PAROQUIAL']);
    expect(screen.getByRole('button', { name: 'Novo Assento' })).toBeInTheDocument();
    await user.click(within(screen.getByText('Maria da Silva').closest('tr') as HTMLElement).getByRole('button', { name: 'Ações' }));
    expect(screen.getByRole('menuitem', { name: /Editar rascunho/ })).toBeInTheDocument();
    expect(screen.queryByRole('menuitem', { name: /Homologar/ })).not.toBeInTheDocument();
  });

  test('bispo diocesano só consulta: sem Novo Assento', async () => {
    await renderPagina(['BISPO_DIOCESANO']);
    expect(screen.queryByRole('button', { name: 'Novo Assento' })).not.toBeInTheDocument();
  });
});
