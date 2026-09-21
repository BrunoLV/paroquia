import {
  calcularLimitesHorario,
  formatarHorarioEvento,
  normalizarTipoHorario,
  obterLabelTurno,
  METADADOS_TIPO_HORARIO,
} from './horarioEventoUtils';

describe('horarioEventoUtils', () => {
  describe('calcularLimitesHorario', () => {
    it('deve calcular limites para MADRUGADA (00:00 às 05:59)', () => {
      const { inicioIso, fimIso } = calcularLimitesHorario('MADRUGADA', '2026-10-15');
      const inicio = new Date(inicioIso);
      const fim = new Date(fimIso);

      expect(inicio.getFullYear()).toBe(2026);
      expect(inicio.getMonth()).toBe(9); // 0-indexed: 9 = outubro
      expect(inicio.getDate()).toBe(15);
      expect(inicio.getHours()).toBe(0);
      expect(inicio.getMinutes()).toBe(0);

      expect(fim.getFullYear()).toBe(2026);
      expect(fim.getMonth()).toBe(9);
      expect(fim.getDate()).toBe(15);
      expect(fim.getHours()).toBe(5);
      expect(fim.getMinutes()).toBe(59);
    });

    it('deve calcular limites para MANHA (06:00 às 11:59)', () => {
      const { inicioIso, fimIso } = calcularLimitesHorario('MANHA', '2026-10-15');
      const inicio = new Date(inicioIso);
      const fim = new Date(fimIso);

      expect(inicio.getHours()).toBe(6);
      expect(inicio.getMinutes()).toBe(0);
      expect(fim.getHours()).toBe(11);
      expect(fim.getMinutes()).toBe(59);
    });

    it('deve calcular limites para TARDE (12:00 às 17:59)', () => {
      const { inicioIso, fimIso } = calcularLimitesHorario('TARDE', '2026-10-15');
      const inicio = new Date(inicioIso);
      const fim = new Date(fimIso);

      expect(inicio.getHours()).toBe(12);
      expect(inicio.getMinutes()).toBe(0);
      expect(fim.getHours()).toBe(17);
      expect(fim.getMinutes()).toBe(59);
    });

    it('deve calcular limites para NOITE (18:00 às 23:59)', () => {
      const { inicioIso, fimIso } = calcularLimitesHorario('NOITE', '2026-10-15');
      const inicio = new Date(inicioIso);
      const fim = new Date(fimIso);

      expect(inicio.getHours()).toBe(18);
      expect(inicio.getMinutes()).toBe(0);
      expect(fim.getHours()).toBe(23);
      expect(fim.getMinutes()).toBe(59);
    });

    it('deve calcular limites para DIA_INTEIRO (00:00 às 23:59)', () => {
      const { inicioIso, fimIso } = calcularLimitesHorario('DIA_INTEIRO', '2026-10-15');
      const inicio = new Date(inicioIso);
      const fim = new Date(fimIso);

      expect(inicio.getHours()).toBe(0);
      expect(inicio.getMinutes()).toBe(0);
      expect(fim.getHours()).toBe(23);
      expect(fim.getMinutes()).toBe(59);
    });
  });

  describe('formatarHorarioEvento', () => {
    it('deve formatar DIA_INTEIRO', () => {
      const texto = formatarHorarioEvento({
        tipoHorario: 'DIA_INTEIRO',
        inicio: '2026-10-15T03:00:00Z',
        fim: '2026-10-16T02:59:59Z',
      });
      expect(texto).toBe('Dia Inteiro (00:00 às 23:59)');
    });

    it('deve formatar MADRUGADA', () => {
      const texto = formatarHorarioEvento({
        tipoHorario: 'MADRUGADA',
        inicio: '2026-10-15T03:00:00Z',
        fim: '2026-10-15T08:59:59Z',
      });
      expect(texto).toBe('Madrugada (00:00 às 05:59)');
    });

    it('deve formatar MANHA', () => {
      const texto = formatarHorarioEvento({
        tipoHorario: 'MANHA',
        inicio: '2026-10-15T09:00:00Z',
        fim: '2026-10-15T14:59:59Z',
      });
      expect(texto).toBe('Manhã (06:00 às 11:59)');
    });

    it('deve formatar TARDE', () => {
      const texto = formatarHorarioEvento({
        tipoHorario: 'TARDE',
        inicio: '2026-10-15T15:00:00Z',
        fim: '2026-10-15T20:59:59Z',
      });
      expect(texto).toBe('Tarde (12:00 às 17:59)');
    });

    it('deve formatar NOITE', () => {
      const texto = formatarHorarioEvento({
        tipoHorario: 'NOITE',
        inicio: '2026-10-15T21:00:00Z',
        fim: '2026-10-16T02:59:59Z',
      });
      expect(texto).toBe('Noite (18:00 às 23:59)');
    });

    it('deve formatar PERSONALIZADO no mesmo dia com horas', () => {
      const inicio = new Date(2026, 9, 15, 12, 15);
      const fim = new Date(2026, 9, 15, 13, 0);

      const texto = formatarHorarioEvento({
        tipoHorario: 'PERSONALIZADO',
        inicio: inicio.toISOString(),
        fim: fim.toISOString(),
      });

      expect(texto).toContain('12:15');
      expect(texto).toContain('13:00');
    });

    it('nunca deve retornar "Invalid Date" para payloads nulos ou com datas inválidas', () => {
      expect(formatarHorarioEvento(null)).toBe('Horário a definir');
      expect(formatarHorarioEvento({})).toBe('Horário a definir');
      expect(
        formatarHorarioEvento({
          tipoHorario: 'PERSONALIZADO',
          inicio: 'data-invalida',
          fim: 'data-invalida',
        } as any)
      ).toBe('Horário a definir');
    });

    it('deve usar fallbackDataUtc se fornecida quando evento não tem início', () => {
      const fallback = '2026-10-15T19:30:00Z';
      const texto = formatarHorarioEvento(null, fallback);
      expect(texto).not.toContain('Invalid Date');
      expect(texto).toMatch(/\d{2}:\d{2}/);
    });

    it('deve reconhecer termos em inglês e traduzir adequadamente', () => {
      expect(formatarHorarioEvento({ tipoHorario: 'MORNING' as any })).toBe('Manhã (06:00 às 11:59)');
      expect(formatarHorarioEvento({ tipoHorario: 'AFTERNOON' as any })).toBe('Tarde (12:00 às 17:59)');
      expect(formatarHorarioEvento({ tipoHorario: 'EVENING' as any })).toBe('Noite (18:00 às 23:59)');
      expect(formatarHorarioEvento({ tipoHorario: 'NIGHT' as any })).toBe('Noite (18:00 às 23:59)');
      expect(formatarHorarioEvento({ tipoHorario: 'ALL_DAY' as any })).toBe('Dia Inteiro (00:00 às 23:59)');
    });

    it('deve formatar períodos múltiplos mesmo que venham em inglês', () => {
      const texto = formatarHorarioEvento({
        tipoHorario: 'PERIODO' as any,
        periodos: ['MORNING', 'NIGHT'] as any,
      });
      expect(texto).toBe('Manhã e Noite (06:00 às 11:59 e 18:00 às 23:59)');
    });
  });

  describe('normalizarTipoHorario e obterLabelTurno', () => {
    it('deve normalizar sinônimos em inglês para enums em português', () => {
      expect(normalizarTipoHorario('MORNING')).toBe('MANHA');
      expect(normalizarTipoHorario('afternoon')).toBe('TARDE');
      expect(normalizarTipoHorario('night')).toBe('NOITE');
      expect(normalizarTipoHorario('ALL_DAY')).toBe('DIA_INTEIRO');
    });

    it('deve obter labels em português a partir de qualquer variante', () => {
      expect(obterLabelTurno('MORNING')).toBe('Manhã');
      expect(obterLabelTurno('AFTERNOON')).toBe('Tarde');
      expect(obterLabelTurno('NIGHT')).toBe('Noite');
      expect(obterLabelTurno('MANHA')).toBe('Manhã');
    });
  });

  describe('METADADOS_TIPO_HORARIO', () => {
    it('deve possuir todas as chaves de tipos', () => {
      expect(METADADOS_TIPO_HORARIO.PERSONALIZADO.label).toBe('Personalizado');
      expect(METADADOS_TIPO_HORARIO.DIA_INTEIRO.label).toBe('Dia Inteiro');
      expect(METADADOS_TIPO_HORARIO.MADRUGADA.label).toBe('Madrugada');
      expect(METADADOS_TIPO_HORARIO.MANHA.label).toBe('Manhã');
      expect(METADADOS_TIPO_HORARIO.TARDE.label).toBe('Tarde');
      expect(METADADOS_TIPO_HORARIO.NOITE.label).toBe('Noite');
    });
  });
});
