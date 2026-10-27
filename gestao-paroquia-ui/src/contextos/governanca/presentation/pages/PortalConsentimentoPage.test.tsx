import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import PortalConsentimentoPage from './PortalConsentimentoPage';
import * as consentimentoApi from '@/contextos/governanca/infrastructure/http/consentimentoPublico';
import { NotificationProvider } from '@/shared/presentation/context/NotificationContext';
import { QueryProvider } from '@/shared/presentation/context/QueryProvider';

vi.mock('@/contextos/governanca/infrastructure/http/consentimentoPublico');

describe('PortalConsentimentoPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  const renderComponent = () =>
    render(
      <QueryProvider>
        <NotificationProvider>
          <MemoryRouter>
            <PortalConsentimentoPage />
          </MemoryRouter>
        </NotificationProvider>
      </QueryProvider>
    );

  it('deve renderizar a etapa inicial de identificação por CPF sem exigir usuário', () => {
    renderComponent();

    expect(screen.getByText(/Portal de Privacidade e Consentimento/i)).toBeInTheDocument();
    expect(screen.getByText(/Identificação do Titular/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/000.000.000-00/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Enviar Código de Verificação/i })).toBeInTheDocument();
  });

  it('deve solicitar código OTP e avançar para a etapa de validação', async () => {
    vi.mocked(consentimentoApi.solicitarCodigoConsentimento).mockResolvedValueOnce({
      emailMascarado: '***@***',
      expiraEmSegundos: 600,
      mensagem: 'Se este CPF estiver cadastrado, um código foi enviado.',
    });

    renderComponent();

    const cpfInput = screen.getByPlaceholderText(/000.000.000-00/i);
    fireEvent.change(cpfInput, { target: { value: '12345678909' } });

    const btnEnviar = screen.getByRole('button', { name: /Enviar Código de Verificação/i });
    fireEvent.click(btnEnviar);

    await waitFor(() => {
      expect(consentimentoApi.solicitarCodigoConsentimento).toHaveBeenCalledWith({
        cpf: '12345678909',
      });
      expect(screen.getByText(/Validação de Posse de E-mail/i)).toBeInTheDocument();
      expect(screen.getAllByText(/Se este CPF estiver cadastrado, um código foi enviado/i).length).toBeGreaterThan(0);
      expect(screen.queryByText(/@teste\.com/i)).not.toBeInTheDocument();
    });
  });
});
