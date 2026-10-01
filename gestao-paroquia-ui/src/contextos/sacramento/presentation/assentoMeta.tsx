import React from 'react';
import DraftsIcon from '@mui/icons-material/Drafts';
import VerifiedIcon from '@mui/icons-material/Verified';
import BlockIcon from '@mui/icons-material/Block';
import LockIcon from '@mui/icons-material/Lock';
import PublicIcon from '@mui/icons-material/Public';
import HourglassTopIcon from '@mui/icons-material/HourglassTop';
import WaterDropIcon from '@mui/icons-material/WaterDrop';
import LocalFireDepartmentIcon from '@mui/icons-material/LocalFireDepartment';
import FavoriteIcon from '@mui/icons-material/Favorite';
import ChurchIcon from '@mui/icons-material/Church';
import type { ChipProps, SvgIconProps } from '@mui/material';
import {
  StatusAssento,
  StatusAverbacao,
  TIPO_SACRAMENTO_LABEL,
  TipoSacramento,
  VisibilidadeAssento,
} from '@/contextos/sacramento/domain/sacramento';

interface Meta {
  label: string;
  color: ChipProps['color'];
  icon: React.ReactElement<SvgIconProps>;
  tooltip: string;
}

const STATUS: Record<StatusAssento, Meta> = {
  DRAFT: { label: 'Rascunho', color: 'warning', icon: React.createElement(DraftsIcon), tooltip: 'Em escrituração; teor editável' },
  SEALED: { label: 'Homologado', color: 'success', icon: React.createElement(VerifiedIcon), tooltip: 'Selado pelo Pároco; teor somente leitura' },
  CANCELADO: { label: 'Cancelado', color: 'error', icon: React.createElement(BlockIcon), tooltip: 'Termo cancelado com motivo registrado' },
};

const VISIBILIDADE: Record<VisibilidadeAssento, Meta> = {
  COMUM: { label: 'Livro comum', color: 'default', icon: React.createElement(PublicIcon), tooltip: 'Visível à secretaria paroquial' },
  RESERVADO: { label: 'Livro reservado', color: 'secondary', icon: React.createElement(LockIcon), tooltip: 'Restrito ao Pároco e ao Bispo Diocesano' },
};

const AVERBACAO: Record<StatusAverbacao, Meta> = {
  PENDENTE_HOMOLOGACAO: { label: 'Pendente', color: 'warning', icon: React.createElement(HourglassTopIcon), tooltip: 'Aguardando homologação do Pároco' },
  HOMOLOGADA: { label: 'Homologada', color: 'success', icon: React.createElement(VerifiedIcon), tooltip: 'Homologada pelo Pároco' },
};

const TIPO_ICON: Record<TipoSacramento, React.ReactElement<SvgIconProps>> = {
  BATISMO: React.createElement(WaterDropIcon),
  CONFIRMACAO: React.createElement(LocalFireDepartmentIcon),
  MATRIMONIO: React.createElement(FavoriteIcon),
  ORDEM: React.createElement(ChurchIcon),
};

export const getStatusAssentoMeta = (s: StatusAssento): Meta => STATUS[s];
export const getVisibilidadeMeta = (v: VisibilidadeAssento): Meta => VISIBILIDADE[v];
export const getStatusAverbacaoMeta = (s: StatusAverbacao): Meta => AVERBACAO[s];
export const getTipoSacramentoMeta = (t: TipoSacramento): Meta => ({
  label: TIPO_SACRAMENTO_LABEL[t],
  color: 'primary',
  icon: TIPO_ICON[t],
  tooltip: `Assento de ${TIPO_SACRAMENTO_LABEL[t]}`,
});
