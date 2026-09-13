import { z } from 'zod';

/**
 * Endereço civil do fiel. Todos os campos são strings livres (máscaras
 * aplicadas na camada de UI); obrigatoriedade dos campos de logradouro é
 * decidida nos schemas de formulário (create/edit) via `superRefine`,
 * pois o mesmo endereço é usado tanto em criação (todos obrigatórios)
 * quanto potencialmente em edições parciais.
 */
const enderecoSchema = z.object({
  logradouro: z.string().optional(),
  numero: z.string().optional(),
  complemento: z.string().optional(),
  bairro: z.string().optional(),
  cep: z.string().optional(),
  cidade: z.string().optional(),
  estado: z.string().optional(),
});

/**
 * Regra canônica de identificação civil: fiéis nascidos no Brasil informam
 * CPF; estrangeiros informam tipo + número de documento (RNE/RNM ou
 * passaporte). Espelha exatamente a regra antes imposta manualmente em
 * `podeSalvarNovo` (FielCreateModal) e replicada (sem bloqueio) em
 * FielEditModal.
 */
function validarIdentificacaoCivil(
  data: { paisOrigem: string; cpf?: string; tipoDocumentoEstrangeiro?: string; numeroDocumentoEstrangeiro?: string },
  ctx: z.RefinementCtx,
) {
  const estrangeiro = data.paisOrigem !== 'BRA';
  if (estrangeiro) {
    if (!data.tipoDocumentoEstrangeiro) {
      ctx.addIssue({ code: 'custom', path: ['tipoDocumentoEstrangeiro'], message: 'Selecione o tipo de documento' });
    }
    if (!data.numeroDocumentoEstrangeiro?.trim()) {
      ctx.addIssue({ code: 'custom', path: ['numeroDocumentoEstrangeiro'], message: 'Informe o número do documento' });
    }
  } else if (!data.cpf?.trim()) {
    ctx.addIssue({ code: 'custom', path: ['cpf'], message: 'CPF é obrigatório' });
  }
}

function validarEnderecoCompleto(
  endereco: { logradouro?: string; bairro?: string; cidade?: string; estado?: string; cep?: string },
  ctx: z.RefinementCtx,
) {
  if (!endereco.logradouro?.trim()) {
    ctx.addIssue({ code: 'custom', path: ['endereco', 'logradouro'], message: 'Logradouro é obrigatório' });
  }
  if (!endereco.bairro?.trim()) {
    ctx.addIssue({ code: 'custom', path: ['endereco', 'bairro'], message: 'Bairro é obrigatório' });
  }
  if (!endereco.cidade?.trim()) {
    ctx.addIssue({ code: 'custom', path: ['endereco', 'cidade'], message: 'Cidade é obrigatória' });
  }
  if (!endereco.estado?.trim()) {
    ctx.addIssue({ code: 'custom', path: ['endereco', 'estado'], message: 'UF é obrigatória' });
  }
  if (!endereco.cep?.trim()) {
    ctx.addIssue({ code: 'custom', path: ['endereco', 'cep'], message: 'CEP é obrigatório' });
  }
}

/** Schema de cadastro de novo fiel — espelha `CreateFielPayload` + regras de `podeSalvarNovo`. */
export const fielCreateSchema = z
  .object({
    nome: z.string().trim().min(1, 'Nome completo é obrigatório'),
    sexo: z.enum(['MASCULINO', 'FEMININO', 'NAO_INFORMADO']),
    paisOrigem: z.string().min(1),
    cpf: z.string().optional(),
    rg: z.string().optional(),
    tipoDocumentoEstrangeiro: z.enum(['RNE_RNM', 'PASSAPORTE']).optional(),
    numeroDocumentoEstrangeiro: z.string().optional(),
    dataNascimento: z.string().min(1, 'Data de nascimento é obrigatória'),
    email: z.string().trim().min(1, 'E-mail é obrigatório'),
    celular: z.string().trim().min(1, 'Celular é obrigatório'),
    isWhatsapp: z.boolean(),
    telefoneFixo: z.string().optional(),
    unidadeTerritorialId: z.string().min(1, 'Selecione a igreja que frequenta'),
    isDizimista: z.boolean(),
    permitirDivulgacaoAniversario: z.boolean(),
    endereco: enderecoSchema,
  })
  .superRefine((data, ctx) => {
    validarIdentificacaoCivil(data, ctx);
    validarEnderecoCompleto(data.endereco, ctx);
  });

export type FielCreateFormInput = z.infer<typeof fielCreateSchema>;

/**
 * Schema de edição cadastral do fiel — espelha `UpdateFielPayload`.
 * Os campos marcados `required` na UI original (sem bloqueio efetivo antes
 * da migração) passam a ser realmente exigidos, alinhando a tela de edição
 * ao mesmo padrão de integridade de dados do cadastro.
 */
export const fielEditSchema = z
  .object({
    nome: z.string().trim().min(1, 'Nome completo é obrigatório'),
    sexo: z.enum(['MASCULINO', 'FEMININO', 'NAO_INFORMADO']),
    paisOrigem: z.string().min(1),
    cpf: z.string().optional(),
    rg: z.string().optional(),
    tipoDocumentoEstrangeiro: z.enum(['RNE_RNM', 'PASSAPORTE']).optional(),
    numeroDocumentoEstrangeiro: z.string().optional(),
    dataNascimento: z.string().min(1, 'Data de nascimento é obrigatória'),
    email: z.string().trim().min(1, 'E-mail é obrigatório'),
    celular: z.string().trim().min(1, 'Celular é obrigatório'),
    isWhatsapp: z.boolean(),
    telefoneFixo: z.string().optional(),
    unidadeTerritorialId: z.string().min(1, 'Selecione a igreja que frequenta'),
    isDizimista: z.boolean(),
    permitirDivulgacaoAniversario: z.boolean(),
    endereco: enderecoSchema,
  })
  .superRefine((data, ctx) => {
    validarIdentificacaoCivil(data, ctx);
    validarEnderecoCompleto(data.endereco, ctx);
  });

export type FielEditFormInput = z.infer<typeof fielEditSchema>;
