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
import { Close as CloseIcon, Send as SendIcon } from '@mui/icons-material';
import { Organizacao } from '../api/types';

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

  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="sm"
      fullWidth
    >
      <form onSubmit={handleSubmit} noValidate>
        <DialogTitle>
          Registrar Ata / Decisão de Conselho (CPC / CPP)
        </DialogTitle>
        <DialogContent dividers>
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
          </Box>
        </DialogContent>
        <DialogActions>
          <Button
            onClick={onClose}
            disabled={isSubmitting}
            color="secondary"
            startIcon={<CloseIcon />}
            sx={{ textTransform: 'none', fontWeight: 600 }}
          >
            Cancelar
          </Button>
          <Button
            type="submit"
            variant="contained"
            color="primary"
            startIcon={<SendIcon />}
            disabled={isSubmitting}
            sx={{ textTransform: 'none', fontWeight: 600, px: 3 }}
          >
            Submeter Ata para Homologação
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
};
