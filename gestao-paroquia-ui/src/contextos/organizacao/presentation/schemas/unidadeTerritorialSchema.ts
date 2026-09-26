import { z } from 'zod';

export const unidadeTerritorialSchema = z.object({
  nome: z.string().trim().min(3, 'O nome deve ter no mínimo 3 caracteres'),
  tipo: z.enum(['MATRIZ', 'COMUNIDADE'], {
    message: 'Selecione o tipo da unidade territorial',
  }),
  ativo: z.boolean().default(true),
  parentId: z.string().nullable().optional(),

  // Canônicos
  padroeiro: z.string().trim().nullable().optional(),
  dataFestaPadroeira: z
    .string()
    .trim()
    .regex(/^(\d{2}-\d{2})?$/, 'Formato de festa deve ser MM-DD (ex: 05-13)')
    .nullable()
    .optional(),
  dataDedicacaoAltar: z.string().nullable().optional(),
  possuiSacrario: z.boolean().default(true),
  possuiPiaBatismal: z.boolean().default(false),
  clerigoResponsavelId: z.string().nullable().optional(),

  // Fiscais e Jurídicos
  cnpj: z.string().trim().nullable().optional(),
  inscricaoMunicipal: z.string().trim().nullable().optional(),
  inscricaoImobiliariaIptu: z.string().trim().nullable().optional(),
  processoImunidadeIptu: z.string().trim().nullable().optional(),

  // Registro de Imóveis (RGI)
  cartorioRgi: z.string().trim().nullable().optional(),
  matriculaImovel: z.string().trim().nullable().optional(),
  tipoAquisicao: z.string().trim().nullable().optional(),
  areaTerrenoM2: z.preprocess(
    (val) => (val === '' || val === null || val === undefined ? null : Number(val)),
    z.number().nullable().optional()
  ),
  areaConstruidaM2: z.preprocess(
    (val) => (val === '' || val === null || val === undefined ? null : Number(val)),
    z.number().nullable().optional()
  ),

  // Regularidade e Bombeiros
  avcbNumero: z.string().trim().nullable().optional(),
  avcbValidade: z.string().nullable().optional(),
  lotacaoMaximaBombeiros: z.preprocess(
    (val) => (val === '' || val === null || val === undefined ? null : Number(val)),
    z.number().nullable().optional()
  ),

  // Concessionárias
  codigoConsumidorEnergia: z.string().trim().nullable().optional(),
  codigoConsumidorAgua: z.string().trim().nullable().optional(),

  // Contato e Endereço Estruturado (LGPD)
  telefone: z.string().trim().nullable().optional(),
  email: z
    .string()
    .trim()
    .email('E-mail inválido')
    .or(z.literal(''))
    .nullable()
    .optional(),
  enderecoCep: z.string().trim().nullable().optional(),
  enderecoLogradouro: z.string().trim().nullable().optional(),
  enderecoNumero: z.string().trim().nullable().optional(),
  enderecoComplemento: z.string().trim().nullable().optional(),
  enderecoBairro: z.string().trim().nullable().optional(),
  enderecoCidade: z.string().trim().nullable().optional(),
  enderecoUf: z
    .string()
    .trim()
    .max(2, 'UF deve ter 2 caracteres')
    .nullable()
    .optional(),
});

export type UnidadeTerritorialFormSchema = z.infer<typeof unidadeTerritorialSchema>;
export type UnidadeTerritorialFormInput = z.input<typeof unidadeTerritorialSchema>;
export type UnidadeTerritorialFormOutput = z.output<typeof unidadeTerritorialSchema>;
