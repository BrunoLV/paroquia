import React, { useState } from 'react';
import {
  Card,
  CardContent,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import { Close as CloseIcon, Gavel as GavelIcon, Send as SendIcon } from '@mui/icons-material';
import { DIALOG_CONTENT_SCROLL_SX } from '@/theme/dialogStyles';
import { confirmDiscard } from '@/utils/confirmDiscard';
import { Organizacao } from '../api/types';
import ActionButton from './ActionButton';

interface CpcDecisaoModalProps {
  open: boolean;
  onClose: () => void;
  onSubmit: (data: { titulo: string; pautaDescricao: string; dataReuniao: string; comunidadeId: string }) => void;
  isSubmitting?: boolean;
  communities: Organizacao[];
}

/**
 * Formulário de registro de ata/decisão do Conselho Pastoral (CPC/CPP). Este modal apenas cria
 * a decisão em estado `PENDENTE_HOMOLOGACAO` — a decisão nasce sempre pendente e só se torna
 * oficial/válida quando homologada por autoridade competente (Pároco, Admin ou Mesa do CPP).
 * A homologação (aprovar -> HOMOLOGADO, ou vetar -> VETADO, exigindo motivo) é um passo
 * separado, feito em outra tela/ação após a submissão daqui — este componente não decide o
 * mérito da ata, só registra a proposta.
 */
export const CpcDecisaoModal: React.FC<CpcDecisaoModalProps> = ({
  open,
  onClose,
  onSubmit,
  isSubmitting = false,
  communities = [],
}) => {
  const [titulo, setTitulo] = useState('');
  const [pautaDescricao, setPautaDescricao] = useState('');
  const [dataReuniao, setDataReuniao] = useState(new Date().toISOString().split('T')[0]);
  const [comunidadeId, setComunidadeId] = useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    // Todos os campos são obrigatórios para que a ata seja auditável quando submetida à
    // homologação (comunidade de origem identifica o CPC/CPP responsável perante o Pároco).
    if (!titulo.trim() || !pautaDescricao.trim() || !dataReuniao || !comunidadeId) {
      return;
    }
    onSubmit({
      titulo: titulo.trim(),
      pautaDescricao: pautaDescricao.trim(),
      dataReuniao,
      comunidadeId,
    });
  };

  const isDirty = Boolean(titulo.trim() || pautaDescricao.trim() || comunidadeId);
  const handleClose = () => confirmDiscard(isDirty, onClose);

  return (
    <Dialog
      open={open}
      onClose={handleClose}
      maxWidth="sm"
      fullWidth
    >
      <form onSubmit={handleSubmit} noValidate>
        <DialogTitle>
          Registrar Ata / Decisão de Conselho (CPC / CPP)
        </DialogTitle>
        <DialogContent dividers sx={DIALOG_CONTENT_SCROLL_SX}>
          <Stack spacing={2.5}>
          <Typography variant="body2" color="textSecondary">
            Preencha os dados da reunião e as deliberações aprovadas pelo conselho. O documento ficará pendente de homologação pelo Pároco / Mesa Diretora do CPP.
          </Typography>

          <Card variant="outlined" sx={{ borderRadius: 2 }}>
            <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
              <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
                <GavelIcon color="primary" fontSize="small" />
                <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                  Decisão do CPC / CPP
                </Typography>
              </Stack>

              <Stack spacing={2.5}>
            <TextField
              label="Título da Ata / Decisão"
              required
              fullWidth
              size="small"
              value={titulo}
              onChange={(e) => setTitulo(e.target.value)}
              disabled={isSubmitting}
            />

            <FormControl fullWidth required size="small">
              <InputLabel id="cpc-comunidade-label">Comunidade de Origem (CPC/CPP)</InputLabel>
              <Select
                labelId="cpc-comunidade-label"
                value={comunidadeId}
                label="Comunidade de Origem (CPC/CPP)"
                onChange={(e) => setComunidadeId(e.target.value)}
                disabled={isSubmitting}
              >
                {communities.map((c) => (
                  <MenuItem key={c.id} value={c.id}>
                    {c.nome}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>

            <TextField
              label="Data da Reunião"
              type="date"
              required
              fullWidth
              size="small"
              value={dataReuniao}
              onChange={(e) => setDataReuniao(e.target.value)}
              slotProps={{ inputLabel: { shrink: true } }}
              disabled={isSubmitting}
            />

            <TextField
              label="Pauta e Deliberações (Descrição Detalhada)"
              required
              fullWidth
              multiline
              rows={4}
              size="small"
              value={pautaDescricao}
              onChange={(e) => setPautaDescricao(e.target.value)}
              disabled={isSubmitting}
            />
              </Stack>
            </CardContent>
          </Card>
          </Stack>
        </DialogContent>
        <DialogActions>
          <ActionButton
            title="Cancelar"
            icon={<CloseIcon />}
            onClick={handleClose}
            disabled={isSubmitting}
            color="secondary"
          />
          <ActionButton
            title="Submeter Ata para Homologação"
            icon={<SendIcon />}
            type="submit"
            color="primary"
            disabled={isSubmitting}
          />
        </DialogActions>
      </form>
    </Dialog>
  );
};
