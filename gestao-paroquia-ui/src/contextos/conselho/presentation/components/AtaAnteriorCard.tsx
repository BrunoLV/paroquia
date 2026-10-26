import React from 'react';
import { Controller, useFormContext, useWatch } from 'react-hook-form';
import { Card, CardContent, FormControl, Grid, MenuItem, Stack, TextField, Typography } from '@mui/material';
import HistoryEduIcon from '@mui/icons-material/HistoryEdu';
import SearchableSelect from '@/shared/presentation/components/SearchableSelect';
import { formatDateBr } from '@/shared/utils/masks';
import { APROVACAO_ATA_ANTERIOR_LABEL, AtaResumo, ataSelada } from '@/contextos/conselho/domain/ata';
import { AtaFormInput } from '@/contextos/conselho/presentation/schemas/ataSchema';

interface AtaAnteriorCardProps {
  /** Atas do mesmo órgão; só as seladas e anteriores à data da reunião podem ser a "ata anterior". */
  atasDoOrgao: AtaResumo[];
  ataAtualId: string | null;
}

/** Cartão "Ata da reunião anterior": a leitura e a aprovação da ata anterior abrem a pauta dos colegiados. */
export const AtaAnteriorCard: React.FC<AtaAnteriorCardProps> = ({ atasDoOrgao, ataAtualId }) => {
  const { control, register, formState: { errors } } = useFormContext<AtaFormInput>();
  const dataReuniao = useWatch({ control, name: 'dataReuniao' });
  const aprovacao = useWatch({ control, name: 'aprovacaoAtaAnterior' });
  const candidatas = atasDoOrgao.filter((a) => a.id !== ataAtualId && ataSelada(a.status) && a.dataReuniao < dataReuniao);

  return (
    <Card variant="outlined" sx={{ borderRadius: 2 }}>
      <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
        <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
          <HistoryEduIcon color="primary" fontSize="small" />
          <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>Ata da reunião anterior</Typography>
        </Stack>
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, sm: 7 }}>
            <Controller name="ataAnteriorId" control={control} render={({ field }) => (
              <FormControl fullWidth size="small">
                <SearchableSelect {...field} labelId="ata-anterior" label="Ata anterior (selada)">
                  <MenuItem value="">Nenhuma</MenuItem>
                  {candidatas.map((a) => (
                    <MenuItem key={a.id} value={a.id}>{`${a.numero ? `nº ${a.numero}` : 'Minuta'} — ${a.titulo} (${formatDateBr(a.dataReuniao)})`}</MenuItem>
                  ))}
                </SearchableSelect>
              </FormControl>
            )} />
          </Grid>
          <Grid size={{ xs: 12, sm: 5 }}>
            <Controller name="aprovacaoAtaAnterior" control={control} render={({ field }) => (
              <FormControl fullWidth size="small">
                <SearchableSelect {...field} labelId="ata-anterior-aprovacao" label="Aprovação"
                  error={Boolean(errors.aprovacaoAtaAnterior)} helperText={errors.aprovacaoAtaAnterior?.message}>
                  {Object.entries(APROVACAO_ATA_ANTERIOR_LABEL).map(([v, l]) => <MenuItem key={v} value={v}>{l}</MenuItem>)}
                </SearchableSelect>
              </FormControl>
            )} />
          </Grid>
          {aprovacao === 'APROVADA_COM_RESSALVAS' && (
            <Grid size={{ xs: 12 }}>
              <TextField label="Ressalvas" fullWidth multiline minRows={2} size="small"
                error={Boolean(errors.ressalvasAtaAnterior)} helperText={errors.ressalvasAtaAnterior?.message}
                {...register('ressalvasAtaAnterior')} />
            </Grid>
          )}
        </Grid>
      </CardContent>
    </Card>
  );
};

export default AtaAnteriorCard;
