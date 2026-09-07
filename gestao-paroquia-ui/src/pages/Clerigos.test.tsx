import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { fireEvent, render, screen } from '@testing-library/react';
import { vi } from 'vitest';
import Clerigos from '@/pages/Clerigos';

const useAuthMock = vi.fn();
vi.mock('@/auth/AuthContext', () => ({ useAuth: () => useAuthMock() }));
vi.mock('@/context/NotificationContext', () => ({
  useNotification: () => ({ showSuccess: vi.fn(), showError: vi.fn() }),
}));
vi.mock('@/hooks/useClerigos', () => ({
  useClerigos: () => ({
    consulta: {
      isLoading: false,
      isError: false,
      data: [
        {
          clerigoId: 'clerigo-1',
          nomeCivil: 'João Silva',
          nomeEclesiastico: 'Padre João',
          grauOrdem: 'PADRE_DIOCESANO',
          funcaoAtual: 'PAROCO',
          dioceseIncardinacao: 'Diocese de Teste',
          dataNascimento: '1970-01-01',
          paisOrigem: 'BRA',
          cpfFormatado: '123.456.789-00',
          rg: null,
          documentoEstrangeiro: null,
          email: 'padre@example.com',
          celularFormatado: '(11) 99999-9999',
          isWhatsapp: true,
          telefoneFixoFormatado: null,
          endereco: 'Casa Paroquial',
          dataOrdenacaoDiaconal: '1998-01-01',
          dataOrdenacaoPresbiteral: '1999-01-01',
          dataOrdenacaoEpiscopal: null,
          isAtivo: true,
        },
      ],
    },
    diretorio: {
      isLoading: false,
      isError: false,
      data: [
        {
          clerigoId: 'clerigo-1',
          nomeEclesiastico: 'Padre João',
          grauOrdem: 'PADRE_DIOCESANO',
          funcaoAtual: 'PAROCO',
          paroquia: 'Paróquia Teste',
        },
      ],
    },
    criar: { mutate: vi.fn(), mutateAsync: vi.fn(), isPending: false },
    atualizar: { mutate: vi.fn(), mutateAsync: vi.fn(), isPending: false },
    inativar: { mutate: vi.fn(), mutateAsync: vi.fn(), isPending: false },
  }),
}));

const renderPage = () =>
  render(
    <QueryClientProvider client={new QueryClient()}>
      <Clerigos />
    </QueryClientProvider>
  );

describe('Clero', () => {
  beforeEach(() => {
    useAuthMock.mockReturnValue({ user: { id: 'admin', roles: ['ADMIN'] } });
  });

  it('oferece cadastro e exibe o histórico canônico na ficha administrativa', async () => {
    renderPage();
    expect(await screen.findByRole('button', { name: /novo clérigo/i })).toBeInTheDocument();
    expect(screen.getByText('Padre João')).toBeInTheDocument();
    expect(screen.getByText(/ordenação presbiteral/i)).toBeInTheDocument();
  });

  it('abre o modal de visualização da ficha administrativa com dados canônicos e civis', async () => {
    renderPage();
    const actionBtn = screen.getByLabelText(/ações/i);
    fireEvent.click(actionBtn);
    const viewOption = screen.getByText(/visualizar ficha/i);
    fireEvent.click(viewOption);

    expect(await screen.findByText('Diocese de incardinação')).toBeInTheDocument();
    expect(screen.getAllByText('Diocese de Teste').length).toBeGreaterThanOrEqual(1);
    expect(screen.getByText('padre@example.com')).toBeInTheDocument();
  });

  it('abre o modal de edição com campos preenchidos', async () => {
    renderPage();
    const actionBtn = screen.getByLabelText(/ações/i);
    fireEvent.click(actionBtn);
    const editOption = screen.getByText(/editar cadastro/i);
    fireEvent.click(editOption);

    expect(await screen.findByText('Editar clérigo')).toBeInTheDocument();
    expect(screen.getByDisplayValue('Padre João')).toBeInTheDocument();
    expect(screen.getByDisplayValue('João Silva')).toBeInTheDocument();
  });

  it('exige confirmação antes de inativar o clérigo', async () => {
    renderPage();
    const actionBtn = screen.getByLabelText(/ações/i);
    fireEvent.click(actionBtn);
    const inativarOption = screen.getByText(/inativar/i);
    fireEvent.click(inativarOption);

    expect(await screen.findByText(/tem certeza que deseja inativar o clérigo/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /inativar/i })).toBeInTheDocument();
  });

  it('não expõe dados civis na experiência institucional', async () => {
    useAuthMock.mockReturnValue({ user: { id: 'fiel', roles: ['MEMBRO'] } });
    renderPage();
    expect(await screen.findByText('Diretório do Clero')).toBeInTheDocument();
    expect(screen.queryByText('João Silva')).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /novo clérigo/i })).not.toBeInTheDocument();
  });
});
