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
      sx={{ '& .MuiDialog-paper': { borderRadius: 0 } }}
    >
      <form onSubmit={handleSubmit} noValidate>
        <DialogTitle sx={{ borderBottom: '1px solid #eee', pb: 2, fontWeight: 700, color: '#37474f', fontSize: '1.25rem' }}>
          Registrar Ata / Decisão de Conselho (CPC / CPP)
        </DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5, mt: 1 }}>
            <Typography variant="body2" color="textSecondary">
              Preencha os dados da reunião e as deliberações aprovadas pelo conselho. O documento ficará pendente de homologação pelo Pároco / Mesa Diretora do CPP.
            </Typography>

            <TextField
              label="Título da Ata / Decisão"
              required
              fullWidth
              size="small"
              value={titulo}
              onChange={(e) => setTitulo(e.target.value)}
              disabled={isSubmitting}
              sx={{ '& .MuiOutlinedInput-root': { borderRadius: 0 } }}
            />

            <FormControl fullWidth required size="small" sx={{ '& .MuiOutlinedInput-root': { borderRadius: 0 } }}>
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
              sx={{ '& .MuiOutlinedInput-root': { borderRadius: 0 } }}
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
              sx={{ '& .MuiOutlinedInput-root': { borderRadius: 0 } }}
            />
          </Box>
        </DialogContent>
        <DialogActions sx={{ borderTop: '1px solid #eee', pt: 2, pb: 2, px: 3 }}>
          <Button
            onClick={onClose}
            disabled={isSubmitting}
            sx={{ textTransform: 'none', fontWeight: 600, color: '#d97706' }}
          >
            Cancelar
          </Button>
          <Button
            type="submit"
            variant="contained"
            disabled={isSubmitting}
            sx={{
              bgcolor: '#1a237e',
              color: '#fff',
              textTransform: 'none',
              fontWeight: 600,
              borderRadius: 0,
              px: 3,
              '&:hover': { bgcolor: '#0d47a1' },
            }}
          >
            Submeter Ata para Homologação
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
};
