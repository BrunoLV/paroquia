import { z } from 'zod';

/** Ressalva formal de retificação material, atestada pelo Pároco. */
export const retificacaoSchema = z.object({
  campo: z.string().trim().min(1, 'Informe o campo retificado').max(128),
  valorOriginal: z.string(),
  valorCorrigido: z.string().trim().min(1, 'Informe o valor correto'),
  justificativa: z.string().trim().min(10, 'Justifique a retificação (mín. 10 caracteres)'),
});
export type RetificacaoFormInput = z.infer<typeof retificacaoSchema>;

/** Cancelamento por duplicidade ou erro formal grave — o motivo é obrigatório e fica registrado. */
export const cancelamentoSchema = z.object({
  motivo: z.string().trim().min(10, 'Informe o motivo do cancelamento (mín. 10 caracteres)'),
});
export type CancelamentoFormInput = z.infer<typeof cancelamentoSchema>;

/** Nota marginal do Batismo. */
export const averbacaoSchema = z.object({
  tipoEvento: z.enum(['CONFIRMACAO', 'MATRIMONIO', 'ORDEM', 'VOTOS_PERPETUOS', 'NULIDADE', 'ANOTACAO_DISCIPLINAR']),
  dataAverbacao: z.string().min(1, 'Informe a data da averbação'),
  numeroDocumento: z.string().max(128),
  teorNota: z.string().trim().min(5, 'Informe o teor da nota marginal'),
  reservada: z.boolean(),
});
export type AverbacaoFormInput = z.infer<typeof averbacaoSchema>;
