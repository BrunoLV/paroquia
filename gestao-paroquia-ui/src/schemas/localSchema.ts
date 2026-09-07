import { z } from 'zod';

/**
 * Schema de validação formal para cadastro e edição de Locais paroquiais.
 */
export const localSchema = z.object({
  nome: z
    .string()
    .min(2, { message: 'Nome deve ter ao menos 2 caracteres' })
    .max(120, { message: 'Nome não pode exceder 120 caracteres' }),
  tipo: z.string().min(1, { message: 'Tipo do local é obrigatório' }),
  capacidade: z.number().int().nonnegative({ message: 'Capacidade não pode ser negativa' }).default(0),
  descricao: z.string().max(255).optional().or(z.literal('')),
  endereco: z.string().max(255).optional().or(z.literal('')),
  ativo: z.boolean().default(true),
});

export type LocalFormInput = z.infer<typeof localSchema>;
