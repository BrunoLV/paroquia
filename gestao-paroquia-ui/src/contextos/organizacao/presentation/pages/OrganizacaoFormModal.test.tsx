import {vi} from "vitest";
import React from 'react';
import {fireEvent, render, screen, waitFor} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import OrganizacaoFormModal from './OrganizacaoFormModal';
import {ThemeProvider} from '@mui/material/styles';
import theme from '@/shared/presentation/theme/theme';
import {Organizacao} from '@/shared/domain/types/types';
import {QueryClient, QueryClientProvider} from '@tanstack/react-query';


describe('OrganizacaoFormModal', () => {
  const mockOnSave = jest.fn();
  const mockOnClose = jest.fn();

  const mockOrganizacoes: Organizacao[] = [
    { id: 'org-matriz', nome: 'Igreja Matriz', tipo: 'MATRIZ', contato: '', ativo: true },
    { id: 'org-comunidade', nome: 'Comunidade São José', tipo: 'COMUNIDADE', contato: '', ativo: true },
    { id: 'org-conselho', nome: 'Conselho Pastoral Paroquial (CPP)', tipo: 'CONSELHO', subtipoConselho: 'CPP', contato: '', parentId: 'org-matriz', ativo: true },
    { id: 'org-caep', nome: 'Conselho Econômico (CAEP)', tipo: 'CONSELHO', subtipoConselho: 'CAEP', contato: '', parentId: 'org-matriz', ativo: true },
    { id: 'org-pastoral', nome: 'Pastoral da Música', tipo: 'PASTORAL', contato: '', parentId: 'org-conselho', ativo: true },
    { id: 'org-inativo-conselho', nome: 'Conselho Inativo', tipo: 'CONSELHO', subtipoConselho: 'CPP', contato: '', parentId: 'org-matriz', ativo: false },
  ];

  const mockPastoraisOficiais = [
    { id: 'po-pastoral-1', nome: 'Catequese Oficial', tipo: 'PASTORAL' as const },
    { id: 'po-pastoral-2', nome: 'Dízimo Oficial', tipo: 'PASTORAL' as const },
    { id: 'po-movimento-1', nome: 'EJC Oficial', tipo: 'MOVIMENTO' as const, estatutoReconhecido: true },
    { id: 'po-movimento-2', nome: 'Movimento sem Estatuto', tipo: 'MOVIMENTO' as const, estatutoReconhecido: false },
  ];

  const defaultProps = {
    open: true,
    onClose: mockOnClose,
    onSave: mockOnSave,
    isPending: false,
    organizacoes: mockOrganizacoes,
    usuarios: [],
    igrejas: [],
    pastoraisOficiais: mockPastoraisOficiais,
  };

  const renderModal = (props: {
    open?: boolean;
    onClose?: () => void;
    onSave?: (data: any) => void;
    organizacao?: Organizacao | null;
    isPending?: boolean;
    usuarios?: any[];
    igrejas?: Organizacao[];
    organizacoes?: Organizacao[];
    pastoraisOficiais?: any[];
  } = {}) => {
    const queryClient = new QueryClient({
      defaultOptions: {
        queries: {
          retry: false,
        },
      },
    });
    const mergedProps = { ...defaultProps, ...props };
    return render(
      <QueryClientProvider client={queryClient}>
        <ThemeProvider theme={theme}>
          <OrganizacaoFormModal {...mergedProps} />
        </ThemeProvider>
      </QueryClientProvider>
    );
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('conselho se articula a Matriz ou Comunidade; demais organismos, a conselho pastoral', () => {
    renderModal();

    const tipoSelect = screen.getByLabelText(/^Tipo\s*\*?$/);
    fireEvent.mouseDown(tipoSelect);
    fireEvent.click(screen.getByRole('option', { name: 'Conselho' }));

    fireEvent.mouseDown(screen.getByLabelText(/Matriz \/ Comunidade/i));
    expect(screen.getByRole('option', { name: 'Igreja Matriz' })).toBeInTheDocument();
    expect(screen.getByRole('option', { name: 'Comunidade São José' })).toBeInTheDocument();
    expect(screen.queryByRole('option', { name: 'Conselho Pastoral Paroquial (CPP)' })).not.toBeInTheDocument();
  });

  test('pastoral aceita só conselhos pastorais ativos como mãe, nunca o CAEP', () => {
    renderModal();

    fireEvent.mouseDown(screen.getByLabelText(/Conselho Pastoral \(CPP\/CPC\)/i));

    expect(screen.getByRole('option', { name: 'Conselho Pastoral Paroquial (CPP)' })).toBeInTheDocument();
    expect(screen.queryByRole('option', { name: 'Conselho Econômico (CAEP)' })).not.toBeInTheDocument();
    expect(screen.queryByRole('option', { name: 'Conselho Inativo' })).not.toBeInTheDocument();
  });

  test('submits tipologia, múnus e conselho mãe ao salvar pastoral', async () => {
    renderModal();

    fireEvent.change(screen.getByLabelText(/Nome/i), { target: { value: 'Nova Pastoral' } });

    fireEvent.mouseDown(screen.getAllByLabelText(/Tipo Oficial \/ Movimento/i)[0]);
    fireEvent.click(screen.getByRole('option', { name: 'Catequese Oficial' }));

    fireEvent.mouseDown(screen.getByLabelText(/Múnus/i));
    fireEvent.click(screen.getByRole('option', { name: /^Ensinar/ }));

    fireEvent.mouseDown(screen.getByLabelText(/Conselho Pastoral \(CPP\/CPC\)/i));
    fireEvent.click(screen.getByRole('option', { name: 'Conselho Pastoral Paroquial (CPP)' }));

    fireEvent.submit(screen.getByRole('button', { name: /^Salvar$/i }));

    await waitFor(() => expect(mockOnSave).toHaveBeenCalledWith(
      expect.objectContaining({
        nome: 'Nova Pastoral',
        tipo: 'PASTORAL',
        parentId: 'org-conselho',
        pastoralOficialId: 'po-pastoral-1',
        setorPastoral: 'FORMACAO_ENSINAR',
        subtipoConselho: null,
      })
    ));
  });

  test('catálogo oficial só aparece para pastoral e movimento; movimento sem estatuto fica desabilitado', async () => {
    const user = userEvent.setup();
    renderModal();

    await user.click(screen.getByRole('combobox', { name: /Tipo Oficial \/ Movimento/i }));
    expect(screen.getByRole('option', { name: 'Catequese Oficial' })).toBeInTheDocument();
    expect(screen.queryByRole('option', { name: 'EJC Oficial' })).not.toBeInTheDocument();
    await user.keyboard('{Escape}');

    const tipoSelect = screen.getByLabelText(/^Tipo\s*\*?$/);
    await user.click(tipoSelect);
    await user.click(screen.getByRole('option', { name: 'Movimento Apostólico' }));

    await user.click(screen.getByRole('combobox', { name: /Tipo Oficial \/ Movimento/i }));
    expect(screen.getByRole('option', { name: 'EJC Oficial' })).toBeInTheDocument();
    expect(screen.getByRole('option', { name: /Movimento sem Estatuto/ })).toHaveAttribute('aria-disabled', 'true');
    await user.keyboard('{Escape}');

    await user.click(tipoSelect);
    await user.click(screen.getByRole('option', { name: 'Serviço Paroquial' }));
    expect(screen.queryAllByLabelText(/Tipo Oficial \/ Movimento/i).length).toBe(0);
  });

  test('exige vínculo oficial, múnus e conselho mãe para pastoral', async () => {
    renderModal();

    fireEvent.change(screen.getByLabelText(/Nome/i), { target: { value: 'Nova Pastoral' } });
    fireEvent.submit(screen.getByRole('button', { name: /^Salvar$/i }));

    await waitFor(() => expect(screen.getByText('A associação oficial é obrigatória')).toBeInTheDocument());
    expect(screen.getByText(/O múnus .* é obrigatório/)).toBeInTheDocument();
    expect(screen.getByText(/Articule o organismo a um conselho pastoral/)).toBeInTheDocument();
    expect(mockOnSave).not.toHaveBeenCalled();
  });

  test('conselho exige subtipo', async () => {
    renderModal();

    fireEvent.change(screen.getByLabelText(/Nome/i), { target: { value: 'Novo Conselho' } });
    fireEvent.mouseDown(screen.getByLabelText(/^Tipo\s*\*?$/));
    fireEvent.click(screen.getByRole('option', { name: 'Conselho' }));
    fireEvent.submit(screen.getByRole('button', { name: /^Salvar$/i }));

    await waitFor(() => expect(screen.getByText('O subtipo do conselho é obrigatório')).toBeInTheDocument());
    expect(mockOnSave).not.toHaveBeenCalled();
  });

  test('coordenação não é editável no cadastro: informa o mandato homologado', () => {
    renderModal({
      organizacao: { id: 'org-pastoral', nome: 'Pastoral da Música', tipo: 'PASTORAL', contato: '', parentId: 'org-conselho', ativo: true, coordenador: 'Maria da Silva' }
    });

    expect(screen.queryByRole('combobox', { name: /^Coordenador$/ })).not.toBeInTheDocument();
    expect(screen.getByText(/Coordenação atual: Maria da Silva/)).toBeInTheDocument();
    expect(screen.getByText(/mandato temporário proposto pelo grupo e homologado pelo Pároco/)).toBeInTheDocument();
  });

  test('contato é separado em e-mail e telefone e ambos são enviados', async () => {
    renderModal();

    fireEvent.change(screen.getByLabelText(/Nome/i), { target: { value: 'Nova Pastoral' } });
    fireEvent.change(screen.getByLabelText(/^E-mail$/), { target: { value: 'pastoral@nsfatima.com' } });
    fireEvent.change(screen.getByLabelText(/^Telefone$/), { target: { value: '(11) 99999-0000' } });

    fireEvent.mouseDown(screen.getAllByLabelText(/Tipo Oficial \/ Movimento/i)[0]);
    fireEvent.click(screen.getByRole('option', { name: 'Catequese Oficial' }));
    fireEvent.mouseDown(screen.getByLabelText(/Múnus/i));
    fireEvent.click(screen.getByRole('option', { name: /^Ensinar/ }));
    fireEvent.mouseDown(screen.getByLabelText(/Conselho Pastoral \(CPP\/CPC\)/i));
    fireEvent.click(screen.getByRole('option', { name: 'Conselho Pastoral Paroquial (CPP)' }));
    fireEvent.submit(screen.getByRole('button', { name: /^Salvar$/i }));

    await waitFor(() => expect(mockOnSave).toHaveBeenCalledWith(
      expect.objectContaining({ email: 'pastoral@nsfatima.com', telefone: '(11) 99999-0000', contato: null })
    ));
  });

  test('e-mail inválido bloqueia o envio', async () => {
    renderModal();

    fireEvent.change(screen.getByLabelText(/Nome/i), { target: { value: 'Nova Pastoral' } });
    fireEvent.change(screen.getByLabelText(/^E-mail$/), { target: { value: 'sem-arroba' } });
    fireEvent.submit(screen.getByRole('button', { name: /^Salvar$/i }));

    await waitFor(() => expect(screen.getByText('E-mail inválido')).toBeInTheDocument());
    expect(mockOnSave).not.toHaveBeenCalled();
  });

  test('renders action buttons with right alignment and standardized labels', () => {
    renderModal();
    const cancelButton = screen.getByRole('button', { name: /Cancelar/i });
    const submitButton = screen.getByRole('button', { name: /^Salvar$/i });

    expect(cancelButton).toBeInTheDocument();
    expect(submitButton).toBeInTheDocument();
  });

  test('renders action buttons with their semantic icons', () => {
    renderModal();
    const cancelButton = screen.getByRole('button', { name: /Cancelar/i });
    const submitButton = screen.getByRole('button', { name: /^Salvar$/i });

    expect(cancelButton.querySelector('[data-testid="CloseIcon"]')).toBeInTheDocument();
    expect(submitButton.querySelector('[data-testid="SaveIcon"]')).toBeInTheDocument();
  });
});
