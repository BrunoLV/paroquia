import React from 'react';
import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { OrganizacaoMembrosModal } from './OrganizacaoMembrosModal';
import { QueryProvider } from '@/shared/presentation/context/QueryProvider';
import { NotificationProvider } from '@/shared/presentation/context/NotificationContext';
import { Organizacao } from '@/shared/domain/types/types';

vi.mock('@/contextos/organizacao/infrastructure/http/organizacoes', () => ({
  getOrganizacaoMembros: vi.fn().mockResolvedValue([]),
  getPapeisPorTipo: vi.fn().mockResolvedValue({
    CONSELHO: [
      { codigo: 'SECRETARIO_CONSELHO', descricao: 'Secretário de Conselho' },
      { codigo: 'MEMBRO_CONSELHO_PASTORAL', descricao: 'Membro do Conselho Pastoral' },
      { codigo: 'COORDENADOR', descricao: 'Coordenador' },
      { codigo: 'SECRETARIO_GRUPO', descricao: 'Secretário de Grupo' },
    ],
    PASTORAL: [
      { codigo: 'COORDENADOR', descricao: 'Coordenador' },
      { codigo: 'VOLUNTARIO', descricao: 'Voluntário' },
    ],
  }),
}));
vi.mock('@/contextos/iam/infrastructure/http/usuarios', () => ({
  adicionarVinculo: vi.fn(),
  removerVinculo: vi.fn(),
}));

const org = (tipo: string): Organizacao => ({ id: 'o1', nome: 'Órgão', tipo, contato: null, ativo: true });

const renderizar = (organizacao: Organizacao) => render(
  <QueryProvider>
    <NotificationProvider>
      <OrganizacaoMembrosModal open onClose={() => undefined} organizacao={organizacao} igrejas={[]} usuarios={[]} />
    </NotificationProvider>
  </QueryProvider>,
);

describe('OrganizacaoMembrosModal', () => {
  beforeEach(() => vi.clearAllMocks());

  it('no conselho oferece só os papéis do conselho, com o coordenador e o vice do conselho', async () => {
    const user = userEvent.setup();
    renderizar(org('CONSELHO'));

    await user.click(await screen.findByRole('combobox', { name: 'Papel' }));

    expect(await screen.findByRole('option', { name: 'Coordenador do conselho' })).toBeInTheDocument();
    expect(screen.getByRole('option', { name: 'Vice-coordenador do conselho' })).toBeInTheDocument();
    expect(screen.getByRole('option', { name: 'Secretário de Conselho' })).toBeInTheDocument();
    expect(screen.queryByRole('option', { name: /Voluntário/ })).not.toBeInTheDocument();
  });

  it('na pastoral não oferece papéis de conselho', async () => {
    const user = userEvent.setup();
    renderizar(org('PASTORAL'));

    await user.click(await screen.findByRole('combobox', { name: 'Papel' }));

    expect(await screen.findByRole('option', { name: /Voluntário/ })).toBeInTheDocument();
    expect(within(screen.getByRole('listbox')).queryByText(/Conselho/)).not.toBeInTheDocument();
  });
});
