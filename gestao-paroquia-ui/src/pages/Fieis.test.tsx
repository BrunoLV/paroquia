import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { vi } from 'vitest';
import Fieis from './Fieis';

const useAuthMock = vi.fn();
vi.mock('@/api/fieis', () => ({
  getFieis: vi.fn(), getFiel: vi.fn(), createFiel: vi.fn(), updateFiel: vi.fn(), alterarStatusFiel: vi.fn(),
}));
vi.mock('@/api/familiares', () => ({ listarFamiliares: vi.fn(), criarRelacionamentoFamiliar: vi.fn(), removerRelacionamentoFamiliar: vi.fn() }));
vi.mock('@/api/ajustesCadastrais', () => ({ listarAjustesPendentesDoFiel: vi.fn() }));
vi.mock('@/api/unidadesTerritoriais', () => ({ getUnidadesTerritoriais: vi.fn() }));
vi.mock('@/auth/AuthContext', () => ({ useAuth: () => useAuthMock() }));
vi.mock('@/context/NotificationContext', () => ({ useNotification: () => ({ showSuccess: vi.fn(), showError: vi.fn() }) }));

import { getFiel, getFieis } from '@/api/fieis';
import { listarAjustesPendentesDoFiel } from '@/api/ajustesCadastrais';
import { listarFamiliares } from '@/api/familiares';
import { getUnidadesTerritoriais } from '@/api/unidadesTerritoriais';

const fiel = { fielId:'fiel-1', id:'fiel-1', nome:'Maria da Silva', sexo:'FEMININO', cpf:'12345678900', dataNascimento:'1990-01-01', email:'maria@test.com', telefone:'11912345678', vocacao:'LEIGO', ativo:true, isAtivo:true, contato:{email:'maria@test.com',celularFormatado:'(11) 91234-5678',isWhatsapp:true}, dadosIdentificacao:{cpfFormatado:'123.456.789-00',paisOrigem:'BRA'}, igrejaFrequenta:{id:'matriz',nome:'Matriz',tipo:'MATRIZ'} };
const renderPage = () => render(<QueryClientProvider client={new QueryClient({defaultOptions:{queries:{retry:false}}})}><Fieis /></QueryClientProvider>);

describe('Ficha civil de fiéis', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    useAuthMock.mockReturnValue({ user:{id:'admin',roles:['ADMIN']}, isAuthenticated:true });
    vi.mocked(getFieis).mockResolvedValue([fiel] as never);
    vi.mocked(getFiel).mockResolvedValue(fiel as never);
    vi.mocked(getUnidadesTerritoriais).mockResolvedValue([{id:'matriz',nome:'Matriz',tipo:'MATRIZ'}] as never);
    vi.mocked(listarFamiliares).mockResolvedValue([]);
    vi.mocked(listarAjustesPendentesDoFiel).mockResolvedValue([]);
  });

  it('lista sem CPF e abre a ficha civil completa', async () => {
    renderPage();
    await screen.findByText('Maria da Silva');
    expect(screen.queryByRole('columnheader', {name:/cpf/i})).not.toBeInTheDocument();
    fireEvent.click(screen.getByText('Maria da Silva'));
    await waitFor(() => expect(screen.getByText('Dados civis')).toBeInTheDocument());
    expect(screen.getByText('País de origem')).toBeInTheDocument();
    expect(screen.queryByText(/vida sacramental/i)).not.toBeInTheDocument();
  });

  it('mostra pendência cadastral apenas na ficha', async () => {
    vi.mocked(listarAjustesPendentesDoFiel).mockResolvedValue([{id:'ajuste-1',fielId:'fiel-1',status:'PENDENTE',email:'novo@test.com',celular:null,telefoneFixo:null,endereco:{logradouro:'Rua Nova'},criadaEmUtc:'2026-01-01'}]);
    renderPage();
    await screen.findByText('Maria da Silva');
    expect(screen.queryByText(/ajuste cadastral pendente/i)).not.toBeInTheDocument();
    fireEvent.click(screen.getByText('Maria da Silva'));
    expect(await screen.findByText(/ajuste cadastral pendente/i)).toBeInTheDocument();
  });

  it('mantém a ficha somente leitura para coordenador', async () => {
    useAuthMock.mockReturnValue({ user:{id:'coord',roles:['CONSELHO_COORDENADOR']}, isAuthenticated:true });
    renderPage();
    await screen.findByText('Maria da Silva');
    expect(screen.queryByRole('button', {name:/novo fiel/i})).not.toBeInTheDocument();
    fireEvent.click(screen.getByText('Maria da Silva'));
    await screen.findByText('Ficha do fiel');
    expect(screen.queryByRole('button', {name:/salvar alterações/i})).not.toBeInTheDocument();
    expect(screen.queryByRole('button', {name:/vincular/i})).not.toBeInTheDocument();
  });
});
