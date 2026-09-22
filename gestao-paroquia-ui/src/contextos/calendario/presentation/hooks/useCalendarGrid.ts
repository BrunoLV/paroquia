import { useMemo, useState } from 'react';
import { isSameDay } from '@/shared/utils/calendarHelpers';

export interface CalendarDay {
  date: Date;
  isCurrentMonth: boolean;
  isToday: boolean;
}

export type ModoVisualizacaoCalendario = 'DIA' | 'SEMANA' | 'MES' | 'ANO' | 'DIAS_ESPECIFICOS' | 'HOJE';

export type CalendarViewMode = ModoVisualizacaoCalendario | 'month' | 'agenda';

/**
 * Hook de navegação temporal e cálculo do período do calendário paroquial.
 * Suporta as 5 modalidades canônicas de visualização:
 * - DIA: foco no dia individual com linha do tempo / timeline
 * - SEMANA: grade dos 7 dias da semana (Domingo a Sábado)
 * - MÊS: grade canônica de 42 células (6 semanas)
 * - ANO: visão dos 12 meses do ano corrente
 * - DIAS_ESPECIFICOS: intervalo customizado definido pelo usuário
 */
export const useCalendarGrid = () => {
  const [currentDate, setCurrentDate] = useState<Date>(new Date());
  const [viewModeInternal, setViewModeInternal] = useState<ModoVisualizacaoCalendario>('MES');

  // Intervalo customizado para o modo DIAS_ESPECIFICOS (padrão: hoje até +14 dias)
  const [customStartDate, setCustomStartDate] = useState<Date>(() => {
    const d = new Date();
    d.setHours(0, 0, 0, 0);
    return d;
  });
  const [customEndDate, setCustomEndDate] = useState<Date>(() => {
    const d = new Date();
    d.setDate(d.getDate() + 14);
    d.setHours(23, 59, 59, 999);
    return d;
  });

  const currentYear = currentDate.getFullYear();
  const currentMonth = currentDate.getMonth();

  // Compatibilidade com callers legados que usam 'month', 'agenda' ou 'HOJE'
  const setViewMode = (mode: CalendarViewMode) => {
    if (mode === 'month') {
      setViewModeInternal('MES');
    } else if (mode === 'agenda') {
      setViewModeInternal('DIAS_ESPECIFICOS');
    } else if (mode === 'HOJE') {
      setViewModeInternal('DIA');
    } else {
      setViewModeInternal(mode);
    }
  };

  const viewMode = viewModeInternal;

  // Grade de 42 células para o modo MÊS
  const calendarDays = useMemo(() => {
    const days: CalendarDay[] = [];
    const firstDay = new Date(currentYear, currentMonth, 1);
    const startDayOfWeek = firstDay.getDay(); // 0 (Sun) to 6 (Sat)
    const daysInMonth = new Date(currentYear, currentMonth + 1, 0).getDate();
    const prevMonthDays = new Date(currentYear, currentMonth, 0).getDate();

    for (let i = startDayOfWeek - 1; i >= 0; i--) {
      const d = new Date(currentYear, currentMonth - 1, prevMonthDays - i);
      days.push({ date: d, isCurrentMonth: false, isToday: isSameDay(d, new Date()) });
    }

    for (let i = 1; i <= daysInMonth; i++) {
      const d = new Date(currentYear, currentMonth, i);
      days.push({ date: d, isCurrentMonth: true, isToday: isSameDay(d, new Date()) });
    }

    const totalCells = 42;
    const remainingCells = totalCells - days.length;
    for (let i = 1; i <= remainingCells; i++) {
      const d = new Date(currentYear, currentMonth + 1, i);
      days.push({ date: d, isCurrentMonth: false, isToday: isSameDay(d, new Date()) });
    }

    return days;
  }, [currentYear, currentMonth]);

  // Lista dos 7 dias para o modo SEMANA (Domingo a Sábado da semana de currentDate)
  const weekDays = useMemo(() => {
    const days: CalendarDay[] = [];
    const dayOfWeek = currentDate.getDay();
    const sunday = new Date(currentDate.getFullYear(), currentDate.getMonth(), currentDate.getDate() - dayOfWeek);

    for (let i = 0; i < 7; i++) {
      const d = new Date(sunday.getFullYear(), sunday.getMonth(), sunday.getDate() + i);
      days.push({
        date: d,
        isCurrentMonth: d.getMonth() === currentMonth,
        isToday: isSameDay(d, new Date()),
      });
    }
    return days;
  }, [currentDate, currentMonth]);

  // Limites temporais (início e fim) de acordo com o modo ativo
  const { startOfPeriod, endOfPeriod } = useMemo(() => {
    switch (viewMode) {
      case 'DIA':
      case 'HOJE': {
        const start = new Date(currentDate.getFullYear(), currentDate.getMonth(), currentDate.getDate(), 0, 0, 0, 0);
        const end = new Date(currentDate.getFullYear(), currentDate.getMonth(), currentDate.getDate(), 23, 59, 59, 999);
        return { startOfPeriod: start, endOfPeriod: end };
      }
      case 'SEMANA': {
        const sunday = weekDays[0].date;
        const saturday = weekDays[6].date;
        const start = new Date(sunday.getFullYear(), sunday.getMonth(), sunday.getDate(), 0, 0, 0, 0);
        const end = new Date(saturday.getFullYear(), saturday.getMonth(), saturday.getDate(), 23, 59, 59, 999);
        return { startOfPeriod: start, endOfPeriod: end };
      }
      case 'MES': {
        const start = new Date(calendarDays[0].date.getFullYear(), calendarDays[0].date.getMonth(), calendarDays[0].date.getDate(), 0, 0, 0, 0);
        const lastDay = calendarDays[calendarDays.length - 1].date;
        const end = new Date(lastDay.getFullYear(), lastDay.getMonth(), lastDay.getDate(), 23, 59, 59, 999);
        return { startOfPeriod: start, endOfPeriod: end };
      }
      case 'ANO': {
        const start = new Date(currentYear, 0, 1, 0, 0, 0, 0);
        const end = new Date(currentYear, 11, 31, 23, 59, 59, 999);
        return { startOfPeriod: start, endOfPeriod: end };
      }
      case 'DIAS_ESPECIFICOS': {
        const start = new Date(customStartDate.getFullYear(), customStartDate.getMonth(), customStartDate.getDate(), 0, 0, 0, 0);
        const end = new Date(customEndDate.getFullYear(), customEndDate.getMonth(), customEndDate.getDate(), 23, 59, 59, 999);
        return { startOfPeriod: start, endOfPeriod: end };
      }
    }
  }, [viewMode, currentDate, currentYear, weekDays, calendarDays, customStartDate, customEndDate]);

  // Handlers contextuais de navegação
  const handlePrev = () => {
    switch (viewMode) {
      case 'DIA':
      case 'HOJE':
        setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth(), currentDate.getDate() - 1));
        break;
      case 'SEMANA':
        setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth(), currentDate.getDate() - 7));
        break;
      case 'MES':
        setCurrentDate(new Date(currentYear, currentMonth - 1, 1));
        break;
      case 'ANO':
        setCurrentDate(new Date(currentYear - 1, currentMonth, 1));
        break;
      case 'DIAS_ESPECIFICOS': {
        const diffMs = customEndDate.getTime() - customStartDate.getTime();
        const days = Math.max(1, Math.round(diffMs / (1000 * 60 * 60 * 24)));
        const newStart = new Date(customStartDate.getTime() - days * 24 * 60 * 60 * 1000);
        const newEnd = new Date(customEndDate.getTime() - days * 24 * 60 * 60 * 1000);
        setCustomStartDate(newStart);
        setCustomEndDate(newEnd);
        break;
      }
    }
  };

  const handleNext = () => {
    switch (viewMode) {
      case 'DIA':
      case 'HOJE':
        setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth(), currentDate.getDate() + 1));
        break;
      case 'SEMANA':
        setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth(), currentDate.getDate() + 7));
        break;
      case 'MES':
        setCurrentDate(new Date(currentYear, currentMonth + 1, 1));
        break;
      case 'ANO':
        setCurrentDate(new Date(currentYear + 1, currentMonth, 1));
        break;
      case 'DIAS_ESPECIFICOS': {
        const diffMs = customEndDate.getTime() - customStartDate.getTime();
        const days = Math.max(1, Math.round(diffMs / (1000 * 60 * 60 * 24)));
        const newStart = new Date(customStartDate.getTime() + days * 24 * 60 * 60 * 1000);
        const newEnd = new Date(customEndDate.getTime() + days * 24 * 60 * 60 * 1000);
        setCustomStartDate(newStart);
        setCustomEndDate(newEnd);
        break;
      }
    }
  };

  const handleToday = () => {
    const today = new Date();
    setCurrentDate(today);
    if (viewMode === 'DIAS_ESPECIFICOS') {
      const start = new Date(today);
      start.setHours(0, 0, 0, 0);
      const end = new Date(today);
      end.setDate(end.getDate() + 14);
      end.setHours(23, 59, 59, 999);
      setCustomStartDate(start);
      setCustomEndDate(end);
    }
  };

  const setCustomRange = (start: Date, end: Date) => {
    const normalizedStart = new Date(start);
    normalizedStart.setHours(0, 0, 0, 0);
    const normalizedEnd = new Date(end);
    normalizedEnd.setHours(23, 59, 59, 999);
    setCustomStartDate(normalizedStart);
    setCustomEndDate(normalizedEnd);
  };

  const handleSelectDate = (date: Date) => {
    setCurrentDate(date);
  };

  return {
    currentDate,
    setCurrentDate: handleSelectDate,
    viewMode,
    setViewMode,
    currentYear,
    currentMonth,
    calendarDays,
    weekDays,
    startOfPeriod,
    endOfPeriod,
    customStartDate,
    customEndDate,
    setCustomStartDate,
    setCustomEndDate,
    setCustomRange,
    handlePrev,
    handleNext,
    handleToday,
    // Aliases retrocompatíveis
    handlePrevMonth: handlePrev,
    handleNextMonth: handleNext,
  };
};
