import React from 'react';
import { Controller, useFormContext, useWatch } from 'react-hook-form';
import { FormControl, Grid, MenuItem } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import IconActionButton from '@/shared/presentation/components/IconActionButton';
import SearchableSelect from '@/shared/presentation/components/SearchableSelect';
import { FUNCOES_PRESENCA, funcaoEhUnica, SITUACAO_PRESENCA_LABEL } from '@/contextos/conselho/domain/ata';
import { AtaFormInput } from '@/contextos/conselho/presentation/schemas/ataSchema';
import { Candidato } from '@/contextos/conselho/presentation/components/ataFormTipos';

interface AtaPresencaLinhaProps {
  indice: number;
  candidatos: Candidato[];
  /** Linha do redator: fixa, não se edita nem se remove. */
  fixa: boolean;
  /** Linha do Pároco, que preside por direito próprio: a pessoa e a função são fixas e ela não se remove. */
  paroco: boolean;
  onRemover: () => void;
}

/** As funções fixas, mais o valor já gravado quando ele não pertence à lista (ata antiga), para não perdê-lo. */
const opcoesDeFuncao = (atual: string): string[] => {
  const rotulos = FUNCOES_PRESENCA.map((f) => f.rotulo);
  return atual && !rotulos.includes(atual) ? [atual, ...rotulos] : rotulos;
};

/**
 * Uma pessoa da lista de presença. As opções já impedem os conflitos: quem consta em outra linha não aparece em
 * "Pessoa", função única usada em outra linha fica desabilitada e quem não esteve presente não tem função.
 */
export const AtaPresencaLinha: React.FC<AtaPresencaLinhaProps> = ({ indice, candidatos, fixa, paroco, onRemover }) => {
  const { control, setValue, formState: { errors } } = useFormContext<AtaFormInput>();
  const presencas = useWatch({ control, name: 'presencas' });
  const outras = presencas.filter((_, j) => j !== indice);
  const pessoasUsadas = new Set(outras.map((p) => p.fielId));
  const funcoesUsadas = new Set(outras.map((p) => p.funcao).filter(Boolean));
  const ausente = (presencas[indice]?.situacao ?? 'PRESENTE') !== 'PRESENTE';
  const erro = errors.presencas?.[indice];

  return (
    <Grid container spacing={2} sx={{ alignItems: 'flex-start' }}>
      <Grid size={{ xs: 12, sm: 5 }}>
        <Controller name={`presencas.${indice}.fielId`} control={control} render={({ field }) => (
          <FormControl fullWidth size="small">
            <SearchableSelect {...field} labelId={`presenca-${indice}`} label="Pessoa" required disabled={fixa || paroco}
              error={Boolean(erro?.fielId)} helperText={erro?.fielId?.message}
              onChange={(e) => {
                field.onChange(e);
                const escolhida = candidatos.find((c) => c.id === e.target.value);
                setValue(`presencas.${indice}.clerigo`, escolhida?.clerigo ?? false);
                const sugerida = escolhida?.funcaoSugerida;
                if (sugerida && !presencas[indice]?.funcao && !(funcaoEhUnica(sugerida) && funcoesUsadas.has(sugerida))) {
                  setValue(`presencas.${indice}.funcao`, sugerida);
                }
              }}>
              {candidatos.filter((c) => c.id === field.value || !pessoasUsadas.has(c.id))
                .map((c) => <MenuItem key={c.id} value={c.id}>{c.organismo ? `${c.nome} (${c.organismo})` : c.nome}</MenuItem>)}
            </SearchableSelect>
          </FormControl>
        )} />
      </Grid>
      <Grid size={{ xs: 12, sm: 3 }}>
        <Controller name={`presencas.${indice}.funcao`} control={control} render={({ field }) => (
          <FormControl fullWidth size="small">
            <SearchableSelect {...field} labelId={`funcao-${indice}`} label="Função" disabled={fixa || paroco || ausente}
              error={Boolean(erro?.funcao)} helperText={erro?.funcao?.message}>
              {opcoesDeFuncao(field.value).map((f) => (
                <MenuItem key={f} value={f} disabled={f !== field.value && funcaoEhUnica(f) && funcoesUsadas.has(f)}>
                  {f}
                </MenuItem>
              ))}
            </SearchableSelect>
          </FormControl>
        )} />
      </Grid>
      <Grid size={{ xs: 9, sm: 3 }}>
        <Controller name={`presencas.${indice}.situacao`} control={control} render={({ field }) => (
          <FormControl fullWidth size="small">
            <SearchableSelect {...field} labelId={`situacao-${indice}`} label="Situação" disabled={fixa}
              onChange={(e) => {
                field.onChange(e);
                if (e.target.value !== 'PRESENTE') setValue(`presencas.${indice}.funcao`, '');
              }}>
              {Object.entries(SITUACAO_PRESENCA_LABEL).map(([v, l]) => <MenuItem key={v} value={v}>{l}</MenuItem>)}
            </SearchableSelect>
          </FormControl>
        )} />
      </Grid>
      <Grid size={{ xs: 3, sm: 1 }} sx={{ display: 'flex', justifyContent: 'center' }}>
        <IconActionButton title="Remover pessoa" icon={<DeleteIcon />} color="error" disabled={fixa || paroco}
          onClick={onRemover} />
      </Grid>
    </Grid>
  );
};

export default AtaPresencaLinha;
