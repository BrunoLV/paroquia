/**
 * Modelo de domínio do assento sacramental (Cân. 535). TypeScript puro: sem React, MUI ou HTTP.
 * Penitência não existe como tipo — jamais é registrada (Cân. 983).
 */

export type TipoSacramento = 'BATISMO' | 'CONFIRMACAO' | 'MATRIMONIO' | 'ORDEM';
export type StatusAssento = 'DRAFT' | 'SEALED' | 'CANCELADO';
export type VisibilidadeAssento = 'COMUM' | 'RESERVADO';
export type StatusAverbacao = 'PENDENTE_HOMOLOGACAO' | 'HOMOLOGADA';

export type PapelParticipante =
  | 'BATIZANDO'
  | 'CRISMANDO'
  | 'CONJUGE'
  | 'ORDENADO'
  | 'PAI'
  | 'MAE'
  | 'ADOTANTE'
  | 'PADRINHO'
  | 'MADRINHA'
  | 'TESTEMUNHA'
  | 'MINISTRO_CELEBRANTE'
  | 'MINISTRO_ASSISTENTE'
  | 'BISPO_ORDENANTE';

export type TipoEventoAverbacao =
  | 'CONFIRMACAO'
  | 'MATRIMONIO'
  | 'ORDEM'
  | 'VOTOS_PERPETUOS'
  | 'NULIDADE'
  | 'ANOTACAO_DISCIPLINAR';

export type CondicaoCanonica = 'BISPO' | 'PRESBITERO' | 'DIACONO';

export const TIPOS_SACRAMENTO: TipoSacramento[] = ['BATISMO', 'CONFIRMACAO', 'MATRIMONIO', 'ORDEM'];

export const TIPO_SACRAMENTO_LABEL: Record<TipoSacramento, string> = {
  BATISMO: 'Batismo',
  CONFIRMACAO: 'Confirmação',
  MATRIMONIO: 'Matrimônio',
  ORDEM: 'Ordem Sacra',
};

export const PAPEL_LABEL: Record<PapelParticipante, string> = {
  BATIZANDO: 'Batizando',
  CRISMANDO: 'Crismando',
  CONJUGE: 'Cônjuge',
  ORDENADO: 'Ordenado',
  PAI: 'Pai',
  MAE: 'Mãe',
  ADOTANTE: 'Adotante',
  PADRINHO: 'Padrinho',
  MADRINHA: 'Madrinha',
  TESTEMUNHA: 'Testemunha',
  MINISTRO_CELEBRANTE: 'Ministro celebrante',
  MINISTRO_ASSISTENTE: 'Ministro assistente',
  BISPO_ORDENANTE: 'Bispo ordenante',
};

/** Papéis cujo vínculo tardio aponta a um cadastro de clérigo (espelha `PapelParticipante.aceitaClerigo`). */
export const PAPEIS_CLERICAIS: PapelParticipante[] = [
  'ORDENADO',
  'MINISTRO_CELEBRANTE',
  'MINISTRO_ASSISTENTE',
  'BISPO_ORDENANTE',
];

export const PAPEIS_POR_TIPO: Record<TipoSacramento, PapelParticipante[]> = {
  BATISMO: ['BATIZANDO', 'PAI', 'MAE', 'ADOTANTE', 'PADRINHO', 'MADRINHA', 'TESTEMUNHA', 'MINISTRO_CELEBRANTE'],
  CONFIRMACAO: ['CRISMANDO', 'PAI', 'MAE', 'PADRINHO', 'MADRINHA', 'MINISTRO_CELEBRANTE'],
  MATRIMONIO: ['CONJUGE', 'TESTEMUNHA', 'MINISTRO_ASSISTENTE'],
  ORDEM: ['ORDENADO', 'BISPO_ORDENANTE'],
};

export const TIPO_EVENTO_AVERBACAO_LABEL: Record<TipoEventoAverbacao, string> = {
  CONFIRMACAO: 'Confirmação',
  MATRIMONIO: 'Matrimônio',
  ORDEM: 'Ordem',
  VOTOS_PERPETUOS: 'Votos perpétuos',
  NULIDADE: 'Declaração de nulidade',
  ANOTACAO_DISCIPLINAR: 'Anotação disciplinar',
};

export interface CampoEspecifico {
  chave: string;
  label: string;
  opcoes?: { valor: string; label: string }[];
}

/** Campos próprios de cada sacramento, validados pelo backend na homologação. */
export const CAMPOS_ESPECIFICOS: Record<TipoSacramento, CampoEspecifico[]> = {
  BATISMO: [
    { chave: 'certidaoNascimentoCartorio', label: 'Cartório da certidão de nascimento' },
    { chave: 'certidaoNascimentoTermo', label: 'Termo da certidão de nascimento' },
    {
      chave: 'estadoFiliacao',
      label: 'Estado da filiação',
      opcoes: [
        { valor: 'MATRIMONIAL', label: 'Pais casados' },
        { valor: 'NAO_CASADOS', label: 'Pais não casados' },
        { valor: 'ADOTIVA', label: 'Filiação adotiva' },
      ],
    },
  ],
  CONFIRMACAO: [
    { chave: 'batismoParoquia', label: 'Paróquia do batismo' },
    { chave: 'batismoDiocese', label: 'Diocese do batismo' },
    { chave: 'batismoLivro', label: 'Livro do batismo' },
    { chave: 'batismoFolha', label: 'Folha do batismo' },
    { chave: 'batismoTermo', label: 'Termo do batismo' },
  ],
  MATRIMONIO: [{ chave: 'numeroProcessoHabilitacao', label: 'Nº do processo de habilitação' }],
  ORDEM: [
    {
      chave: 'grau',
      label: 'Grau conferido',
      opcoes: [
        { valor: 'DIACONATO_PERMANENTE', label: 'Diaconato permanente' },
        { valor: 'DIACONATO_TRANSITORIO', label: 'Diaconato transitório' },
        { valor: 'PRESBITERADO', label: 'Presbiterado' },
      ],
    },
    { chave: 'incardinacao', label: 'Diocese ou congregação de incardinação' },
  ],
};

export interface Participante {
  id: string;
  papel: PapelParticipante;
  nomeHistorico: string;
  documento: string | null;
  localNascimento: string | null;
  catolico: boolean | null;
  crismado: boolean | null;
  idadeNaCelebracao: number | null;
  condicaoCanonica: CondicaoCanonica | null;
  fielId: string | null;
  clerigoId: string | null;
}

export interface Averbacao {
  id: string;
  tipoEvento: TipoEventoAverbacao;
  dataAverbacao: string;
  numeroDocumento: string | null;
  teorNota: string;
  reservada: boolean;
  status: StatusAverbacao;
  inseridaPor: string | null;
  homologadaPor: string | null;
}

export interface Ressalva {
  id: string;
  campo: string;
  valorOriginal: string | null;
  valorCorrigido: string;
  justificativa: string;
  atestadaPor: string | null;
  atestadaEmUtc: string;
}

export interface NotificacaoCanonica {
  id: string;
  destinatarioParoquia: string;
  emitidaEm: string;
  enviadaEm: string | null;
}

export interface Assento {
  id: string;
  tipo: TipoSacramento;
  status: StatusAssento;
  visibilidade: VisibilidadeAssento;
  dataLavratura: string;
  livro: string | null;
  folha: string | null;
  termo: string | null;
  paroquiaCustodiante: string | null;
  dataCelebracao: string | null;
  localCelebracao: string | null;
  dadoSensivel: boolean;
  dadosEspecificos: Record<string, string>;
  participantes: Participante[];
  averbacoes: Averbacao[];
  ressalvas: Ressalva[];
  notificacoes: NotificacaoCanonica[];
  homologadoPor: string | null;
  homologadoEmUtc: string | null;
  motivoCancelamento: string | null;
}

export interface AssentoResumo {
  id: string;
  tipo: TipoSacramento;
  status: StatusAssento;
  visibilidade: VisibilidadeAssento;
  livro: string | null;
  folha: string | null;
  termo: string | null;
  dataCelebracao: string | null;
  sujeito: string | null;
}

export interface HistoricoEntrada {
  id: string;
  acao: string;
  ator: string;
  perfil: string | null;
  enderecoIp: string | null;
  ocorridoEmUtc: string;
}

export interface Certidao {
  assentoId: string;
  tipo: TipoSacramento;
  texto: string;
  averbacoes: string[];
}

export interface ParticipantePayload {
  id?: string;
  papel: PapelParticipante;
  nomeHistorico?: string;
  documento?: string;
  localNascimento?: string;
  catolico?: boolean;
  crismado?: boolean;
  idadeNaCelebracao?: number;
  condicaoCanonica?: CondicaoCanonica;
  fielId?: string;
  clerigoId?: string;
}

export interface TeorPayload {
  livro?: string;
  folha?: string;
  termo?: string;
  paroquiaCustodiante?: string;
  dataCelebracao?: string;
  localCelebracao?: string;
  dadoSensivel: boolean;
  dadosEspecificos: Record<string, string>;
  participantes: ParticipantePayload[];
}

export interface LavrarAssentoPayload {
  tipo: TipoSacramento;
  visibilidade: VisibilidadeAssento;
  teor: TeorPayload;
}

export interface AverbacaoPayload {
  tipoEvento: TipoEventoAverbacao;
  dataAverbacao: string;
  numeroDocumento?: string;
  teorNota: string;
  reservada: boolean;
}

export interface RetificacaoPayload {
  campo: string;
  valorOriginal?: string;
  valorCorrigido: string;
  justificativa: string;
}
