import { z } from 'zod';

/**
 * Endereço civil do fiel. Todos os campos são strings livres (máscaras
 * aplicadas na camada de UI); obrigatoriedade dos campos de logradouro é
 * decidida nos schemas de formulário (create/edit) via `superRefine`,
 * pois o mesmo endereço é usado tanto em criação (todos obrigatórios)
 * quanto potencialmente em edições parciais.
 */
export const enderecoSchema = z.object({
  cep: z.string().optional(),
  logradouro: z.string().optional(),
  numero: z.string().optional(),
  complemento: z.string().optional(),
  bairro: z.string().optional(),
  cidade: z.string().optional(),
  uf: z.string().optional(),
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

function calcularIdade(dataNascStr?: string): number | null {
  if (!dataNascStr) return null;
  const parts = dataNascStr.split('-');
  if (parts.length === 3) {
    const ano = parseInt(parts[0], 10);
    const mes = parseInt(parts[1], 10) - 1;
    const dia = parseInt(parts[2], 10);
    const nasc = new Date(ano, mes, dia);
    const hoje = new Date();
    let idade = hoje.getFullYear() - nasc.getFullYear();
    const m = hoje.getMonth() - nasc.getMonth();
    if (m < 0 || (m === 0 && hoje.getDate() < nasc.getDate())) {
      idade--;
    }
    return idade;
  }
  const nasc = new Date(dataNascStr);
  if (isNaN(nasc.getTime())) return null;
  const hoje = new Date();
  let idade = hoje.getFullYear() - nasc.getFullYear();
  const m = hoje.getMonth() - nasc.getMonth();
  if (m < 0 || (m === 0 && hoje.getDate() < nasc.getDate())) {
    idade--;
  }
  return idade;
}

function validarSalvaguardaMenor(
  data: {
    dataNascimento?: string;
    termoConsentimentoAceito?: boolean;
    responsavelMenorNome?: string;
    responsavelMenorCpf?: string;
  },
  ctx: z.RefinementCtx,
) {
  const idade = calcularIdade(data.dataNascimento);
  if (idade !== null && idade < 18 && data.termoConsentimentoAceito) {
    if (!data.responsavelMenorNome?.trim()) {
      ctx.addIssue({
        code: 'custom',
        path: ['responsavelMenorNome'],
        message: 'Nome do responsável legal é obrigatório para menores de 18 anos (Art. 14 LGPD)',
      });
    }
    if (!data.responsavelMenorCpf?.trim()) {
      ctx.addIssue({
        code: 'custom',
        path: ['responsavelMenorCpf'],
        message: 'CPF do responsável legal é obrigatório para menores de 18 anos',
      });
    }
  }
}

function validarEnderecoCompleto(
  endereco: {
    logradouro?: string;
    bairro?: string;
    cidade?: string;
    uf?: string;
    estado?: string;
    cep?: string;
  },
  ctx: z.RefinementCtx,
) {
  if (!endereco.cep?.trim()) {
    ctx.addIssue({ code: 'custom', path: ['endereco', 'cep'], message: 'CEP é obrigatório' });
  }
  if (!endereco.logradouro?.trim()) {
    ctx.addIssue({ code: 'custom', path: ['endereco', 'logradouro'], message: 'Logradouro é obrigatório' });
  }
  if (!endereco.bairro?.trim()) {
    ctx.addIssue({ code: 'custom', path: ['endereco', 'bairro'], message: 'Bairro é obrigatório' });
  }
  if (!endereco.cidade?.trim()) {
    ctx.addIssue({ code: 'custom', path: ['endereco', 'cidade'], message: 'Cidade é obrigatória' });
  }
  const ufValue = endereco.uf?.trim() || endereco.estado?.trim();
  if (!ufValue) {
    ctx.addIssue({ code: 'custom', path: ['endereco', 'uf'], message: 'UF é obrigatória' });
  } else if (ufValue.length > 2) {
    ctx.addIssue({ code: 'custom', path: ['endereco', 'uf'], message: 'UF deve ter 2 caracteres' });
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
    termoConsentimentoAceito: z.boolean().optional(),
    usoImagemAutorizado: z.boolean().optional(),
    responsavelMenorNome: z.string().optional(),
    responsavelMenorCpf: z.string().optional(),
    endereco: enderecoSchema,
  })
  .superRefine((data, ctx) => {
    validarIdentificacaoCivil(data, ctx);
    validarEnderecoCompleto(data.endereco, ctx);
    validarSalvaguardaMenor(data, ctx);
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
    termoConsentimentoAceito: z.boolean().optional(),
    usoImagemAutorizado: z.boolean().optional(),
    responsavelMenorNome: z.string().optional(),
    responsavelMenorCpf: z.string().optional(),
    endereco: enderecoSchema,
  })
  .superRefine((data, ctx) => {
    validarIdentificacaoCivil(data, ctx);
    validarEnderecoCompleto(data.endereco, ctx);
    validarSalvaguardaMenor(data, ctx);
  });

export type FielEditFormInput = z.infer<typeof fielEditSchema>;
