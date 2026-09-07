import React from 'react';
import ChurchIcon from '@mui/icons-material/Church';
import VolunteerActivismIcon from '@mui/icons-material/VolunteerActivism';
import SelfImprovementIcon from '@mui/icons-material/SelfImprovement';
import WorkspacePremiumIcon from '@mui/icons-material/WorkspacePremium';
import AdminPanelSettingsIcon from '@mui/icons-material/AdminPanelSettings';
import ManageAccountsIcon from '@mui/icons-material/ManageAccounts';
import SupervisorAccountIcon from '@mui/icons-material/SupervisorAccount';
import ElderlyIcon from '@mui/icons-material/Elderly';
import PersonIcon from '@mui/icons-material/Person';
import type { ChipProps, SvgIconProps } from '@mui/material';
import type { FuncaoEclesiastica, GrauOrdem } from '@/api/types';

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
    icon: React.createElement(VolunteerActivismIcon),
    tooltip: 'Diácono Transitório',
  },
  DIACONO_PERMANENTE: {
    label: 'Diácono Permanente',
    color: 'info',
    icon: React.createElement(VolunteerActivismIcon),
    tooltip: 'Diácono Permanente',
  },
  PADRE_DIOCESANO: {
    label: 'Padre Diocesano',
    color: 'primary',
    icon: React.createElement(ChurchIcon),
    tooltip: 'Padre Diocesano',
  },
  PADRE_RELIGIOSO: {
    label: 'Padre Religioso',
    color: 'secondary',
    icon: React.createElement(SelfImprovementIcon),
    tooltip: 'Padre Religioso',
  },
  BISPO: {
    label: 'Bispo',
    color: 'warning',
    icon: React.createElement(WorkspacePremiumIcon),
    tooltip: 'Bispo',
  },
};

const FUNCAO_ECLESIASTICA_META: Record<FuncaoEclesiastica, CleroMeta> = {
  BISPO_DIOCESANO: {
    label: 'Bispo Diocesano',
    color: 'warning',
    icon: React.createElement(WorkspacePremiumIcon),
    tooltip: 'Bispo Diocesano',
  },
  PAROCO: {
    label: 'Pároco',
    color: 'primary',
    icon: React.createElement(AdminPanelSettingsIcon),
    tooltip: 'Pároco',
  },
  ADMINISTRADOR_PAROQUIAL: {
    label: 'Administrador Paroquial',
    color: 'secondary',
    icon: React.createElement(ManageAccountsIcon),
    tooltip: 'Administrador Paroquial',
  },
  VIGARIO_PAROQUIAL: {
    label: 'Vigário Paroquial',
    color: 'info',
    icon: React.createElement(SupervisorAccountIcon),
    tooltip: 'Vigário Paroquial',
  },
  DIACONO_AUXILIAR: {
    label: 'Diácono Auxiliar',
    color: 'default',
    icon: React.createElement(VolunteerActivismIcon),
    tooltip: 'Diácono Auxiliar',
  },
  EMERITO: {
    label: 'Emérito',
    color: 'default',
    icon: React.createElement(ElderlyIcon),
    tooltip: 'Emérito',
  },
};

export const getGrauOrdemMeta = (grau?: GrauOrdem | string | null): CleroMeta => {
  if (grau && grau in GRAU_ORDEM_META) {
    return GRAU_ORDEM_META[grau as GrauOrdem];
  }
  return {
    label: grau?.replaceAll('_', ' ') || 'Não informado',
    color: 'default',
    icon: React.createElement(ChurchIcon),
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
