export type LiturgicalSeason =
  | 'ADVENTO'
  | 'NATAL'
  | 'QUARESMA'
  | 'TRIDUO_PASCAL'
  | 'PASCOA'
  | 'TEMPO_COMUM';

export interface LiturgicalSeasonStyle {
  season: LiturgicalSeason;
  label: string;
  backgroundColor: string;
  textColor: string;
}

/**
 * Calculates Easter Sunday for a given year using Meeus/Jones/Butcher algorithm.
 */
export function calculateEaster(year: number): Date {
  const a = year % 19;
  const b = Math.floor(year / 100);
  const c = year % 100;
  const d = Math.floor(b / 4);
  const e = b % 4;
  const f = Math.floor((b + 8) / 25);
  const g = Math.floor((b - f + 1) / 3);
  const h = (19 * a + b - d - g + 15) % 30;
  const i = Math.floor(c / 4);
  const k = c % 4;
  const l = (32 + 2 * e + 2 * i - h - k) % 7;
  const m = Math.floor((a + 11 * h + 22 * l) / 451);
  const month = Math.floor((h + l - 7 * m + 114) / 31) - 1; // 0-indexed month
  const day = ((h + l - 7 * m + 114) % 31) + 1;
  return new Date(year, month, day);
}

/**
 * Calculates the 1st Sunday of Advent (Sunday closest to Nov 30).
 */
export function calculateAdventStart(year: number): Date {
  const nov30 = new Date(year, 10, 30); // Nov is month 10
  const dayOfWeek = nov30.getDay(); // 0 (Sun) to 6 (Sat)
  let offset = 0;
  if (dayOfWeek === 0) {
    offset = 0;
  } else if (dayOfWeek <= 3) {
    offset = -dayOfWeek;
  } else {
    offset = 7 - dayOfWeek;
  }
  const advent = new Date(year, 10, 30 + offset);
  advent.setHours(0, 0, 0, 0);
  return advent;
}

/**
 * Returns the Liturgical Season for a given date.
 */
export function getLiturgicalSeason(dateInput: Date): LiturgicalSeason {
  const date = new Date(dateInput.getFullYear(), dateInput.getMonth(), dateInput.getDate());
  const year = date.getFullYear();

  // Easter and related dates for current year
  const easter = calculateEaster(year);
  const ashWednesday = new Date(year, easter.getMonth(), easter.getDate() - 46);
  const holyThursday = new Date(year, easter.getMonth(), easter.getDate() - 3);
  const easterSaturday = new Date(year, easter.getMonth(), easter.getDate() - 1);
  const pentecost = new Date(year, easter.getMonth(), easter.getDate() + 49);

  // Advent for current year
  const adventStart = calculateAdventStart(year);
  const christmas = new Date(year, 11, 25);

  // Baptism of the Lord (End of Christmas season, approx Jan 8-13)
  const jan6 = new Date(year, 0, 6);
  const dayOfJan6 = jan6.getDay();
  const baptismOfLord = new Date(year, 0, 6 + (dayOfJan6 === 0 ? 7 : 7 - dayOfJan6));

  // Check Christmas season from previous year ending in Jan of current year
  if (date.getMonth() === 0 && date <= baptismOfLord) {
    return 'NATAL';
  }

  // Check Advent (1st Sunday of Advent to Dec 24)
  if (date >= adventStart && date < christmas) {
    return 'ADVENTO';
  }

  // Check Christmas (Dec 25 to Dec 31)
  if (date >= christmas) {
    return 'NATAL';
  }

  // Check Quaresma (Ash Wednesday to Holy Thursday morning)
  if (date >= ashWednesday && date < holyThursday) {
    return 'QUARESMA';
  }

  // Check Tríduo Pascal (Holy Thursday, Good Friday, Holy Saturday)
  if (date >= holyThursday && date <= easterSaturday) {
    return 'TRIDUO_PASCAL';
  }

  // Check Tempo Pascal (Easter Sunday to Pentecost)
  if (date >= easter && date <= pentecost) {
    return 'PASCOA';
  }

  // Otherwise: Tempo Comum
  return 'TEMPO_COMUM';
}

/**
 * Returns subtle pastel background styling and label for a Liturgical Season.
 */
export function getLiturgicalSeasonStyle(season: LiturgicalSeason): LiturgicalSeasonStyle {
  switch (season) {
    case 'ADVENTO':
      return {
        season,
        label: 'ADVENTO',
        backgroundColor: 'rgba(156, 39, 176, 0.08)',
        textColor: '#7b1fa2',
      };
    case 'QUARESMA':
      return {
        season,
        label: 'QUARESMA',
        backgroundColor: 'rgba(156, 39, 176, 0.08)',
        textColor: '#7b1fa2',
      };
    case 'TRIDUO_PASCAL':
      return {
        season,
        label: 'TRÍDUO PASCAL',
        backgroundColor: 'rgba(211, 47, 47, 0.10)',
        textColor: '#c62828',
      };
    case 'PASCOA':
      return {
        season,
        label: 'PÁSCOA',
        backgroundColor: 'rgba(255, 193, 7, 0.10)',
        textColor: '#b78103',
      };
    case 'NATAL':
      return {
        season,
        label: 'NATAL',
        backgroundColor: 'rgba(255, 193, 7, 0.10)',
        textColor: '#b78103',
      };
    case 'TEMPO_COMUM':
    default:
      return {
        season,
        label: 'TEMPO COMUM',
        backgroundColor: 'rgba(46, 125, 50, 0.08)',
        textColor: '#2e7d32',
      };
  }
}

/**
 * Calculates the Liturgical Sunday Cycle letter (A, B, or C) for a given civil year.
 * Formula: year % 3 === 1 -> 'A', year % 3 === 2 -> 'B', year % 3 === 0 -> 'C'.
 */
export function getLiturgicalCycleLetter(year: number): 'A' | 'B' | 'C' {
  const rem = Math.abs(year) % 3;
  if (rem === 1) return 'A';
  if (rem === 2) return 'B';
  return 'C';
}

/**
 * Returns the precedence weight (1 to 5, lower = higher priority).
 */
export function getPrecedenciaPeso(season: LiturgicalSeason): number {
  switch (season) {
    case 'TRIDUO_PASCAL':
      return 1;
    case 'QUARESMA':
    case 'ADVENTO':
      return 3;
    case 'PASCOA':
    case 'NATAL':
      return 4;
    case 'TEMPO_COMUM':
    default:
      return 5;
  }
}
