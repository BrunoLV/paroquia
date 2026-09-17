import { describe, it, expect } from 'vitest';
import {
  calculateEaster,
  getLiturgicalSeason,
  getLiturgicalSeasonStyle,
  getLiturgicalCycleLetter,
  getPrecedenciaPeso,
  LiturgicalSeason,
} from './liturgicalCalendar';

describe('liturgicalCalendar utils', () => {
  describe('calculateEaster', () => {
    it('calculates Easter Sunday correctly for known years', () => {
      // 2026: April 5
      const easter2026 = calculateEaster(2026);
      expect(easter2026.getFullYear()).toBe(2026);
      expect(easter2026.getMonth()).toBe(3); // April is month 3 (0-indexed)
      expect(easter2026.getDate()).toBe(5);

      // 2025: April 20
      const easter2025 = calculateEaster(2025);
      expect(easter2025.getMonth()).toBe(3);
      expect(easter2025.getDate()).toBe(20);
    });
  });

  describe('getLiturgicalSeason', () => {
    it('identifies Advento in December prior to Christmas', () => {
      // Dec 10, 2026 -> Advento
      const date = new Date(2026, 11, 10);
      expect(getLiturgicalSeason(date)).toBe('ADVENTO');
    });

    it('identifies Natal between Dec 25 and Baptism of the Lord in early January', () => {
      // Dec 25 -> Natal
      expect(getLiturgicalSeason(new Date(2026, 11, 25))).toBe('NATAL');
      // Jan 2 -> Natal
      expect(getLiturgicalSeason(new Date(2026, 0, 2))).toBe('NATAL');
    });

    it('identifies Quaresma during Lent (Ash Wednesday to Holy Thursday)', () => {
      // Ash Wednesday 2026 is Feb 18 (Easter April 5 - 46 days)
      const ashWednesday = new Date(2026, 1, 18);
      expect(getLiturgicalSeason(ashWednesday)).toBe('QUARESMA');

      // March 15, 2026 -> Quaresma
      expect(getLiturgicalSeason(new Date(2026, 2, 15))).toBe('QUARESMA');
    });

    it('identifies Tríduo Pascal for Good Friday and Holy Saturday', () => {
      // Good Friday 2026 is April 3 (Easter April 5 - 2 days)
      const goodFriday = new Date(2026, 3, 3);
      expect(getLiturgicalSeason(goodFriday)).toBe('TRIDUO_PASCAL');
    });

    it('identifies Tempo Pascal from Easter Sunday through Pentecost', () => {
      // Easter Sunday 2026: April 5
      expect(getLiturgicalSeason(new Date(2026, 3, 5))).toBe('PASCOA');
      // 30 days after Easter -> Tempo Pascal
      expect(getLiturgicalSeason(new Date(2026, 4, 5))).toBe('PASCOA');
    });

    it('identifies Tempo Comum for ordinary days', () => {
      // July 15, 2026 -> Tempo Comum
      expect(getLiturgicalSeason(new Date(2026, 6, 15))).toBe('TEMPO_COMUM');
    });
  });

  describe('getLiturgicalSeasonStyle', () => {
    it('returns subtle pastel colors and labels for each season', () => {
      const quaresmaStyle = getLiturgicalSeasonStyle('QUARESMA');
      expect(quaresmaStyle.label).toBe('QUARESMA');
      expect(quaresmaStyle.backgroundColor).toContain('rgba');

      const adventoStyle = getLiturgicalSeasonStyle('ADVENTO');
      expect(adventoStyle.label).toBe('ADVENTO');

      const pascoaStyle = getLiturgicalSeasonStyle('PASCOA');
      expect(pascoaStyle.label).toBe('PÁSCOA');

      const comumStyle = getLiturgicalSeasonStyle('TEMPO_COMUM');
      expect(comumStyle.label).toBe('TEMPO COMUM');

      const triduoStyle = getLiturgicalSeasonStyle('TRIDUO_PASCAL');
      expect(triduoStyle.label).toBe('TRÍDUO PASCAL');

      const natalStyle = getLiturgicalSeasonStyle('NATAL');
      expect(natalStyle.label).toBe('NATAL');
    });
  });

  describe('getLiturgicalCycleLetter', () => {
    it('calculates cycle letter matching 1->A, 2->B, 0->C formula', () => {
      expect(getLiturgicalCycleLetter(2026)).toBe('A'); // 2026 % 3 = 1
      expect(getLiturgicalCycleLetter(2027)).toBe('B'); // 2027 % 3 = 2
      expect(getLiturgicalCycleLetter(2028)).toBe('C'); // 2028 % 3 = 0
      expect(getLiturgicalCycleLetter(2030)).toBe('B'); // 2030 % 3 = 2
    });
  });

  describe('getPrecedenciaPeso', () => {
    it('returns correct precedence weight ranking 1 to 5', () => {
      expect(getPrecedenciaPeso('TRIDUO_PASCAL')).toBe(1);
      expect(getPrecedenciaPeso('QUARESMA')).toBe(3);
      expect(getPrecedenciaPeso('ADVENTO')).toBe(3);
      expect(getPrecedenciaPeso('PASCOA')).toBe(4);
      expect(getPrecedenciaPeso('NATAL')).toBe(4);
      expect(getPrecedenciaPeso('TEMPO_COMUM')).toBe(5);
    });
  });
});
