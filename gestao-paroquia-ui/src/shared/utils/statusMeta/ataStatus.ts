import React from 'react';
import BlockIcon from '@mui/icons-material/Block';
import EditNoteIcon from '@mui/icons-material/EditNote';
import HourglassEmptyIcon from '@mui/icons-material/HourglassEmpty';
import TaskAltIcon from '@mui/icons-material/TaskAlt';
import VerifiedIcon from '@mui/icons-material/Verified';
import type { ChipProps, SvgIconProps } from '@mui/material';
import { STATUS_ATA_LABEL, StatusAta } from '@/contextos/conselho/domain/ata';

interface AtaStatusMeta {
  label: string;
  color: ChipProps['color'];
  icon: React.ReactElement<SvgIconProps>;
  tooltip: string;
}

const ATA_STATUS_META: Record<StatusAta, AtaStatusMeta> = {
  RASCUNHO: { label: STATUS_ATA_LABEL.RASCUNHO, color: 'default', icon: React.createElement(EditNoteIcon), tooltip: 'Em redação; ainda é minuta, sem efeito' },
  PENDENTE_HOMOLOGACAO: { label: STATUS_ATA_LABEL.PENDENTE_HOMOLOGACAO, color: 'warning', icon: React.createElement(HourglassEmptyIcon), tooltip: 'Aguardando a homologação do Pároco' },
  HOMOLOGADA: { label: STATUS_ATA_LABEL.HOMOLOGADA, color: 'success', icon: React.createElement(VerifiedIcon), tooltip: 'Homologada pelo Pároco; imutável' },
  VETADA: { label: STATUS_ATA_LABEL.VETADA, color: 'error', icon: React.createElement(BlockIcon), tooltip: 'Vetada pelo Pároco; devolvida ao redator' },
  REGISTRADA: { label: STATUS_ATA_LABEL.REGISTRADA, color: 'info', icon: React.createElement(TaskAltIcon), tooltip: 'Registrada; imutável' },
};

export const getAtaStatusMeta = (status: StatusAta): AtaStatusMeta => ATA_STATUS_META[status];
