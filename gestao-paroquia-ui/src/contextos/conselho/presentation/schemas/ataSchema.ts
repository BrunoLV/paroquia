import { z } from 'zod';
import {
  Ata,
  ConteudoAtaPayload,
  funcaoEhUnica,
  RascunharAtaPayload,
  TipoReuniao,
  TipoReuniaoInfo,
} from '@/contextos/conselho/domain/ata';

const SITUACOES = ['PRESENTE', 'AUSENTE_JUSTIFICADA', 'AUSENTE'] as const;
const RESULTADOS = ['PARECER_FAVORAVEL', 'PARECER_CONTRARIO', 'ADIADA', 'INFORMATIVA'] as const;
const CARATERES = ['ORDINARIA', 'EXTRAORDINARIA'] as const;
const MODALIDADES = ['PRESENCIAL', 'VIRTUAL', 'HIBRIDA'] as const;
const APROVACOES = ['NAO_APLICAVEL', 'APROVADA', 'APROVADA_COM_RESSALVAS'] as const;

const votoSchema = z.number({ error: 'Informe um número.' }).int('Use números inteiros.').min(0, 'Não pode ser negativo.');

const conflitoSchema = z.object({
  declaranteFielId: z.string().min(1, 'Selecione quem declara o conflito.'),
  descricao: z.string().trim().min(1, 'Descreva o conflito de interesses.').max(500, 'Máximo de 500 caracteres.'),
});

const deliberacaoSchema = z.object({
  texto: z.string().trim().min(1, 'Informe a deliberação.').max(4000, 'Máximo de 4000 caracteres.'),
  resultado: z.enum(RESULTADOS),
  comVotacao: z.boolean(),
  favor: votoSchema,
  contra: votoSchema,
  abstencoes: votoSchema,
  conflitos: z.array(conflitoSchema),
});

const itemSchema = z.object({
  titulo: z.string().trim().min(1, 'Informe o título do item.').max(160, 'Máximo de 160 caracteres.'),
  relato: z.string().max(4000, 'Máximo de 4000 caracteres.'),
  deliberacoes: z.array(deliberacaoSchema),
});

const presencaSchema = z.object({
  fielId: z.string().min(1, 'Selecione a pessoa.'),
  clerigo: z.boolean(),
  funcao: z.string().max(80, 'Máximo de 80 caracteres.'),
  situacao: z.enum(SITUACOES),
});

/** Conflitos da lista de presença: pessoa repetida, função única repetida e função de quem não esteve presente. */
const validarFuncoes = (presencas: z.infer<typeof presencaSchema>[], ctx: z.RefinementCtx): void => {
  const pessoas = new Set<string>();
  const funcoes = new Set<string>();
  presencas.forEach((p, i) => {
    if (p.fielId && pessoas.has(p.fielId)) {
      ctx.addIssue({ code: 'custom', path: ['presencas', i, 'fielId'], message: 'Esta pessoa já consta na lista.' });
    }
    pessoas.add(p.fielId);
    if (!p.funcao) return;
    if (p.situacao !== 'PRESENTE') {
      ctx.addIssue({ code: 'custom', path: ['presencas', i, 'funcao'], message: 'Só quem esteve presente exerce função.' });
    } else if (funcaoEhUnica(p.funcao)) {
      if (funcoes.has(p.funcao)) {
        ctx.addIssue({ code: 'custom', path: ['presencas', i, 'funcao'], message: `A função ${p.funcao} só pode ser exercida por uma pessoa.` });
      }
      funcoes.add(p.funcao);
    }
  });
};

/**
 * Formulário da ata. Reflete só o formato do conteúdo; quais campos valem (lista nominal ou total, votação) é regra
 * do tipo de reunião, vinda do servidor, e é aplicada em `paraConteudoPayload`.
 */
export const ataSchema = z
  .object({
    tipo: z.string().min(1, 'Selecione o tipo de reunião.'),
    organizacaoConvocanteId: z.string().min(1, 'Selecione o órgão convocante.'),
    organizacaoAssociadaId: z.string(),
    titulo: z.string().trim().min(1, 'Informe o título da ata.').max(160, 'Máximo de 160 caracteres.'),
    dataReuniao: z.string().min(1, 'Informe a data da reunião.'),
    local: z.string().trim().min(1, 'Informe o local da reunião.').max(200, 'Máximo de 200 caracteres.'),
    carater: z.enum(CARATERES),
    modalidade: z.enum(MODALIDADES),
    presencas: z.array(presencaSchema),
    totalParticipantes: z.number().int().min(0, 'Não pode ser negativo.').nullable(),
    ataAnteriorId: z.string(),
    aprovacaoAtaAnterior: z.enum(APROVACOES),
    ressalvasAtaAnterior: z.string().max(2000, 'Máximo de 2000 caracteres.'),
    itens: z.array(itemSchema),
  })
  .superRefine((dados, ctx) => {
    if (dados.ataAnteriorId && dados.aprovacaoAtaAnterior === 'NAO_APLICAVEL') {
      ctx.addIssue({ code: 'custom', path: ['aprovacaoAtaAnterior'], message: 'Informe se a ata anterior foi aprovada.' });
    }
    if (dados.aprovacaoAtaAnterior === 'APROVADA_COM_RESSALVAS' && !dados.ressalvasAtaAnterior.trim()) {
      ctx.addIssue({ code: 'custom', path: ['ressalvasAtaAnterior'], message: 'Informe as ressalvas feitas à ata anterior.' });
    }
    validarFuncoes(dados.presencas, ctx);
  });

export type AtaFormInput = z.infer<typeof ataSchema>;
export type ItemFormInput = AtaFormInput['itens'][number];
export type DeliberacaoFormInput = ItemFormInput['deliberacoes'][number];

export const presencaVazia = (): AtaFormInput['presencas'][number] => ({
  fielId: '',
  clerigo: false,
  funcao: '',
  situacao: 'PRESENTE',
});

export const deliberacaoVazia = (): DeliberacaoFormInput => ({
  texto: '',
  resultado: 'PARECER_FAVORAVEL',
  comVotacao: false,
  favor: 0,
  contra: 0,
  abstencoes: 0,
  conflitos: [],
});

export const itemVazio = (): ItemFormInput => ({ titulo: '', relato: '', deliberacoes: [] });

/** Formulário de uma ata nova, com a data de hoje e reunião ordinária presencial. */
export const ataVazia = (hoje: string = new Date().toISOString().slice(0, 10)): AtaFormInput => ({
  tipo: '',
  organizacaoConvocanteId: '',
  organizacaoAssociadaId: '',
  titulo: '',
  dataReuniao: hoje,
  local: '',
  carater: 'ORDINARIA',
  modalidade: 'PRESENCIAL',
  presencas: [],
  totalParticipantes: null,
  ataAnteriorId: '',
  aprovacaoAtaAnterior: 'NAO_APLICAVEL',
  ressalvasAtaAnterior: '',
  itens: [],
});

/** Carrega uma ata existente no formulário de edição do rascunho. */
export const ataParaFormulario = (ata: Ata): AtaFormInput => ({
  tipo: ata.tipo,
  organizacaoConvocanteId: ata.organizacaoConvocanteId,
  organizacaoAssociadaId: ata.organizacaoAssociadaId ?? '',
  titulo: ata.conteudo.titulo,
  dataReuniao: ata.conteudo.dataReuniao,
  local: ata.conteudo.local,
  carater: ata.conteudo.carater,
  modalidade: ata.conteudo.modalidade,
  presencas: ata.conteudo.presencas.map((p) => ({
    fielId: p.fielId, clerigo: p.clerigo, funcao: p.funcao ?? '', situacao: p.situacao,
  })),
  totalParticipantes: ata.conteudo.totalParticipantes,
  ataAnteriorId: ata.conteudo.ataAnteriorId ?? '',
  aprovacaoAtaAnterior: ata.conteudo.aprovacaoAtaAnterior,
  ressalvasAtaAnterior: ata.conteudo.ressalvasAtaAnterior ?? '',
  itens: ata.conteudo.itens.map((item) => ({
    titulo: item.titulo,
    relato: item.relato ?? '',
    deliberacoes: item.deliberacoes.map((d) => ({
      texto: d.texto,
      resultado: d.resultado,
      comVotacao: d.votacao !== null,
      favor: d.votacao?.favor ?? 0,
      contra: d.votacao?.contra ?? 0,
      abstencoes: d.votacao?.abstencoes ?? 0,
      conflitos: d.conflitos.map((c) => ({ declaranteFielId: c.declaranteFielId, descricao: c.descricao })),
    })),
  })),
});

const opcional = (texto: string): string | undefined => (texto.trim() ? texto.trim() : undefined);

/**
 * Converte o formulário no corpo da API aplicando as regras do tipo: presença nominal ou apenas o total, e votação
 * só onde o tipo vota. Campos que o tipo não admite são descartados em vez de enviados e rejeitados pelo servidor.
 */
export const paraConteudoPayload = (dados: AtaFormInput, regras: TipoReuniaoInfo): ConteudoAtaPayload => ({
  titulo: dados.titulo.trim(),
  dataReuniao: dados.dataReuniao,
  local: dados.local.trim(),
  carater: dados.carater,
  modalidade: dados.modalidade,
  presenca:
    regras.modoPresenca === 'QUANTITATIVA'
      ? { total: dados.totalParticipantes ?? undefined }
      : {
          nominal: dados.presencas.map((p) => ({
            fielId: p.fielId, clerigo: p.clerigo || undefined, funcao: opcional(p.funcao), situacao: p.situacao,
          })),
        },
  ataAnterior: dados.ataAnteriorId
    ? {
        ataId: dados.ataAnteriorId,
        aprovacao: dados.aprovacaoAtaAnterior,
        ressalvas: dados.aprovacaoAtaAnterior === 'APROVADA_COM_RESSALVAS' ? opcional(dados.ressalvasAtaAnterior) : undefined,
      }
    : { aprovacao: 'NAO_APLICAVEL' },
  itens: dados.itens.map((item) => ({
    titulo: item.titulo.trim(),
    relato: opcional(item.relato),
    deliberacoes: item.deliberacoes.map((d) => ({
      texto: d.texto.trim(),
      resultado: d.resultado,
      votacao:
        regras.permiteVotacao && d.comVotacao
          ? { favor: d.favor, contra: d.contra, abstencoes: d.abstencoes }
          : undefined,
      conflitos: d.conflitos.map((c) => ({ declaranteFielId: c.declaranteFielId, descricao: c.descricao.trim() })),
    })),
  })),
});

/** Corpo de abertura do rascunho: tipo, órgãos e conteúdo. */
export const paraRascunhoPayload = (dados: AtaFormInput, regras: TipoReuniaoInfo): RascunharAtaPayload => ({
  tipo: dados.tipo as TipoReuniao,
  organizacaoConvocanteId: dados.organizacaoConvocanteId,
  organizacaoAssociadaId: regras.exigeOrganizacaoAssociada ? opcional(dados.organizacaoAssociadaId) : undefined,
  conteudo: paraConteudoPayload(dados, regras),
});
