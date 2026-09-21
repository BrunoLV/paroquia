import { Evento, TipoHorarioEvento } from '@/shared/domain/types/types';

export interface MetaTipoHorario {
  tipo: TipoHorarioEvento;
  label: string;
  descricao: string;
  inicioHoraMinuto?: string;
  fimHoraMinuto?: string;
}

export const METADADOS_TIPO_HORARIO: Record<TipoHorarioEvento, MetaTipoHorario> = {
  PERSONALIZADO: {
    tipo: 'PERSONALIZADO',
    label: 'Personalizado',
    descricao: 'Horários específicos de início e término',
  },
  DIA_INTEIRO: {
    tipo: 'DIA_INTEIRO',
    label: 'Dia Inteiro',
    descricao: '00:00 às 23:59',
    inicioHoraMinuto: '00:00',
    fimHoraMinuto: '23:59',
  },
  MADRUGADA: {
    tipo: 'MADRUGADA',
    label: 'Madrugada',
    descricao: '00:00 às 05:59',
    inicioHoraMinuto: '00:00',
    fimHoraMinuto: '05:59',
  },
  MANHA: {
    tipo: 'MANHA',
    label: 'Manhã',
    descricao: '06:00 às 11:59',
    inicioHoraMinuto: '06:00',
    fimHoraMinuto: '11:59',
  },
  TARDE: {
    tipo: 'TARDE',
    label: 'Tarde',
    descricao: '12:00 às 17:59',
    inicioHoraMinuto: '12:00',
    fimHoraMinuto: '17:59',
  },
  NOITE: {
    tipo: 'NOITE',
    label: 'Noite',
    descricao: '18:00 às 23:59',
    inicioHoraMinuto: '18:00',
    fimHoraMinuto: '23:59',
  },
};

/**
 * Calcula os limites de início e fim no fuso local para um determinado tipo de horário pré-definido.
 * Recebe `dateStr` no formato `YYYY-MM-DD`.
 */
export const calcularLimitesHorario = (
  tipo: TipoHorarioEvento,
  dateStr: string
): { inicioIso: string; fimIso: string } => {
  const [ano, mes, dia] = dateStr.split('-').map(Number);

  switch (tipo) {
    case 'MADRUGADA': {
      const inicio = new Date(ano, mes - 1, dia, 0, 0, 0, 0);
      const fim = new Date(ano, mes - 1, dia, 5, 59, 59, 999);
      return { inicioIso: inicio.toISOString(), fimIso: fim.toISOString() };
    }
    case 'MANHA': {
      const inicio = new Date(ano, mes - 1, dia, 6, 0, 0, 0);
      const fim = new Date(ano, mes - 1, dia, 11, 59, 59, 999);
      return { inicioIso: inicio.toISOString(), fimIso: fim.toISOString() };
    }
    case 'TARDE': {
      const inicio = new Date(ano, mes - 1, dia, 12, 0, 0, 0);
      const fim = new Date(ano, mes - 1, dia, 17, 59, 59, 999);
      return { inicioIso: inicio.toISOString(), fimIso: fim.toISOString() };
    }
    case 'NOITE': {
      const inicio = new Date(ano, mes - 1, dia, 18, 0, 0, 0);
      const fim = new Date(ano, mes - 1, dia, 23, 59, 59, 999);
      return { inicioIso: inicio.toISOString(), fimIso: fim.toISOString() };
    }
    case 'DIA_INTEIRO': {
      const inicio = new Date(ano, mes - 1, dia, 0, 0, 0, 0);
      const fim = new Date(ano, mes - 1, dia, 23, 59, 59, 999);
      return { inicioIso: inicio.toISOString(), fimIso: fim.toISOString() };
    }
    default: {
      const inicio = new Date(ano, mes - 1, dia, 9, 0, 0, 0);
      const fim = new Date(ano, mes - 1, dia, 10, 0, 0, 0);
      return { inicioIso: inicio.toISOString(), fimIso: fim.toISOString() };
    }
  }
};

export const TURNOS_PREDEFINIDOS: TipoHorarioEvento[] = [
  'MADRUGADA',
  'MANHA',
  'TARDE',
  'NOITE',
];

/**
 * Calcula os limites de início e fim no fuso local para múltiplos turnos pré-definidos.
 * Garante que a faixa total englobe desde o início do turno mais cedo até o término do mais tarde.
 */
export const calcularLimitesMultiplosPeriodos = (
  periodos: TipoHorarioEvento[],
  dateStr: string
): { inicioIso: string; fimIso: string } => {
  if (!periodos || periodos.length === 0) {
    return calcularLimitesHorario('PERSONALIZADO', dateStr);
  }
  if (periodos.length === 1) {
    return calcularLimitesHorario(periodos[0], dateStr);
  }

  let menorInicio: Date | null = null;
  let maiorFim: Date | null = null;

  for (const p of periodos) {
    const { inicioIso, fimIso } = calcularLimitesHorario(p, dateStr);
    const ini = new Date(inicioIso);
    const fim = new Date(fimIso);
    if (!menorInicio || ini < menorInicio) {
      menorInicio = ini;
    }
    if (!maiorFim || fim > maiorFim) {
      maiorFim = fim;
    }
  }

  return {
    inicioIso: (menorInicio ?? new Date()).toISOString(),
    fimIso: (maiorFim ?? new Date()).toISOString(),
  };
};

/** Mapeamento e normalização de termos em inglês ou formatos variantes para enums canônicos */
const SINONIMOS_TIPO_HORARIO: Record<string, TipoHorarioEvento> = {
  MANHA: 'MANHA',
  MORNING: 'MANHA',
  TARDE: 'TARDE',
  AFTERNOON: 'TARDE',
  NOITE: 'NOITE',
  EVENING: 'NOITE',
  NIGHT: 'NOITE',
  MADRUGADA: 'MADRUGADA',
  DAWN: 'MADRUGADA',
  DIA_INTEIRO: 'DIA_INTEIRO',
  ALL_DAY: 'DIA_INTEIRO',
  PERSONALIZADO: 'PERSONALIZADO',
  CUSTOM: 'PERSONALIZADO',
};

/**
 * Normaliza qualquer variante de string para o TipoHorarioEvento canônico em português.
 */
export const normalizarTipoHorario = (valor?: string | null): TipoHorarioEvento => {
  if (!valor) return 'PERSONALIZADO';
  const clean = valor.trim().toUpperCase();
  return SINONIMOS_TIPO_HORARIO[clean] || (clean as TipoHorarioEvento);
};

/**
 * Retorna o rótulo amigável em português para um turno, garantindo tradução mesmo com termos em inglês.
 */
export const obterLabelTurno = (valor?: string | null): string => {
  if (!valor) return '';
  const tipoNormalizado = normalizarTipoHorario(valor);
  return METADADOS_TIPO_HORARIO[tipoNormalizado]?.label || valor;
};

/**
 * Formata o horário do evento para exibição amigável em listas, cards e diálogos de detalhes,
 * suportando múltiplos turnos pré-definidos, termos em inglês e fallbacks defensivos contra datas nulas/inválidas.
 */
export const formatarHorarioEvento = (
  evento?: (Partial<Evento> & {
    periodos?: string[] | null;
    startDate?: string | null;
    endDate?: string | null;
    dataInicio?: string | null;
    dataFim?: string | null;
  }) | null,
  fallbackDataUtc?: string | null
): string => {
  if (!evento) {
    if (fallbackDataUtc) {
      const dt = new Date(fallbackDataUtc);
      if (!isNaN(dt.getTime())) {
        return dt.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
      }
    }
    return 'Horário a definir';
  }

  // Normaliza lista de períodos (se informada)
  const periodosLimpos = (evento.periodos || [])
    .map((p) => normalizarTipoHorario(p))
    .filter((p) => p && p !== 'PERSONALIZADO' && p !== 'DIA_INTEIRO');

  if (periodosLimpos.length > 1) {
    const ordenados = [...periodosLimpos].sort((a, b) => {
      const ordem: Record<string, number> = { MADRUGADA: 1, MANHA: 2, TARDE: 3, NOITE: 4 };
      return (ordem[a] || 99) - (ordem[b] || 99);
    });

    const nomes = ordenados.map((p) => METADADOS_TIPO_HORARIO[p]?.label || p);
    const faixas = ordenados.map((p) => METADADOS_TIPO_HORARIO[p]?.descricao || '');

    const textoNomes = nomes.length === 2 ? `${nomes[0]} e ${nomes[1]}` : nomes.join(', ');
    const textoFaixas = faixas.length === 2 ? `${faixas[0]} e ${faixas[1]}` : faixas.join(' e ');

    return `${textoNomes} (${textoFaixas})`;
  }

  if (periodosLimpos.length === 1) {
    const meta = METADADOS_TIPO_HORARIO[periodosLimpos[0]];
    if (meta) {
      return `${meta.label} (${meta.descricao})`;
    }
  }

  const tipoRaw = evento.tipoHorario;
  const tipo = normalizarTipoHorario(tipoRaw);

  if (tipo === 'DIA_INTEIRO') {
    return 'Dia Inteiro (00:00 às 23:59)';
  }
  if (tipo === 'MADRUGADA') {
    return 'Madrugada (00:00 às 05:59)';
  }
  if (tipo === 'MANHA') {
    return 'Manhã (06:00 às 11:59)';
  }
  if (tipo === 'TARDE') {
    return 'Tarde (12:00 às 17:59)';
  }
  if (tipo === 'NOITE') {
    return 'Noite (18:00 às 23:59)';
  }

  // Resolução de datas com fallbacks
  const rawStart = evento.inicio || evento.startDate || evento.dataInicio || fallbackDataUtc;
  const rawEnd = evento.fim || evento.endDate || evento.dataFim;

  const start = rawStart ? new Date(rawStart) : null;
  const end = rawEnd ? new Date(rawEnd) : null;

  const startValido = start && !isNaN(start.getTime());
  const endValido = end && !isNaN(end.getTime());

  if (startValido && endValido) {
    const isSameDay =
      start.getFullYear() === end.getFullYear() &&
      start.getMonth() === end.getMonth() &&
      start.getDate() === end.getDate();

    const startHora = start.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
    const endHora = end.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });

    if (isSameDay) {
      return `${startHora} às ${endHora}`;
    }

    return `${start.toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' })} às ${end.toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' })}`;
  }

  if (startValido) {
    const startHora = start.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
    return `Início às ${startHora}`;
  }

  return 'Horário a definir';
};

