import { vi } from 'vitest';
import { act, render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { ThemeProvider } from '@mui/material/styles';
import theme from '@/shared/presentation/theme/theme';
import apiClient from '@/shared/infrastructure/http/client';
import { QueryProvider } from '@/shared/presentation/context/QueryProvider';
import VinculosPendentesPanel from './VinculosPendentesPanel';

vi.mock('@/shared/infrastructure/http/client');
const mockedApiClient = apiClient as jest.Mocked<typeof apiClient>;

vi.mock('@/shared/presentation/context/NotificationContext', () => ({
  useNotification: () => ({ showError: vi.fn(), showSuccess: vi.fn() }),
}));

const pendente = {
  assentoId: 'a1', tipoSacramento: 'BATISMO', sujeito: 'Maria da Silva', livro: '7', folha: '3', termo: '9',
  participanteId: 'p1', papel: 'PADRINHO', nomeHistorico: 'Carlos Souza', tipoPessoa: 'FIEL', pessoaId: 'f1',
  pessoaNome: 'Carlos de Souza',
};

const renderPainel = async (dados: unknown[]) => {
  mockedApiClient.get.mockImplementation((url: string) =>
    url === '/sacramentos/vinculos-pendentes' ? Promise.resolve({ data: dados }) : Promise.reject(new Error(url)));
  mockedApiClient.post.mockResolvedValue({ data: {} });
  await act(async () => {
    render(
      <MemoryRouter>
        <QueryProvider>
          <ThemeProvider theme={theme}>
            <VinculosPendentesPanel />
          </ThemeProvider>
        </QueryProvider>
      </MemoryRouter>,
    );
  });
};

describe('VinculosPendentesPanel', () => {
  beforeEach(() => vi.clearAllMocks());

  test('exibe participação, nome no termo e cadastro proposto para conferência', async () => {
    await renderPainel([pendente]);
    expect(await screen.findByText('Padrinho')).toBeInTheDocument();
    expect(screen.getByText('Carlos Souza')).toBeInTheDocument();
    expect(screen.getByText(/Carlos de Souza \(fiel\)/)).toBeInTheDocument();
  });

  test('a tabela não tem botões de decisão: só o menu de ações com Visualizar detalhes', async () => {
    const user = userEvent.setup();
    await renderPainel([pendente]);
    await screen.findByText('Padrinho');
    expect(screen.queryByRole('button', { name: /Aprovar vínculo/ })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /Rejeitar vínculo/ })).not.toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: 'Ações' }));
    expect(screen.getByRole('menuitem', { name: /Visualizar detalhes/ })).toBeInTheDocument();
  });

  test('aprova e rejeita pelo drawer de detalhes, nos endpoints do Pároco', async () => {
    const user = userEvent.setup();
    await renderPainel([pendente]);
    await user.click(await screen.findByRole('button', { name: 'Ações' }));
    await user.click(screen.getByRole('menuitem', { name: /Visualizar detalhes/ }));
    expect(await screen.findByText('TOMADA DE DECISÃO')).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: 'Aprovar vínculo' }));
    expect(mockedApiClient.post).toHaveBeenCalledWith('/sacramentos/a1/participantes/p1/vinculo/aprovacao');
    await user.click(await screen.findByRole('button', { name: 'Ações' }));
    await user.click(screen.getByRole('menuitem', { name: /Visualizar detalhes/ }));
    await user.click(await screen.findByRole('button', { name: 'Rejeitar vínculo' }));
    expect(mockedApiClient.post).toHaveBeenCalledWith('/sacramentos/a1/participantes/p1/vinculo/rejeicao');
  });

  test('fila vazia mostra o estado vazio', async () => {
    await renderPainel([]);
    expect(await screen.findByText(/Nenhum vínculo sacramental aguardando aprovação/)).toBeInTheDocument();
  });
});
