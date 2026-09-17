import React from 'react';
import AccountBalanceIcon from '@mui/icons-material/AccountBalance';
import GroupsIcon from '@mui/icons-material/Groups';
import ChurchIcon from '@mui/icons-material/Church';
import ForumIcon from '@mui/icons-material/Forum';
import AccountBalanceWalletIcon from '@mui/icons-material/AccountBalanceWallet';
import VolunteerActivismIcon from '@mui/icons-material/VolunteerActivism';
import DirectionsWalkIcon from '@mui/icons-material/DirectionsWalk';
import HelpOutlineIcon from '@mui/icons-material/HelpOutlineOutlined';
import type { ChipProps, SvgIconProps } from '@mui/material';
import type { TipoOrganismoParoquial } from '@/shared/domain/types/types';

export type { TipoOrganismoParoquial };

interface OrganismoTipoMeta {
  label: string;
  color: ChipProps['color'];
  icon: React.ReactElement<SvgIconProps>;
  tooltip: string;
}

const ORGANISMO_TIPO_META: Record<TipoOrganismoParoquial, OrganismoTipoMeta> = {
  MATRIZ: { label: 'Matriz', color: 'primary', icon: React.createElement(AccountBalanceIcon), tooltip: 'Matriz' },
  COMUNIDADE: { label: 'Comunidade', color: 'secondary', icon: React.createElement(GroupsIcon), tooltip: 'Comunidade' },
  CLERO: { label: 'Clero', color: 'primary', icon: React.createElement(ChurchIcon), tooltip: 'Clero' },
  CONSELHO_PASTORAL: { label: 'Conselho Pastoral Paroquial (CPP)', color: 'info', icon: React.createElement(ForumIcon), tooltip: 'Conselho Pastoral Paroquial (CPP)' },
  CONSELHO_ECONOMICO: { label: 'Conselho Econômico Paroquial (CAE)', color: 'info', icon: React.createElement(AccountBalanceWalletIcon), tooltip: 'Conselho Econômico Paroquial (CAE)' },
  PASTORAL: { label: 'Pastoral', color: 'success', icon: React.createElement(VolunteerActivismIcon), tooltip: 'Pastoral' },
  MOVIMENTO: { label: 'Movimento', color: 'warning', icon: React.createElement(DirectionsWalkIcon), tooltip: 'Movimento' },
  OUTRO: { label: 'Outro', color: 'default', icon: React.createElement(HelpOutlineIcon), tooltip: 'Outro tipo de organismo' },
};

const DEFAULT_META: OrganismoTipoMeta = ORGANISMO_TIPO_META.OUTRO;

export const getOrganismoTipoMeta = (tipo: TipoOrganismoParoquial | string): OrganismoTipoMeta =>
  ORGANISMO_TIPO_META[tipo as TipoOrganismoParoquial] ?? DEFAULT_META;
