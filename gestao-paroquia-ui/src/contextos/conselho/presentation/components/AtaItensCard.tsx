import React from 'react';
import { useFieldArray, useFormContext } from 'react-hook-form';
import { Card, CardContent, Paper, Stack, TextField, Typography } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import DeleteIcon from '@mui/icons-material/Delete';
import GavelIcon from '@mui/icons-material/Gavel';
import ListAltIcon from '@mui/icons-material/ListAlt';
import ActionButton from '@/shared/presentation/components/ActionButton';
import { AtaFormInput, deliberacaoVazia, itemVazio } from '@/contextos/conselho/presentation/schemas/ataSchema';
import { Candidato } from '@/contextos/conselho/presentation/components/ataFormTipos';
import DeliberacaoFields from '@/contextos/conselho/presentation/components/DeliberacaoFields';

interface AtaItensCardProps {
  permiteVotacao: boolean;
  candidatos: Candidato[];
}

interface ItemFieldsProps extends AtaItensCardProps {
  indice: number;
  onRemover: () => void;
}

const ItemFields: React.FC<ItemFieldsProps> = ({ indice, permiteVotacao, candidatos, onRemover }) => {
  const { control, register, formState: { errors } } = useFormContext<AtaFormInput>();
  const { fields, append, remove } = useFieldArray({ control, name: `itens.${indice}.deliberacoes` });
  const erros = errors.itens?.[indice];

  return (
    <Paper variant="outlined" sx={{ p: 2, borderRadius: 2 }}>
      <Stack spacing={2}>
        <TextField label={`Item ${indice + 1} da pauta`} fullWidth size="small" required
          error={Boolean(erros?.titulo)} helperText={erros?.titulo?.message} {...register(`itens.${indice}.titulo`)} />
        <TextField label="Relato resumido da discussão" fullWidth multiline minRows={2} size="small"
          error={Boolean(erros?.relato)} helperText={erros?.relato?.message} {...register(`itens.${indice}.relato`)} />
        {fields.map((campo, j) => (
          <DeliberacaoFields key={campo.id} item={indice} indice={j} permiteVotacao={permiteVotacao}
            candidatos={candidatos} onRemover={() => remove(j)} />
        ))}
        <Stack direction="row" spacing={1}>
          <ActionButton title="Adicionar deliberação" icon={<GavelIcon />} variant="outlined"
            onClick={() => append(deliberacaoVazia())} />
          <ActionButton title="Remover item" icon={<DeleteIcon />} color="error" variant="outlined" onClick={onRemover} />
        </Stack>
      </Stack>
    </Paper>
  );
};

/** Cartão "Pauta e deliberações": itens da ordem do dia, cada um com relato e as deliberações decorrentes. */
export const AtaItensCard: React.FC<AtaItensCardProps> = ({ permiteVotacao, candidatos }) => {
  const { control } = useFormContext<AtaFormInput>();
  const { fields, append, remove } = useFieldArray({ control, name: 'itens' });

  return (
    <Card variant="outlined" sx={{ borderRadius: 2 }}>
      <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
        <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
          <ListAltIcon color="primary" fontSize="small" />
          <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>Pauta e deliberações</Typography>
        </Stack>
        <Stack spacing={2}>
          {fields.map((campo, i) => (
            <ItemFields key={campo.id} indice={i} permiteVotacao={permiteVotacao} candidatos={candidatos}
              onRemover={() => remove(i)} />
          ))}
          <ActionButton title="Adicionar item de pauta" icon={<AddIcon />} variant="outlined"
            onClick={() => append(itemVazio())} />
        </Stack>
      </CardContent>
    </Card>
  );
};

export default AtaItensCard;
