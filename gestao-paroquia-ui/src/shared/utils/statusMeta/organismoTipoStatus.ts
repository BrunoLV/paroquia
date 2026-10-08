import React from 'react';
import ChurchIcon from '@mui/icons-material/Church';
import PlaceIcon from '@mui/icons-material/Place';
import BadgeIcon from '@mui/icons-material/Badge';
import HistoryEduIcon from '@mui/icons-material/HistoryEdu';
import AccountBalanceWalletIcon from '@mui/icons-material/AccountBalanceWallet';
import VolunteerActivismIcon from '@mui/icons-material/VolunteerActivism';
import Diversity1Icon from '@mui/icons-material/Diversity1';
import CategoryIcon from '@mui/icons-material/Category';
import RoomServiceIcon from '@mui/icons-material/RoomService';
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
  MATRIZ: { label: 'Matriz', color: 'primary', icon: React.createElement(ChurchIcon), tooltip: 'Matriz' },
  COMUNIDADE: { label: 'Comunidade', color: 'secondary', icon: React.createElement(PlaceIcon), tooltip: 'Comunidade' },
  CLERO: { label: 'Clero', color: 'primary', icon: React.createElement(BadgeIcon), tooltip: 'Clero' },
  CONSELHO_PASTORAL: { label: 'Conselho Pastoral Paroquial (CPP)', color: 'info', icon: React.createElement(HistoryEduIcon), tooltip: 'Conselho Pastoral Paroquial (CPP)' },
  CONSELHO_ECONOMICO: { label: 'Conselho Econômico Paroquial (CAE)', color: 'info', icon: React.createElement(AccountBalanceWalletIcon), tooltip: 'Conselho Econômico Paroquial (CAE)' },
  PASTORAL: { label: 'Pastoral Orgânica', color: 'success', icon: React.createElement(VolunteerActivismIcon), tooltip: 'Pastoral orgânica: ação da Igreja local vinculada ao plano pastoral' },
  MOVIMENTO: { label: 'Movimento Apostólico', color: 'warning', icon: React.createElement(Diversity1Icon), tooltip: 'Movimento apostólico ou associação de fiéis com estatutos reconhecidos' },
  SERVICO_PAROQUIAL: { label: 'Serviço Paroquial', color: 'secondary', icon: React.createElement(RoomServiceIcon), tooltip: 'Serviço paroquial: suporte litúrgico e operacional direto' },
  CONSELHO: { label: 'Conselho', color: 'info', icon: React.createElement(HistoryEduIcon), tooltip: 'Conselho paroquial ou comunitário' },
  OUTRO: { label: 'Outro', color: 'default', icon: React.createElement(CategoryIcon), tooltip: 'Outro tipo de organismo' },
};

const DEFAULT_META: OrganismoTipoMeta = ORGANISMO_TIPO_META.OUTRO;

export const getOrganismoTipoMeta = (tipo: TipoOrganismoParoquial | string): OrganismoTipoMeta =>
  ORGANISMO_TIPO_META[tipo as TipoOrganismoParoquial] ?? DEFAULT_META;
