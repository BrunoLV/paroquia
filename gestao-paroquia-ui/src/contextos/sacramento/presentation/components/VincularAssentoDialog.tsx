import React, { useEffect, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import {
  Autocomplete, Card, CardContent, Dialog, DialogActions, DialogContent, DialogTitle, FormControl, InputLabel,
  MenuItem, Select, Stack, TextField, Typography,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import LinkIcon from '@mui/icons-material/Link';
import ActionButton from '@/shared/presentation/components/ActionButton';
import { DIALOG_CONTENT_SCROLL_SX } from '@/shared/presentation/theme/dialogStyles';
import { listarAssentos, obterAssento } from '@/contextos/sacramento/infrastructure/http/sacramentos';
import {
  AssentoResumo, PAPEIS_CLERICAIS, PAPEL_LABEL, TIPO_SACRAMENTO_LABEL,
} from '@/contextos/sacramento/domain/sacramento';

interface VincularAssentoDialogProps {
  open: boolean;
  tipoPessoa: 'FIEL' | 'CLERIGO';
  isPending: boolean;
  onClose: () => void;
  onConfirm: (vinculo: { assentoId: string; participanteId: string }) => void;
}

const rotulo = (a: AssentoResumo): string =>
  `${TIPO_SACRAMENTO_LABEL[a.tipo]} — ${a.sujeito ?? 'sem identificação'}`
  + ([a.livro, a.folha, a.termo].every(Boolean) ? ` (${a.livro}/${a.folha}/${a.termo})` : ' (termo pendente)');

/**
 * Parte da ficha do Fiel/Clérigo: escolhe um assento existente e a posição ainda sem cadastro vinculada
 * a esta pessoa. Só o ponteiro muda; o texto lavrado permanece. Fiéis ocupam posições leigas; clérigos,
 * posições ministeriais (celebrante, assistente, ordenado, bispo ordenante).
 */
export const VincularAssentoDialog: React.FC<VincularAssentoDialogProps> = ({
  open, tipoPessoa, isPending, onClose, onConfirm,
}) => {
  const clerical = tipoPessoa === 'CLERIGO';
  const [assento, setAssento] = useState<AssentoResumo | null>(null);
  const [participanteId, setParticipanteId] = useState('');

  const { data: assentos = [] } = useQuery({
    queryKey: ['sacramentos', 'lista', 'todos'],
    queryFn: () => listarAssentos(),
    enabled: open,
  });
  const { data: detalhe } = useQuery({
    queryKey: ['sacramentos', 'detalhe', assento?.id ?? ''],
    queryFn: () => obterAssento(assento?.id as string),
    enabled: open && assento !== null,
  });

  useEffect(() => {
    if (open) {
      setAssento(null);
      setParticipanteId('');
    }
  }, [open]);

  const posicoes = (detalhe?.participantes ?? []).filter(
    (p) => !p.fielId && !p.clerigoId && PAPEIS_CLERICAIS.includes(p.papel) === clerical,
  );

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <form
        onSubmit={(e) => {
          e.preventDefault();
          if (assento && participanteId) onConfirm({ assentoId: assento.id, participanteId });
        }}
        noValidate
      >
        <DialogTitle>Vincular a registro sacramental</DialogTitle>
        <DialogContent dividers sx={DIALOG_CONTENT_SCROLL_SX}>
          <Stack spacing={2.5}>
            <Card variant="outlined" sx={{ borderRadius: 2 }}>
              <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
                <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
                  <LinkIcon color="primary" fontSize="small" />
                  <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                    Assento e posição
                  </Typography>
                </Stack>
                <Stack spacing={2}>
                  <Autocomplete
                    options={assentos.filter((a) => a.status !== 'CANCELADO')}
                    value={assento}
                    onChange={(_, valor) => { setAssento(valor); setParticipanteId(''); }}
                    getOptionLabel={rotulo}
                    isOptionEqualToValue={(a, b) => a.id === b.id}
                    noOptionsText="Nenhum assento encontrado"
                    renderInput={(params) => <TextField {...params} size="small" label="Registro sacramental" />}
                  />
                  <FormControl fullWidth size="small" disabled={!assento}>
                    <InputLabel id="posicao-vinculo">Posição no termo</InputLabel>
                    <Select
                      labelId="posicao-vinculo"
                      label="Posição no termo"
                      value={participanteId}
                      onChange={(e) => setParticipanteId(e.target.value)}
                    >
                      {posicoes.map((p) => (
                        <MenuItem key={p.id} value={p.id}>{`${PAPEL_LABEL[p.papel]}: ${p.nomeHistorico}`}</MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                  {assento && detalhe && posicoes.length === 0 && (
                    <Typography variant="caption" color="text.secondary">
                      Este termo não tem posição sem vínculo compatível com {clerical ? 'clérigo' : 'fiel'}.
                    </Typography>
                  )}
                </Stack>
              </CardContent>
            </Card>
          </Stack>
        </DialogContent>
        <DialogActions sx={{ px: 3, py: 2 }}>
          <ActionButton title="Cancelar" icon={<CloseIcon />} color="secondary" onClick={onClose} />
          <ActionButton title="Vincular" icon={<LinkIcon />} color="primary" type="submit" disabled={isPending || !participanteId} />
        </DialogActions>
      </form>
    </Dialog>
  );
};

export default VincularAssentoDialog;
