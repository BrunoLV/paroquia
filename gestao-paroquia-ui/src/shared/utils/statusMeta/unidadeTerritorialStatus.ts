import React from 'react';
import ChurchIcon from '@mui/icons-material/Church';
import PlaceIcon from '@mui/icons-material/Place';
import FlareIcon from '@mui/icons-material/Flare';
import WaterDropIcon from '@mui/icons-material/WaterDrop';
import VerifiedIcon from '@mui/icons-material/Verified';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import ErrorIcon from '@mui/icons-material/Error';
import HelpOutlineIcon from '@mui/icons-material/HelpOutlineOutlined';
import ShieldIcon from '@mui/icons-material/Shield';
import type { ChipProps, SvgIconProps } from '@mui/material';
import { formatDateBr } from '@/shared/utils/masks';

export interface UnidadeStatusMeta {
  label: string;
  color: ChipProps['color'];
  icon: React.ReactElement<SvgIconProps>;
  tooltip: string;
}

const TIPO_META: Record<string, UnidadeStatusMeta> = {
  MATRIZ: {
    label: 'Igreja Matriz',
    color: 'primary',
    icon: React.createElement(ChurchIcon),
    tooltip: 'Igreja Matriz Paroquial',
  },
  COMUNIDADE: {
    label: 'Comunidade',
    color: 'default',
    icon: React.createElement(PlaceIcon),
    tooltip: 'Comunidade ou Capela vinculada',
  },
};

const DEFAULT_TIPO_META: UnidadeStatusMeta = {
  label: 'Comunidade',
  color: 'default',
  icon: React.createElement(PlaceIcon),
  tooltip: 'Comunidade Paroquial',
};

/**
 * Retorna metadados visuais de status para o tipo de unidade territorial (Matriz ou Comunidade).
 */
export const getUnidadeTerritorialTipoMeta = (tipo?: string | null): UnidadeStatusMeta => {
  if (!tipo) return DEFAULT_TIPO_META;
  return TIPO_META[tipo.toUpperCase()] ?? DEFAULT_TIPO_META;
};

/**
 * Retorna metadados visuais para indicação de Sacrário.
 */
export const getSacrarioMeta = (_possui: boolean = true): UnidadeStatusMeta => ({
  label: 'Sacrário',
  color: 'primary',
  icon: React.createElement(FlareIcon),
  tooltip: 'Possui Sacrário consagrado',
});

/**
 * Retorna metadados visuais para indicação de Pia Batismal.
 */
export const getPiaBatismalMeta = (_possui: boolean = true): UnidadeStatusMeta => ({
  label: 'Pia Batismal',
  color: 'info',
  icon: React.createElement(WaterDropIcon),
  tooltip: 'Possui Pia Batismal para celebração do sacramento',
});

/**
 * Calcula e formata a situação do Auto de Vistoria do Corpo de Bombeiros (AVCB).
 */
export const getAvcbStatusMeta = (
  avcbNumero?: string | null,
  avcbValidade?: string | null
): UnidadeStatusMeta => {
  if (!avcbNumero && !avcbValidade) {
    return {
      label: 'Não informado',
      color: 'default',
      icon: React.createElement(HelpOutlineIcon),
      tooltip: 'AVCB não cadastrado',
    };
  }

  if (!avcbValidade) {
    return {
      label: `Nº ${avcbNumero}`,
      color: 'default',
      icon: React.createElement(ShieldIcon),
      tooltip: 'Nº de AVCB registrado (sem data de validade)',
    };
  }

  const hoje = new Date();
  const validade = new Date(avcbValidade);
  const diasRestantes = Math.ceil((validade.getTime() - hoje.getTime()) / (1000 * 3600 * 24));
  const validadeFormatada = formatDateBr(avcbValidade);

  if (diasRestantes < 0) {
    return {
      label: 'AVCB Vencido',
      color: 'error',
      icon: React.createElement(ErrorIcon),
      tooltip: `Vencido em ${validadeFormatada}`,
    };
  }

  if (diasRestantes <= 60) {
    return {
      label: 'AVCB a Vencer',
      color: 'warning',
      icon: React.createElement(WarningAmberIcon),
      tooltip: `Vence em ${validadeFormatada} (${diasRestantes} dias restantes)`,
    };
  }

  return {
    label: 'AVCB Regular',
    color: 'success',
    icon: React.createElement(VerifiedIcon),
    tooltip: `Regularizado até ${validadeFormatada}`,
  };
};
