import React, { useState, useEffect, useMemo } from 'react';
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
  Alert,
} from '@mui/material';
import { useAuth } from '../auth/AuthContext';
import { Evento, Organizacao, Local, Projeto } from '../api/types';

const CATEGORIES = [
  'PASTORAL',
  'SOCIAL',
  'LITURGICO',
  'ADMINISTRATIVO',
  'SACRAMENTAL',
  'FORMATIVO',
  'ASSISTENCIAL',
];

const STATUSES = ['RASCUNHO', 'CONFIRMADO', 'ADICIONADO_EXTRA', 'CANCELADO'];

const formatLocalDate = (d: Date) => {
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const date = String(d.getDate()).padStart(2, '0');
  const hours = String(d.getHours()).padStart(2, '0');
  const mins = String(d.getMinutes()).padStart(2, '0');
  return `${year}-${month}-${date}T${hours}:${mins}`;
};

const formatIsoToLocal = (isoString: string) => {
  if (!isoString) return '';
  return formatLocalDate(new Date(isoString));
};

interface EventRegistrationModalProps {
  open: boolean;
  onClose: () => void;
  onSubmit: (data: any) => void;
  isSubmitting?: boolean;
  organizations: Organizacao[];
  locais?: Local[];
  projetos?: Projeto[];
  initialDate?: Date;
  initialEvent?: Evento | null;
  error?: string;
}

const EventRegistrationModal: React.FC<EventRegistrationModalProps> = ({
  open,
  onClose,
  onSubmit,
  isSubmitting = false,
  organizations = [],
  locais = [],
  projetos = [],
  initialDate,
  initialEvent,
  error,
}) => {
  const { user } = useAuth();
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [category, setCategory] = useState('PASTORAL');
  const [orgId, setOrgId] = useState('');
  const [localId, setLocalId] = useState('');
  const [projetoId, setProjetoId] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [status, setStatus] = useState('CONFIRMADO');
  const [justification, setJustification] = useState('');

  const filteredOrganizations = useMemo(() => {
    if (!user) return [];
    if (user.roles.includes('ADMIN')) return organizations;
    
    const userOrgIds = user.organizations.map(org => org.id);
    return organizations.filter(org => userOrgIds.includes(org.id));
  }, [user, organizations]);

  // Reset form or set initial values when modal opens or initialEvent changes
  useEffect(() => {
    if (open) {
      if (initialEvent) {
        setTitle(initialEvent.titulo || '');
        setDescription(initialEvent.descricao || '');
        setCategory(initialEvent.categoria || 'PASTORAL');
        setOrgId(initialEvent.organizacaoResponsavelId || '');
        setLocalId(initialEvent.localId || '');
        setProjetoId(initialEvent.projetoId || '');
        setStartDate(formatIsoToLocal(initialEvent.inicio));
        setEndDate(formatIsoToLocal(initialEvent.fim));
        setStatus(initialEvent.status || 'CONFIRMADO');
        setJustification(initialEvent.adicionadoExtraJustificativa || '');
      } else {
        setTitle('');
        setDescription('');
        setCategory('PASTORAL');
        setLocalId('');
        setProjetoId('');
        setStatus('CONFIRMADO');
        setJustification('');
        
        const start = initialDate ? new Date(initialDate) : new Date();
        start.setHours(9, 0, 0, 0);
        const end = new Date(start);
        end.setHours(10, 0, 0, 0);

        setStartDate(formatLocalDate(start));
        setEndDate(formatLocalDate(end));

        if (filteredOrganizations.length > 0) {
          setOrgId(filteredOrganizations[0].id);
        }
      }
    }
  }, [open, initialDate, initialEvent, filteredOrganizations]);

  const handleFormSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSubmit({
      titulo: title,
      descricao: description,
      categoria: category,
      organizacaoResponsavelId: orgId,
      localId: localId || null,
      projetoId: projetoId || null,
      inicio: startDate ? new Date(startDate).toISOString() : '',
      fim: endDate ? new Date(endDate).toISOString() : '',
      status,
      adicionadoExtraJustificativa: justification || null,
    });
  };

  return (
    <Dialog open={open} onClose={() => !isSubmitting && onClose()} maxWidth="sm" fullWidth sx={{ '& .MuiDialog-paper': { borderRadius: '16px' } }}>
      <form onSubmit={handleFormSubmit}>
        <DialogTitle sx={{ borderBottom: '1px solid #eee', pb: 2, fontWeight: 700, color: '#37474f', fontSize: '1.25rem' }}>
          {initialEvent ? 'Editar Evento' : 'Criar Novo Evento'}
        </DialogTitle>
        <DialogContent sx={{ pt: 3 }}>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5, mt: 1 }}>
            {error && (
              <Alert severity="error" sx={{ borderRadius: '8px' }}>
                {error}
              </Alert>
            )}
            
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
                <FormControl fullWidth size="small">
                  <InputLabel id="new-status-label">Status</InputLabel>
                  <Select
                    labelId="new-status-label"
                    value={status}
                    label="Status"
                    onChange={(e) => setStatus(e.target.value)}
                    disabled={isSubmitting}
                  >
                    {STATUSES.map((st) => (
                      <MenuItem key={st} value={st}>
                        {st}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Grid>
            </Grid>

            <FormControl fullWidth size="small" required>
              <InputLabel id="new-org-label">Organização Responsável</InputLabel>
              <Select
                labelId="new-org-label"
                value={orgId}
                label="Organização Responsável"
                onChange={(e) => setOrgId(e.target.value)}
                disabled={isSubmitting}
              >
                {filteredOrganizations.map((org) => (
                  <MenuItem key={org.id} value={org.id}>
                    {org.nome}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>

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

            <TextField
              label="Justificativa Extra (Opcional)"
              value={justification}
              onChange={(e) => setJustification(e.target.value)}
              placeholder="Preencha caso seja um agendamento extraordinário"
              fullWidth
              disabled={isSubmitting}
              size="small"
              slotProps={{ htmlInput: { maxLength: 4000 } }}
            />
          </Box>
        </DialogContent>
        <DialogActions sx={{ borderTop: '1px solid #eee', px: 3, py: 2 }}>
          <Button onClick={onClose} disabled={isSubmitting} color="secondary" sx={{ fontWeight: 600 }}>
            Cancelar
          </Button>
          <Button type="submit" variant="contained" color="primary" disabled={isSubmitting} sx={{ fontWeight: 600, borderRadius: '8px' }}>
            {initialEvent ? 'Salvar Alterações' : 'Criar Evento'}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
};

export default EventRegistrationModal;
