import { z } from 'zod';

/**
 * Schema de validação formal para cadastro e edição de Fiel.
 * Valida regras civis (CPF, nascimento), canônicas (vocação, estado civil)
 * e consentimento explícito sob as diretrizes da LGPD.
 */
export const fielSchema = z.object({
  nome: z
    .string()
    .min(3, { message: 'Nome deve conter ao menos 3 caracteres' })
    .max(150, { message: 'Nome não pode exceder 150 caracteres' }),
  sexo: z.enum(['MASCULINO', 'FEMININO', 'NAO_INFORMADO']).default('NAO_INFORMADO'),
  cpf: z
    .string()
    .max(14, { message: 'CPF inválido' })
    .optional()
    .or(z.literal('')),
  rg: z.string().max(20).optional().or(z.literal('')),
  dataNascimento: z.string().optional().or(z.literal('')),
  profissao: z.string().max(100).optional().or(z.literal('')),
  email: z
    .string()
    .email({ message: 'E-mail em formato inválido' })
    .optional()
    .or(z.literal('')),
  telefone: z.string().max(20).optional().or(z.literal('')),
  celularWhatsapp: z.string().max(20).optional().or(z.literal('')),
  preferenciaCanalComunicacao: z
    .enum(['WHATSAPP', 'EMAIL', 'SMS', 'TELEFONE', 'NENHUM'])
    .default('WHATSAPP'),
  estadoCivil: z
    .enum([
      'SOLTEIRO',
      'CASADO_IGREJA',
      'CASADO_CIVIL',
      'VIUVO',
      'DIVORCIADO',
      'UNIAO_ESTAVEL',
    ])
    .default('SOLTEIRO'),
  vocacao: z.enum(['LEIGO', 'SACERDOTE', 'DIACONO', 'RELIGIOSO']).default('LEIGO'),
  habilidadesVoluntariado: z.string().max(500).optional().or(z.literal('')),
  ativo: z.boolean().default(true),
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
  vidaSacramental: z
    .object({
      batizado: z.boolean().default(false),
      dataBatismo: z.string().optional().or(z.literal('')),
      paroquiaBatismo: z.string().max(150).optional().or(z.literal('')),
      celebranteBatismo: z.string().max(150).optional().or(z.literal('')),
      livroBatismo: z.string().max(20).optional().or(z.literal('')),
      folhaBatismo: z.string().max(20).optional().or(z.literal('')),
      termoBatismo: z.string().max(20).optional().or(z.literal('')),
      padrinhosBatismo: z.string().max(200).optional().or(z.literal('')),

      primeiraComunhao: z.boolean().default(false),
      dataPrimeiraComunhao: z.string().optional().or(z.literal('')),
      paroquiaPrimeiraComunhao: z.string().max(150).optional().or(z.literal('')),
      turmaCatequese: z.string().max(100).optional().or(z.literal('')),

      crismado: z.boolean().default(false),
      dataCrisma: z.string().optional().or(z.literal('')),
      paroquiaCrisma: z.string().max(150).optional().or(z.literal('')),
      celebranteCrisma: z.string().max(150).optional().or(z.literal('')),
      padrinhoCrisma: z.string().max(200).optional().or(z.literal('')),

      casadoIgrejaCatolica: z.boolean().default(false),
      dataMatrimonio: z.string().optional().or(z.literal('')),
      paroquiaMatrimonio: z.string().max(150).optional().or(z.literal('')),
      numeroProcessoMatrimonial: z.string().max(50).optional().or(z.literal('')),
      certidaoMatrimonio: z.string().max(50).optional().or(z.literal('')),
      nomePaiCertidao: z.string().max(150).optional().or(z.literal('')),
      nomeMaeCertidao: z.string().max(150).optional().or(z.literal('')),
      testemunhasMatrimonio: z.string().max(250).optional().or(z.literal('')),
    })
    .default({
      batizado: false,
      primeiraComunhao: false,
      crismado: false,
      casadoIgrejaCatolica: false,
    }),
  consentimentoLgpd: z
    .object({
      termoConsentimentoAceito: z.boolean().default(false),
      usoImagemAutorizado: z.boolean().default(false),
      permitirDivulgacaoAniversario: z.boolean().default(false),
    })
    .default({
      termoConsentimentoAceito: false,
      usoImagemAutorizado: false,
      permitirDivulgacaoAniversario: false,
    }),
});

export type FielFormInput = z.infer<typeof fielSchema>;
