import { describe, expect, it } from 'vitest';
import { formatarDiasSemanaComPreposicao, formatarResumoRecorrencia } from './recorrenciaFormatUtils';
import { RegraRecorrencia } from '@/contextos/calendario/presentation/components/FormularioRecorrencia';

describe('recorrenciaFormatUtils', () => {
  describe('formatarDiasSemanaComPreposicao', () => {
    it('deve formatar um dia feminino com a preposição "às"', () => {
      expect(formatarDiasSemanaComPreposicao(['TUESDAY'])).toBe('às terças-feiras');
    });

    it('deve formatar um dia masculino com a preposição "aos"', () => {
      expect(formatarDiasSemanaComPreposicao(['SUNDAY'])).toBe('aos domingos');
      expect(formatarDiasSemanaComPreposicao(['SATURDAY'])).toBe('aos sábados');
    });

    it('deve formatar múltiplos dias femininos com "às" unificado', () => {
      expect(formatarDiasSemanaComPreposicao(['TUESDAY', 'THURSDAY'])).toBe('às terças-feiras e quintas-feiras');
    });

    it('deve formatar múltiplos dias masculinos com "aos" unificado', () => {
      expect(formatarDiasSemanaComPreposicao(['SATURDAY', 'SUNDAY'])).toBe('aos sábados e domingos');
    });

    it('deve formatar dias com gêneros mistos preservando preposições individuais', () => {
      expect(formatarDiasSemanaComPreposicao(['SUNDAY', 'WEDNESDAY'])).toBe('aos domingos e às quartas-feiras');
    });

    it('deve retornar string vazia para lista vazia ou nula', () => {
      expect(formatarDiasSemanaComPreposicao([])).toBe('');
    });
  });

  describe('formatarResumoRecorrencia', () => {
    it('deve formatar evento semanal com número de ocorrências e data calculada', () => {
      const rule: RegraRecorrencia = {
        frequencia: 'SEMANAL',
        intervalo: 1,
        diasDaSemana: ['TUESDAY'],
        numeroOcorrencias: 4,
      };
      // Início: 2026-03-03 (Terça-feira). 4 semanas -> 2026-03-24
      const resumo = formatarResumoRecorrencia(rule, '2026-03-03T19:00:00');
      expect(resumo).toContain('Semanalmente, às terças-feiras');
      expect(resumo).toContain('Total de 4 ocorrências');
      expect(resumo).toContain('término em 24/03/2026');
    });

    it('deve formatar evento semanal com 1 ocorrência no singular', () => {
      const rule: RegraRecorrencia = {
        frequencia: 'SEMANAL',
        intervalo: 1,
        diasDaSemana: ['FRIDAY'],
        numeroOcorrencias: 1,
      };
      const resumo = formatarResumoRecorrencia(rule, '2026-03-06T19:00:00');
      expect(resumo).toContain('Semanalmente, às sextas-feiras');
      expect(resumo).toContain('Total de 1 ocorrência');
    });

    it('deve indicar que se estende indefinidamente quando não houver data limite nem número de ocorrências', () => {
      const rule: RegraRecorrencia = {
        frequencia: 'SEMANAL',
        intervalo: 1,
        diasDaSemana: ['TUESDAY'],
      };
      const resumo = formatarResumoRecorrencia(rule);
      expect(resumo).toBe('Semanalmente, às terças-feiras · Estende-se indefinidamente');
    });

    it('deve formatar evento diário contínuo', () => {
      const rule: RegraRecorrencia = {
        frequencia: 'DIARIA',
        intervalo: 1,
      };
      const resumo = formatarResumoRecorrencia(rule);
      expect(resumo).toBe('Diariamente · Estende-se indefinidamente');
    });

    it('deve formatar evento diário a cada N dias com data limite', () => {
      const rule: RegraRecorrencia = {
        frequencia: 'DIARIA',
        intervalo: 3,
        dataLimite: '2026-12-31',
      };
      const resumo = formatarResumoRecorrencia(rule);
      expect(resumo).toBe('A cada 3 dias · Término em 31/12/2026');
    });

    it('deve formatar evento mensal relativo feminino com concordância ("na primeira")', () => {
      const rule: RegraRecorrencia = {
        frequencia: 'MENSAL',
        intervalo: 1,
        posicaoNoMes: 'PRIMEIRO',
        diasDaSemana: ['TUESDAY'],
        dataLimite: '2026-12-31',
      };
      const resumo = formatarResumoRecorrencia(rule);
      expect(resumo).toBe('Mensalmente, na primeira terça-feira do mês · Término em 31/12/2026');
    });

    it('deve formatar evento mensal relativo masculino com concordância ("no primeiro")', () => {
      const rule: RegraRecorrencia = {
        frequencia: 'MENSAL',
        intervalo: 1,
        posicaoNoMes: 'PRIMEIRO',
        diasDaSemana: ['SUNDAY'],
        dataLimite: '2026-12-31',
      };
      const resumo = formatarResumoRecorrencia(rule);
      expect(resumo).toBe('Mensalmente, no primeiro domingo do mês · Término em 31/12/2026');
    });

    it('deve formatar evento mensal relativo no último sábado do mês', () => {
      const rule: RegraRecorrencia = {
        frequencia: 'MENSAL',
        intervalo: 1,
        posicaoNoMes: 'ULTIMO',
        diasDaSemana: ['SATURDAY'],
      };
      const resumo = formatarResumoRecorrencia(rule);
      expect(resumo).toBe('Mensalmente, no último sábado do mês · Estende-se indefinidamente');
    });

    it('deve formatar evento mensal por dia do mês fixo', () => {
      const rule: RegraRecorrencia = {
        frequencia: 'MENSAL',
        intervalo: 1,
        diaDoMes: 15,
      };
      const resumo = formatarResumoRecorrencia(rule);
      expect(resumo).toBe('Mensalmente, todo dia 15 · Estende-se indefinidamente');
    });

    it('deve retornar string vazia para regra nula ou indefinida', () => {
      expect(formatarResumoRecorrencia(null)).toBe('');
      expect(formatarResumoRecorrencia(undefined)).toBe('');
    });
  });
});
