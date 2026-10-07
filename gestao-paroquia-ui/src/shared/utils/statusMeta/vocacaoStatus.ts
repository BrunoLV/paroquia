import React from 'react';
import PersonIcon from '@mui/icons-material/Person';
import WorkspacePremiumIcon from '@mui/icons-material/WorkspacePremium';
import CoPresentIcon from '@mui/icons-material/CoPresent';
import ShieldIcon from '@mui/icons-material/Shield';
import type { ChipProps, SvgIconProps } from '@mui/material';
import type { Vocacao } from '@/shared/domain/types/types';

export type { Vocacao };

interface VocacaoMeta {
  label: string;
  color: ChipProps['color'];
  icon: React.ReactElement<SvgIconProps>;
  tooltip: string;
}

const VOCACAO_META: Record<Vocacao, VocacaoMeta> = {
  LEIGO: { label: 'Leigo(a)', color: 'default', icon: React.createElement(PersonIcon), tooltip: 'Leigo(a)' },
  SACERDOTE: { label: 'Sacerdote', color: 'primary', icon: React.createElement(WorkspacePremiumIcon), tooltip: 'Sacerdote' },
  DIACONO: { label: 'Diácono', color: 'primary', icon: React.createElement(CoPresentIcon), tooltip: 'Diácono (Ministro sagrado ordenado)' },
  RELIGIOSO: { label: 'Religioso(a)', color: 'primary', icon: React.createElement(ShieldIcon), tooltip: 'Religioso(a)' },
};

export const getVocacaoMeta = (vocacao: Vocacao): VocacaoMeta => VOCACAO_META[vocacao];
