import { z } from 'zod';

/** Cadastro de ativo patrimonial — espelha `CadastrarAtivoPayload`. */
export const ativoSchema = z.object({
  nome: z
    .string()
    .trim()
    .min(2, 'Nome deve ter ao menos 2 caracteres')
    .max(200, 'Nome não pode exceder 200 caracteres'),
  descricao: z.string().max(4000).optional().or(z.literal('')),
  categoria: z.enum(['ELETRONICO', 'MOBILIARIO', 'PARAMENTO', 'INFRAESTRUTURA']),
  grupoParoquialId: z.string().min(1, 'Selecione a unidade territorial proprietária'),
  isReservavel: z.boolean(),
  dataAquisicao: z.string().min(1, 'Data de aquisição é obrigatória'),
  valorAquisicao: z
    .number({ message: 'Informe um valor numérico' })
    .nonnegative('Valor de aquisição não pode ser negativo'),
});

export type AtivoFormInput = z.infer<typeof ativoSchema>;

/** Edição de ativo — somente a descrição. */
export const descricaoAtivoSchema = z.object({
  descricao: z.string().max(4000).optional().or(z.literal('')),
});

export type DescricaoAtivoFormInput = z.infer<typeof descricaoAtivoSchema>;

/** Envio para manutenção. */
export const manutencaoAtivoSchema = z
  .object({
    descricaoReparo: z
      .string()
      .trim()
      .min(10, 'Descreva o reparo com ao menos 10 caracteres')
      .max(4000),
    dataInicio: z.string().min(1, 'Informe o início da manutenção'),
    dataFimPrevista: z.string().min(1, 'Informe a previsão de término'),
  })
  .refine(
    (v) => !v.dataInicio || !v.dataFimPrevista || new Date(v.dataFimPrevista) > new Date(v.dataInicio),
    { path: ['dataFimPrevista'], message: 'A previsão de término deve ser posterior ao início' },
  );

export type ManutencaoAtivoFormInput = z.infer<typeof manutencaoAtivoSchema>;
