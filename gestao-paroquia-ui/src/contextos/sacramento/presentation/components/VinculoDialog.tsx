import React, { useEffect, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import {
  Autocomplete, Card, CardContent, Dialog, DialogActions, DialogContent, DialogTitle, Stack, TextField, Typography,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import LinkIcon from '@mui/icons-material/Link';
import ActionButton from '@/shared/presentation/components/ActionButton';
import { DIALOG_CONTENT_SCROLL_SX } from '@/shared/presentation/theme/dialogStyles';
import { getFieis } from '@/contextos/fiel/infrastructure/http/fieis';
import { listarClerigos } from '@/contextos/clero/infrastructure/http/clerigos';
import { PAPEL_LABEL, PAPEIS_CLERICAIS, Participante } from '@/contextos/sacramento/domain/sacramento';

interface Candidato {
  id: string;
  nome: string;
}

interface VinculoDialogProps {
  open: boolean;
  participante: Participante | null;
  isPending: boolean;
  onClose: () => void;
  onConfirm: (vinculo: { fielId?: string; clerigoId?: string }) => void;
}

/**
 * Vínculo tardio (late binding): associa o participante lavrado a um cadastro existente. Só o ponteiro
 * relacional muda; o nome atestado na celebração permanece intacto, mesmo em termo homologado.
 */
export const VinculoDialog: React.FC<VinculoDialogProps> = ({ open, participante, isPending, onClose, onConfirm }) => {
  const clerical = participante ? PAPEIS_CLERICAIS.includes(participante.papel) : false;
  const [escolhido, setEscolhido] = useState<Candidato | null>(null);

  const { data: candidatos = [] } = useQuery({
    queryKey: ['sacramentos', 'candidatos', clerical ? 'clerigo' : 'fiel'],
    queryFn: async (): Promise<Candidato[]> =>
      clerical
        ? (await listarClerigos()).map((c) => ({ id: c.clerigoId, nome: c.nomeCivil }))
        : (await getFieis()).flatMap((f) => (f.nome ? [{ id: f.id, nome: f.nome }] : [])),
    enabled: open && participante !== null,
  });

  useEffect(() => {
    if (open) setEscolhido(null);
  }, [open]);

  const confirmar = (event: React.FormEvent) => {
    event.preventDefault();
    if (!escolhido) return;
    onConfirm(clerical ? { clerigoId: escolhido.id } : { fielId: escolhido.id });
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <form onSubmit={confirmar} noValidate>
        <DialogTitle>Vincular cadastro</DialogTitle>
        <DialogContent dividers sx={DIALOG_CONTENT_SCROLL_SX}>
          <Stack spacing={2.5}>
            <Card variant="outlined" sx={{ borderRadius: 2 }}>
              <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
                <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
                  <LinkIcon color="primary" fontSize="small" />
                  <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                    {participante ? `${PAPEL_LABEL[participante.papel]}: ${participante.nomeHistorico}` : 'Participante'}
                  </Typography>
                </Stack>
                <Autocomplete
                  options={candidatos}
                  value={escolhido}
                  onChange={(_, valor) => setEscolhido(valor)}
                  getOptionLabel={(c) => c.nome}
                  isOptionEqualToValue={(a, b) => a.id === b.id}
                  noOptionsText="Nenhum cadastro encontrado"
                  renderInput={(params) => (
                    <TextField
                      {...params}
                      size="small"
                      label={clerical ? 'Cadastro de clérigo' : 'Cadastro de fiel'}
                      helperText="O texto lavrado no termo não é alterado pelo vínculo."
                    />
                  )}
                />
              </CardContent>
            </Card>
          </Stack>
        </DialogContent>
        <DialogActions sx={{ px: 3, py: 2 }}>
          <ActionButton title="Cancelar" icon={<CloseIcon />} color="secondary" onClick={onClose} />
          <ActionButton title="Vincular" icon={<LinkIcon />} color="primary" type="submit" disabled={isPending || !escolhido} />
        </DialogActions>
      </form>
    </Dialog>
  );
};

export default VinculoDialog;
