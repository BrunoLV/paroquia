import { z } from 'zod';

/**
 * Schema de validação formal para cadastro e edição de Clérigo — espelha
 * `ClerigoPayload`. Campos obrigatórios seguem os marcadores `required`
 * já presentes na UI original (`ClerigoFormModal`), antes apenas visuais
 * e não bloqueantes; agora efetivamente exigidos.
 */
export const clerigoEnderecoSchema = z.object({
  cep: z.string().optional(),
  logradouro: z.string().optional(),
  numero: z.string().optional(),
  complemento: z.string().optional(),
  bairro: z.string().optional(),
  cidade: z.string().optional(),
  uf: z.string().optional(),
}).optional();

export const clerigoSchema = z.object({
  nomeEclesiastico: z.string().trim().min(1, 'Nome eclesiástico é obrigatório'),
  nomeCivil: z.string().optional(),
  grauOrdem: z.enum(['DIACONO_TRANSITORIO', 'DIACONO_PERMANENTE', 'PADRE_DIOCESANO', 'PADRE_RELIGIOSO', 'BISPO']),
  funcaoAtual: z.enum([
    'BISPO_DIOCESANO',
    'PAROCO',
    'ADMINISTRADOR_PAROQUIAL',
    'VIGARIO_PAROQUIAL',
    'DIACONO_AUXILIAR',
    'EMERITO',
  ]),
  dioceseIncardinacao: z.string().trim().min(1, 'Diocese de incardinação é obrigatória'),
  cpf: z.string().optional(),
  rg: z.string().optional(),
  dataNascimento: z.string().optional(),
  paisOrigem: z.string().optional(),
  documentoEstrangeiro: z.string().optional(),
  dataOrdenacaoDiaconal: z.string().nullable().optional(),
  dataOrdenacaoPresbiteral: z.string().nullable().optional(),
  dataOrdenacaoEpiscopal: z.string().nullable().optional(),
  email: z.string().trim().min(1, 'E-mail é obrigatório'),
  celular: z.string().trim().min(1, 'Celular é obrigatório'),
  telefoneFixo: z.string().optional(),
  endereco: clerigoEnderecoSchema,
  isWhatsapp: z.boolean(),
});

export type ClerigoFormInput = z.infer<typeof clerigoSchema>;
