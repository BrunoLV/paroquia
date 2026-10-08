import React from 'react';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { OrganizacaoMandatosModal } from './OrganizacaoMandatosModal';
import * as mandatosApi from '@/contextos/organizacao/infrastructure/http/mandatos';
import * as organizacoesApi from '@/contextos/organizacao/infrastructure/http/organizacoes';
import { NotificationProvider } from '@/shared/presentation/context/NotificationContext';
import { QueryProvider } from '@/shared/presentation/context/QueryProvider';
import { MandatoCoordenacao, Organizacao } from '@/shared/domain/types/types';

vi.mock('@/contextos/organizacao/infrastructure/http/mandatos');
vi.mock('@/contextos/organizacao/infrastructure/http/organizacoes');

const organizacao: Organizacao = { id: 'org-1', nome: 'Pastoral Familiar', tipo: 'PASTORAL', contato: null, ativo: true };

const mandato = (parcial: Partial<MandatoCoordenacao>): MandatoCoordenacao => ({
  id: 'm-1',
  organizacaoId: 'org-1',
  fielId: 'f-1',
  fielNome: 'Maria da Silva',
  funcao: 'COORDENADOR',
  inicio: '2026-01-01',
  fim: '2027-12-31',
  status: 'PROPOSTO',
  vigente: false,
  ...parcial,
});

const renderModal = (canPropor: boolean, canHomologar: boolean) =>
  render(
    <QueryProvider>
      <NotificationProvider>
        <OrganizacaoMandatosModal
          open
          onClose={() => undefined}
          organizacao={organizacao}
          canPropor={canPropor}
          canHomologar={canHomologar}
        />
      </NotificationProvider>
    </QueryProvider>,
  );

describe('OrganizacaoMandatosModal', () => {
  beforeEach(() => {
    vi.resetAllMocks();
    vi.mocked(mandatosApi.getMandatos).mockResolvedValue([
      mandato({}),
      mandato({ id: 'm-2', status: 'HOMOLOGADO', vigente: true, fielNome: 'João Souza', funcao: 'VICE_COORDENADOR' }),
    ]);
    vi.mocked(organizacoesApi.getOrganizacaoMembros).mockResolvedValue([
      { vinculoId: 'v-1', fielId: 'f-1', fielNome: 'Maria da Silva', papel: 'membro-ativo', ativo: true },
    ]);
  });

  it('lista os mandatos com a situação canônica', async () => {
    renderModal(false, false);

    expect(await screen.findByText('Maria da Silva')).toBeInTheDocument();
    expect(screen.getByText('Aguardando homologação')).toBeInTheDocument();
    expect(screen.getByText('Homologado')).toBeInTheDocument();
    expect(screen.getByText('Vice-Coordenador')).toBeInTheDocument();
  });

  it('sem permissão não oferece proposta nem ações de homologação', async () => {
    renderModal(false, false);

    await screen.findByText('Maria da Silva');
    expect(screen.queryByText('Propor mandato')).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /Ações/i })).not.toBeInTheDocument();
  });

  it('Pároco homologa a proposta pelo menu de ações', async () => {
    vi.mocked(mandatosApi.homologarMandato).mockResolvedValue(mandato({ status: 'HOMOLOGADO' }));
    renderModal(true, true);

    await screen.findByText('Maria da Silva');
    fireEvent.click(screen.getAllByRole('button', { name: /Ações/i })[0]);
    fireEvent.click(await screen.findByRole('menuitem', { name: /Homologar/i }));

    await waitFor(() => expect(mandatosApi.homologarMandato).toHaveBeenCalledWith('org-1', 'm-1'));
  });

  it('veto exige motivo', async () => {
    vi.mocked(mandatosApi.vetarMandato).mockResolvedValue(mandato({ status: 'VETADO' }));
    renderModal(true, true);

    await screen.findByText('Maria da Silva');
    fireEvent.click(screen.getAllByRole('button', { name: /Ações/i })[0]);
    fireEvent.click(await screen.findByRole('menuitem', { name: /Vetar/i }));

    const confirmar = await screen.findByRole('button', { name: /^Vetar$/ });
    expect(confirmar).toBeDisabled();
    fireEvent.change(screen.getByLabelText(/Motivo do veto/i), { target: { value: 'Sem formação básica' } });
    expect(confirmar).toBeEnabled();
    fireEvent.click(confirmar);

    await waitFor(() => expect(mandatosApi.vetarMandato).toHaveBeenCalledWith('org-1', 'm-1', 'Sem formação básica'));
  });
});
