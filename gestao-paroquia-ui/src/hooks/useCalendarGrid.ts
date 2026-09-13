import { useMemo, useState } from 'react';
import { isSameDay } from '../utils/calendarHelpers';

export interface CalendarDay {
  date: Date;
  isCurrentMonth: boolean;
  isToday: boolean;
}

/**
 * Estado de navegação do calendário (mês corrente / modo de visualização) e a grade de 42
 * células (6 semanas) do mês, incluindo dias de meses vizinhos para preencher a grade —
 * mesma lógica do `Calendar.tsx` original, só isolada para não recalcular a grade a cada
 * re-render motivado por filtros/dados que não afetam a navegação.
 */
export const useCalendarGrid = () => {
  const [currentDate, setCurrentDate] = useState(new Date());
  const [viewMode, setViewMode] = useState<'month' | 'agenda'>('month');

  const currentYear = currentDate.getFullYear();
  const currentMonth = currentDate.getMonth();

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

  const handlePrevMonth = () => setCurrentDate(new Date(currentYear, currentMonth - 1, 1));
  const handleNextMonth = () => setCurrentDate(new Date(currentYear, currentMonth + 1, 1));
  const handleToday = () => setCurrentDate(new Date());

  return {
    currentDate,
    viewMode,
    setViewMode,
    currentYear,
    currentMonth,
    calendarDays,
    handlePrevMonth,
    handleNextMonth,
    handleToday,
  };
};
