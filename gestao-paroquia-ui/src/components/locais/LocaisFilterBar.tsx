import React from 'react';
import { Box, FormControl, InputLabel, MenuItem, Paper, Select, TextField } from '@mui/material';
import { Search as SearchIcon } from '@mui/icons-material';

interface LocaisFilterBarProps {
  searchTerm: string;
  onSearchChange: (value: string) => void;
  tipoFilter: string;
  onTipoFilterChange: (value: string) => void;
  statusFilter: string;
  onStatusFilterChange: (value: string) => void;
}

export const LocaisFilterBar: React.FC<LocaisFilterBarProps> = ({
  searchTerm,
  onSearchChange,
  tipoFilter,
  onTipoFilterChange,
  statusFilter,
  onStatusFilterChange,
}) => (
  <Paper sx={{ p: 2, mb: 3 }}>
    <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, flexWrap: 'wrap' }}>
      <SearchIcon color="action" />
      <TextField
        size="small"
        placeholder="Buscar local por nome ou endereço..."
        value={searchTerm}
        onChange={(e) => onSearchChange(e.target.value)}
        sx={{ flexGrow: 1, minWidth: 260 }}
      />
      <FormControl size="small" sx={{ minWidth: 160 }}>
        <InputLabel id="tipo-filtro-label">Tipo de Local</InputLabel>
        <Select
          labelId="tipo-filtro-label"
          label="Tipo de Local"
          value={tipoFilter}
          onChange={(e) => onTipoFilterChange(e.target.value)}
        >
          <MenuItem value="TODOS">Todos os Tipos</MenuItem>
          <MenuItem value="IGREJA">Igreja / Templo</MenuItem>
          <MenuItem value="SALAO_PAROQUIAL">Salão Paroquial</MenuItem>
          <MenuItem value="SALA_REUNIAO">Sala de Reunião</MenuItem>
          <MenuItem value="SECRETARIA">Secretaria</MenuItem>
          <MenuItem value="CAPELA">Capela</MenuItem>
          <MenuItem value="OUTRO">Outro</MenuItem>
        </Select>
      </FormControl>
      <FormControl size="small" sx={{ minWidth: 140 }}>
        <InputLabel id="status-filtro-label">Status</InputLabel>
        <Select
          labelId="status-filtro-label"
          label="Status"
          value={statusFilter}
          onChange={(e) => onStatusFilterChange(e.target.value)}
        >
          <MenuItem value="TODOS">Todos</MenuItem>
          <MenuItem value="ATIVO">Ativos</MenuItem>
          <MenuItem value="INATIVO">Inativos</MenuItem>
        </Select>
      </FormControl>
    </Box>
  </Paper>
);
