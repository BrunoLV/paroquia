import { z } from 'zod';

export const dioceseSchema = z.object({
  nome: z.string().trim().min(3, 'O nome da diocese deve ter no mínimo 3 caracteres'),
  bispoDiocesano: z.string().trim().min(3, 'Informe o nome do Bispo Diocesano'),
  cnpjMitra: z.string().trim().nullable().optional(),
  cidade: z.string().trim().nullable().optional(),
  uf: z.string().trim().max(2, 'UF deve ter 2 caracteres').nullable().optional(),
  contato: z.string().trim().nullable().optional(),
  ativo: z.boolean(),
});

export type DioceseFormSchema = z.infer<typeof dioceseSchema>;

export const governoEclesiasticoSchema = z.object({
  dioceseId: z.string().min(1, 'Selecione a Diocese'),
  parocoId: z.string().nullable().optional(),
  dataPosseParoco: z.string().nullable().optional(),
  decretoErecaoParoquia: z.string().trim().nullable().optional(),
  dataInstalacaoParoquia: z.string().nullable().optional(),
  livroTomboNumero: z.string().trim().nullable().optional(),
});

export type GovernoEclesiasticoFormSchema = z.infer<typeof governoEclesiasticoSchema>;
