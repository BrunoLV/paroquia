import React from 'react';
import { Card, CardContent, Dialog, DialogActions, DialogContent, DialogTitle, Stack, Typography } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import ContentCopyIcon from '@mui/icons-material/ContentCopy';
import DescriptionIcon from '@mui/icons-material/Description';
import ActionButton from '@/shared/presentation/components/ActionButton';
import { DIALOG_CONTENT_SCROLL_SX } from '@/shared/presentation/theme/dialogStyles';
import { Certidao } from '@/contextos/sacramento/domain/sacramento';

interface CertidaoDialogProps {
  certidao: Certidao | null;
  onClose: () => void;
}

/**
 * Certidão narrativa de assento homologado. O backend já omite dados sensíveis (filiação adotiva,
 * notas reservadas, livro reservado); a tela apenas exibe e permite copiar o texto emitido.
 */
export const CertidaoDialog: React.FC<CertidaoDialogProps> = ({ certidao, onClose }) => (
  <Dialog open={certidao !== null} onClose={onClose} maxWidth="md" fullWidth>
    <DialogTitle>Certidão</DialogTitle>
    <DialogContent dividers sx={DIALOG_CONTENT_SCROLL_SX}>
      <Stack spacing={2.5}>
        <Card variant="outlined" sx={{ borderRadius: 2 }}>
          <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
            <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
              <DescriptionIcon color="primary" fontSize="small" />
              <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                Extrato do assento
              </Typography>
            </Stack>
            <Typography variant="body2">{certidao?.texto}</Typography>
            {certidao?.averbacoes.map((nota) => (
              <Typography key={nota} variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                À margem: {nota}
              </Typography>
            ))}
          </CardContent>
        </Card>
      </Stack>
    </DialogContent>
    <DialogActions sx={{ px: 3, py: 2 }}>
      <ActionButton title="Fechar" icon={<CloseIcon />} color="secondary" onClick={onClose} />
      <ActionButton
        title="Copiar texto"
        icon={<ContentCopyIcon />}
        color="primary"
        onClick={() => {
          if (certidao) void navigator.clipboard.writeText(certidao.texto);
        }}
      />
    </DialogActions>
  </Dialog>
);

export default CertidaoDialog;
