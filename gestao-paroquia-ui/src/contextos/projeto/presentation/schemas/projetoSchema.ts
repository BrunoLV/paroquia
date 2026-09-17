import { z } from 'zod';

/**
 * Schema de validação formal para cadastro e edição de Projetos e Ações Pastorais.
 */
export const projetoSchema = z
  .object({
    titulo: z
      .string()
      .min(3, { message: 'Título deve ter ao menos 3 caracteres' })
      .max(150, { message: 'Título não pode exceder 150 caracteres' }),
    descricao: z.string().max(1000).optional().or(z.literal('')),
    dataInicio: z.string().min(1, { message: 'Data de início é obrigatória' }),
    dataFim: z.string().optional().or(z.literal('')),
    orcamentoPrevisto: z.number().nonnegative({ message: 'Orçamento não pode ser negativo' }).default(0),
    responsavelId: z.string().uuid().optional().or(z.literal('')),
  })
  .refine(
    (data) => {
      if (!data.dataFim || !data.dataInicio) return true;
      return new Date(data.dataFim) >= new Date(data.dataInicio);
    },
    {
      message: 'A data de término não pode ser anterior à data de início',
      path: ['dataFim'],
    }
  );

export type ProjetoFormInput = z.infer<typeof projetoSchema>;
