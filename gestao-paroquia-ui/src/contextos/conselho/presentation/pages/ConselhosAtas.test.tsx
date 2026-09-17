import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import ConselhosAtas from './ConselhosAtas';
import { QueryProvider } from '@/shared/presentation/context/QueryProvider';
import { NotificationProvider } from '@/shared/presentation/context/NotificationContext';
import * as conselhosApi from '@/contextos/conselho/infrastructure/http/conselhos';
import * as organizacoesApi from '@/contextos/organizacao/infrastructure/http/organizacoes';

vi.mock('@/contextos/iam/presentation/auth/AuthContext', () => ({
  useAuth: () => ({
    user: {
      id: '1',
      name: 'Pároco Teste',
      roles: ['ROLE_PAROCO'],
      activeOrganizationId: 'org-1',
      organizations: [],
    },
    isAuthenticated: true,
  }),
}));

vi.mock('@/contextos/conselho/infrastructure/http/conselhos');
vi.mock('@/contextos/organizacao/infrastructure/http/organizacoes');

const renderComponent = () => {
  return render(
    <QueryProvider>
      <NotificationProvider>
        <BrowserRouter>
          <ConselhosAtas />
        </BrowserRouter>
      </NotificationProvider>
    </QueryProvider>
  );
};

describe('ConselhosAtas Page', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.spyOn(conselhosApi, 'getCpcDecisoes').mockResolvedValue([
      {
        id: 'dec-1',
        titulo: 'Reforma do Telhado da Capela',
        pautaDescricao: 'Aprovação de orçamento para reforma',
        dataReuniao: '2026-07-29',
        comunidadeId: 'comm-1',
        conselhoTipo: 'CPC',
        statusHomologacao: 'PENDENTE_HOMOLOGACAO',
        solicitanteId: 'user-1',
        criadoEmUtc: '2026-07-29T00:00:00Z',
      },
    ]);
    vi.spyOn(organizacoesApi, 'getOrganizacoes').mockResolvedValue([
      { id: 'comm-1', nome: 'Comunidade São José', tipo: 'COMUNIDADE', contato: null, ativo: true },
    ]);
  });

  it('renders title and decisions table', async () => {
    renderComponent();

    expect(screen.getByText('Governança dos Conselhos (CPP & CPC)')).toBeInTheDocument();
    await waitFor(() => {
      expect(screen.getByText('Reforma do Telhado da Capela')).toBeInTheDocument();
      expect(screen.getByText('Comunidade São José')).toBeInTheDocument();
    });
  });

  it('opens create modal when clicking Nova Ata button', async () => {
    renderComponent();

    const newBtn = screen.getByRole('button', { name: /Nova Ata \/ Decisão/i });
    fireEvent.click(newBtn);

    expect(screen.getByText('Registrar Ata / Decisão de Conselho (CPC / CPP)')).toBeInTheDocument();
  });
});
