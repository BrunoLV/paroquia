import React, { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  Box,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Grid,
} from '@mui/material';

const CATEGORIES = [
  'PASTORAL',
  'SOCIAL',
  'LITURGICO',
  'ADMINISTRATIVO',
  'SACRAMENTAL',
  'FORMATIVO',
  'ASSISTENCIAL',
];

interface Organizacao {
  id: string;
  nome: string;
}

interface EventRegistrationModalProps {
  open: boolean;
  onClose: () => void;
  onSubmit: (data: any) => void;
  isSubmitting?: boolean;
  organizations: Organizacao[];
  initialDate?: Date;
}

const EventRegistrationModal: React.FC<EventRegistrationModalProps> = ({
  open,
  onClose,
  onSubmit,
  isSubmitting = false,
  organizations = [],
}) => {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [category, setCategory] = useState('PASTORAL');
  const [orgId, setOrgId] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');

  const handleFormSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSubmit({
      titulo: title,
      descricao: description,
      categoria: category,
      organizacaoResponsavelId: orgId,
      inicio: startDate ? new Date(startDate).toISOString() : '',
      fim: endDate ? new Date(endDate).toISOString() : '',
    });
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth sx={{ '& .MuiDialog-paper': { borderRadius: '16px' } }}>
      <form onSubmit={handleFormSubmit}>
        <DialogTitle sx={{ borderBottom: '1px solid #eee', pb: 2, fontWeight: 700, color: '#37474f', fontSize: '1.25rem' }}>
          Criar Novo Evento
        </DialogTitle>
        <DialogContent sx={{ pt: 3 }}>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5, mt: 1 }}>
            <TextField
              required
              label="Título do Evento"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              fullWidth
              disabled={isSubmitting}
              size="small"
              slotProps={{ htmlInput: { maxLength: 160 } }}
            />

            <TextField
              label="Descrição"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              multiline
              rows={3}
              fullWidth
              disabled={isSubmitting}
              size="small"
              slotProps={{ htmlInput: { maxLength: 4000 } }}
            />

            <Grid container spacing={2}>
              <Grid size={{ xs: 6 }}>
                <FormControl fullWidth size="small">
                  <InputLabel id="new-cat-label">Categoria</InputLabel>
                  <Select
                    labelId="new-cat-label"
                    value={category}
                    label="Categoria"
                    onChange={(e) => setCategory(e.target.value)}
                    disabled={isSubmitting}
                  >
                    {CATEGORIES.map((cat) => (
                      <MenuItem key={cat} value={cat}>
                        {cat}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Grid>
              <Grid size={{ xs: 6 }}>
                <FormControl fullWidth size="small" required>
                  <InputLabel id="new-org-label">Organização Responsável</InputLabel>
                  <Select
                    labelId="new-org-label"
                    value={orgId}
                    label="Organização Responsável"
                    onChange={(e) => setOrgId(e.target.value)}
                    disabled={isSubmitting}
                  >
                    {organizations.map((org) => (
                      <MenuItem key={org.id} value={org.id}>
                        {org.nome}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Grid>
            </Grid>

            <Grid container spacing={2}>
              <Grid size={{ xs: 6 }}>
                <TextField
                  required
                  label="Data e Hora de Início"
                  type="datetime-local"
                  value={startDate}
                  onChange={(e) => setStartDate(e.target.value)}
                  slotProps={{ inputLabel: { shrink: true } }}
                  fullWidth
                  disabled={isSubmitting}
                  size="small"
                />
              </Grid>
              <Grid size={{ xs: 6 }}>
                <TextField
                  required
                  label="Data e Hora de Término"
                  type="datetime-local"
                  value={endDate}
                  onChange={(e) => setEndDate(e.target.value)}
                  slotProps={{ inputLabel: { shrink: true } }}
                  fullWidth
                  disabled={isSubmitting}
                  size="small"
                />
              </Grid>
            </Grid>
          </Box>
        </DialogContent>
        <DialogActions sx={{ borderTop: '1px solid #eee', px: 3, py: 2 }}>
          <Button onClick={onClose} disabled={isSubmitting} color="secondary" sx={{ fontWeight: 600 }}>
            Cancelar
          </Button>
          <Button type="submit" variant="contained" color="primary" disabled={isSubmitting} sx={{ fontWeight: 600, borderRadius: '8px' }}>
            Criar Evento
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
};

export default EventRegistrationModal;
