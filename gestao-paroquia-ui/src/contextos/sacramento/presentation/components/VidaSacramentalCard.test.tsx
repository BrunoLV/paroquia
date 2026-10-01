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
  dataCelebracao: null, sujeito: 'Maria da Silva', participanteId: 'p1', papelVinculado: 'BATIZANDO', vinculoPendente: false,
}];
const pendentes = [{ ...vinculados[0], vinculoPendente: true }];

const renderCard = async (roles: string[], dados = vinculados) => {
  mockedApiClient.get.mockImplementation((url: string) => {
    if (url === '/autenticacao/meu-perfil') return Promise.resolve({ data: { id: '1', name: 'U', roles, organizations: [] } });
    if (url === '/sacramentos/vinculados/pessoa-1') return Promise.resolve({ data: dados });
    return Promise.reject(new Error(`Não mockado: ${url}`));
  });
  await act(async () => {
    render(
      <MemoryRouter>
        <QueryProvider>
          <AuthProvider>
            <ThemeProvider theme={theme}>
              <VidaSacramentalCard pessoaId="pessoa-1" />
            </ThemeProvider>
          </AuthProvider>
        </QueryProvider>
      </MemoryRouter>,
    );
  });
};

describe('VidaSacramentalCard', () => {
  beforeEach(() => vi.clearAllMocks());

  test.each([['PAROCO'], ['SECRETARIA_PAROQUIAL'], ['BISPO_DIOCESANO']])(
    '%s apenas consulta: lista a participação e não oferece vincular, aprovar nem desvincular',
    async (papel) => {
      await renderCard([papel]);
      expect(await screen.findByText(/Batizando em «Maria da Silva»/)).toBeInTheDocument();
      expect(screen.queryByRole('button')).not.toBeInTheDocument();
    },
  );

  test('vínculo pendente aparece como aviso, sem ação de aprovação na ficha', async () => {
    await renderCard(['PAROCO'], pendentes);
    expect(await screen.findByText(/pendente de aprovação do Pároco/)).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /Aprovar/ })).not.toBeInTheDocument();
  });

  test('perfil sem acesso ao livro não vê a seção nem consulta a API', async () => {
    await renderCard(['ADMIN']);
    expect(screen.queryByText('Vida sacramental')).not.toBeInTheDocument();
    expect(mockedApiClient.get).not.toHaveBeenCalledWith('/sacramentos/vinculados/pessoa-1');
  });
});
