import React, { useEffect, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Alert, Card, CardContent, Dialog, DialogActions, DialogContent, DialogTitle, Grid, Skeleton, Stack, TextField, Typography } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import SaveIcon from '@mui/icons-material/Save';
import { DIALOG_CONTENT_SCROLL_SX } from '@/shared/presentation/theme/dialogStyles';
import { getIdoneidade, registrarIdoneidade, RegistroIdoneidade } from '@/contextos/membro/infrastructure/http/membros';
import { useNotification } from '@/shared/presentation/context/NotificationContext';
import { extractErrorMessage } from '@/shared/utils/errorUtils';
import ActionButton from '@/shared/presentation/components/ActionButton';

interface IdoneidadeModalProps {
  open: boolean;
  onClose: () => void;
  /** Vínculo (membro de organização) cuja idoneidade é atestada. */
  vinculoId: string | null;
  nome: string;
  organizacaoId?: string;
}

const VAZIO: RegistroIdoneidade = {
  formacaoConcluidaEm: null,
  vivenciaSacramentalAtestadaEm: null,
  termoProtecaoMenoresEm: null,
  termoProtecaoMenoresValidoAte: null,
  enviadoEm: null,
};

const CAMPOS: { campo: keyof RegistroIdoneidade; rotulo: string }[] = [
  { campo: 'formacaoConcluidaEm', rotulo: 'Formação doutrinária básica concluída em' },
  { campo: 'vivenciaSacramentalAtestadaEm', rotulo: 'Vivência sacramental atestada em' },
  { campo: 'termoProtecaoMenoresEm', rotulo: 'Termo de proteção de menores assinado em' },
  { campo: 'termoProtecaoMenoresValidoAte', rotulo: 'Termo válido até (opcional)' },
  { campo: 'enviadoEm', rotulo: 'Envio (missão) conferido em' },
];

/**
 * Atesta a idoneidade pastoral do agente: formação, vivência sacramental, termo de proteção de menores e
 * envio. Guarda somente datas; os documentos permanecem fora do sistema (LGPD).
 */
export const IdoneidadeModal: React.FC<IdoneidadeModalProps> = ({ open, onClose, vinculoId, nome, organizacaoId }) => {
  const { showError, showSuccess } = useNotification();
  const queryClient = useQueryClient();
  const [registro, setRegistro] = useState<RegistroIdoneidade>(VAZIO);

  const { data, isLoading } = useQuery({
    queryKey: ['idoneidade', vinculoId],
    queryFn: () => getIdoneidade(vinculoId!),
    enabled: open && !!vinculoId,
  });

  useEffect(() => {
    if (data) {
      setRegistro({
        formacaoConcluidaEm: data.formacaoConcluidaEm ?? null,
        vivenciaSacramentalAtestadaEm: data.vivenciaSacramentalAtestadaEm ?? null,
        termoProtecaoMenoresEm: data.termoProtecaoMenoresEm ?? null,
        termoProtecaoMenoresValidoAte: data.termoProtecaoMenoresValidoAte ?? null,
        enviadoEm: data.enviadoEm ?? null,
      });
    }
  }, [data]);

  const salvar = useMutation({
    mutationFn: () => registrarIdoneidade(vinculoId!, registro),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['idoneidade', vinculoId] });
      queryClient.invalidateQueries({ queryKey: ['organizacaoMembros', organizacaoId] });
      showSuccess('Idoneidade registrada com sucesso!');
      onClose();
    },
    onError: (err) => showError(extractErrorMessage(err, 'Erro ao registrar idoneidade.')),
  });

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>{`Idoneidade — ${nome}`}</DialogTitle>
      <DialogContent dividers sx={DIALOG_CONTENT_SCROLL_SX}>
        {isLoading ? (
          <Skeleton variant="rectangular" height={160} />
        ) : (
          <Stack spacing={2}>
            {data && (
              <Alert severity={data.apto ? 'success' : 'warning'}>
                {data.apto
                  ? 'Sem pendências para a função atual.'
                  : data.pendencias.map((p) => <Typography key={p} variant="body2">{p}</Typography>)}
              </Alert>
            )}
            <Card variant="outlined" sx={{ borderRadius: 2 }}>
              <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
                <Grid container spacing={2}>
                  {CAMPOS.map(({ campo, rotulo }) => (
                    <Grid key={campo} size={{ xs: 12 }}>
                      <TextField
                        label={rotulo}
                        type="date"
                        size="small"
                        fullWidth
                        value={registro[campo] ?? ''}
                        onChange={(e) => setRegistro((r) => ({ ...r, [campo]: e.target.value || null }))}
                        slotProps={{ inputLabel: { shrink: true } }}
                      />
                    </Grid>
                  ))}
                </Grid>
              </CardContent>
            </Card>
          </Stack>
        )}
      </DialogContent>
      <DialogActions>
        <ActionButton title="Cancelar" icon={<CloseIcon />} color="secondary" onClick={onClose} />
        <ActionButton
          title="Salvar"
          icon={<SaveIcon />}
          color="primary"
          disabled={isLoading || salvar.isPending}
          onClick={() => salvar.mutate()}
        />
      </DialogActions>
    </Dialog>
  );
};
