import { z } from 'zod';

/**
 * Regra de recorrência do evento — mesma forma de `RecurrenceRule`
 * (`src/components/RecurrenceForm.tsx`). Não validamos a exclusão mútua dos
 * campos aqui: quem garante consistência (dataLimite XOR numeroOcorrencias,
 * diaDoMes XOR posicaoNoMes/diasDaSemana) é o próprio `RecurrenceForm`.
 */
const recurrenceRuleSchema = z.object({
  frequencia: z.enum(['DIARIA', 'SEMANAL', 'MENSAL']),
  intervalo: z.number(),
  diasDaSemana: z.array(z.string()).optional(),
  posicaoNoMes: z.string().optional(),
  diaDoMes: z.number().optional(),
  dataLimite: z.string().optional(),
  numeroOcorrencias: z.number().optional(),
});

export const tipoHorarioEnum = z.enum([
  'PERSONALIZADO',
  'DIA_INTEIRO',
  'MADRUGADA',
  'MANHA',
  'TARDE',
  'NOITE',
]);

export type TipoHorarioForm = z.infer<typeof tipoHorarioEnum>;

export const formatoEnum = z.enum(['PRESENCIAL', 'ONLINE', 'HIBRIDO']);
export type FormatoForm = z.infer<typeof formatoEnum>;

/**
 * Schema do formulário de cadastro/edição de evento — cobre as validações
 * estruturais do formulário (campos obrigatórios, consistência início/fim,
 * formato de horário por turno/dia inteiro, justificativa para status ADICIONADO_EXTRA).
 * Regras que dependem de dados externos ao formulário (ano paroquial fechado,
 * igrejas disponíveis, conflito de datas com o projeto vinculado) permanecem em
 * `useCadastroEventoForm` pois não são propriedades do próprio form.
 */
export const eventoFormSchema = z
  .object({
    titulo: z.string().trim().min(1, 'O título do evento é obrigatório.'),
    descricao: z.string(),
    categoria: z.string().min(1),
    status: z.string().min(1),
    formato: formatoEnum.default('PRESENCIAL'),
    organizacaoResponsavelId: z.string().min(1, 'A organização responsável é obrigatória.'),
    igrejaVinculoId: z.string(),
    localId: z.string(),
    localIds: z.array(z.string()).optional().default([]),
    projetoId: z.string(),
    participantes: z.array(z.string()),
    selectedAtivoIds: z.array(z.string()),
    tipoHorario: tipoHorarioEnum,
    periodos: z.array(tipoHorarioEnum).optional().default([]),
    eventDate: z.string().optional(),
    startDate: z.string(),
    endDate: z.string(),
    endTime: z.string(),
    duracaoHoras: z.number().min(0).max(24).optional(),
    duracaoMinutos: z.number().min(0).max(59).optional(),
    justification: z.string(),
    isRecurring: z.boolean(),
    liberarConflitoAgenda: z.boolean(),
    recurrenceRule: recurrenceRuleSchema,
  })
  .superRefine((data, ctx) => {
    if (data.tipoHorario === 'PERSONALIZADO') {
      if (!data.startDate) {
        ctx.addIssue({ code: 'custom', path: ['startDate'], message: 'A data e hora de início é obrigatória.' });
      }
      if (data.isRecurring) {
        if (!data.endTime) {
          ctx.addIssue({ code: 'custom', path: ['endTime'], message: 'A hora de término é obrigatória.' });
        }
      } else {
        if (!data.endDate) {
          ctx.addIssue({ code: 'custom', path: ['endDate'], message: 'A data e hora de término é obrigatória.' });
        } else if (data.startDate && new Date(data.startDate) > new Date(data.endDate)) {
          ctx.addIssue({
            code: 'custom',
            path: ['endDate'],
            message: 'A data de início não pode ser posterior à data de término.',
          });
        }
      }
    } else {
      // DIA_INTEIRO ou Períodos pré-definidos (MADRUGADA, MANHA, TARDE, NOITE)
      const dateVal = data.eventDate || (data.startDate ? data.startDate.split('T')[0] : '');
      if (!dateVal) {
        ctx.addIssue({ code: 'custom', path: ['eventDate'], message: 'A data do evento é obrigatória.' });
      }
    }

    if (data.formato === 'PRESENCIAL' || data.formato === 'HIBRIDO') {
      const hasLocais = (data.localIds && data.localIds.length > 0) || Boolean(data.localId);
      if (!hasLocais) {
        ctx.addIssue({
          code: 'custom',
          path: ['localIds'],
          message: 'Ao menos um local deve ser selecionado para eventos presenciais ou híbridos.',
        });
      }
    }

    if (data.status === 'ADICIONADO_EXTRA' && !data.justification.trim()) {
      ctx.addIssue({
        code: 'custom',
        path: ['justification'],
        message: 'A justificativa é obrigatória para eventos adicionados extraordinariamente.',
      });
    }
  });

export type EventoFormInput = z.input<typeof eventoFormSchema>;
export type EventoFormOutput = z.output<typeof eventoFormSchema>;
