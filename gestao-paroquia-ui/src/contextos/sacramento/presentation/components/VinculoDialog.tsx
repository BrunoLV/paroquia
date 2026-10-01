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
import { getFieis } from '@/contextos/fiel/infrastructure/http/fieis';
import { listarClerigos } from '@/contextos/clero/infrastructure/http/clerigos';
import {
  PAPEL_LABEL, PAPEIS_CLERICAIS, PapelParticipante, Participante,
} from '@/contextos/sacramento/domain/sacramento';

interface Candidato {
  id: string;
  nome: string;
}

export interface VinculoEscolhido {
  participanteId: string;
  papel: PapelParticipante;
  fielId?: string;
  clerigoId?: string;
}

interface VinculoDialogProps {
  open: boolean;
  /** Posições do termo ainda sem vínculo (nem pendente). */
  semVinculo: Participante[];
  /** Posição pré-selecionada ao abrir a partir de uma linha de participante. */
  inicial: Participante | null;
  isPending: boolean;
  /** Pároco vincula direto; Secretaria propõe e aguarda aprovação na tela de Aprovações. */
  propoe: boolean;
  onClose: () => void;
  onConfirm: (vinculo: VinculoEscolhido) => void;
}

/**
 * Vínculo tardio feito a partir do registro do sacramento (Cân. 535): o operador informa o tipo de
 * participação (padrinho, testemunha, ministro...), escolhe a posição correspondente no termo e o cadastro
 * de fiel ou clérigo. Só o ponteiro muda; o nome atestado na celebração permanece.
 */
export const VinculoDialog: React.FC<VinculoDialogProps> = ({
  open, semVinculo, inicial, isPending, propoe, onClose, onConfirm,
}) => {
  const [papel, setPapel] = useState<PapelParticipante | ''>('');
  const [participanteId, setParticipanteId] = useState('');
  const [escolhido, setEscolhido] = useState<Candidato | null>(null);

  const papeis = Array.from(new Set(semVinculo.map((p) => p.papel)));
  const posicoes = semVinculo.filter((p) => p.papel === papel);
  const clerical = papel !== '' && PAPEIS_CLERICAIS.includes(papel);

  useEffect(() => {
    if (!open) return;
    setPapel(inicial?.papel ?? '');
    setParticipanteId(inicial?.id ?? '');
    setEscolhido(null);
  }, [open, inicial]);

  const { data: candidatos = [] } = useQuery({
    queryKey: ['sacramentos', 'candidatos', clerical ? 'clerigo' : 'fiel'],
    queryFn: async (): Promise<Candidato[]> =>
      clerical
        ? (await listarClerigos()).map((c) => ({ id: c.clerigoId, nome: c.nomeCivil }))
        : (await getFieis()).flatMap((f) => (f.nome ? [{ id: f.id, nome: f.nome }] : [])),
    enabled: open && papel !== '',
  });

  const escolherPapel = (novo: PapelParticipante) => {
    setPapel(novo);
    setEscolhido(null);
    const opcoes = semVinculo.filter((p) => p.papel === novo);
    setParticipanteId(opcoes.length === 1 ? opcoes[0].id : '');
  };

  const confirmar = (event: React.FormEvent) => {
    event.preventDefault();
    if (papel === '' || !participanteId || !escolhido) return;
    onConfirm({ participanteId, papel, ...(clerical ? { clerigoId: escolhido.id } : { fielId: escolhido.id }) });
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <form onSubmit={confirmar} noValidate>
        <DialogTitle>Vincular fiel ou clérigo ao registro</DialogTitle>
        <DialogContent dividers sx={DIALOG_CONTENT_SCROLL_SX}>
          <Stack spacing={2.5}>
            <Card variant="outlined" sx={{ borderRadius: 2 }}>
              <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
                <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
                  <LinkIcon color="primary" fontSize="small" />
                  <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                    Participação no sacramento
                  </Typography>
                </Stack>
                <Stack spacing={2}>
                  <FormControl fullWidth size="small" required>
                    <InputLabel id="vinculo-papel">Tipo de participação</InputLabel>
                    <Select
                      labelId="vinculo-papel"
                      label="Tipo de participação"
                      value={papel}
                      onChange={(e) => escolherPapel(e.target.value as PapelParticipante)}
                    >
                      {papeis.map((p) => (
                        <MenuItem key={p} value={p}>{PAPEL_LABEL[p]}</MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                  <FormControl fullWidth size="small" required disabled={papel === ''}>
                    <InputLabel id="vinculo-posicao">Posição no termo</InputLabel>
                    <Select
                      labelId="vinculo-posicao"
                      label="Posição no termo"
                      value={participanteId}
                      onChange={(e) => setParticipanteId(e.target.value)}
                    >
                      {posicoes.map((p) => (
                        <MenuItem key={p.id} value={p.id}>{p.nomeHistorico}</MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                  <Autocomplete
                    options={candidatos}
                    value={escolhido}
                    disabled={papel === ''}
                    onChange={(_, valor) => setEscolhido(valor)}
                    getOptionLabel={(c) => c.nome}
                    isOptionEqualToValue={(a, b) => a.id === b.id}
                    noOptionsText="Nenhum cadastro encontrado"
                    renderInput={(params) => (
                      <TextField
                        {...params}
                        required
                        size="small"
                        label={clerical ? 'Cadastro de clérigo' : 'Cadastro de fiel'}
                        helperText={propoe
                          ? 'O vínculo ficará pendente até a aprovação do Pároco na tela de Aprovações.'
                          : 'O texto lavrado no termo não é alterado pelo vínculo.'}
                      />
                    )}
                  />
                </Stack>
              </CardContent>
            </Card>
          </Stack>
        </DialogContent>
        <DialogActions sx={{ px: 3, py: 2 }}>
          <ActionButton title="Cancelar" icon={<CloseIcon />} color="secondary" onClick={onClose} />
          <ActionButton
            title={propoe ? 'Propor vínculo' : 'Vincular'}
            icon={<LinkIcon />}
            color="primary"
            type="submit"
            disabled={isPending || papel === '' || !participanteId || !escolhido}
          />
        </DialogActions>
      </form>
    </Dialog>
  );
};

export default VinculoDialog;
