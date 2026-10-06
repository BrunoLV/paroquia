import React from 'react';
import ChurchIcon from '@mui/icons-material/Church';
import BadgeIcon from '@mui/icons-material/Badge';
import CoPresentIcon from '@mui/icons-material/CoPresent';
import ShieldIcon from '@mui/icons-material/Shield';
import WorkspacePremiumIcon from '@mui/icons-material/WorkspacePremium';
import AssignmentIndIcon from '@mui/icons-material/AssignmentInd';
import SupervisorAccountIcon from '@mui/icons-material/SupervisorAccount';
import PersonIcon from '@mui/icons-material/Person';
import type { ChipProps, SvgIconProps } from '@mui/material';
import type { FuncaoEclesiastica, GrauOrdem } from '@/shared/domain/types/types';

export interface CleroMeta {
  label: string;
  color: ChipProps['color'];
  icon: React.ReactElement<SvgIconProps>;
  tooltip: string;
}

const GRAU_ORDEM_META: Record<GrauOrdem, CleroMeta> = {
  DIACONO_TRANSITORIO: {
    label: 'Diácono Transitório',
    color: 'info',
    icon: React.createElement(CoPresentIcon),
    tooltip: 'Diácono Transitório (1º grau da Ordem)',
  },
  DIACONO_PERMANENTE: {
    label: 'Diácono Permanente',
    color: 'info',
    icon: React.createElement(CoPresentIcon),
    tooltip: 'Diácono Permanente (1º grau da Ordem)',
  },
  PADRE_DIOCESANO: {
    label: 'Padre Diocesano',
    color: 'primary',
    icon: React.createElement(BadgeIcon),
    tooltip: 'Padre Diocesano (2º grau da Ordem / Presbítero)',
  },
  PADRE_RELIGIOSO: {
    label: 'Padre Religioso',
    color: 'secondary',
    icon: React.createElement(ShieldIcon),
    tooltip: 'Padre Religioso (2º grau da Ordem / Consagrado Regular)',
  },
  BISPO: {
    label: 'Bispo',
    color: 'warning',
    icon: React.createElement(WorkspacePremiumIcon),
    tooltip: 'Bispo (3º grau e plenitude do Sacramento da Ordem)',
  },
};

const FUNCAO_ECLESIASTICA_META: Record<FuncaoEclesiastica, CleroMeta> = {
  BISPO_DIOCESANO: {
    label: 'Bispo Diocesano',
    color: 'warning',
    icon: React.createElement(WorkspacePremiumIcon),
    tooltip: 'Bispo Diocesano (Pastor e autoridade da Diocese)',
  },
  PAROCO: {
    label: 'Pároco',
    color: 'primary',
    icon: React.createElement(ChurchIcon),
    tooltip: 'Pároco (Pastor titular e autoridade própria da Paróquia)',
  },
  ADMINISTRADOR_PAROQUIAL: {
    label: 'Administrador Paroquial',
    color: 'secondary',
    icon: React.createElement(AssignmentIndIcon),
    tooltip: 'Administrador Paroquial (Provisão e regência administrativa temporária)',
  },
  VIGARIO_PAROQUIAL: {
    label: 'Vigário Paroquial',
    color: 'info',
    icon: React.createElement(SupervisorAccountIcon),
    tooltip: 'Vigário Paroquial (Cooperador pastoral paroquial)',
  },
  DIACONO_AUXILIAR: {
    label: 'Diácono Auxiliar',
    color: 'default',
    icon: React.createElement(CoPresentIcon),
    tooltip: 'Diácono Auxiliar (Ministério da Palavra, Altar e Caridade)',
  },
  EMERITO: {
    label: 'Emérito',
    color: 'default',
    icon: React.createElement(WorkspacePremiumIcon),
    tooltip: 'Clérigo Emérito (Jubilado por mérito canônico)',
  },
};

export const getGrauOrdemMeta = (grau?: GrauOrdem | string | null): CleroMeta => {
  if (grau && grau in GRAU_ORDEM_META) {
    return GRAU_ORDEM_META[grau as GrauOrdem];
  }
  return {
    label: grau?.replaceAll('_', ' ') || 'Não informado',
    color: 'default',
    icon: React.createElement(WorkspacePremiumIcon),
    tooltip: grau || 'Não informado',
  };
};

export const getFuncaoEclesiasticaMeta = (funcao?: FuncaoEclesiastica | string | null): CleroMeta => {
  if (funcao && funcao in FUNCAO_ECLESIASTICA_META) {
    return FUNCAO_ECLESIASTICA_META[funcao as FuncaoEclesiastica];
  }
  return {
    label: funcao?.replaceAll('_', ' ') || 'Sem encargo atual',
    color: 'default',
    icon: React.createElement(PersonIcon),
    tooltip: funcao || 'Sem encargo atual',
  };
};
