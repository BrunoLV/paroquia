import React from 'react';
import DevicesIcon from '@mui/icons-material/Devices';
import ChairIcon from '@mui/icons-material/Chair';
import CheckroomIcon from '@mui/icons-material/Checkroom';
import EngineeringIcon from '@mui/icons-material/Engineering';
import CategoryIcon from '@mui/icons-material/Category';
import type { ChipProps, SvgIconProps } from '@mui/material';
import type { AtivoCategoria } from '@/shared/domain/types/types';

export type { AtivoCategoria };

interface CategoriaAtivoMeta {
  label: string;
  color: ChipProps['color'];
  icon: React.ReactElement<SvgIconProps>;
  tooltip: string;
}

const CATEGORIA_ATIVO_META: Record<AtivoCategoria, CategoriaAtivoMeta> = {
  ELETRONICO: { label: 'Eletrônico', color: 'info', icon: React.createElement(DevicesIcon), tooltip: 'Equipamento Eletrônico' },
  MOBILIARIO: { label: 'Mobiliário', color: 'warning', icon: React.createElement(ChairIcon), tooltip: 'Mobiliário / Móvel' },
  PARAMENTO: { label: 'Paramento', color: 'secondary', icon: React.createElement(CheckroomIcon), tooltip: 'Paramento Litúrgico / Vestuário' },
  INFRAESTRUTURA: { label: 'Infraestrutura', color: 'primary', icon: React.createElement(EngineeringIcon), tooltip: 'Infraestrutura / Instalações' },
};

const DEFAULT_META: CategoriaAtivoMeta = { label: 'Outro', color: 'default', icon: React.createElement(CategoryIcon), tooltip: 'Categoria não classificada' };

export const getCategoriaAtivoMeta = (categoria: string): CategoriaAtivoMeta =>
  CATEGORIA_ATIVO_META[categoria as AtivoCategoria] ?? DEFAULT_META;
