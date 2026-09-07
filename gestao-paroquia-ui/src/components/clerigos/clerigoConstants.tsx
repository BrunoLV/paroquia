import React from 'react';
import { Box, Paper, Stack, Typography } from '@mui/material';
import type { Clerigo, ClerigoPayload, FuncaoEclesiastica, GrauOrdem } from '@/api/types';
import { formatCpf, formatPhone, formatRg } from '@/utils/masks';

export const GRAUS_ORDEM: { value: GrauOrdem; label: string }[] = [
  { value: 'DIACONO_PERMANENTE', label: 'Diácono Permanente' },
  { value: 'DIACONO_TRANSITORIO', label: 'Diácono Transitório' },
  { value: 'PADRE_DIOCESANO', label: 'Padre Diocesano' },
  { value: 'PADRE_RELIGIOSO', label: 'Padre Religioso' },
  { value: 'BISPO', label: 'Bispo' },
];

export const FUNCOES_ECLESIASTICAS: { value: FuncaoEclesiastica; label: string }[] = [
  { value: 'BISPO_DIOCESANO', label: 'Bispo Diocesano' },
  { value: 'PAROCO', label: 'Pároco' },
  { value: 'ADMINISTRADOR_PAROQUIAL', label: 'Administrador Paroquial' },
  { value: 'VIGARIO_PAROQUIAL', label: 'Vigário Paroquial' },
  { value: 'DIACONO_AUXILIAR', label: 'Diácono Auxiliar' },
  { value: 'EMERITO', label: 'Emérito' },
];

export const PAISES_ORIGEM = [
  { codigo: 'BRA', nome: 'Brasil' },
  { codigo: 'ARG', nome: 'Argentina' },
  { codigo: 'BOL', nome: 'Bolívia' },
  { codigo: 'COL', nome: 'Colômbia' },
  { codigo: 'PRT', nome: 'Portugal' },
  { codigo: 'VEN', nome: 'Venezuela' },
  { codigo: 'ITA', nome: 'Itália' },
] as const;

export const obterNomePais = (codigo?: string | null): string => {
  if (!codigo) return 'Brasil';
  const pais = PAISES_ORIGEM.find((p) => p.codigo === codigo);
  return pais ? pais.nome : codigo;
};

export const estadoInicialForm = (): ClerigoPayload => ({
  nomeCivil: '',
  nomeEclesiastico: '',
  dataNascimento: '',
  paisOrigem: 'BRA',
  cpf: '',
  rg: '',
  documentoEstrangeiro: '',
  email: '',
  celular: '',
  isWhatsapp: false,
  telefoneFixo: '',
  endereco: '',
  grauOrdem: 'PADRE_DIOCESANO',
  dioceseIncardinacao: '',
  funcaoAtual: 'PAROCO',
  dataOrdenacaoDiaconal: '',
  dataOrdenacaoPresbiteral: '',
  dataOrdenacaoEpiscopal: '',
});

export const clerigoParaPayload = (c: Clerigo): ClerigoPayload => ({
  nomeCivil: c.nomeCivil || '',
  nomeEclesiastico: c.nomeEclesiastico || '',
  dataNascimento: c.dataNascimento || '',
  paisOrigem: c.paisOrigem || 'BRA',
  cpf: formatCpf(c.cpfFormatado || ''),
  rg: formatRg(c.rg || ''),
  documentoEstrangeiro: c.documentoEstrangeiro || '',
  email: c.email || '',
  celular: formatPhone(c.celularFormatado || ''),
  isWhatsapp: Boolean(c.isWhatsapp),
  telefoneFixo: formatPhone(c.telefoneFixoFormatado || ''),
  endereco: c.endereco || '',
  grauOrdem: c.grauOrdem || 'PADRE_DIOCESANO',
  dioceseIncardinacao: c.dioceseIncardinacao || '',
  funcaoAtual: c.funcaoAtual || 'PAROCO',
  dataOrdenacaoDiaconal: c.dataOrdenacaoDiaconal || '',
  dataOrdenacaoPresbiteral: c.dataOrdenacaoPresbiteral || '',
  dataOrdenacaoEpiscopal: c.dataOrdenacaoEpiscopal || '',
});

export const InfoField = ({
  label,
  value,
  icon,
}: {
  label: string;
  value?: React.ReactNode;
  icon?: React.ReactNode;
}) => (
  <Paper
    variant="outlined"
    sx={{
      p: 1.5,
      borderRadius: 1.5,
      bgcolor: 'background.paper',
      height: '100%',
      display: 'flex',
      flexDirection: 'column',
      justifyContent: 'center',
    }}
  >
    <Stack direction="row" spacing={0.75} sx={{ alignItems: 'center', mb: 0.5 }}>
      {icon && <Box sx={{ color: 'primary.main', display: 'flex', fontSize: 16 }}>{icon}</Box>}
      <Typography variant="caption" color="text.secondary" sx={{ fontWeight: 600 }}>
        {label}
      </Typography>
    </Stack>
    <Typography variant="body2" sx={{ fontWeight: 500 }}>
      {value || '-'}
    </Typography>
  </Paper>
);
