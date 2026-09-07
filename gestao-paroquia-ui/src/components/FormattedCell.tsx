import React from 'react';
import { Box, Typography } from '@mui/material';
import PhoneIcon from '@mui/icons-material/Phone';
import EmailIcon from '@mui/icons-material/Email';
import LocationOnIcon from '@mui/icons-material/LocationOn';
import AttachMoneyIcon from '@mui/icons-material/AttachMoney';
import BadgeIcon from '@mui/icons-material/Badge';
import { formatPhone } from '../utils/phoneUtils';
import { formatCurrencyBRL } from '../utils/currencyUtils';
import { CpfMaskVariant, formatCpf } from '../utils/cpfUtils';

interface FormattedCellProps {
  value?: string | number | null;
  variant?: 'body2' | 'caption' | 'subtitle2';
  color?: string;
  maskVariant?: CpfMaskVariant;
  sx?: object;
}

type EnderecoExibivel = Record<'logradouro' | 'numero' | 'bairro' | 'cidade' | 'estado', string | null | undefined>;

export function FormattedPhone({ value, variant = 'body2', color = 'text.primary', sx }: FormattedCellProps) {
  if (!value || !String(value).trim()) {
    return <Typography variant={variant} color="text.secondary">-</Typography>;
  }

  const formatted = formatPhone(String(value));

  return (
    <Box sx={{ display: 'inline-flex', alignItems: 'center', gap: 0.5, ...sx }}>
      <PhoneIcon sx={{ fontSize: '0.9rem', color: 'text.secondary' }} />
      <Typography variant={variant} color={color}>
        {formatted}
      </Typography>
    </Box>
  );
}

export function FormattedEmail({ value, variant = 'body2', color = 'text.primary', sx }: FormattedCellProps) {
  if (!value || !String(value).trim()) {
    return <Typography variant={variant} color="text.secondary">-</Typography>;
  }

  return (
    <Box sx={{ display: 'inline-flex', alignItems: 'center', gap: 0.5, ...sx }}>
      <EmailIcon sx={{ fontSize: '0.9rem', color: 'text.secondary' }} />
      <Typography variant={variant} color={color}>
        {String(value)}
      </Typography>
    </Box>
  );
}

export function FormattedAddress({ value, variant = 'caption', color = 'text.secondary', sx }: Omit<FormattedCellProps, 'value'> & { value?: string | object | null }) {
  if (!value) {
    return <Typography variant={variant} color="text.secondary">-</Typography>;
  }

  let text = '';
  if (typeof value === 'object' && value !== null) {
    const end = value as Partial<EnderecoExibivel>;
    const parts = [
      end.logradouro ? `${end.logradouro}${end.numero ? `, ${end.numero}` : ''}` : '',
      end.bairro,
      end.cidade ? `${end.cidade}${end.estado ? `/${end.estado}` : ''}` : ''
    ].filter(Boolean);
    text = parts.join(' - ');
  } else {
    text = String(value).trim();
  }

  if (!text) {
    return <Typography variant={variant} color="text.secondary">-</Typography>;
  }

  return (
    <Box sx={{ display: 'inline-flex', alignItems: 'center', gap: 0.5, ...sx }}>
      <LocationOnIcon sx={{ fontSize: '0.9rem', color: 'text.secondary' }} />
      <Typography variant={variant} color={color}>
        {text}
      </Typography>
    </Box>
  );
}

export function FormattedCurrency({ value, variant = 'body2', color = 'text.primary', sx }: FormattedCellProps) {
  const numValue = typeof value === 'number' ? value : Number(value || 0);

  return (
    <Box sx={{ display: 'inline-flex', alignItems: 'center', gap: 0.25, justifyContent: 'flex-end', ...sx }}>
      <AttachMoneyIcon sx={{ fontSize: '1rem', color: 'success.main' }} />
      <Typography variant={variant} color={color} sx={{ fontWeight: 600 }}>
        {formatCurrencyBRL(numValue)}
      </Typography>
    </Box>
  );
}

export function FormattedContact({ value, variant = 'body2', color = 'text.primary', sx }: FormattedCellProps) {
  if (!value || !String(value).trim()) {
    return <Typography variant={variant} color="text.secondary">-</Typography>;
  }

  const str = String(value).trim();
  if (str.includes('@')) {
    return <FormattedEmail value={str} variant={variant} color={color} sx={sx} />;
  }

  return <FormattedPhone value={str} variant={variant} color={color} sx={sx} />;
}

export function FormattedCpf({ value, variant = 'body2', color = 'text.primary', maskVariant = 'full', sx }: FormattedCellProps) {
  if (!value || !String(value).trim()) {
    return <Typography variant={variant} color="text.secondary">-</Typography>;
  }

  const formatted = formatCpf(String(value), maskVariant);

  return (
    <Box sx={{ display: 'inline-flex', alignItems: 'center', gap: 0.5, ...sx }}>
      <BadgeIcon sx={{ fontSize: '0.9rem', color: 'text.secondary' }} />
      <Typography variant={variant} color={color}>
        {formatted}
      </Typography>
    </Box>
  );
}
