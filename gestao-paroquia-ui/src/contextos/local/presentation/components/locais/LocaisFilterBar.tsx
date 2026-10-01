import SearchableSelect from '@/shared/presentation/components/SearchableSelect';
import React from 'react';
import { FormControl, MenuItem, Paper, Stack } from '@mui/material';
import SearchField from '@/shared/presentation/components/SearchField';
import { Organizacao } from '@/shared/domain/types/types';

interface LocaisFilterBarProps {
  searchTerm: string;
  onSearchChange: (value: string) => void;
  tipoFilter: string;
  onTipoFilterChange: (value: string) => void;
  statusFilter: string;
  onStatusFilterChange: (value: string) => void;
  igrejaFilter: string;
  onIgrejaFilterChange: (value: string) => void;
  igrejas: Organizacao[];
}

export const LocaisFilterBar: React.FC<LocaisFilterBarProps> = ({
  searchTerm,
  onSearchChange,
  tipoFilter,
  onTipoFilterChange,
  statusFilter,
  onStatusFilterChange,
  igrejaFilter,
  onIgrejaFilterChange,
  igrejas,
}) => (
  <Paper sx={{ p: 2, mb: 3 }}>
    <Stack direction="row" spacing={2} sx={{ alignItems: 'center', flexWrap: 'wrap', gap: 1.5 }}>
      <SearchField
        value={searchTerm}
        onChange={onSearchChange}
        placeholder="Buscar local por nome ou endereço..."
        sx={{ flexGrow: 1, minWidth: 260 }}
      />
      <FormControl size="small" sx={{ minWidth: 180 }}>

        <SearchableSelect
          labelId="igreja-filtro-label"
          label="Igreja / Comunidade"
          value={igrejaFilter}
          onChange={(e) => onIgrejaFilterChange(e.target.value)}
        >
          <MenuItem value="TODOS">Todas as Igrejas</MenuItem>
          {igrejas.map((org) => (
            <MenuItem key={org.id} value={org.id}>
              {org.nome}
            </MenuItem>
          ))}
        </SearchableSelect>
      </FormControl>
      <FormControl size="small" sx={{ minWidth: 150 }}>

        <SearchableSelect
          labelId="tipo-filtro-label"
          label="Tipo de Local"
          value={tipoFilter}
          onChange={(e) => onTipoFilterChange(e.target.value)}
        >
          <MenuItem value="TODOS">Todos os Tipos</MenuItem>
          <MenuItem value="SALAO_PAROQUIAL">Salão Paroquial</MenuItem>
          <MenuItem value="IGREJA">Igreja / Matriz</MenuItem>
          <MenuItem value="CAPELA">Capela</MenuItem>
          <MenuItem value="SALA_REUNIAO">Sala de Reunião</MenuItem>
          <MenuItem value="AUDITORIO">Auditório</MenuItem>
          <MenuItem value="QUADRA">Quadra</MenuItem>
          <MenuItem value="SECRETARIA">Secretaria</MenuItem>
          <MenuItem value="OUTRO">Outro</MenuItem>
        </SearchableSelect>
      </FormControl>
      <FormControl size="small" sx={{ minWidth: 130 }}>

        <SearchableSelect
          labelId="status-filtro-label"
          label="Status"
          value={statusFilter}
          onChange={(e) => onStatusFilterChange(e.target.value)}
        >
          <MenuItem value="TODOS">Todos</MenuItem>
          <MenuItem value="ATIVO">Ativos</MenuItem>
          <MenuItem value="INATIVO">Inativos</MenuItem>
        </SearchableSelect>
      </FormControl>
    </Stack>
  </Paper>
);
