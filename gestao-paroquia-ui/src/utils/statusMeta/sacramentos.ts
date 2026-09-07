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

export type SacramentoKey = 'batizado' | 'primeiraComunhao' | 'crismado' | 'casadoIgrejaCatolica';

export interface SacramentoItem {
  key: SacramentoKey;
  label: string;
  color: 'success' | 'info' | 'secondary' | 'warning';
  icon: React.ReactElement<SvgIconProps>;
  possuiDetalhes: boolean;
}

/** Cor não incluída de propósito: já difere hoje entre telas (Fieis/Settings) — mantida por chamada. */
export const SACRAMENTO_META = {
  batizado: { icon: React.createElement(WaterDropIcon), tooltip: 'Batismo' },
  primeiraComunhao: { icon: React.createElement(HostiaIcon), tooltip: '1ª Eucaristia' },
  crismado: { icon: React.createElement(LocalFireDepartmentIcon), tooltip: 'Crisma' },
  casadoIgrejaCatolica: { icon: React.createElement(FavoriteIcon), tooltip: 'Matrimônio' },
} satisfies Record<SacramentoKey, SacramentoMeta>;

function isTruthy(val: unknown): boolean {
  return val === true || val === 'true' || val === 1 || val === '1';
}

export function getSacramentosFiel(fiel?: {
  batizado?: boolean | string | number;
  primeiraComunhao?: boolean | string | number;
  crismado?: boolean | string | number;
  casadoIgrejaCatolica?: boolean | string | number;
  vidaSacramental?: {
    batizado?: boolean | string | number;
    dataBatismo?: string | null;
    paroquiaBatismo?: string | null;
    celebranteBatismo?: string | null;
    livroBatismo?: string | null;
    folhaBatismo?: string | null;
    termoBatismo?: string | null;
    padrinhosBatismo?: string | null;
    primeiraComunhao?: boolean | string | number;
    dataPrimeiraComunhao?: string | null;
    paroquiaPrimeiraComunhao?: string | null;
    turmaCatequese?: string | null;
    crismado?: boolean | string | number;
    dataCrisma?: string | null;
    paroquiaCrisma?: string | null;
    celebranteCrisma?: string | null;
    padrinhoCrisma?: string | null;
    casadoIgrejaCatolica?: boolean | string | number;
    dataMatrimonio?: string | null;
    paroquiaMatrimonio?: string | null;
    numeroProcessoMatrimonial?: string | null;
    certidaoMatrimonio?: string | null;
  } | null;
} | null): SacramentoItem[] {
  if (!fiel) return [];
  const items: SacramentoItem[] = [];
  const vs = fiel.vidaSacramental;

  const hasBatismoDetalhes = Boolean(
    vs?.dataBatismo ||
    vs?.paroquiaBatismo ||
    vs?.livroBatismo ||
    vs?.folhaBatismo ||
    vs?.termoBatismo ||
    vs?.celebranteBatismo ||
    vs?.padrinhosBatismo
  );
  const isBatizado = isTruthy(fiel.batizado) || isTruthy(vs?.batizado) || hasBatismoDetalhes;

  const hasEucaristiaDetalhes = Boolean(
    vs?.dataPrimeiraComunhao ||
    vs?.paroquiaPrimeiraComunhao ||
    vs?.turmaCatequese
  );
  const isEucaristia = isTruthy(fiel.primeiraComunhao) || isTruthy(vs?.primeiraComunhao) || hasEucaristiaDetalhes;

  const hasCrismaDetalhes = Boolean(
    vs?.dataCrisma ||
    vs?.paroquiaCrisma ||
    vs?.celebranteCrisma ||
    vs?.padrinhoCrisma
  );
  const isCrismado = isTruthy(fiel.crismado) || isTruthy(vs?.crismado) || hasCrismaDetalhes;

  const hasMatrimonioDetalhes = Boolean(
    vs?.dataMatrimonio ||
    vs?.paroquiaMatrimonio ||
    vs?.numeroProcessoMatrimonial ||
    vs?.certidaoMatrimonio
  );
  const isCasado = isTruthy(fiel.casadoIgrejaCatolica) || isTruthy(vs?.casadoIgrejaCatolica) || hasMatrimonioDetalhes;

  if (isBatizado) {
    items.push({
      key: 'batizado',
      label: SACRAMENTO_META.batizado.tooltip,
      color: 'success',
      icon: SACRAMENTO_META.batizado.icon,
      possuiDetalhes: hasBatismoDetalhes,
    });
  }
  if (isEucaristia) {
    items.push({
      key: 'primeiraComunhao',
      label: SACRAMENTO_META.primeiraComunhao.tooltip,
      color: 'info',
      icon: SACRAMENTO_META.primeiraComunhao.icon,
      possuiDetalhes: hasEucaristiaDetalhes,
    });
  }
  if (isCrismado) {
    items.push({
      key: 'crismado',
      label: SACRAMENTO_META.crismado.tooltip,
      color: 'secondary',
      icon: SACRAMENTO_META.crismado.icon,
      possuiDetalhes: hasCrismaDetalhes,
    });
  }
  if (isCasado) {
    items.push({
      key: 'casadoIgrejaCatolica',
      label: SACRAMENTO_META.casadoIgrejaCatolica.tooltip,
      color: 'warning',
      icon: SACRAMENTO_META.casadoIgrejaCatolica.icon,
      possuiDetalhes: hasMatrimonioDetalhes,
    });
  }

  return items;
}

export function hasSacramentoPendencia(fiel?: {
  batizado?: boolean;
  primeiraComunhao?: boolean;
  crismado?: boolean;
  casadoIgrejaCatolica?: boolean;
  vidaSacramental?: any;
} | null): boolean {
  const sacramentos = getSacramentosFiel(fiel);
  return sacramentos.some((s) => !s.possuiDetalhes);
}
