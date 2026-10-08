import React from 'react';
import VerifiedUserIcon from '@mui/icons-material/VerifiedUser';
import GppMaybeIcon from '@mui/icons-material/GppMaybe';
import RemoveIcon from '@mui/icons-material/Remove';
import type { ChipProps, SvgIconProps } from '@mui/material';

interface IdoneidadeMeta {
  label: string;
  color: ChipProps['color'];
  icon: React.ReactElement<SvgIconProps>;
  tooltip: string;
}

/** Resume a idoneidade do agente para a função que ocupa; `apto` nulo indica item legado sem vínculo formal. */
export const getIdoneidadeMeta = (apto: boolean | null | undefined, pendencias: string[] = []): IdoneidadeMeta => {
  if (apto == null) {
    return { label: '—', color: 'default', icon: React.createElement(RemoveIcon), tooltip: 'Sem vínculo formal' };
  }
  return apto
    ? { label: 'Apto', color: 'success', icon: React.createElement(VerifiedUserIcon), tooltip: 'Sem pendências de idoneidade' }
    : {
        label: `${pendencias.length} pendência${pendencias.length === 1 ? '' : 's'}`,
        color: 'warning',
        icon: React.createElement(GppMaybeIcon),
        tooltip: pendencias.join(' '),
      };
};
