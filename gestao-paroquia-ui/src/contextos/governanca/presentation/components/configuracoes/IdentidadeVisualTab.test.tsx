import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import { IdentidadeVisualTab } from './IdentidadeVisualTab';
import * as configApi from '@/contextos/governanca/infrastructure/http/configuracaoParoquia';
import { NotificationProvider } from '@/shared/presentation/context/NotificationContext';
import { QueryProvider } from '@/shared/presentation/context/QueryProvider';

vi.mock('@/contextos/governanca/infrastructure/http/configuracaoParoquia');

const mockConfiguracao: configApi.ParoquiaConfiguracao = {
  id: 'uuid-1',
  nome: 'Paróquia Nossa Senhora de Fátima',
  diocese: 'Diocese de Santo André',
  endereco: 'Praça da Matriz, 100',
  telefone: '(11) 4455-6677',
  email: 'contato@fatima.org.br',
  cnpj: '12.345.678/0001-90',
  corPrimaria: '#1976D2',
  corSecundaria: '#9C27B0',
  temLogoFundoClaro: true,
  temLogoFundoEscuro: false,
  logoFundoClaroUrl: '/api/v1/configuracao-paroquia/logo/claro',
  logoFundoEscuroUrl: '/api/v1/configuracao-paroquia/logo/escuro',
};

const renderWithProviders = (ui: React.ReactElement) => {
  return render(
    <QueryProvider>
      <NotificationProvider>
        {ui}
      </NotificationProvider>
    </QueryProvider>
  );
};

describe('IdentidadeVisualTab', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('deve renderizar dados institucionais e campos de cores corretamente', async () => {
    vi.mocked(configApi.obterConfiguracaoParoquia).mockResolvedValueOnce(mockConfiguracao);

    renderWithProviders(<IdentidadeVisualTab />);

    expect(await screen.findByText('Paróquia Nossa Senhora de Fátima')).toBeInTheDocument();
    expect(screen.getByText('Diocese de Santo André')).toBeInTheDocument();
    expect(screen.getByText('12.345.678/0001-90')).toBeInTheDocument();
    expect(screen.getByText('Logotipo para Fundo Claro')).toBeInTheDocument();
    expect(screen.getByText('Logotipo para Fundo Escuro')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Salvar Cores/i })).toBeInTheDocument();
  });

  it('deve permitir atualizar cores institucionais', async () => {
    vi.mocked(configApi.obterConfiguracaoParoquia).mockResolvedValueOnce(mockConfiguracao);
    vi.mocked(configApi.atualizarIdentidadeVisual).mockResolvedValueOnce({
      ...mockConfiguracao,
      corPrimaria: '#003366',
    });

    renderWithProviders(<IdentidadeVisualTab />);

    await screen.findByText('Paróquia Nossa Senhora de Fátima');

    const salvarBtn = screen.getByRole('button', { name: /Salvar Cores/i });
    fireEvent.click(salvarBtn);

    await waitFor(() => {
      expect(configApi.atualizarIdentidadeVisual).toHaveBeenCalledWith({
        corPrimaria: '#1976D2',
        corSecundaria: '#9C27B0',
      });
    });
  });
});
