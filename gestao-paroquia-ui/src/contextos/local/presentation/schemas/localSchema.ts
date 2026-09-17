import { z } from 'zod';

/**
 * Schema de validação formal para cadastro e edição de Locais paroquiais.
 */
export const localSchema = z.object({
  nome: z
    .string()
    .min(2, 'Nome deve ter ao menos 2 caracteres')
    .max(120, 'Nome não pode exceder 120 caracteres'),
  tipo: z.string().min(1, 'Tipo do local é obrigatório'),
  igrejaId: z.string().optional(),
  capacidadeMaxima: z
    .number({ message: 'Capacidade máxima deve ser um número' })
    .int('Capacidade deve ser um número inteiro')
    .positive('Capacidade máxima deve ser maior que zero'),
  descricao: z.string().optional(),
  cep: z.string().optional(),
  logradouro: z.string().max(150, 'Logradouro não pode exceder 150 caracteres').optional(),
  numero: z.string().max(20, 'Número não pode exceder 20 caracteres').optional(),
  complemento: z.string().max(100, 'Complemento não pode exceder 100 caracteres').optional(),
  bairro: z.string().max(100, 'Bairro não pode exceder 100 caracteres').optional(),
  cidade: z.string().max(100, 'Cidade não pode exceder 100 caracteres').optional(),
  uf: z.string().max(2, 'UF deve conter no máximo 2 caracteres').optional(),
  ativo: z.boolean(),
});

export type LocalFormInput = z.infer<typeof localSchema>;
