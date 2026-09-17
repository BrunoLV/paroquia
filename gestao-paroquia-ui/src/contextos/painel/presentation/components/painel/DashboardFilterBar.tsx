import React from 'react';
import {useQuery} from '@tanstack/react-query';
import {MenuItem, Stack, TextField, ToggleButton, ToggleButtonGroup} from '@mui/material';
import {getOrganizacoes} from '@/contextos/organizacao/infrastructure/http/organizacoes';
import {Organizacao, PeriodoDashboard} from '@/shared/domain/types/types';
import {useDashboardFilter} from '@/shared/presentation/context/DashboardFilterContext';

const RECORTES: Array<{value: PeriodoDashboard; label: string}> = [
  {value: 'HOJE', label: 'Hoje'},
  {value: 'SEMANA', label: '7 dias'},
  {value: 'MES', label: 'Mês'},
];

const DashboardFilterBar: React.FC = () => {
  const {periodo, grupoParoquialId, setPeriodo, setGrupoParoquialId} = useDashboardFilter();
  const {data: organizacoes = []} = useQuery<Organizacao[]>({
    queryKey: ['organizacoes'],
    queryFn: getOrganizacoes,
  });

  return (
    <Stack direction={{xs: 'column', sm: 'row'}} spacing={2} sx={{alignItems: {sm: 'center'}}}>
      <ToggleButtonGroup
        size="small"
        exclusive
        value={periodo}
        onChange={(_e, value: PeriodoDashboard | null) => value && setPeriodo(value)}
      >
        {RECORTES.map((recorte) => (
          <ToggleButton key={recorte.value} value={recorte.value}>
            {recorte.label}
          </ToggleButton>
        ))}
      </ToggleButtonGroup>
      <TextField
        select
        size="small"
        label="Comunidade"
        sx={{minWidth: 200}}
        value={grupoParoquialId ?? ''}
        onChange={(e) => setGrupoParoquialId(e.target.value || undefined)}
      >
        <MenuItem value="">Todas (meu escopo)</MenuItem>
        {organizacoes.filter((org) => org.ativo).map((org) => (
          <MenuItem key={org.id} value={org.id}>
            {org.nome}
          </MenuItem>
        ))}
      </TextField>
    </Stack>
  );
};

export default DashboardFilterBar;
