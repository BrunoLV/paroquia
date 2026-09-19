import { RecurrenceRule } from '@/contextos/calendario/presentation/components/FormularioRecorrencia';

export const formatLocalDate = (d: Date): string => {
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const date = String(d.getDate()).padStart(2, '0');
  const hours = String(d.getHours()).padStart(2, '0');
  const mins = String(d.getMinutes()).padStart(2, '0');
  return `${year}-${month}-${date}T${hours}:${mins}`;
};

export const formatIsoToLocal = (isoString: string): string => {
  if (!isoString) return '';
  return formatLocalDate(new Date(isoString));
};

export const getRecurrenceDates = (start: Date, rule: RecurrenceRule, limit: Date): Date[] => {
  const dates: Date[] = [];
  let current = new Date(start);
  const maxOccurrences = rule.numeroOcorrencias || 1000;
  const interval = rule.intervalo || 1;

  if (rule.frequencia === 'DIARIA') {
    while (current <= limit && dates.length < maxOccurrences) {
      dates.push(new Date(current));
      current.setDate(current.getDate() + interval);
    }
  } else if (rule.frequencia === 'SEMANAL') {
    const days = rule.diasDaSemana || [];
    if (days.length === 0) {
      const dayNames = ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'];
      days.push(dayNames[start.getDay()]);
    }
    const dayOfWeekMap: Record<string, number> = {
      SUNDAY: 0, MONDAY: 1, TUESDAY: 2, WEDNESDAY: 3, THURSDAY: 4, FRIDAY: 5, SATURDAY: 6
    };

    while (current <= limit && dates.length < maxOccurrences) {
      const startOfWeek = new Date(current);
      const weekDates: Date[] = [];
      for (const day of days) {
        const targetDay = dayOfWeekMap[day];
        const diff = targetDay - startOfWeek.getDay();
        const occurrenceDate = new Date(startOfWeek);
        occurrenceDate.setDate(startOfWeek.getDate() + diff);
        if (occurrenceDate >= start && occurrenceDate <= limit) {
          weekDates.push(occurrenceDate);
        }
      }
      weekDates.sort((a, b) => a.getTime() - b.getTime());
      for (const d of weekDates) {
        if (dates.length < maxOccurrences) {
          dates.push(d);
        }
      }
      current.setDate(current.getDate() + 7 * interval);
    }
  } else if (rule.frequencia === 'MENSAL') {
    while (current <= limit && dates.length < maxOccurrences) {
      dates.push(new Date(current));
      current.setMonth(current.getMonth() + interval);
    }
  }
  return dates;
};
