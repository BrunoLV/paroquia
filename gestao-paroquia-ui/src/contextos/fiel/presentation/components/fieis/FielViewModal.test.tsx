import { vi } from 'vitest';
import { act, render, screen } from '@testing-library/react';
import { ThemeProvider } from '@mui/material/styles';
import theme from '@/shared/presentation/theme/theme';
import apiClient from '@/shared/infrastructure/http/client';
import { QueryProvider } from '@/shared/presentation/context/QueryProvider';
import { Fiel } from '@/shared/domain/types/types';
import { FielViewModal } from './FielViewModal';

vi.mock('@/shared/infrastructure/http/client');
const mockedApiClient = apiClient as jest.Mocked<typeof apiClient>;

vi.mock('@/contextos/iam/presentation/auth/AuthContext', () => ({
  useAuth: () => ({ user: { id: '1', username: 'p', name: 'Pároco', roles: ['PAROCO'], organizations: [] } }),
}));
vi.mock('@/shared/presentation/context/NotificationContext', () => ({
  useNotification: () => ({ showSuccess: vi.fn(), showError: vi.fn() }),
}));

describe('FielViewModal — Vida sacramental', () => {
  test('consulta os assentos pelo fielId serializado pela API e exibe a participação do fiel', async () => {
    mockedApiClient.get.mockImplementation((url: string) =>
      url === '/sacramentos/vinculados/f-1'
        ? Promise.resolve({
          data: [{
            id: 'a1', tipo: 'BATISMO', status: 'SEALED', visibilidade: 'COMUM', livro: '1', folha: '10', termo: '100',
            dataCelebracao: null, sujeito: 'Sofia Oliveira', participanteId: 'p1', papelVinculado: 'MAE',
            vinculoPendente: false,
          }],
        })
        : Promise.reject(new Error(`Não mockado: ${url}`)));
    // A API devolve `fielId` (a propriedade `id` existe só como método no backend, não no JSON).
    const fiel = { fielId: 'f-1', nome: 'Maria Oliveira', isAtivo: true } as unknown as Fiel;
    await act(async () => {
      render(
        <QueryProvider>
          <ThemeProvider theme={theme}>
            <FielViewModal
              open
              fiel={fiel}
              familiares={[]}
              ajustesPendentes={[]}
              podeGerirCadastro={false}
              onClose={vi.fn()}
              onEdit={vi.fn()}
              onInativar={vi.fn()}
            />
          </ThemeProvider>
        </QueryProvider>,
      );
    });
    expect(await screen.findByText(/Mãe em «Sofia Oliveira»/)).toBeInTheDocument();
    expect(mockedApiClient.get).toHaveBeenCalledWith('/sacramentos/vinculados/f-1');
  });
});
