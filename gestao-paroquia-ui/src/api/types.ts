export interface Organizacao {
  id: string;
  nome: string;
  tipo: string;
  contato: string | null;
  ativo: boolean;
}

export interface Local {
  id: string;
  nome: string;
  tipo: string;
  endereco: string | null;
  capacidade: number | null;
  caracteristicas: string | null;
  ativo: boolean;
}

export interface Projeto {
  id: string;
  nome: string;
  descricao: string | null;
  dataInicio: string | null;
  dataFim: string | null;
  organizacaoResponsavelId: string;
  ativo: boolean;
}

export interface Evento {
  id: string;
  titulo: string;
  descricao: string;
  categoria: string;
  organizacaoResponsavelId: string;
  projetoId: string | null;
  nomeProjeto: string | null;
  localId: string | null;
  inicio: string;
  fim: string;
  status: string;
  participantes?: string[];
  recorrenciaId?: string | null;
  conflictState: string | null;
  conflictReason: string | null;
  adicionadoExtraJustificativa?: string | null;
}
