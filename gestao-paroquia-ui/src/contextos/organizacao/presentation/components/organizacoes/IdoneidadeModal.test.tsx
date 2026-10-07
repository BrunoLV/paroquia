import React from 'react';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { IdoneidadeModal } from './IdoneidadeModal';
import * as membrosApi from '@/contextos/membro/infrastructure/http/membros';
import { NotificationProvider } from '@/shared/presentation/context/NotificationContext';
import { QueryProvider } from '@/shared/presentation/context/QueryProvider';

vi.mock('@/contextos/membro/infrastructure/http/membros');

const renderModal = (onClose = () => undefined) =>
  render(
    <QueryProvider>
      <NotificationProvider>
        <IdoneidadeModal open onClose={onClose} vinculoId="v-1" nome="Maria da Silva" organizacaoId="org-1" />
      </NotificationProvider>
    </QueryProvider>,
  );

describe('IdoneidadeModal', () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it('mostra as pendências da função e carrega as datas já atestadas', async () => {
    vi.mocked(membrosApi.getIdoneidade).mockResolvedValue({
      formacaoConcluidaEm: '2025-01-01',
      apto: false,
      pendencias: ['Termo de proteção de menores e vulneráveis ausente ou vencido.'],
    });
    renderModal();

    expect(await screen.findByText(/Termo de proteção de menores e vulneráveis ausente/)).toBeInTheDocument();
    expect(screen.getByLabelText(/Formação doutrinária básica concluída em/)).toHaveValue('2025-01-01');
  });

  it('registra somente as datas informadas e fecha ao salvar', async () => {
    vi.mocked(membrosApi.getIdoneidade).mockResolvedValue({ apto: false, pendencias: ['Formação doutrinária básica não registrada.'] });
    vi.mocked(membrosApi.registrarIdoneidade).mockResolvedValue({ apto: true, pendencias: [] });
    const onClose = vi.fn();
    renderModal(onClose);

    await screen.findByText(/Formação doutrinária básica não registrada/);
    fireEvent.change(screen.getByLabelText(/Envio \(missão\) conferido em/), { target: { value: '2026-05-01' } });
    fireEvent.click(screen.getByRole('button', { name: /^Salvar$/ }));

    await waitFor(() => expect(membrosApi.registrarIdoneidade).toHaveBeenCalledWith('v-1', {
      formacaoConcluidaEm: null,
      vivenciaSacramentalAtestadaEm: null,
      termoProtecaoMenoresEm: null,
      termoProtecaoMenoresValidoAte: null,
      enviadoEm: '2026-05-01',
    }));
    await waitFor(() => expect(onClose).toHaveBeenCalled());
  });
});
