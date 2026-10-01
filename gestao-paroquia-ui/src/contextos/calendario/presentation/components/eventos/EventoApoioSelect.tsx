import SearchableSelect from '@/shared/presentation/components/SearchableSelect';
import React from 'react';
import { FormControl, MenuItem } from '@mui/material';
import { Controller, type Control } from 'react-hook-form';
import { Organizacao } from '@/shared/domain/types/types';
import { EventoFormInput } from '@/contextos/calendario/presentation/schemas/eventoSchema';

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

        <SearchableSelect
          {...field}
          labelId="new-apoio-label"
          multiple
          label="Organizações de Apoio"
          disabled={disabled}
          onChange={(e) => field.onChange(e.target.value)}

        >
          {organizations
            .filter((org) => org.id !== orgId && (org.ativo || field.value.includes(org.id)))
            .map((org) => (
              <MenuItem key={org.id} value={org.id}>
                {org.nome}
              </MenuItem>
            ))}
        </SearchableSelect>
      </FormControl>
    )}
  />
);

export default EventoApoioSelect;
