import SearchableSelect from '@/shared/presentation/components/SearchableSelect';
import React from 'react';
import { Box, FormControl, MenuItem, Paper } from '@mui/material';
import SearchField from '@/shared/presentation/components/SearchField';

interface OrganizacoesFilterBarProps {
  searchTerm: string;
  onSearchChange: (value: string) => void;
  tipoFilter: string;
  onTipoFilterChange: (value: string) => void;
  statusFilter: string;
  onStatusFilterChange: (value: string) => void;
}

export const OrganizacoesFilterBar: React.FC<OrganizacoesFilterBarProps> = ({
  searchTerm,
  onSearchChange,
  tipoFilter,
  onTipoFilterChange,
  statusFilter,
  onStatusFilterChange,
}) => (
  <Paper sx={{ p: 2, mb: 3 }}>
    <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, flexWrap: 'wrap' }}>
      <SearchField
        value={searchTerm}
        onChange={onSearchChange}
        placeholder="Buscar por nome, coordenador ou tipo..."
        sx={{ flexGrow: 1, minWidth: 280 }}
      />
      <FormControl size="small" sx={{ minWidth: 180 }}>

        <SearchableSelect
          labelId="tipo-org-filtro-label"
          label="Tipo"
          value={tipoFilter}
          onChange={(e) => onTipoFilterChange(e.target.value)}
        >
          <MenuItem value="TODOS">Todos os Tipos</MenuItem>
          <MenuItem value="PASTORAL">Pastoral</MenuItem>
          <MenuItem value="MOVIMENTO">Movimento</MenuItem>
          <MenuItem value="SERVICO">Serviço</MenuItem>
          <MenuItem value="COMISSAO">Comissão</MenuItem>
          <MenuItem value="CONSELHO">Conselho</MenuItem>
          <MenuItem value="OUTRO">Outro</MenuItem>
        </SearchableSelect>
      </FormControl>
      <FormControl size="small" sx={{ minWidth: 140 }}>

        <SearchableSelect
          labelId="status-org-filtro-label"
          label="Status"
          value={statusFilter}
          onChange={(e) => onStatusFilterChange(e.target.value)}
        >
          <MenuItem value="TODOS">Todos</MenuItem>
          <MenuItem value="ATIVO">Ativos</MenuItem>
          <MenuItem value="INATIVO">Inativos</MenuItem>
        </SearchableSelect>
      </FormControl>
    </Box>
  </Paper>
);
