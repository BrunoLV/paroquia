import { describe, it, expect } from 'vitest';
import { eventoFormSchema } from './eventoSchema';

describe('eventoFormSchema', () => {
  const baseValid = {
    titulo: 'Missa Comunitária',
    descricao: 'Celebração paroquial',
    categoria: 'LITURGICO',
    status: 'CONFIRMADO',
    organizacaoResponsavelId: 'org-123',
    igrejaVinculoId: 'igreja-123',
    localId: 'local-123',
    projetoId: '',
    participantes: [],
    selectedAtivoIds: [],
    justification: '',
    isRecurring: false,
    liberarConflitoAgenda: false,
    recurrenceRule: {
      frequencia: 'SEMANAL' as const,
      intervalo: 1,
    },
  };

  it('deve validar evento PERSONALIZADO com início e fim coerentes', () => {
    const input = {
      ...baseValid,
      tipoHorario: 'PERSONALIZADO' as const,
      startDate: '2026-10-15T14:00',
      endDate: '2026-10-15T16:00',
      endTime: '',
    };

    const result = eventoFormSchema.safeParse(input);
    expect(result.success).toBe(true);
  });

  it('deve rejeitar evento PERSONALIZADO com início posterior ao término', () => {
    const input = {
      ...baseValid,
      tipoHorario: 'PERSONALIZADO' as const,
      startDate: '2026-10-15T18:00',
      endDate: '2026-10-15T16:00',
      endTime: '',
    };

    const result = eventoFormSchema.safeParse(input);
    expect(result.success).toBe(false);
    if (!result.success) {
      expect(result.error.issues.some((i) => i.path.includes('endDate'))).toBe(true);
    }
  });

  it('deve validar evento DIA_INTEIRO com eventDate sem exigir endDate ou endTime', () => {
    const input = {
      ...baseValid,
      tipoHorario: 'DIA_INTEIRO' as const,
      eventDate: '2026-10-15',
      startDate: '',
      endDate: '',
      endTime: '',
    };

    const result = eventoFormSchema.safeParse(input);
    expect(result.success).toBe(true);
  });

  it('deve validar evento de período MANHA com eventDate sem exigir endDate', () => {
    const input = {
      ...baseValid,
      tipoHorario: 'MANHA' as const,
      eventDate: '2026-10-15',
      startDate: '',
      endDate: '',
      endTime: '',
    };

    const result = eventoFormSchema.safeParse(input);
    expect(result.success).toBe(true);
  });

  it('deve exigir justificativa quando status for ADICIONADO_EXTRA', () => {
    const input = {
      ...baseValid,
      tipoHorario: 'DIA_INTEIRO' as const,
      eventDate: '2026-10-15',
      startDate: '',
      endDate: '',
      endTime: '',
      status: 'ADICIONADO_EXTRA',
      justification: '',
    };

    const result = eventoFormSchema.safeParse(input);
    expect(result.success).toBe(false);
    if (!result.success) {
      expect(result.error.issues.some((i) => i.path.includes('justification'))).toBe(true);
    }
  });

  it('deve aceitar formatos de evento PRESENCIAL, ONLINE e HIBRIDO', () => {
    for (const formato of ['PRESENCIAL', 'ONLINE', 'HIBRIDO'] as const) {
      const input = {
        ...baseValid,
        formato,
        tipoHorario: 'DIA_INTEIRO' as const,
        eventDate: '2026-10-15',
        startDate: '',
        endDate: '',
        endTime: '',
      };
      const result = eventoFormSchema.safeParse(input);
      expect(result.success).toBe(true);
      if (result.success) {
        expect(result.data.formato).toBe(formato);
      }
    }
  });

  it('deve aplicar PRESENCIAL como formato padrão quando omitido', () => {
    const input = {
      ...baseValid,
      tipoHorario: 'DIA_INTEIRO' as const,
      eventDate: '2026-10-15',
      startDate: '',
      endDate: '',
      endTime: '',
    };
    const result = eventoFormSchema.safeParse(input);
    expect(result.success).toBe(true);
    if (result.success) {
      expect(result.data.formato).toBe('PRESENCIAL');
    }
  });

  it('deve rejeitar evento PRESENCIAL sem nenhum local selecionado', () => {
    const input = {
      ...baseValid,
      formato: 'PRESENCIAL' as const,
      localId: '',
      localIds: [],
      tipoHorario: 'DIA_INTEIRO' as const,
      eventDate: '2026-10-15',
      startDate: '',
      endDate: '',
      endTime: '',
    };
    const result = eventoFormSchema.safeParse(input);
    expect(result.success).toBe(false);
    if (!result.success) {
      expect(result.error.issues.some((i) => i.path.includes('localIds'))).toBe(true);
    }
  });

  it('deve rejeitar evento HIBRIDO sem nenhum local selecionado', () => {
    const input = {
      ...baseValid,
      formato: 'HIBRIDO' as const,
      localId: '',
      localIds: [],
      tipoHorario: 'DIA_INTEIRO' as const,
      eventDate: '2026-10-15',
      startDate: '',
      endDate: '',
      endTime: '',
    };
    const result = eventoFormSchema.safeParse(input);
    expect(result.success).toBe(false);
    if (!result.success) {
      expect(result.error.issues.some((i) => i.path.includes('localIds'))).toBe(true);
    }
  });

  it('deve validar com sucesso evento ONLINE mesmo sem nenhum local selecionado', () => {
    const input = {
      ...baseValid,
      formato: 'ONLINE' as const,
      localId: '',
      localIds: [],
      tipoHorario: 'DIA_INTEIRO' as const,
      eventDate: '2026-10-15',
      startDate: '',
      endDate: '',
      endTime: '',
    };
    const result = eventoFormSchema.safeParse(input);
    expect(result.success).toBe(true);
  });
});
