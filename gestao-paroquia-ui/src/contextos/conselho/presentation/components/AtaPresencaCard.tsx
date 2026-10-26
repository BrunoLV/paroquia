import React, { useEffect } from 'react';
import { useFieldArray, useFormContext, useWatch } from 'react-hook-form';
import { Card, CardContent, Stack, TextField, Typography } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import GroupsIcon from '@mui/icons-material/Groups';
import ActionButton from '@/shared/presentation/components/ActionButton';
import { funcaoEhUnica, PessoaOpcao, TipoReuniaoInfo } from '@/contextos/conselho/domain/ata';
import { AtaFormInput, presencaVazia } from '@/contextos/conselho/presentation/schemas/ataSchema';
import { Candidato } from '@/contextos/conselho/presentation/components/ataFormTipos';
import AtaBuscaFiel from '@/contextos/conselho/presentation/components/AtaBuscaFiel';
import AtaPresencaLinha from '@/contextos/conselho/presentation/components/AtaPresencaLinha';
import { usePresencaRedator } from '@/contextos/conselho/presentation/hooks/useAtas';

interface AtaPresencaCardProps {
  regras: TipoReuniaoInfo | undefined;
  candidatos: Candidato[];
  /** Avisa que um fiel veio da busca e passa a constar na presença, para que as listas passem a oferecê-lo. */
  onFielBuscado: (pessoa: PessoaOpcao) => void;
}

/**
 * Cartão "Presença": lista nominal (a pessoa é identificada pelo cadastro, nunca digitada) ou, nas assembleias,
 * apenas o total de participantes. Qual modo vale é regra do tipo de reunião.
 */
export const AtaPresencaCard: React.FC<AtaPresencaCardProps> = ({ regras, candidatos, onFielBuscado }) => {
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
    const linha = { fielId: redator.fielId, clerigo: false, funcao: redator.funcao, situacao: 'PRESENTE' as const };
    if (i === -1) insert(0, linha);
    else if (atuais[i].funcao !== linha.funcao || atuais[i].situacao !== linha.situacao) setValue(`presencas.${i}`, linha);
  }, [redator, presencas, getValues, insert, setValue]);

  // Nos conselhos o Pároco preside por direito próprio: já consta, como Presidente se presente (a situação é editável).
  const paroco = regras?.presidenciaNata ? candidatos.find((c) => c.paroco) : undefined;
  useEffect(() => {
    if (!paroco) return;
    const atuais = getValues('presencas');
    const i = atuais.findIndex((p) => p.fielId === paroco.id);
    if (i === -1) insert(0, { fielId: paroco.id, clerigo: true, funcao: 'Presidente', situacao: 'PRESENTE' });
    else if (atuais[i].situacao === 'PRESENTE' && atuais[i].funcao !== 'Presidente') {
      setValue(`presencas.${i}.funcao`, 'Presidente');
    }
  }, [paroco, presencas, getValues, insert, setValue]);

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
                fixa={redator !== null && presencas?.[i]?.fielId === redator.fielId}
                paroco={paroco !== undefined && presencas?.[i]?.fielId === paroco.id} />
            ))}
            <Stack direction="row" spacing={1}>
              <ActionButton title="Adicionar pessoa" icon={<AddIcon />} variant="outlined" onClick={() => append(presencaVazia())} />
              <ActionButton title="Incluir todos os membros" icon={<GroupsIcon />} variant="outlined"
                disabled={candidatos.every((c) => c.clerigo || c.organismo)}
                onClick={() => {
                  const atuais = getValues('presencas');
                  const ja = new Set(atuais.map((p) => p.fielId));
                  const funcoes = new Set(atuais.map((p) => p.funcao).filter(Boolean));
                  candidatos.filter((c) => !c.clerigo && !c.organismo && !ja.has(c.id)).forEach((c) => {
                    const sugerida = c.funcaoSugerida && !(funcaoEhUnica(c.funcaoSugerida) && funcoes.has(c.funcaoSugerida))
                      ? c.funcaoSugerida : '';
                    if (sugerida) funcoes.add(sugerida);
                    append({ ...presencaVazia(), fielId: c.id, funcao: sugerida });
                  });
                }} />
            </Stack>
            {regras?.modoPresenca === 'NOMINAL_LIVRE' && (
              <AtaBuscaFiel tipo={tipo} organizacaoId={orgaoId} onEscolher={(pessoa) => {
                onFielBuscado(pessoa);
                if (!getValues('presencas').some((p) => p.fielId === pessoa.id)) {
                  append({ ...presencaVazia(), fielId: pessoa.id });
                }
              }} />
            )}
          </Stack>
        )}
      </CardContent>
    </Card>
  );
};

export default AtaPresencaCard;
