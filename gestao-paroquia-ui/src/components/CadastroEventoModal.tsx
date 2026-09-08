import React, {useEffect, useMemo, useState} from 'react';
import {
    Alert,
    Box,
    Chip,
    Collapse,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    FormControl,
    FormControlLabel,
    FormHelperText,
    Grid,
    InputLabel,
    MenuItem,
    Select,
    Switch,
    TextField,
} from '@mui/material';
import {Close as CloseIcon, Save as SaveIcon} from '@mui/icons-material';
import {useAuth} from '../auth/AuthContext';
import {isGlobalCreator} from '../auth/permissions';
import {useNotification} from '../context/NotificationContext';
import {AnoParoquial, AtivoPatrimonial, Evento, Local, Organizacao, Projeto} from '../api/types';
import RecurrenceForm, {RecurrenceRule} from './RecurrenceForm';
import ActionButton from './ActionButton';

const CATEGORIES = [
  'SOLENIDADE',
  'PASTORAL',
  'SOCIAL',
  'LITURGICO',
  'ADMINISTRATIVO',
  'SACRAMENTAL',
  'FORMATIVO',
  'ASSISTENCIAL',
  'MOVIMENTO',
];

const CATEGORY_PRIORITIES: Record<string, { label: string; priority: number; color: 'error' | 'warning' | 'primary' | 'default' }> = {
  SOLENIDADE: { label: 'Solenidade (Nível 1 - Global)', priority: 1, color: 'error' },
  LITURGICO: { label: 'Litúrgico (Nível 2 - Territorial)', priority: 2, color: 'warning' },
  SACRAMENTAL: { label: 'Sacramental (Nível 2 - Territorial)', priority: 2, color: 'warning' },
  PASTORAL: { label: 'Pastoral (Nível 3 - Orgânico)', priority: 3, color: 'primary' },
  FORMATIVO: { label: 'Formativo (Nível 3 - Orgânico)', priority: 3, color: 'primary' },
  ADMINISTRATIVO: { label: 'Administrativo (Nível 3 - Orgânico)', priority: 3, color: 'primary' },
  ASSISTENCIAL: { label: 'Assistencial (Nível 3 - Orgânico)', priority: 3, color: 'primary' },
  SOCIAL: { label: 'Social (Nível 3 - Orgânico)', priority: 3, color: 'primary' },
  MOVIMENTO: { label: 'Movimento (Nível 4 - Grupo)', priority: 4, color: 'default' },
};

const STATUSES = ['RASCUNHO', 'CONFIRMADO', 'ADICIONADO_EXTRA', 'CANCELADO'];

import { formatIsoToLocal, formatLocalDate, getRecurrenceDates } from '../utils/recurrenceUtils';


interface CadastroEventoModalProps {
  open: boolean;
  onClose: () => void;
  onSubmit: (data: Record<string, unknown>) => void;
  isSubmitting?: boolean;
  organizations: Organizacao[];
  locais?: Local[];
  ativos?: AtivoPatrimonial[];
  projetos?: Projeto[];
  anosParoquiais?: AnoParoquial[];
  initialDate?: Date;
  initialEvent?: Evento | null;
}

const CadastroEventoModal: React.FC<CadastroEventoModalProps> = ({
  open,
  onClose,
  onSubmit,
  isSubmitting = false,
  organizations = [],
  locais = [],
  ativos = [],
  projetos = [],
  anosParoquiais = [],
  initialDate,
  initialEvent,
}) => {
  const { user } = useAuth();
  const { showError } = useNotification();
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [category, setCategory] = useState('PASTORAL');
  const [orgId, setOrgId] = useState('');
  const [igrejaVinculoId, setIgrejaVinculoId] = useState('');
  const [localId, setLocalId] = useState('');
  const [selectedAtivoIds, setSelectedAtivoIds] = useState<string[]>([]);
  const [projetoId, setProjetoId] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [endTime, setEndTime] = useState('10:00');
  const [status, setStatus] = useState('CONFIRMADO');
  const [justification, setJustification] = useState('');
  const [participantes, setParticipantes] = useState<string[]>([]);
  const [isRecurring, setIsRecurring] = useState(false);
  const [bypassAgendaConflict, setBypassAgendaConflict] = useState(false);
  const [recurrenceRule, setRecurrenceRule] = useState<RecurrenceRule>({
    frequencia: 'SEMANAL',
    intervalo: 1,
    diasDaSemana: [],
  });

  const selectedYear = useMemo(() => {
    if (!startDate) return new Date().getFullYear();
    const d = new Date(startDate);
    return isNaN(d.getTime()) ? new Date().getFullYear() : d.getFullYear();
  }, [startDate]);

  const isClosedYear = useMemo(() => {
    if (!anosParoquiais || anosParoquiais.length === 0) return false;
    const anoConfig = anosParoquiais.find(a => a.ano === selectedYear);
    return anoConfig?.status === 'FECHADO';
  }, [anosParoquiais, selectedYear]);

  const isParoco = useMemo(() => {
    if (!user) return false;
    return user.roles?.some(r => r === 'PAROCO' || r === 'ROLE_CLERO_PAROCO' || r === 'ADMIN' || r === 'ROLE_ADMIN') || false;
  }, [user]);

  const churches = useMemo(() => {
    return organizations.filter(o => o.ativo && (o.tipo === 'MATRIZ' || o.tipo === 'COMUNIDADE'));
  }, [organizations]);

  const filteredOrganizations = useMemo(() => {
    if (!user) return [];
    if (isGlobalCreator(user)) return organizations;
    
    const userOrgIds = Array.isArray(user.organizations) ? user.organizations.map((org: { id: string }) => org.id) : [];
    return organizations.filter(org => userOrgIds.includes(org.id));
  }, [user, organizations]);

  const selectedProject = useMemo(() => {
    if (!projetoId) return null;
    return projetos.find(p => p.id === projetoId) || null;
  }, [projetoId, projetos]);

  const selectedProjectInfo = useMemo(() => {
    if (!selectedProject) return '';
    const startStr = selectedProject.inicio ? new Date(selectedProject.inicio).toLocaleDateString('pt-BR') : '';
    const endStr = selectedProject.fim ? new Date(selectedProject.fim).toLocaleDateString('pt-BR') : '';
    if (startStr && endStr) {
      return `${startStr} a ${endStr}`;
    }
    if (startStr) {
      return `A partir de ${startStr}`;
    }
    if (endStr) {
      return `Até ${endStr}`;
    }
    return 'Sem limite de datas';
  }, [selectedProject]);

  const projectValidationError = useMemo(() => {
    if (!selectedProject) return null;
    const projStart = selectedProject.inicio ? new Date(selectedProject.inicio) : null;
    const projEnd = selectedProject.fim ? new Date(selectedProject.fim) : null;
    
    if (!projStart && !projEnd) return null;

    if (!startDate) return null;
    const eventStart = new Date(startDate);

    if (projStart && eventStart < projStart) {
      return `A data de início do evento (${eventStart.toLocaleDateString('pt-BR')}) é anterior ao início do projeto (${projStart.toLocaleDateString('pt-BR')}).`;
    }

    if (isRecurring) {
      const { dataLimite, numeroOcorrencias } = recurrenceRule;
      
      if (!dataLimite && !numeroOcorrencias) {
        return 'Para eventos vinculados a projetos, a repetição deve possuir um limite de data ou número de ocorrências.';
      }

      if (projEnd) {
        if (dataLimite) {
          const limitDate = new Date(dataLimite + 'T23:59:59');
          if (limitDate > projEnd) {
            return `O limite da repetição (${limitDate.toLocaleDateString('pt-BR')}) excede o fim do projeto (${projEnd.toLocaleDateString('pt-BR')}).`;
          }
        }

        if (numeroOcorrencias) {
          const paddingLimit = new Date(projEnd);
          paddingLimit.setDate(paddingLimit.getDate() + 10);
          
          const occurrences = getRecurrenceDates(eventStart, recurrenceRule, paddingLimit);
          if (occurrences.length > 0) {
            const lastOccurrence = occurrences[occurrences.length - 1];
            if (lastOccurrence > projEnd) {
              return `A última ocorrência (${lastOccurrence.toLocaleDateString('pt-BR')}) excede o fim do projeto (${projEnd.toLocaleDateString('pt-BR')}).`;
            }
          }
        }
      }
    } else {
      if (!endDate) return null;
      const eventEnd = new Date(endDate);
      if (projEnd && eventEnd > projEnd) {
        return `A data de término do evento (${eventEnd.toLocaleDateString('pt-BR')}) excede o término do projeto (${projEnd.toLocaleDateString('pt-BR')}).`;
      }
    }

    return null;
  }, [selectedProject, startDate, endDate, isRecurring, recurrenceRule]);

  // Reset form or set initial values when modal opens or initialEvent changes
  useEffect(() => {
    if (open) {
      if (initialEvent) {
        setTitle(initialEvent.titulo || '');
        setDescription(initialEvent.descricao || '');
        setCategory(initialEvent.categoria || 'PASTORAL');
        setOrgId(initialEvent.organizacaoResponsavelId || '');
        setIgrejaVinculoId(initialEvent.igrejaVinculoId || '');
        setLocalId(initialEvent.localId || '');
        setProjetoId(initialEvent.projetoId || '');
        setStartDate(formatIsoToLocal(initialEvent.inicio));
        setEndDate(formatIsoToLocal(initialEvent.fim));
        setSelectedAtivoIds(initialEvent.ativoIds || []);

        if (initialEvent.fim) {
          const d = new Date(initialEvent.fim);
          setEndTime(`${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`);
        }

        setStatus(initialEvent.status || 'CONFIRMADO');
        setJustification(initialEvent.adicionadoExtraJustificativa || '');
        setParticipantes(initialEvent.participantes || []);
        setIsRecurring(!!initialEvent.recorrenciaId);
        
        if (initialEvent.recorrencia) {
          setRecurrenceRule(initialEvent.recorrencia as unknown as RecurrenceRule);
        }
      } else {
        setTitle('');
        setDescription('');
        setCategory('PASTORAL');
        setIgrejaVinculoId('');
        setLocalId('');
        setSelectedAtivoIds([]);
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

      if (!initialEvent) {
        if (filteredOrganizations.length > 0 && !orgId) {
          setOrgId(filteredOrganizations[0].id);
        }
        if (churches.length > 0 && !igrejaVinculoId) {
          setIgrejaVinculoId(churches[0].id);
        }
      }
    }
  }, [open, initialDate, initialEvent, filteredOrganizations, churches]);

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
      if (startDate && endDate && new Date(startDate) > new Date(endDate)) {
        showError('A data de início não pode ser posterior à data de término.');
        return;
      }
    }

    if (projectValidationError) {
      showError(projectValidationError);
      return;
    }

    if (isClosedYear && (!justification || !justification.trim())) {
      showError(`A justificativa é obrigatória pois o Ano Paroquial de ${selectedYear} está FECHADO.`);
      return;
    }

    if (status === 'ADICIONADO_EXTRA' && (!justification || !justification.trim())) {
      showError('A justificativa é obrigatória para eventos adicionados extraordinariamente.');
      return;
    }

    if (churches.length > 0 && !igrejaVinculoId) {
      showError('A comunidade/igreja de vínculo é obrigatória.');
      return;
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
      igrejaVinculoId: igrejaVinculoId || null,
      localId: localId || null,
      projetoId: projetoId || null,
      participantes,
      ativoIds: selectedAtivoIds,
      inicio: startDate ? new Date(startDate).toISOString() : '',
      fim: finalEndDate ? new Date(finalEndDate).toISOString() : '',
      status: isClosedYear && status !== 'RASCUNHO' ? 'ADICIONADO_EXTRA' : status,
      adicionadoExtraJustificativa: justification || null,
      recurrence: isRecurring ? recurrenceRule : null,
      bypassAgendaConflict: isParoco ? bypassAgendaConflict : false,
    });
  };

  return (
    <Dialog open={open} onClose={() => !isSubmitting && onClose()} maxWidth="md" fullWidth>
      <form onSubmit={handleFormSubmit} noValidate>
        <DialogTitle>
          {initialEvent ? 'Editar Evento' : 'Criar Novo Evento'}
        </DialogTitle>
        <DialogContent dividers>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5, mt: 1 }}>
            {isClosedYear && (
              <Alert severity="warning">
                <strong>Ano Paroquial Fechado ({selectedYear}):</strong> Este ano administrativo está travado para auditoria. Inclusões serão registradas como <strong>ADICIONADO_EXTRA</strong> (requer justificativa obrigatória abaixo).
              </Alert>
            )}
            <TextField
              label="Título do Evento"
              required
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

            <FormControl fullWidth size="small" required error={!igrejaVinculoId}>
              <InputLabel id="new-igreja-vinculo-label">Comunidade / Igreja de Atuação</InputLabel>
              <Select
                labelId="new-igreja-vinculo-label"
                value={igrejaVinculoId}
                label="Comunidade / Igreja de Atuação"
                onChange={(e) => setIgrejaVinculoId(e.target.value)}
                disabled={isSubmitting}
                data-testid="event-church-select"
              >
                <MenuItem value="" disabled>
                  <em>Selecione a Comunidade / Igreja</em>
                </MenuItem>
                {churches.map((ch) => (
                  <MenuItem key={ch.id} value={ch.id}>
                    {ch.nome}
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
                <FormControl fullWidth size="small" error={!!projectValidationError}>
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
                  {projectValidationError ? (
                    <FormHelperText error>
                      {projectValidationError}
                    </FormHelperText>
                  ) : selectedProjectInfo ? (
                    <FormHelperText>
                      Período: {selectedProjectInfo}
                    </FormHelperText>
                  ) : null}
                </FormControl>
              </Grid>
            </Grid>

            {/* Ativos Patrimoniais — apenas exibido quando há ativos reserváveis disponíveis */}
            {ativos.length > 0 && (
              <FormControl fullWidth size="small">
                <InputLabel id="new-ativos-label">Bens / Ativos Patrimoniais</InputLabel>
                <Select
                  labelId="new-ativos-label"
                  multiple
                  value={selectedAtivoIds}
                  label="Bens / Ativos Patrimoniais"
                  onChange={(e) =>
                    setSelectedAtivoIds(typeof e.target.value === 'string' ? e.target.value.split(',') : e.target.value)
                  }
                  disabled={isSubmitting}
                  renderValue={(selected) => (
                    <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                      {(selected as string[]).map((id) => {
                        const ativo = ativos.find(a => a.id === id);
                        return (
                          <Chip
                            key={id}
                            label={ativo ? `${ativo.nome} (${ativo.categoria})` : id}
                            size="small"
                          />
                        );
                      })}
                    </Box>
                  )}
                >
                  {ativos
                    .filter(a => a.reservavel && a.status === 'DISPONIVEL')
                    .map((a) => (
                      <MenuItem key={a.id} value={a.id}>
                        {a.nome} — {a.categoria}
                      </MenuItem>
                    ))}
                </Select>
              </FormControl>
            )}

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
                  required
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
                    required
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
                    required
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
                  label="Evento Recorrente"
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

                {isParoco && (
                  <Box sx={{ mt: 1, pt: 1, borderTop: '1px dashed', borderColor: 'divider' }}>
                    <FormControlLabel
                      control={
                        <Switch
                          checked={bypassAgendaConflict}
                          onChange={(e) => setBypassAgendaConflict(e.target.checked)}
                          color="warning"
                          disabled={isSubmitting}
                        />
                      }
                      label="Ignorar Conflitos de Agenda Eclesial (Bypass Pároco)"
                    />
                  </Box>
                )}
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
        <DialogActions>
          <ActionButton
            title="Cancelar"
            icon={<CloseIcon />}
            onClick={onClose}
            disabled={isSubmitting}
            color="secondary"
          />
          <ActionButton
            title={initialEvent ? 'Salvar Alterações' : 'Salvar Evento'}
            icon={<SaveIcon />}
            type="submit"
            color="primary"
            disabled={isSubmitting || !!projectValidationError}
          />
        </DialogActions>
      </form>
    </Dialog>
  );
};

export default CadastroEventoModal;
