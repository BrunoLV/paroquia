import { z } from 'zod';

/**
 * Schema de validação formal para cadastro de Ativos Patrimoniais.
 */
export const ativoSchema = z.object({
  codigoPatrimonio: z.string().max(50).optional().or(z.literal('')),
  descricao: z
    .string()
    .min(3, { message: 'Descrição deve ter ao menos 3 caracteres' })
    .max(255, { message: 'Descrição não pode exceder 255 caracteres' }),
  categoria: z.string().min(1, { message: 'Categoria é obrigatória' }),
  localId: z.string().uuid({ message: 'Local inválido' }).optional().or(z.literal('')),
  valorAquisicao: z.number().nonnegative().optional().default(0),
  estadoConservacao: z.enum(['NOVO', 'BOM', 'REGULAR', 'RUIM', 'INSERVIVEL']).default('BOM'),
  observacoes: z.string().max(500).optional().or(z.literal('')),
});

export type AtivoFormInput = z.infer<typeof ativoSchema>;
