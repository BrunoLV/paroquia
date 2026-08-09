import React, {useEffect, useState} from 'react';
import {
    Avatar,
    Box,
    CircularProgress,
    Dialog,
    DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle,
    Divider,
    Paper,
    Stack,
    TextField,
    Typography,
} from '@mui/material';
import {
    Check as CheckIcon,
    Close as CloseIcon,
    Delete as DeleteIcon,
    Edit as EditIcon,
    Send as SendIcon,
} from '@mui/icons-material';
import {useAuth} from '../auth/AuthContext';
import {useNotification} from '../context/NotificationContext';
import {extractErrorMessage} from '../utils/errorUtils';
import StatusIcon from './StatusIcon';
import {getObservacaoTipoMeta} from '../utils/statusMeta/observacaoTipo';
import {Observacao} from '../api/types';
import {createObservacao, deleteObservacao as deleteObsApi, getObservacoes, updateObservacao} from '../api/observacoes';
import ActionButton from './ActionButton';

interface ObservacoesEventoProps {
  eventoId: string;
}

const ObservacoesEvento = ({ eventoId }: ObservacoesEventoProps) => {
  const { user } = useAuth();
  const { showError, showSuccess } = useNotification();

  const [observations, setObservations] = useState<Observacao[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [newNote, setNewNote] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Editing states
  const [editingId, setEditingId] = useState<string | null>(null);
  const [editText, setEditText] = useState('');
  const [isUpdating, setIsUpdating] = useState(false);

  // Deleting states
  const [deleteConfirmId, setDeleteConfirmId] = useState<string | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  const fetchObservations = async () => {
    setIsLoading(true);
    try {
      const data = await getObservacoes(eventoId);
      // Sort observations by creation date ascending
      const sorted = [...data].sort(
        (a, b) => new Date(a.criadoEmUtc).getTime() - new Date(b.criadoEmUtc).getTime()
      );
      setObservations(sorted);
    } catch (err: unknown) {
      console.error('Error fetching event observations:', err);
      showError('Não foi possível carregar as observações do evento.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    if (eventoId) {
      fetchObservations();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [eventoId]);

  const handleAddNote = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newNote.trim()) return;

    setIsSubmitting(true);
    try {
      await createObservacao(eventoId, newNote.trim());
      setNewNote('');
      showSuccess('Observação adicionada com sucesso.');
      await fetchObservations();
    } catch (err: unknown) {
      showError(extractErrorMessage(err, 'Erro ao adicionar observação.'));
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleStartEdit = (obs: Observacao) => {
    setEditingId(obs.id);
    setEditText(obs.conteudo);
  };

  const handleCancelEdit = () => {
    setEditingId(null);
    setEditText('');
  };

  const handleUpdateNote = async (obsId: string) => {
    if (!editText.trim()) return;

    setIsUpdating(true);
    try {
      await updateObservacao(eventoId, obsId, editText.trim());
      setEditingId(null);
      setEditText('');
      showSuccess('Observação atualizada com sucesso.');
      await fetchObservations();
    } catch (err: unknown) {
      showError(extractErrorMessage(err, 'Erro ao atualizar observação.'));
    } finally {
      setIsUpdating(false);
    }
  };

  const handleDeleteNote = async () => {
    if (!deleteConfirmId) return;

    setIsDeleting(true);
    try {
      await deleteObsApi(eventoId, deleteConfirmId);
      setDeleteConfirmId(null);
      showSuccess('Observação excluída com sucesso.');
      await fetchObservations();
    } catch (err: unknown) {
      showError(extractErrorMessage(err, 'Erro ao excluir observação.'));
    } finally {
      setIsDeleting(false);
    }
  };

  const isAuthor = (obs: Observacao) => {
    return user && obs.usuarioId === user.id;
  };

  if (isLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
        <CircularProgress size={30} />
      </Box>
    );
  }

  return (
    <Box sx={{ mt: 3, borderTop: '1px solid #eee', pt: 3 }}>
      <Typography variant="h6" sx={{ fontWeight: 600, color: '#37474f', mb: 2 }}>
        Observações e Notas ({observations.length})
      </Typography>

      <Stack spacing={2} sx={{ maxWaitHeight: '300px', overflowY: 'auto', mb: 3, pr: 1 }}>
        {observations.length === 0 ? (
          <Typography variant="body2" color="text.secondary" sx={{ fontStyle: 'italic', textAlign: 'center', py: 2 }}>
            Nenhuma observação registrada para este evento.
          </Typography>
        ) : (
          observations.map((obs) => {
            const isNote = obs.tipo === 'NOTA';
            const isOwn = isAuthor(obs);

            return (
              <Paper
                key={obs.id}
                variant="outlined"
                sx={{
                  p: 1.5,
                  
                  bgcolor: isNote ? '#ffffff' : '#fcfcfc',
                  borderColor: isNote ? '#e0e0e0' : '#e0f2f1',
                  borderLeft: isNote ? '3px solid #1976d2' : '3px solid #009688',
                }}
              >
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
                  <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
                    <Avatar sx={{ width: 24, height: 24, fontSize: '0.75rem', bgcolor: isNote ? (isOwn ? 'primary.main' : 'grey.600') : 'teal.600' }}>
                      {isNote ? (isOwn ? 'V' : 'U') : 'S'}
                    </Avatar>
                    <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
                      {isNote ? (isOwn ? 'Você' : 'Usuário') : 'Sistema'}
                    </Typography>
                    {!isNote && (
                      <StatusIcon {...getObservacaoTipoMeta(obs.tipo)} sx={{ fontSize: '0.65rem', height: 18, fontWeight: 600 }} />
                    )}
                  </Stack>
                  <Typography variant="caption" color="text.secondary">
                    {new Date(obs.criadoEmUtc).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' })}
                  </Typography>
                </Box>

                {editingId === obs.id ? (
                  <Box sx={{ mt: 1 }}>
                    <TextField
                      fullWidth
                      multiline
                      rows={2}
                      size="small"
                      value={editText}
                      onChange={(e) => setEditText(e.target.value)}
                      disabled={isUpdating}
                    />
                    <Stack direction="row" spacing={1} sx={{ mt: 1, justifyContent: 'flex-end' }}>
                      <ActionButton
                        title="Cancelar edição"
                        icon={<CloseIcon fontSize="small" />}
                        size="small"
                        onClick={handleCancelEdit}
                        disabled={isUpdating}
                        color="error"
                      />
                      <ActionButton
                        title="Salvar alteração"
                        icon={isUpdating ? <CircularProgress size={16} /> : <CheckIcon fontSize="small" />}
                        size="small"
                        onClick={() => handleUpdateNote(obs.id)}
                        disabled={isUpdating || !editText.trim()}
                        color="success"
                      />
                    </Stack>
                  </Box>
                ) : (
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                    <Typography variant="body2" sx={{ color: '#37474f', whiteSpace: 'pre-wrap' }}>
                      {obs.conteudo}
                    </Typography>
                    {isNote && isOwn && (
                      <Stack direction="row" spacing={0.5}>
                        <ActionButton
                          title="Editar Observação"
                          ariaLabel="edit"
                          icon={<EditIcon fontSize="small" sx={{ fontSize: '1rem' }} />}
                          size="small"
                          color="secondary"
                          onClick={() => handleStartEdit(obs)}
                        />
                        <ActionButton
                          title="Excluir Observação"
                          ariaLabel="delete"
                          icon={<DeleteIcon fontSize="small" sx={{ fontSize: '1rem' }} />}
                          size="small"
                          color="error"
                          onClick={() => setDeleteConfirmId(obs.id)}
                        />
                      </Stack>
                    )}
                  </Box>
                )}
              </Paper>
            );
          })
        )}
      </Stack>

      <Divider sx={{ my: 2 }} />

      {/* Add note form */}
      <Box component="form" onSubmit={handleAddNote}>
        <Stack direction="row" spacing={1}>
          <TextField
            fullWidth
            size="small"
            placeholder="Adicione uma nota..."
            value={newNote}
            onChange={(e) => setNewNote(e.target.value)}
            disabled={isSubmitting}
            autoComplete="off"
          />
          <ActionButton
            title="Adicionar Observação"
            icon={isSubmitting ? <CircularProgress size={20} color="inherit" /> : <SendIcon />}
            type="submit"
            color="primary"
            disabled={isSubmitting || !newNote.trim()}
          />
        </Stack>
      </Box>

      {/* Delete confirmation dialog */}
      <Dialog open={!!deleteConfirmId} onClose={() => setDeleteConfirmId(null)}>
        <DialogTitle>Excluir Observação?</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Tem certeza que deseja excluir permanentemente esta observação? Esta ação não pode ser desfeita.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <ActionButton title="Cancelar" icon={<CloseIcon />} onClick={() => setDeleteConfirmId(null)} color="secondary" disabled={isDeleting} />
          <ActionButton
            title="Excluir Observação"
            icon={isDeleting ? <CircularProgress size={20} color="inherit" /> : <DeleteIcon />}
            onClick={handleDeleteNote}
            color="error"
            disabled={isDeleting}
          />
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default ObservacoesEvento;
