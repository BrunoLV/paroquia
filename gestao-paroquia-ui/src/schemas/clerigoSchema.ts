import { z } from 'zod';

/**
 * Schema de validação formal para cadastro e edição de Clérigo.
 * Assegura conformidade canônica com graus da ordem e normas de privacidade
 * equiparadas às de fiéis leigos sob a LGPD.
 */
export const clerigoSchema = z.object({
  nome: z
    .string()
    .min(3, { message: 'Nome deve conter ao menos 3 caracteres' })
    .max(150, { message: 'Nome não pode exceder 150 caracteres' }),
  nomeReligioso: z.string().max(150).optional().or(z.literal('')),
  grauOrdenacao: z.enum(['DIACONO', 'PADRE', 'BISPO'], {
    message: 'Grau de ordenação é obrigatório',
  }),
  cpf: z
    .string()
    .max(14, { message: 'CPF inválido' })
    .optional()
    .or(z.literal('')),
  rg: z.string().max(20).optional().or(z.literal('')),
  dataNascimento: z.string().optional().or(z.literal('')),
  dataOrdenacaoDiaconal: z.string().optional().or(z.literal('')),
  dataOrdenacaoPresbiteral: z.string().optional().or(z.literal('')),
  dataOrdenacaoEpiscopal: z.string().optional().or(z.literal('')),
  bispoOrdenante: z.string().max(150).optional().or(z.literal('')),
  dioceseOrigem: z.string().max(150).optional().or(z.literal('')),
  situacaoCanonica: z
    .enum(['ATIVO', 'AFASTADO', 'APOSENTADO', 'EMERITO', 'FALECIDO'])
    .default('ATIVO'),
  email: z
    .string()
    .email({ message: 'E-mail em formato inválido' })
    .optional()
    .or(z.literal('')),
  telefone: z.string().max(20).optional().or(z.literal('')),
  celularWhatsapp: z.string().max(20).optional().or(z.literal('')),
  endereco: z
    .object({
      logradouro: z.string().max(150).optional().or(z.literal('')),
      numero: z.string().max(20).optional().or(z.literal('')),
      complemento: z.string().max(100).optional().or(z.literal('')),
      bairro: z.string().max(100).optional().or(z.literal('')),
      cep: z.string().max(10).optional().or(z.literal('')),
      cidade: z.string().max(100).optional().or(z.literal('')),
      estado: z.string().max(2).optional().or(z.literal('')),
    })
    .default({}),
});

export type ClerigoFormInput = z.infer<typeof clerigoSchema>;
