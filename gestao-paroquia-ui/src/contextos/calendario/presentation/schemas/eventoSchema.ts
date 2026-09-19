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

/**
 * Schema do formulário de cadastro/edição de evento — cobre as validações
 * estruturais do formulário (campos obrigatórios, consistência início/fim,
 * justificativa para status ADICIONADO_EXTRA). Regras que dependem de dados
 * externos ao formulário (ano paroquial fechado, igrejas disponíveis,
 * conflito de datas com o projeto vinculado) permanecem em
 * `useCadastroEventoForm` pois não são propriedades do próprio form.
 */
export const eventoFormSchema = z
  .object({
    titulo: z.string().trim().min(1, 'O título do evento é obrigatório.'),
    descricao: z.string(),
    categoria: z.string().min(1),
    status: z.string().min(1),
    grupoParoquialResponsavelId: z.string().min(1, 'A organização responsável é obrigatória.'),
    igrejaVinculoId: z.string(),
    localId: z.string(),
    projetoId: z.string(),
    participantes: z.array(z.string()),
    selectedAtivoIds: z.array(z.string()),
    startDate: z.string().min(1, 'A data e hora de início é obrigatória.'),
    endDate: z.string(),
    endTime: z.string(),
    justification: z.string(),
    isRecurring: z.boolean(),
    liberarConflitoAgenda: z.boolean(),
    recurrenceRule: recurrenceRuleSchema,
  })
  .superRefine((data, ctx) => {
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

    if (data.status === 'ADICIONADO_EXTRA' && !data.justification.trim()) {
      ctx.addIssue({
        code: 'custom',
        path: ['justification'],
        message: 'A justificativa é obrigatória para eventos adicionados extraordinariamente.',
      });
    }
  });

export type EventoFormInput = z.infer<typeof eventoFormSchema>;
