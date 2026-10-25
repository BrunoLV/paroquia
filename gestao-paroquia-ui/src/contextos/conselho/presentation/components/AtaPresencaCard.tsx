import React, { useEffect } from 'react';
import { useFieldArray, useFormContext, useWatch } from 'react-hook-form';
import { Card, CardContent, Stack, TextField, Typography } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import GroupsIcon from '@mui/icons-material/Groups';
import ActionButton from '@/shared/presentation/components/ActionButton';
import { TipoReuniaoInfo } from '@/contextos/conselho/domain/ata';
import { AtaFormInput, presencaVazia } from '@/contextos/conselho/presentation/schemas/ataSchema';
import { Candidato } from '@/contextos/conselho/presentation/components/ataFormTipos';
import AtaPresencaLinha from '@/contextos/conselho/presentation/components/AtaPresencaLinha';
import { usePresencaRedator } from '@/contextos/conselho/presentation/hooks/useAtas';

interface AtaPresencaCardProps {
  regras: TipoReuniaoInfo | undefined;
  candidatos: Candidato[];
}

/**
 * Cartão "Presença": lista nominal (a pessoa é identificada pelo cadastro, nunca digitada) ou, nas assembleias,
 * apenas o total de participantes. Qual modo vale é regra do tipo de reunião.
 */
export const AtaPresencaCard: React.FC<AtaPresencaCardProps> = ({ regras, candidatos }) => {
  const { control, register, getValues, setValue, formState: { errors } } = useFormContext<AtaFormInput>();
  const { fields, append, insert, remove } = useFieldArray({ control, name: 'presencas' });
  const quantitativa = regras?.modoPresenca === 'QUANTITATIVA';
  const presencas = useWatch({ control, name: 'presencas' });
  const tipo = useWatch({ control, name: 'tipo' });
  const orgaoId = useWatch({ control, name: 'organizacaoConvocanteId' });
  const redator = usePresencaRedator(regras && !quantitativa ? tipo : '', orgaoId).data ?? null;

  // Quem redige a ata é sempre incluído como presente, com a função do seu papel (o servidor confere o mesmo).
  useEffect(() => {
    if (!redator) return;
    const atuais = getValues('presencas');
    const i = atuais.findIndex((p) => p.fielId === redator.fielId);
    const linha = { fielId: redator.fielId, funcao: redator.funcao, situacao: 'PRESENTE' as const };
    if (i === -1) insert(0, linha);
    else if (atuais[i].funcao !== linha.funcao || atuais[i].situacao !== linha.situacao) setValue(`presencas.${i}`, linha);
  }, [redator, presencas, getValues, insert, setValue]);

  return (
    <Card variant="outlined" sx={{ borderRadius: 2 }}>
      <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
        <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
          <GroupsIcon color="primary" fontSize="small" />
          <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>Presença</Typography>
        </Stack>
        {quantitativa ? (
          <TextField label="Total de participantes" type="number" size="small" fullWidth
            error={Boolean(errors.totalParticipantes)} helperText={errors.totalParticipantes?.message}
            slotProps={{ htmlInput: { min: 0 } }}
            {...register('totalParticipantes', { setValueAs: (v: string) => (v === '' || v == null ? null : Number(v)) })} />
        ) : (
          <Stack spacing={2}>
            {fields.map((campo, i) => (
              <AtaPresencaLinha key={campo.id} indice={i} candidatos={candidatos} onRemover={() => remove(i)}
                fixa={redator !== null && presencas?.[i]?.fielId === redator.fielId} />
            ))}
            <Stack direction="row" spacing={1}>
              <ActionButton title="Adicionar pessoa" icon={<AddIcon />} variant="outlined" onClick={() => append(presencaVazia())} />
              <ActionButton title="Incluir todos os membros" icon={<GroupsIcon />} variant="outlined"
                disabled={candidatos.length === 0}
                onClick={() => {
                  const ja = new Set(getValues('presencas').map((p) => p.fielId));
                  candidatos.filter((c) => !ja.has(c.id)).forEach((c) => append({ ...presencaVazia(), fielId: c.id }));
                }} />
            </Stack>
          </Stack>
        )}
      </CardContent>
    </Card>
  );
};

export default AtaPresencaCard;
