import { z } from 'zod';
import {
  Assento,
  CondicaoCanonica,
  LavrarAssentoPayload,
  PapelParticipante,
  ParticipantePayload,
  TeorPayload,
  TIPOS_SACRAMENTO,
} from '@/contextos/sacramento/domain/sacramento';

const PAPEIS = [
  'BATIZANDO', 'CRISMANDO', 'CONJUGE', 'ORDENADO', 'PAI', 'MAE', 'ADOTANTE', 'PADRINHO', 'MADRINHA',
  'TESTEMUNHA', 'MINISTRO_CELEBRANTE', 'MINISTRO_ASSISTENTE', 'BISPO_ORDENANTE',
] as const satisfies readonly PapelParticipante[];

/**
 * Participante do assento. O nome só é obrigatório quando não há vínculo a cadastro: com `fielId` ou
 * `clerigoId` o backend preenche o snapshot textual a partir do cadastro (late binding).
 */
export const participanteSchema = z
  .object({
    id: z.string().optional(),
    papel: z.enum(PAPEIS),
    nomeHistorico: z.string().trim().max(255),
    documento: z.string().max(255),
    localNascimento: z.string().max(255),
    catolico: z.boolean(),
    crismado: z.boolean(),
    idadeNaCelebracao: z.string().regex(/^\d{0,3}$/, 'Idade inválida'),
    condicaoCanonica: z.enum(['', 'BISPO', 'PRESBITERO', 'DIACONO']),
    fielId: z.string().optional(),
    clerigoId: z.string().optional(),
  })
  .refine((p) => p.nomeHistorico.length > 0 || Boolean(p.fielId) || Boolean(p.clerigoId), {
    path: ['nomeHistorico'],
    message: 'Informe o nome como atestado na celebração',
  });

/**
 * Teor do assento em rascunho: livro, folha, termo e demais requisitos canônicos podem estar
 * pendentes — a homologação do Pároco é quem os exige.
 */
export const assentoSchema = z.object({
  tipo: z.enum(TIPOS_SACRAMENTO as [string, ...string[]]),
  visibilidade: z.enum(['COMUM', 'RESERVADO']),
  livro: z.string().max(32),
  folha: z.string().max(32),
  termo: z.string().max(32),
  paroquiaCustodiante: z.string().max(255),
  localCelebracao: z.string().max(255),
  dataCelebracao: z.string(),
  dadoSensivel: z.boolean(),
  dadosEspecificos: z.record(z.string(), z.string()),
  participantes: z.array(participanteSchema).min(1, 'Informe ao menos um participante'),
});

export type AssentoFormInput = z.infer<typeof assentoSchema>;
export type ParticipanteFormInput = z.infer<typeof participanteSchema>;

export const participanteVazio = (papel: PapelParticipante): ParticipanteFormInput => ({
  papel,
  nomeHistorico: '',
  documento: '',
  localNascimento: '',
  catolico: false,
  crismado: false,
  idadeNaCelebracao: '',
  condicaoCanonica: '',
});

const opcional = (v: string): string | undefined => (v.trim() === '' ? undefined : v.trim());

const paraParticipantePayload = (p: ParticipanteFormInput): ParticipantePayload => ({
  id: p.id,
  papel: p.papel,
  nomeHistorico: opcional(p.nomeHistorico),
  documento: opcional(p.documento),
  localNascimento: opcional(p.localNascimento),
  catolico: p.catolico,
  crismado: p.crismado,
  idadeNaCelebracao: p.idadeNaCelebracao === '' ? undefined : Number(p.idadeNaCelebracao),
  condicaoCanonica: p.condicaoCanonica === '' ? undefined : (p.condicaoCanonica as CondicaoCanonica),
  fielId: p.fielId,
  clerigoId: p.clerigoId,
});

/** Converte o formulário no teor enviado à API; `datetime-local` ganha os segundos esperados pelo backend. */
export const paraTeorPayload = (f: AssentoFormInput): TeorPayload => ({
  livro: opcional(f.livro),
  folha: opcional(f.folha),
  termo: opcional(f.termo),
  paroquiaCustodiante: opcional(f.paroquiaCustodiante),
  localCelebracao: opcional(f.localCelebracao),
  dataCelebracao: f.dataCelebracao ? `${f.dataCelebracao.slice(0, 16)}:00` : undefined,
  dadoSensivel: f.dadoSensivel,
  dadosEspecificos: Object.fromEntries(Object.entries(f.dadosEspecificos).filter(([, v]) => v.trim() !== '')),
  participantes: f.participantes.map(paraParticipantePayload),
});

export const paraLavrarPayload = (f: AssentoFormInput): LavrarAssentoPayload => ({
  tipo: f.tipo as LavrarAssentoPayload['tipo'],
  visibilidade: f.visibilidade,
  teor: paraTeorPayload(f),
});

/** Reidrata o formulário de edição a partir do assento; os ids preservam os vínculos tardios existentes. */
export const assentoParaForm = (a: Assento): AssentoFormInput => ({
  tipo: a.tipo,
  visibilidade: a.visibilidade,
  livro: a.livro ?? '',
  folha: a.folha ?? '',
  termo: a.termo ?? '',
  paroquiaCustodiante: a.paroquiaCustodiante ?? '',
  localCelebracao: a.localCelebracao ?? '',
  dataCelebracao: a.dataCelebracao ? a.dataCelebracao.slice(0, 16) : '',
  dadoSensivel: a.dadoSensivel,
  dadosEspecificos: a.dadosEspecificos,
  participantes: a.participantes.map((p) => ({
    id: p.id,
    papel: p.papel,
    nomeHistorico: p.nomeHistorico,
    documento: p.documento ?? '',
    localNascimento: p.localNascimento ?? '',
    catolico: p.catolico ?? false,
    crismado: p.crismado ?? false,
    idadeNaCelebracao: p.idadeNaCelebracao === null ? '' : String(p.idadeNaCelebracao),
    condicaoCanonica: p.condicaoCanonica ?? '',
    fielId: p.fielId ?? undefined,
    clerigoId: p.clerigoId ?? undefined,
  })),
});
