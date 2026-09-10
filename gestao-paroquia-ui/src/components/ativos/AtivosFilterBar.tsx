import React from 'react';
import {
  Box,
  FormControl,
  FormControlLabel,
  InputLabel,
  MenuItem,
  Paper,
  Select,
  Stack,
  Switch,
  TextField,
} from '@mui/material';
import { Search as SearchIcon } from '@mui/icons-material';
import StatusIcon from '@/components/StatusIcon';
import { getCategoriaAtivoMeta } from '@/utils/statusMeta/categoriaAtivoStatus';
import { getAtivoStatusMeta } from '@/utils/statusMeta/ativoStatus';
import { AtivoCategoria, AtivoStatus, Organizacao } from '@/api/types';

const STATUS_OPCOES: AtivoStatus[] = ['DISPONIVEL', 'RESERVADO', 'EM_MANUTENCAO'];

interface AtivosFilterBarProps {
  organizacoes: Organizacao[];
  selectedOrgId: string;
  onOrgChange: (value: string) => void;
  categorias: AtivoCategoria[];
  categoryFilter: string;
  onCategoryFilterChange: (value: string) => void;
  statusFilter: string;
  onStatusFilterChange: (value: string) => void;
  apenasReservaveis: boolean;
  onApenasReservaveisChange: (value: boolean) => void;
  searchTerm: string;
  onSearchChange: (value: string) => void;
}

/** Barra de filtros dos Ativos Patrimoniais — mesmo padrão visual de LocaisFilterBar. */
export const AtivosFilterBar: React.FC<AtivosFilterBarProps> = ({
  organizacoes,
  selectedOrgId,
  onOrgChange,
  categorias,
  categoryFilter,
  onCategoryFilterChange,
  statusFilter,
  onStatusFilterChange,
  apenasReservaveis,
  onApenasReservaveisChange,
  searchTerm,
  onSearchChange,
}) => (
  <Paper sx={{ p: 2, mb: 3 }}>
    <Stack direction="row" spacing={2} sx={{ alignItems: 'center', flexWrap: 'wrap', gap: 1.5 }}>
      <SearchIcon color="action" />
      <TextField
        size="small"
        placeholder="Buscar por nome ou tombamento..."
        value={searchTerm}
        onChange={(e) => onSearchChange(e.target.value)}
        sx={{ flexGrow: 1, minWidth: 240 }}
      />
      <FormControl size="small" sx={{ minWidth: 200 }}>
        <InputLabel id="organizacao-select-label">Organização</InputLabel>
        <Select
          labelId="organizacao-select-label"
          label="Organização"
          value={selectedOrgId}
          onChange={(e) => onOrgChange(e.target.value as string)}
        >
          {organizacoes.map((org) => (
            <MenuItem key={org.id} value={org.id}>
              {org.nome}
            </MenuItem>
          ))}
        </Select>
      </FormControl>
      <FormControl size="small" sx={{ minWidth: 170 }}>
        <InputLabel id="categoria-select-label">Tipo</InputLabel>
        <Select
          labelId="categoria-select-label"
          label="Tipo"
          value={categoryFilter}
          onChange={(e) => onCategoryFilterChange(e.target.value as string)}
        >
          <MenuItem value="TODAS">Todos os Tipos</MenuItem>
          {categorias.map((cat) => (
            <MenuItem key={cat} value={cat}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <StatusIcon {...getCategoriaAtivoMeta(cat)} />
              </Box>
            </MenuItem>
          ))}
        </Select>
      </FormControl>
      <FormControl size="small" sx={{ minWidth: 160 }}>
        <InputLabel id="status-select-label">Status</InputLabel>
        <Select
          labelId="status-select-label"
          label="Status"
          value={statusFilter}
          onChange={(e) => onStatusFilterChange(e.target.value as string)}
        >
          <MenuItem value="TODOS">Todos os Status</MenuItem>
          {STATUS_OPCOES.map((status) => (
            <MenuItem key={status} value={status}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <StatusIcon {...getAtivoStatusMeta(status)} />
              </Box>
            </MenuItem>
          ))}
        </Select>
      </FormControl>
      <FormControlLabel
        control={
          <Switch
            checked={apenasReservaveis}
            onChange={(e) => onApenasReservaveisChange(e.target.checked)}
          />
        }
        label="Apenas Reserváveis"
      />
    </Stack>
  </Paper>
);

export default AtivosFilterBar;
