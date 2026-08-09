import React from 'react';
import WaterDropIcon from '@mui/icons-material/WaterDrop';
import LocalFireDepartmentIcon from '@mui/icons-material/LocalFireDepartment';
import FavoriteIcon from '@mui/icons-material/Favorite';
import type { SvgIconProps } from '@mui/material';
import HostiaIcon from '../../components/icons/HostiaIcon';

interface SacramentoMeta {
  icon: React.ReactElement<SvgIconProps>;
  tooltip: string;
}

/** Cor não incluída de propósito: já difere hoje entre telas (Fieis/Settings) — mantida por chamada. */
export const SACRAMENTO_META = {
  batizado: { icon: React.createElement(WaterDropIcon), tooltip: 'Batizado' },
  primeiraComunhao: { icon: React.createElement(HostiaIcon), tooltip: 'Primeira Comunhão realizada' },
  crismado: { icon: React.createElement(LocalFireDepartmentIcon), tooltip: 'Crismado' },
  casadoIgrejaCatolica: { icon: React.createElement(FavoriteIcon), tooltip: 'Casado na Igreja Católica' },
} satisfies Record<string, SacramentoMeta>;
