import {describe, expect, it} from 'vitest';
import {formatarAgendaFimDeSemana} from './agendaFimDeSemana';
import {WeeklyEvent} from '@/api/types';

const evento = (titulo: string, inicio: string): WeeklyEvent => ({
  titulo,
  inicio,
  localNome: 'Salão',
  grupoParoquialNome: 'Pastoral A',
});

describe('formatarAgendaFimDeSemana', () => {
  it('inclui apenas eventos de sábado e domingo, ordenados', () => {
    const texto = formatarAgendaFimDeSemana([
      evento('Reunião quarta', '2026-09-16T19:00:00'),
      evento('Missa domingo', '2026-09-20T10:00:00'),
      evento('Novena sábado', '2026-09-19T18:00:00'),
    ]);

    expect(texto).toContain('Novena sábado');
    expect(texto).toContain('Missa domingo');
    expect(texto).not.toContain('Reunião quarta');
    expect(texto.indexOf('Novena sábado')).toBeLessThan(texto.indexOf('Missa domingo'));
  });

  it('devolve mensagem vazia quando não há eventos de fim de semana', () => {
    expect(formatarAgendaFimDeSemana([evento('Reunião', '2026-09-16T19:00:00')]))
      .toBe('Nenhum evento agendado para o fim de semana.');
  });
});
