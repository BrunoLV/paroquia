import { z } from 'zod';

export const reservaBaseSchema = z.object({
  tipoItem: z.enum(['LOCAL', 'ATIVO'], {
    message: 'Selecione se deseja reservar um Local ou Ativo.',
  }),
  itemId: z.string().uuid('Selecione um item válido.'),
  dataHoraInicio: z.string().min(1, 'Data e hora de início são obrigatórias.'),
  dataHoraFim: z.string().min(1, 'Data e hora de término são obrigatórias.'),
  bufferMinutos: z.number().min(0, 'Margem de carência não pode ser negativa.'),
  descricao: z
    .string()
    .trim()
    .min(3, 'Finalidade da reserva deve conter pelo menos 3 caracteres.'),
  pastoralMovimentoId: z.string().uuid('Selecione a Pastoral ou Movimento solicitante.'),
  eventoId: z.string().uuid().nullable().optional(),
  isRecorrente: z.boolean(),
  tipoRecorrencia: z.enum(['SEMANAL', 'QUINZENAL', 'MENSAL']).optional(),
  quantidadeOcorrencias: z.number().min(2).max(52).optional(),
});

export const reservaSchema = reservaBaseSchema
  .refine(
    (data) => {
      if (!data.dataHoraInicio || !data.dataHoraFim) return true;
      const inicio = new Date(data.dataHoraInicio).getTime();
      const fim = new Date(data.dataHoraFim).getTime();
      return fim > inicio;
    },
    {
      message: 'A data/hora de término deve ser posterior à data/hora de início.',
      path: ['dataHoraFim'],
    }
  )
  .refine(
    (data) => {
      if (data.isRecorrente) {
        return !!data.tipoRecorrencia && !!data.quantidadeOcorrencias && data.quantidadeOcorrencias >= 2;
      }
      return true;
    },
    {
      message: 'Para agendamento recorrente, defina a frequência e ao menos 2 ocorrências.',
      path: ['quantidadeOcorrencias'],
    }
  );

export type ReservaFormData = z.infer<typeof reservaSchema>;

export const cancelamentoReservaSchema = z.object({
  motivo: z
    .string()
    .trim()
    .min(5, 'Justificativa de cancelamento deve ter pelo menos 5 caracteres.'),
});

export type CancelamentoReservaFormData = z.infer<typeof cancelamentoReservaSchema>;

export const devolucaoAtivoSchema = z.object({
  checkAvarias: z
    .string()
    .trim()
    .min(3, 'Relatório de conferência de avarias é obrigatório (ex: "Sem avarias, testado").'),
});

export type DevolucaoAtivoFormData = z.infer<typeof devolucaoAtivoSchema>;
