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
  Chip,
  Switch,
  Collapse,
  FormControlLabel,
} from '@mui/material';
import { useAuth } from '../auth/AuthContext';
import { useNotification } from '../context/NotificationContext';
import { Evento, Organizacao, Local, Projeto } from '../api/types';
import RecurrenceForm, { RecurrenceRule } from './RecurrenceForm';

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
}) => {
  const { user } = useAuth();
  const { showError } = useNotification();
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [category, setCategory] = useState('PASTORAL');
  const [orgId, setOrgId] = useState('');
  const [localId, setLocalId] = useState('');
  const [projetoId, setProjetoId] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [endTime, setEndTime] = useState('10:00');
  const [status, setStatus] = useState('CONFIRMADO');
  const [justification, setJustification] = useState('');
  const [participantes, setParticipantes] = useState<string[]>([]);
  const [isRecurring, setIsRecurring] = useState(false);
  const [recurrenceRule, setRecurrenceRule] = useState<RecurrenceRule>({
    frequencia: 'SEMANAL',
    intervalo: 1,
    diasDaSemana: [],
  });

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

        if (initialEvent.fim) {
          const d = new Date(initialEvent.fim);
          setEndTime(`${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`);
        }

        setStatus(initialEvent.status || 'CONFIRMADO');
        setJustification(initialEvent.adicionadoExtraJustificativa || '');
        setParticipantes(initialEvent.participantes || []);
        setIsRecurring(!!initialEvent.recorrenciaId);
      } else {
        setTitle('');
        setDescription('');
        setCategory('PASTORAL');
        setLocalId('');
        setProjetoId('');
        setStatus('CONFIRMADO');
        setJustification('');
        setParticipantes([]);
        setIsRecurring(false);
        setRecurrenceRule({
          frequencia: 'SEMANAL',
          intervalo: 1,
          diasDaSemana: [],
        });
        
        const start = initialDate ? new Date(initialDate) : new Date();
        start.setHours(9, 0, 0, 0);
        const end = new Date(start);
        end.setHours(10, 0, 0, 0);
        setStartDate(formatLocalDate(start));
        setEndDate(formatLocalDate(end));
        setEndTime('10:00');
      }

      if (!initialEvent && filteredOrganizations.length > 0) {
        setOrgId(filteredOrganizations[0].id);
      }
    }
  }, [open, initialDate, initialEvent, filteredOrganizations]);

  const handleFormSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    if (!title || !title.trim()) {
      showError('O título do evento é obrigatório.');
      return;
    }

    if (!orgId) {
      showError('A organização responsável é obrigatória.');
      return;
    }

    if (!startDate) {
      showError('A data e hora de início é obrigatória.');
      return;
    }

    if (isRecurring) {
      if (!endTime) {
        showError('A hora de término é obrigatória.');
        return;
      }
    } else {
      if (!endDate) {
        showError('A data e hora de término é obrigatória.');
        return;
      }
      if (new Date(startDate) > new Date(endDate)) {
        showError('A data de início não pode ser posterior à data de término.');
        return;
      }
    }

    // Determine real end date based on mode (recurrence uses same day + separate time)
    let finalEndDate = endDate;
    if (isRecurring && startDate) {
      const datePart = startDate.split('T')[0];
      finalEndDate = `${datePart}T${endTime}`;
    }

    onSubmit({
      titulo: title,
      descricao: description,
      categoria: category,
      organizacaoResponsavelId: orgId,
      localId: localId || null,
      projetoId: projetoId || null,
      participantes,
      inicio: startDate ? new Date(startDate).toISOString() : '',
      fim: finalEndDate ? new Date(finalEndDate).toISOString() : '',
      status,
      adicionadoExtraJustificativa: justification || null,
      recurrence: isRecurring ? recurrenceRule : null,
    });
  };

  return (
    <Dialog open={open} onClose={() => !isSubmitting && onClose()} maxWidth="sm" fullWidth sx={{ '& .MuiDialog-paper': { borderRadius: 0 } }}>
      <form onSubmit={handleFormSubmit}>
        <DialogTitle sx={{ borderBottom: '1px solid #eee', pb: 2, fontWeight: 700, color: '#37474f', fontSize: '1.25rem' }}>
          {initialEvent ? 'Editar Evento' : 'Criar Novo Evento'}
        </DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5, mt: 1 }}>
            <TextField
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

            <FormControl fullWidth size="small">
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
                <FormControl fullWidth size="small">
                  <InputLabel id="new-local-label">Local</InputLabel>
                  <Select
                    labelId="new-local-label"
                    value={localId}
                    label="Local"
                    onChange={(e) => setLocalId(e.target.value)}
                    disabled={isSubmitting}
                  >
                    <MenuItem value="">
                      <em>Nenhum</em>
                    </MenuItem>
                    {locais.map((l) => (
                      <MenuItem key={l.id} value={l.id}>
                        {l.nome}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Grid>
              <Grid size={{ xs: 6 }}>
                <FormControl fullWidth size="small">
                  <InputLabel id="new-projeto-label">Projeto</InputLabel>
                  <Select
                    labelId="new-projeto-label"
                    value={projetoId}
                    label="Projeto"
                    onChange={(e) => setProjetoId(e.target.value)}
                    disabled={isSubmitting}
                  >
                    <MenuItem value="">
                      <em>Nenhum</em>
                    </MenuItem>
                    {projetos.map((p) => (
                      <MenuItem key={p.id} value={p.id}>
                        {p.nome}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Grid>
            </Grid>

            <FormControl fullWidth size="small">
              <InputLabel id="new-apoio-label">Organizações de Apoio</InputLabel>
              <Select
                labelId="new-apoio-label"
                multiple
                value={participantes}
                label="Organizações de Apoio"
                onChange={(e) => setParticipantes(typeof e.target.value === 'string' ? e.target.value.split(',') : e.target.value)}
                disabled={isSubmitting}
                renderValue={(selected) => (
                  <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                    {(selected as string[]).map((id) => (
                      <Chip key={id} label={organizations.find(o => o.id === id)?.nome || id} size="small" />
                    ))}
                  </Box>
                )}
              >
                {organizations
                  .filter(org => org.id !== orgId)
                  .map((org) => (
                    <MenuItem key={org.id} value={org.id}>
                      {org.nome}
                    </MenuItem>
                  ))}
              </Select>
            </FormControl>

            <Grid container spacing={2}>
              <Grid size={{ xs: 6 }}>
                <TextField
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
                {isRecurring ? (
                  <TextField
                    label="Hora de Término"
                    type="time"
                    value={endTime}
                    onChange={(e) => setEndTime(e.target.value)}
                    slotProps={{ inputLabel: { shrink: true } }}
                    fullWidth
                    disabled={isSubmitting}
                    size="small"
                  />
                ) : (
                  <TextField
                    label="Data e Hora de Término"
                    type="datetime-local"
                    value={endDate}
                    onChange={(e) => setEndDate(e.target.value)}
                    slotProps={{ inputLabel: { shrink: true } }}
                    fullWidth
                    disabled={isSubmitting}
                    size="small"
                  />
                )}
              </Grid>
            </Grid>

            {!initialEvent && (
              <>
                <FormControlLabel
                  control={
                    <Switch
                      checked={isRecurring}
                      onChange={(e) => setIsRecurring(e.target.checked)}
                      disabled={isSubmitting}
                    />
                  }
                  label="Repetir este evento"
                />

                <Collapse in={isRecurring}>
                  <Box sx={{ mb: 1 }}>
                    <RecurrenceForm
                      rule={recurrenceRule}
                      onChange={setRecurrenceRule}
                      disabled={isSubmitting}
                    />
                  </Box>
                </Collapse>
              </>
            )}

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
          <Button type="submit" variant="contained" color="primary" disabled={isSubmitting} sx={{ fontWeight: 600 }}>
            {initialEvent ? 'Salvar Alterações' : 'Criar Evento'}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
};

export default EventRegistrationModal;
