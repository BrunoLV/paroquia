import React from 'react';
import { Box, Paper, Stack, Typography } from '@mui/material';
import { CreateFielPayload, Endereco, Fiel, UpdateFielPayload } from '@/shared/domain/types/types';
import { TipoRelacionamentoFamiliar } from '@/contextos/fiel/infrastructure/http/familiares';
import { formatCep, formatCpf, formatDocumentoEstrangeiro, formatPhone, formatRg } from '@/shared/utils/masks';

export const vazio = (): CreateFielPayload => ({
  nome: '',
  sexo: 'MASCULINO',
  dataNascimento: '',
  paisOrigem: 'BRA',
  cpf: '',
  rg: '',
  tipoDocumentoEstrangeiro: undefined,
  numeroDocumentoEstrangeiro: '',
  email: '',
  celular: '',
  isWhatsapp: true,
  endereco: {},
  unidadeTerritorialId: '',
  isDizimista: false,
  permitirDivulgacaoAniversario: false,
});

export const PAISES_ORIGEM = [
  { codigo: 'BRA', nome: 'Brasil' },
  { codigo: 'ARG', nome: 'Argentina' },
  { codigo: 'BOL', nome: 'Bolívia' },
  { codigo: 'COL', nome: 'Colômbia' },
  { codigo: 'PRT', nome: 'Portugal' },
  { codigo: 'VEN', nome: 'Venezuela' },
] as const;

export const SEXO_LABELS: Record<string, string> = {
  MASCULINO: 'Masculino',
  FEMININO: 'Feminino',
  NAO_INFORMADO: 'Não informado',
};

export const TIPO_DOC_LABELS: Record<string, string> = {
  RNE_RNM: 'RNE / RNM',
  PASSAPORTE: 'Passaporte',
};

export const TIPO_RELACAO_LABELS: Record<string, string> = {
  CONJUGE: 'Cônjuge',
  GENITOR: 'Genitor(a)',
  FILHO: 'Filho(a)',
  PAI: 'Pai',
  MAE: 'Mãe',
  IRMAO: 'Irmão(ã)',
  OUTRO: 'Outro',
};

export const obterNomePais = (codigo?: string | null): string => {
  if (!codigo) return 'Brasil';
  const pais = PAISES_ORIGEM.find((p) => p.codigo === codigo);
  return pais ? pais.nome : codigo;
};

export const formatarEndereco = (endereco?: Endereco | string | null): string => {
  if (!endereco) return 'Não informado';
  if (typeof endereco === 'string') return endereco;
  const partes: string[] = [];
  if (endereco.logradouro) {
    let log = endereco.logradouro;
    if (endereco.numero) log += `, ${endereco.numero}`;
    if (endereco.complemento) log += ` - ${endereco.complemento}`;
    partes.push(log);
  }
  if (endereco.bairro) partes.push(endereco.bairro);
  const uf = endereco.uf || endereco.estado;
  if (endereco.cidade || uf) {
    partes.push([endereco.cidade, uf].filter(Boolean).join('/'));
  }
  if (endereco.cep) partes.push(`CEP: ${formatCep(endereco.cep)}`);
  return partes.length > 0 ? partes.join(', ') : 'Não informado';
};

export const cadastroVazio = (): UpdateFielPayload => ({
  nome: '',
  sexo: 'MASCULINO',
  dataNascimento: '',
  paisOrigem: 'BRA',
  cpf: '',
  rg: '',
  tipoDocumentoEstrangeiro: undefined,
  numeroDocumentoEstrangeiro: '',
  email: '',
  celular: '',
  isWhatsapp: true,
  telefoneFixo: '',
  endereco: {},
  unidadeTerritorialId: '',
  isDizimista: false,
  permitirDivulgacaoAniversario: false,
});

export const paraCadastro = (fiel: Fiel): UpdateFielPayload => ({
  ...cadastroVazio(),
  nome: fiel.nome ?? '',
  sexo: fiel.sexo ?? 'MASCULINO',
  dataNascimento: fiel.dataNascimento ?? '',
  paisOrigem: fiel.dadosIdentificacao?.paisOrigem ?? 'BRA',
  cpf: formatCpf(fiel.dadosIdentificacao?.cpfFormatado ?? fiel.cpf),
  rg: formatRg(fiel.dadosIdentificacao?.rg ?? fiel.rg ?? ''),
  tipoDocumentoEstrangeiro:
    fiel.dadosIdentificacao?.tipoDocumentoEstrangeiro ?? undefined,
  numeroDocumentoEstrangeiro: formatDocumentoEstrangeiro(
    fiel.dadosIdentificacao?.tipoDocumentoEstrangeiro,
    fiel.dadosIdentificacao?.documentoEstrangeiroFormatado ?? '',
  ),
  email: fiel.contato?.email ?? fiel.email ?? '',
  celular: formatPhone(fiel.contato?.celularFormatado ?? fiel.telefone),
  isWhatsapp: fiel.contato?.isWhatsapp ?? true,
  telefoneFixo: formatPhone(
    fiel.contato?.telefoneFixoFormatado ?? fiel.telefoneFixo,
  ),
  endereco:
    typeof fiel.endereco === 'object' && fiel.endereco
      ? {
          cep: formatCep(fiel.endereco.cep),
          logradouro: fiel.endereco.logradouro ?? '',
          numero: fiel.endereco.numero ?? '',
          complemento: fiel.endereco.complemento ?? '',
          bairro: fiel.endereco.bairro ?? '',
          cidade: fiel.endereco.cidade ?? '',
          uf: (fiel.endereco.uf || fiel.endereco.estado || '').toUpperCase(),
          estado: (fiel.endereco.uf || fiel.endereco.estado || '').toUpperCase(),
        }
      : {},
  unidadeTerritorialId: fiel.igrejaFrequenta?.id ?? '',
  isDizimista: fiel.isDizimista ?? false,
  permitirDivulgacaoAniversario: fiel.permitirDivulgacaoAniversario ?? false,
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
