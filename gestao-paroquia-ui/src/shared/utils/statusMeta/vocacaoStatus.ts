import React from 'react';
import PersonIcon from '@mui/icons-material/Person';
import ChurchIcon from '@mui/icons-material/Church';
import VolunteerActivismIcon from '@mui/icons-material/VolunteerActivism';
import SelfImprovementIcon from '@mui/icons-material/SelfImprovement';
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
  SACERDOTE: { label: 'Sacerdote', color: 'primary', icon: React.createElement(ChurchIcon), tooltip: 'Sacerdote' },
  DIACONO: { label: 'Diácono', color: 'primary', icon: React.createElement(VolunteerActivismIcon), tooltip: 'Diácono' },
  RELIGIOSO: { label: 'Religioso(a)', color: 'primary', icon: React.createElement(SelfImprovementIcon), tooltip: 'Religioso(a)' },
};

export const getVocacaoMeta = (vocacao: Vocacao): VocacaoMeta => VOCACAO_META[vocacao];
