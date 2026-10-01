import { vi } from 'vitest';
import { act, render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { ThemeProvider } from '@mui/material/styles';
import theme from '@/shared/presentation/theme/theme';
import apiClient from '@/shared/infrastructure/http/client';
import { AuthProvider } from '@/contextos/iam/presentation/auth/AuthContext';
import { QueryProvider } from '@/shared/presentation/context/QueryProvider';
import VidaSacramentalCard from './VidaSacramentalCard';

vi.mock('@/shared/infrastructure/http/client');
const mockedApiClient = apiClient as jest.Mocked<typeof apiClient>;

vi.mock('@/shared/presentation/context/NotificationContext', () => ({
  useNotification: () => ({ showError: vi.fn(), showSuccess: vi.fn() }),
}));

const vinculados = [{
  id: 'a1', tipo: 'BATISMO', status: 'SEALED', visibilidade: 'COMUM', livro: '7', folha: '3', termo: '9',
  dataCelebracao: null, sujeito: 'Maria da Silva', participanteId: 'p1', papelVinculado: 'BATIZANDO',
}];

const renderCard = async (roles: string[], tipoPessoa: 'FIEL' | 'CLERIGO' = 'FIEL') => {
  mockedApiClient.get.mockImplementation((url: string) => {
    if (url === '/autenticacao/meu-perfil') return Promise.resolve({ data: { id: '1', name: 'U', roles, organizations: [] } });
    if (url === '/sacramentos/vinculados/pessoa-1') return Promise.resolve({ data: vinculados });
    return Promise.reject(new Error(`Não mockado: ${url}`));
  });
  await act(async () => {
    render(
      <MemoryRouter>
        <QueryProvider>
          <AuthProvider>
            <ThemeProvider theme={theme}>
              <VidaSacramentalCard pessoaId="pessoa-1" tipoPessoa={tipoPessoa} />
            </ThemeProvider>
          </AuthProvider>
        </QueryProvider>
      </MemoryRouter>,
    );
  });
};

describe('VidaSacramentalCard', () => {
  beforeEach(() => vi.clearAllMocks());

  test('lista os assentos vinculados com a posição da pessoa e permite vincular/desvincular à secretaria', async () => {
    await renderCard(['SECRETARIA_PAROQUIAL']);
    expect(await screen.findByText(/Batizando em «Maria da Silva»/)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Vincular a registro' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Desvincular de Maria da Silva/ })).toBeInTheDocument();
  });

  test('bispo diocesano apenas consulta: sem ações de vínculo', async () => {
    await renderCard(['BISPO_DIOCESANO']);
    expect(await screen.findByText(/Batizando em «Maria da Silva»/)).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Vincular a registro' })).not.toBeInTheDocument();
  });

  test('perfil sem acesso ao livro não vê a seção nem consulta a API', async () => {
    await renderCard(['ADMIN']);
    expect(screen.queryByText('Vida sacramental')).not.toBeInTheDocument();
    expect(mockedApiClient.get).not.toHaveBeenCalledWith('/sacramentos/vinculados/pessoa-1');
  });
});
