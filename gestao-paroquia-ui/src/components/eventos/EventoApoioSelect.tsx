import React from 'react';
import { Box, Chip, FormControl, InputLabel, MenuItem, Select } from '@mui/material';
import { Controller, type Control } from 'react-hook-form';
import { Organizacao } from '../../api/types';
import { EventoFormInput } from '../../schemas/eventoSchema';

interface EventoApoioSelectProps {
  control: Control<EventoFormInput>;
  disabled: boolean;
  organizations: Organizacao[];
  orgId: string;
}

/** Multi-select de organizações de apoio (participantes) do evento. */
const EventoApoioSelect: React.FC<EventoApoioSelectProps> = ({ control, disabled, organizations, orgId }) => (
  <Controller
    name="participantes"
    control={control}
    render={({ field }) => (
      <FormControl fullWidth size="small">
        <InputLabel id="new-apoio-label">Organizações de Apoio</InputLabel>
        <Select
          {...field}
          labelId="new-apoio-label"
          multiple
          label="Organizações de Apoio"
          disabled={disabled}
          onChange={(e) => field.onChange(typeof e.target.value === 'string' ? e.target.value.split(',') : e.target.value)}
          renderValue={(selected) => (
            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
              {(selected as string[]).map((id) => (
                <Chip key={id} label={organizations.find((o) => o.id === id)?.nome || id} size="small" />
              ))}
            </Box>
          )}
        >
          {organizations
            .filter((org) => org.id !== orgId && (org.ativo || field.value.includes(org.id)))
            .map((org) => (
              <MenuItem key={org.id} value={org.id}>
                {org.nome}
              </MenuItem>
            ))}
        </Select>
      </FormControl>
    )}
  />
);

export default EventoApoioSelect;
