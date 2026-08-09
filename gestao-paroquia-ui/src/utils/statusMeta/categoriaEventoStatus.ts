import React from 'react';
import CelebrationIcon from '@mui/icons-material/Celebration';
import VolunteerActivismIcon from '@mui/icons-material/VolunteerActivism';
import GroupsIcon from '@mui/icons-material/Groups';
import ChurchIcon from '@mui/icons-material/Church';
import DescriptionIcon from '@mui/icons-material/Description';
import WaterDropIcon from '@mui/icons-material/WaterDrop';
import SchoolIcon from '@mui/icons-material/School';
import FavoriteIcon from '@mui/icons-material/Favorite';
import DirectionsWalkIcon from '@mui/icons-material/DirectionsWalk';
import HelpOutlineIcon from '@mui/icons-material/HelpOutlineOutlined';
import type { ChipProps, SvgIconProps } from '@mui/material';
import type { CategoriaEvento } from '../../api/types';

export type { CategoriaEvento };

interface CategoriaEventoMeta {
  label: string;
  color: ChipProps['color'];
  icon: React.ReactElement<SvgIconProps>;
  tooltip: string;
}

const CATEGORIA_EVENTO_META: Record<CategoriaEvento, CategoriaEventoMeta> = {
  SOLENIDADE: { label: 'Solenidade', color: 'secondary', icon: React.createElement(CelebrationIcon), tooltip: 'Solenidade' },
  PASTORAL: { label: 'Pastoral', color: 'success', icon: React.createElement(VolunteerActivismIcon), tooltip: 'Pastoral' },
  SOCIAL: { label: 'Social', color: 'warning', icon: React.createElement(GroupsIcon), tooltip: 'Social' },
  LITURGICO: { label: 'Litúrgico', color: 'secondary', icon: React.createElement(ChurchIcon), tooltip: 'Litúrgico' },
  ADMINISTRATIVO: { label: 'Administrativo', color: 'default', icon: React.createElement(DescriptionIcon), tooltip: 'Administrativo' },
  SACRAMENTAL: { label: 'Sacramental', color: 'info', icon: React.createElement(WaterDropIcon), tooltip: 'Sacramental' },
  FORMATIVO: { label: 'Formativo', color: 'primary', icon: React.createElement(SchoolIcon), tooltip: 'Formativo' },
  ASSISTENCIAL: { label: 'Assistencial', color: 'info', icon: React.createElement(FavoriteIcon), tooltip: 'Assistencial' },
  MOVIMENTO: { label: 'Movimento', color: 'primary', icon: React.createElement(DirectionsWalkIcon), tooltip: 'Movimento' },
};

const DEFAULT_META: CategoriaEventoMeta = { label: 'Outro', color: 'default', icon: React.createElement(HelpOutlineIcon), tooltip: 'Categoria não classificada' };

export const getCategoriaEventoMeta = (categoria: string): CategoriaEventoMeta =>
  CATEGORIA_EVENTO_META[categoria as CategoriaEvento] ?? DEFAULT_META;
