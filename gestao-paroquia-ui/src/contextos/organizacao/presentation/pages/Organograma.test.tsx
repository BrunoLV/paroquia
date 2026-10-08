import {vi} from "vitest";
import React from 'react';
import {act, fireEvent, render, screen} from '@testing-library/react';
import Organograma from './Organograma';
import {ThemeProvider} from '@mui/material/styles';
import theme from '@/shared/presentation/theme/theme';
import {MemoryRouter} from 'react-router-dom';
import apiClient from '@/shared/infrastructure/http/client';
import {AuthProvider} from '@/contextos/iam/presentation/auth/AuthContext';
import {QueryProvider} from '@/shared/presentation/context/QueryProvider';

vi.mock('@/shared/infrastructure/http/client');
const mockedApiClient = apiClient as jest.Mocked<typeof apiClient>;

describe('Organograma Page', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  const mockOrganogramaData = {
    id: 'org-clero',
    label: 'Clero Paroquial',
    type: 'ORGANIZACAO',
    subtype: 'CLERO',
    responsavelNome: 'Padre Pedro',
    responsavelPapel: 'PÁROCO',
    viceResponsavelNome: 'João Batista',
    children: [
      {
        id: 'org-conselho',
        label: 'Conselho Paroquial',
        type: 'ORGANIZACAO',
        subtype: 'CONSELHO',
        responsavelNome: 'Maria do Conselho',
        responsavelPapel: 'COORDENADOR',
        viceResponsavelNome: 'José do Conselho',
        children: [
          {
            id: 'org-catequese',
            label: 'Pastoral da Catequese',
            type: 'ORGANIZACAO',
            subtype: 'PASTORAL',
            responsavelNome: 'Maria Oliveira',
            responsavelPapel: 'COORDENADOR',
            viceResponsavelNome: 'Lúcia de Oliveira',
            children: [
              {
                id: 'membro-carlos',
                label: 'Carlos de Oliveira',
                type: 'MEMBRO',
                subtype: 'membro',
                responsavelNome: null,
                responsavelPapel: null,
                viceResponsavelNome: null,
                children: []
              }
            ]
          }
        ]
      },
      {
        id: 'org-economico',
        label: 'Conselho Econômico',
        type: 'ORGANIZACAO',
        subtype: 'CONSELHO_ECONOMICO',
        responsavelNome: 'Pedro Finanças',
        responsavelPapel: 'COORDENADOR',
        viceResponsavelNome: 'Ana Administração',
        children: []
      }
    ]
  };

  const renderOrganograma = async (user: any) => {
    mockedApiClient.get.mockImplementation((url) => {
      if (url === '/autenticacao/meu-perfil') {
        return Promise.resolve({ data: user });
      }
      if (url === '/organizacoes/organograma') {
        return Promise.resolve({ data: mockOrganogramaData });
      }
      return Promise.reject(new Error('Not found'));
    });

    await act(async () => {
      render(
        <MemoryRouter>
          <QueryProvider>
            <AuthProvider>
              <ThemeProvider theme={theme}>
                <Organograma />
              </ThemeProvider>
            </AuthProvider>
          </QueryProvider>
        </MemoryRouter>
      );
    });
  };

  test('renders page structure and nodes correctly', async () => {
    const adminUser = { id: '1', name: 'Admin', roles: ['ADMIN'], organizations: [] };
    await renderOrganograma(adminUser);

    expect(await screen.findByText('Clero Paroquial')).toBeInTheDocument();
    expect(screen.getByText('Conselho Paroquial')).toBeInTheDocument();
    expect(screen.getByText('Conselho Econômico')).toBeInTheDocument();
    
    // In tree mode, the intermediate "Pastorais e Movimentos" group is not rendered.
    expect(screen.queryByText('Pastorais e Movimentos')).not.toBeInTheDocument();
    expect(screen.getByText('Pastoral da Catequese')).toBeInTheDocument();
    
    // Check responsibles names
    expect(screen.getByText(/Padre Pedro/i)).toBeInTheDocument();
    expect(screen.getByText('Maria do Conselho')).toBeInTheDocument();
    expect(screen.getByText('Maria Oliveira')).toBeInTheDocument();

    // Export button should be visible in tree mode
    expect(screen.getByRole('button', { name: /Exportar Organograma/i })).toBeInTheDocument();

    // Now, switch to Grid view
    const gridBtn = screen.getByRole('button', { name: /Visualização em Grade/i });
    await act(async () => {
      fireEvent.click(gridBtn);
    });

    // In grid mode, "Pastorais e Movimentos" group header should be visible
    expect(screen.getByText('Pastorais e Movimentos')).toBeInTheDocument();
    // Export button should NOT be visible in grid mode
    expect(screen.queryByRole('button', { name: /Exportar Organograma/i })).not.toBeInTheDocument();
  });

  test('expands members list inside pastoral card on click', async () => {
    const user = { id: '2', name: 'User', roles: ['USER'], organizations: [] };
    await renderOrganograma(user);

    expect(await screen.findByText('Clero Paroquial')).toBeInTheDocument();

    // Member 'Carlos de Oliveira' should not be initially visible since the card is collapsed
    expect(screen.queryByText('Carlos de Oliveira')).not.toBeInTheDocument();

    const expandBtn = screen.getByRole('button', { name: /Ver Membros/i });
    await act(async () => {
      fireEvent.click(expandBtn);
    });

    // Now it should be visible
    expect(screen.getByText('Carlos de Oliveira')).toBeInTheDocument();
  });

  test('renders tree correctly with UNIDADE_TERRITORIAL and ORGANIZACAO node types', async () => {
    const mockRefactoredData = {
      id: 'ut-clero',
      label: 'Clero Paroquial',
      type: 'UNIDADE_TERRITORIAL',
      subtype: 'CLERO',
      responsavelNome: 'Padre Antônio',
      responsavelPapel: 'PÁROCO',
      viceResponsavelNome: null,
      children: [
        {
          id: 'ut-matriz',
          label: 'Matriz São José',
          type: 'UNIDADE_TERRITORIAL',
          subtype: 'MATRIZ',
          responsavelNome: null,
          responsavelPapel: null,
          viceResponsavelNome: null,
          children: [
            {
              id: 'gp-conselho',
              label: 'Conselho Pastoral Matriz',
              type: 'ORGANIZACAO',
              subtype: 'CONSELHO',
              responsavelNome: 'Coordenador Matriz',
              responsavelPapel: 'COORDENADOR',
              viceResponsavelNome: null,
              children: [
                {
                  id: 'gp-ministerio',
                  label: 'Ministério de Música',
                  type: 'ORGANIZACAO',
                  subtype: 'MINISTERIO',
                  responsavelNome: 'Líder de Música',
                  responsavelPapel: 'COORDENADOR',
                  viceResponsavelNome: null,
                  children: [],
                },
              ],
            },
          ],
        },
      ],
    };

    mockedApiClient.get.mockImplementation((url) => {
      if (url === '/autenticacao/meu-perfil') {
        return Promise.resolve({ data: { id: '1', name: 'Admin', roles: ['ADMIN'] } });
      }
      if (url === '/organizacoes/organograma') {
        return Promise.resolve({ data: mockRefactoredData });
      }
      return Promise.reject(new Error('Not found'));
    });

    await act(async () => {
      render(
        <MemoryRouter>
          <QueryProvider>
            <AuthProvider>
              <ThemeProvider theme={theme}>
                <Organograma />
              </ThemeProvider>
            </AuthProvider>
          </QueryProvider>
        </MemoryRouter>
      );
    });

    expect(await screen.findByText('Clero Paroquial')).toBeInTheDocument();
    expect(screen.getByText('Matriz São José')).toBeInTheDocument();
    expect(screen.getByText('Conselho Pastoral Matriz')).toBeInTheDocument();
    expect(screen.getByText('Ministério de Música')).toBeInTheDocument();
  });
});
