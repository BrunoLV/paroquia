import React from 'react';
import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import ConcorrenciaAlertaBanner from './ConcorrenciaAlertaBanner';
import { ConflitoItemDto } from '@/api/metricasTypes';

describe('ConcorrenciaAlertaBanner', () => {
  it('não deve renderizar nada quando a lista de conflitos estiver vazia', () => {
    const { container } = render(
      <ConcorrenciaAlertaBanner conflitos={[]} nivelRisco="BAIXO" />
    );
    expect(container.firstChild).toBeNull();
  });

  it('deve renderizar o alerta de concorrência com o título e detalhes do evento', () => {
    const conflitosMock: ConflitoItemDto[] = [
      {
        eventoIdA: '1',
        tituloEventoA: 'Encontro de Casais Matriz',
        pastoralA: 'Pastoral Familiar',
        eventoIdB: '2',
        tituloEventoB: 'Encontro de Casais Capela',
        pastoralB: 'Pastoral Familiar Capela',
        dataEvento: '30/07/2026 19:00',
        publicoAlvoCompartilhado: 'Casais',
        mensagemAlerta: 'Risco de esvaziamento por concorrência',
      },
    ];

    render(
      <ConcorrenciaAlertaBanner conflitos={conflitosMock} nivelRisco="MEDIO" />
    );

    expect(
      screen.getByText(/Atenção: Risco de Concorrência Interpastoral \(Médio\)/i)
    ).toBeInTheDocument();
    expect(
      screen.getByText(/Encontro de Casais Matriz \(Pastoral Familiar\) vs Encontro de Casais Capela/i)
    ).toBeInTheDocument();
  });
});
