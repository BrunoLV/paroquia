import React from 'react';
import { render, screen } from '@testing-library/react';
import { ThemeProvider } from '@mui/material/styles';
import { MemoryRouter } from 'react-router-dom';
import theme from '@/shared/presentation/theme/theme';
import ListaProgressoProjetosAtivos from './ListaProgressoProjetosAtivos';
import type { ProjectDetail } from '@/shared/domain/types/types';

describe('ListaProgressoProjetosAtivos', () => {
  it('renderiza projetos e destaca projetos em risco no topo com diagnóstico explícito', () => {
    const projects: ProjectDetail[] = [
      {
        nome: 'Projeto Saudável',
        status: 'EM_ANDAMENTO',
        percentualTempoDecorrido: 40,
        totalEventos: 10,
        eventosRealizados: 4,
        percentualExecucao: 40,
        eventosPendentesAprovacao: 0,
        emRisco: false,
        proximoMarco: null,
        envolvidos: ['Pastoral Litúrgica'],
      },
      {
        nome: 'Projeto Crítico',
        status: 'EM_ANDAMENTO',
        percentualTempoDecorrido: 80,
        totalEventos: 10,
        eventosRealizados: 2,
        percentualExecucao: 20,
        eventosPendentesAprovacao: 3,
        emRisco: true,
        proximoMarco: null,
        envolvidos: ['Equipe de Obras'],
      },
    ];

    render(
      <MemoryRouter>
        <ThemeProvider theme={theme}>
          <ListaProgressoProjetosAtivos projects={projects} />
        </ThemeProvider>
      </MemoryRouter>
    );

    expect(screen.getByText('Acompanhamento de Projetos Ativos')).toBeInTheDocument();
    expect(screen.getByText('1 em risco')).toBeInTheDocument();
    expect(screen.getByText('2 ativos')).toBeInTheDocument();

    // Projeto Crítico deve ter a tag Em Risco e o diagnóstico
    expect(screen.getByText('Projeto Crítico')).toBeInTheDocument();
    expect(screen.getByText('Em Risco')).toBeInTheDocument();
    expect(screen.getByText(/Cronograma descompassado/i)).toBeInTheDocument();
    expect(screen.getByText(/3 evento\(s\) travado\(s\) aguardando aprovação/i)).toBeInTheDocument();

    // Projeto Saudável também presente
    expect(screen.getByText('Projeto Saudável')).toBeInTheDocument();

    // Botão de navegação para gestão de projetos
    expect(screen.getByRole('button', { name: /Gerenciar Projetos/i })).toBeInTheDocument();
  });

  it('exibe estado vazio quando não há projetos cadastrados', () => {
    render(
      <MemoryRouter>
        <ThemeProvider theme={theme}>
          <ListaProgressoProjetosAtivos projects={[]} />
        </ThemeProvider>
      </MemoryRouter>
    );

    expect(screen.getByText('Nenhum projeto ativo cadastrado.')).toBeInTheDocument();
  });
});
