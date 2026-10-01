import React, { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  Box,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  InputLabel,
  MenuItem,
  Paper,
  Select,
  Skeleton,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import CloseIcon from '@mui/icons-material/Close';
import DeleteIcon from '@mui/icons-material/Delete';
import { DIALOG_CONTENT_SCROLL_SX } from '@/shared/presentation/theme/dialogStyles';
import { getOrganizacaoMembros } from '@/contextos/organizacao/infrastructure/http/organizacoes';
import { adicionarVinculo, AdicionarVinculoRequest, removerVinculo, Usuario } from '@/contextos/iam/infrastructure/http/usuarios';
import { Organizacao, PAPEL_ECLESIAL_LABELS } from '@/shared/domain/types/types';
import { useNotification } from '@/shared/presentation/context/NotificationContext';
import { extractErrorMessage } from '@/shared/utils/errorUtils';
import ActionButton from '@/shared/presentation/components/ActionButton';
import StatusIcon from '@/shared/presentation/components/StatusIcon';
import TableActionMenu from '@/shared/presentation/components/TableActionMenu';
import { getSituacaoAtivoMeta } from '@/shared/utils/statusMeta/situacaoStatus';

interface OrganizacaoMembrosModalProps {
  open: boolean;
  onClose: () => void;
  organizacao: Organizacao | null;
  igrejas: Organizacao[];
  usuarios: Usuario[];
}

export const OrganizacaoMembrosModal: React.FC<OrganizacaoMembrosModalProps> = ({
  open,
  onClose,
  organizacao,
  igrejas,
  usuarios,
}) => {
  const { showError, showSuccess } = useNotification();
  const queryClient = useQueryClient();

  const [novoUsuarioId, setNovoUsuarioId] = useState('');
  const [novoPapel, setNovoPapel] = useState('');
  const [novaIgrejaId, setNovaIgrejaId] = useState('');

  const { data: membros = [], isLoading } = useQuery({
    queryKey: ['organizacaoMembros', organizacao?.id],
    queryFn: () => getOrganizacaoMembros(organizacao!.id),
    enabled: open && !!organizacao,
  });

  const addMembroMutation = useMutation({
    mutationFn: ({ userId, request }: { userId: string; request: AdicionarVinculoRequest }) =>
      adicionarVinculo(userId, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['organizacaoMembros', organizacao?.id] });
      queryClient.invalidateQueries({ queryKey: ['usuarios'] });
      queryClient.invalidateQueries({ queryKey: ['organizacoes'] });
      showSuccess('Membro vinculado com sucesso!');
      setNovoUsuarioId('');
      setNovoPapel('');
      setNovaIgrejaId('');
    },
    onError: (err) => showError(extractErrorMessage(err, 'Erro ao vincular membro.')),
  });

  const removeMembroMutation = useMutation({
    mutationFn: ({ userId, vinculoId }: { userId: string; vinculoId: string }) =>
      removerVinculo(userId, vinculoId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['organizacaoMembros', organizacao?.id] });
      queryClient.invalidateQueries({ queryKey: ['usuarios'] });
      queryClient.invalidateQueries({ queryKey: ['organizacoes'] });
      showSuccess('Membro removido com sucesso!');
    },
    onError: (err) => showError(extractErrorMessage(err, 'Erro ao remover membro.')),
  });

  const isPastoralOuMovimento = organizacao?.tipo === 'PASTORAL' || organizacao?.tipo === 'MOVIMENTO';

  const handleAddMembro = () => {
    if (!novoUsuarioId || !novoPapel || !organizacao) return;
    if (isPastoralOuMovimento && !novaIgrejaId) {
      showError('A igreja de atuação é obrigatória para pastorais e movimentos');
      return;
    }
    addMembroMutation.mutate({
      userId: novoUsuarioId,
      request: {
        grupoParoquialId: organizacao.id,
        organizacaoId: organizacao.id,
        tipo: organizacao.tipo,
        papel: novoPapel,
        igrejaVinculoId: isPastoralOuMovimento ? novaIgrejaId : undefined,
      },
    });
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>Membros - {organizacao?.nome}</DialogTitle>
      <DialogContent dividers sx={DIALOG_CONTENT_SCROLL_SX}>
        {isLoading ? (
          <Box sx={{ py: 2 }}>
            <Skeleton variant="text" width={200} height={28} sx={{ mb: 1.5 }} />
            {[1, 2, 3].map((i) => (
              <Skeleton key={i} variant="rectangular" height={36} sx={{ mb: 1 }} />
            ))}
          </Box>
        ) : (
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
            <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
              Membros Vinculados:
            </Typography>
            <TableContainer component={Paper} variant="outlined">
              <Table size="small">
                <TableHead sx={{ bgcolor: 'action.hover' }}>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 600 }}>Nome do Membro</TableCell>
                    {isPastoralOuMovimento && (
                      <TableCell sx={{ fontWeight: 600 }}>Igreja de Atuação</TableCell>
                    )}
                    <TableCell sx={{ fontWeight: 600 }}>Papel</TableCell>
                    <TableCell sx={{ fontWeight: 600 }}>Status</TableCell>
                    <TableCell align="right" sx={{ fontWeight: 600 }}>Ações</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {membros.map((m) => (
                    <TableRow key={m.vinculoId} hover>
                      <TableCell sx={{ fontWeight: 500 }}>{m.usuarioNome || m.nomeCompleto || m.fielNome || '-'}</TableCell>
                      {isPastoralOuMovimento && (
                        <TableCell>{igrejas.find((o) => o.id === m.igrejaVinculoId)?.nome || '-'}</TableCell>
                      )}
                      <TableCell>{PAPEL_ECLESIAL_LABELS[m.papel] || m.papel}</TableCell>
                      <TableCell><StatusIcon {...getSituacaoAtivoMeta(m.ativo)} /></TableCell>
                      <TableCell align="right">
                        <TableActionMenu
                          actions={[
                            {
                              label: 'Remover',
                              icon: <DeleteIcon fontSize="small" />,
                              onClick: () => removeMembroMutation.mutate({ userId: m.usuarioId || m.fielId || '', vinculoId: m.vinculoId }),
                              disabled: removeMembroMutation.isPending,
                            },
                          ]}
                        />
                      </TableCell>
                    </TableRow>
                  ))}
                  {membros.length === 0 && (
                    <TableRow>
                      <TableCell colSpan={isPastoralOuMovimento ? 5 : 4} align="center" sx={{ py: 2, color: 'text.secondary' }}>
                        Nenhum membro vinculado a esta pastoral/movimento.
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </TableContainer>

            <Box sx={{ borderBottom: 1, borderColor: 'divider', my: 1 }} />

            <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
              Vincular Novo Membro:
            </Typography>
            <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', alignItems: 'center' }}>
              <FormControl sx={{ minWidth: 240 }} size="small">
                <InputLabel id="add-member-user-label">Usuário</InputLabel>
                <Select
                  labelId="add-member-user-label"
                  value={novoUsuarioId}
                  label="Usuário"
                  onChange={(e) => setNovoUsuarioId(e.target.value)}
                >
                  {usuarios.map((u) => (
                    <MenuItem key={u.id} value={u.id}>
                      {u.nome} ({u.username})
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>

              <FormControl sx={{ minWidth: 200 }} size="small">
                <InputLabel id="add-member-role-label">Papel</InputLabel>
                <Select
                  labelId="add-member-role-label"
                  value={novoPapel}
                  label="Papel"
                  onChange={(e) => setNovoPapel(e.target.value)}
                >
                  <MenuItem value="COORDENADOR">Coordenador de Grupo/Pastoral</MenuItem>
                  <MenuItem value="SECRETARIO_GRUPO">Secretário de Grupo/Pastoral</MenuItem>
                  <MenuItem value="MEMBRO_ATIVO">Membro Ativo</MenuItem>
                  <MenuItem value="VOLUNTARIO">Voluntário</MenuItem>
                  <MenuItem value="SECRETARIO_CONSELHO">Secretário de Conselho</MenuItem>
                  <MenuItem value="MEMBRO_CONSELHO_PASTORAL">Membro do Conselho Pastoral (CPP/CPC)</MenuItem>
                  <MenuItem value="MEMBRO_CONSELHO_ECONOMICO">Membro do Conselho Econômico (CAE)</MenuItem>
                </Select>
              </FormControl>

              {isPastoralOuMovimento && (
                <FormControl sx={{ minWidth: 200 }} size="small">
                  <InputLabel id="add-member-church-label">Igreja de Atuação</InputLabel>
                  <Select
                    labelId="add-member-church-label"
                    value={novaIgrejaId}
                    label="Igreja de Atuação"
                    onChange={(e) => setNovaIgrejaId(e.target.value)}
                    inputProps={{ 'data-testid': 'add-member-church-select' }}
                  >
                    {igrejas.map((o) => (
                      <MenuItem key={o.id} value={o.id}>
                        {o.nome}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              )}

              <ActionButton
                title="Adicionar Vínculo"
                icon={addMembroMutation.isPending ? <CircularProgress size={20} /> : <AddIcon />}
                color="primary"
                onClick={handleAddMembro}
                disabled={!novoUsuarioId || !novoPapel || (isPastoralOuMovimento && !novaIgrejaId) || addMembroMutation.isPending}
              />
            </Box>
          </Box>
        )}
      </DialogContent>
      <DialogActions>
        <ActionButton title="Fechar" icon={<CloseIcon />} color="secondary" onClick={onClose} />
      </DialogActions>
    </Dialog>
  );
};
