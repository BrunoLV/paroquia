import React from 'react';
import HourglassEmptyIcon from '@mui/icons-material/HourglassEmpty';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import CancelIcon from '@mui/icons-material/Cancel';
import ErrorOutlineIcon from '@mui/icons-material/ErrorOutlineOutlined';
import AddCircleOutlineIcon from '@mui/icons-material/AddCircleOutlineOutlined';
import EditNoteIcon from '@mui/icons-material/EditNote';
import BlockIcon from '@mui/icons-material/Block';
import InventoryIcon from '@mui/icons-material/Inventory';
import type { ChipProps, SvgIconProps } from '@mui/material';
import type { AprovacaoResponse, ProjetoAprovacaoResponse } from '@/shared/domain/types/types';

export type AprovacaoStatus = AprovacaoResponse['status'];
export type AprovacaoTipoSolicitacao = AprovacaoResponse['tipoSolicitacao'] | ProjetoAprovacaoResponse['tipoSolicitacao'];

interface AprovacaoStatusMeta {
  label: string;
  color: ChipProps['color'];
  icon: React.ReactElement<SvgIconProps>;
  tooltip: string;
  variant?: ChipProps['variant'];
}

const APROVACAO_STATUS_META: Record<AprovacaoStatus, AprovacaoStatusMeta> = {
  PENDENTE: { label: 'Pendente', color: 'warning', icon: React.createElement(HourglassEmptyIcon), tooltip: 'Aguardando decisão' },
  APROVADA: { label: 'Aprovada', color: 'success', icon: React.createElement(CheckCircleIcon), tooltip: 'Solicitação aprovada' },
  REPROVADA: { label: 'Reprovada', color: 'error', icon: React.createElement(CancelIcon), tooltip: 'Solicitação reprovada' },
  FALHA_EXECUCAO: { label: 'Falha na Execução', color: 'error', icon: React.createElement(ErrorOutlineIcon), tooltip: 'Aprovada, mas falhou ao executar', variant: 'outlined' },
};

const APROVACAO_TIPO_META: Record<AprovacaoTipoSolicitacao, AprovacaoStatusMeta> = {
  CRIACAO_EVENTO: { label: 'Criação de Evento', color: 'info', icon: React.createElement(AddCircleOutlineIcon), tooltip: 'Solicitação de criação de evento' },
  CRIACAO: { label: 'Criação de Projeto', color: 'info', icon: React.createElement(AddCircleOutlineIcon), tooltip: 'Solicitação de criação de projeto' },
  EDICAO_EVENTO: { label: 'Edição de Evento', color: 'warning', icon: React.createElement(EditNoteIcon), tooltip: 'Solicitação de edição de evento' },
  ALTERACAO_HORARIO: { label: 'Alteração de Horário', color: 'warning', icon: React.createElement(EditNoteIcon), tooltip: 'Solicitação de alteração de horário' },
  CANCELAMENTO: { label: 'Cancelamento de Evento', color: 'error', icon: React.createElement(BlockIcon), tooltip: 'Solicitação de cancelamento' },
  RECLASSIFICACAO: { label: 'Reclassificação', color: 'secondary', icon: React.createElement(EditNoteIcon), tooltip: 'Solicitação de reclassificação' },
  ALOCACAO_ATIVO_EMPRESTIMO: { label: 'Alocação de Ativo', color: 'secondary', icon: React.createElement(InventoryIcon), tooltip: 'Solicitação de empréstimo de ativo' },
  OUTRO: { label: 'Outro', color: 'default', icon: React.createElement(EditNoteIcon), tooltip: 'Outro tipo de solicitação' },
  UNKNOWN_LEGACY: { label: 'Legado', color: 'default', icon: React.createElement(EditNoteIcon), tooltip: 'Tipo legado não classificado' },
};

export const getAprovacaoStatusMeta = (status: AprovacaoStatus): AprovacaoStatusMeta => APROVACAO_STATUS_META[status];

export const getAprovacaoTipoMeta = (tipo: AprovacaoTipoSolicitacao): AprovacaoStatusMeta => APROVACAO_TIPO_META[tipo];

/**
 * Extrai o assunto real da solicitação (título do evento, ativo emprestado, etc.) do
 * `actionPayloadJson` — o que efetivamente identifica a solicitação para quem está
 * decidindo, ao contrário do `tipoSolicitacao` (que só diz a categoria).
 */
export const getAprovacaoDetalhe = (aprovacao: AprovacaoResponse): string => {
  let payload: Record<string, unknown> | null = null;
  try {
    payload = aprovacao.actionPayloadJson ? JSON.parse(aprovacao.actionPayloadJson) : null;
  } catch {
    payload = null;
  }
  if (aprovacao.tipoSolicitacao === 'ALOCACAO_ATIVO_EMPRESTIMO') {
    return payload?.ativoNome ? `Empréstimo — ${payload.ativoNome}` : 'Empréstimo de Ativo';
  }
  return (payload?.titulo as string) || 'Sem título / Cancelamento';
};
