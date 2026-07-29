import React, { useState } from 'react';
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  TextField,
  Box,
  Typography,
} from '@mui/material';
import { Organizacao } from '../api/types';

interface CpcDecisaoModalProps {
  open: boolean;
  onClose: () => void;
  onSubmit: (data: { titulo: string; pautaDescricao: string; dataReuniao: string; comunidadeId: string }) => void;
  isSubmitting?: boolean;
  communities: Organizacao[];
}

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

  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="sm"
      fullWidth
      PaperProps={{ sx: { borderRadius: 0 } }}
    >
      <DialogTitle sx={{ fontWeight: 600 }}>
        Registrar Ata / Decisão de Conselho (CPC / CPP)
      </DialogTitle>
      <form onSubmit={handleSubmit}>
        <DialogContent dividers sx={{ py: 3 }}>
          <Box display="flex" flexDirection="column" gap={2.5}>
            <Typography variant="body2" color="textSecondary">
              Preencha os dados da reunião e as deliberações aprovadas pelo conselho. O documento ficará pendente de homologação pelo Pároco / Mesa Diretora do CPP.
            </Typography>

            <TextField
              label="Título da Ata / Decisão *"
              size="small"
              fullWidth
              value={titulo}
              onChange={(e) => setTitulo(e.target.value)}
              required
              disabled={isSubmitting}
              sx={{ '& .MuiOutlinedInput-root': { borderRadius: 0 } }}
            />

            <FormControl fullWidth size="small" required sx={{ '& .MuiOutlinedInput-root': { borderRadius: 0 } }}>
              <InputLabel id="cpc-comunidade-label">Comunidade de Origem (CPC/CPP) *</InputLabel>
              <Select
                labelId="cpc-comunidade-label"
                value={comunidadeId}
                label="Comunidade de Origem (CPC/CPP) *"
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
              label="Data da Reunião *"
              type="date"
              size="small"
              fullWidth
              value={dataReuniao}
              onChange={(e) => setDataReuniao(e.target.value)}
              slotProps={{ inputLabel: { shrink: true } }}
              required
              disabled={isSubmitting}
              sx={{ '& .MuiOutlinedInput-root': { borderRadius: 0 } }}
            />

            <TextField
              label="Pauta e Deliberações (Descrição Detalhada) *"
              size="small"
              fullWidth
              multiline
              rows={4}
              value={pautaDescricao}
              onChange={(e) => setPautaDescricao(e.target.value)}
              required
              disabled={isSubmitting}
              sx={{ '& .MuiOutlinedInput-root': { borderRadius: 0 } }}
            />
          </Box>
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={onClose} disabled={isSubmitting} color="inherit" sx={{ borderRadius: 0 }}>
            Cancelar
          </Button>
          <Button type="submit" variant="contained" color="primary" disabled={isSubmitting} sx={{ borderRadius: 0 }}>
            Submeter Ata para Homologação
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
};
