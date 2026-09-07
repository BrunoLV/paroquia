import React, { useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  InputLabel,
  Skeleton,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TablePagination,
  TableRow,
  TextField,
  Typography,
} from '@mui/material';
import { ArrowBack as ArrowBackIcon, Block as BlockIcon, Close as CloseIcon, WarningAmber as WarningAmberIcon } from '@mui/icons-material';
import { cancelarAlocacao, getAlocacoesPorAtivo } from '../api/ativos';
import { AtivoAlocacaoResponse } from '../api/types';
import { useNotification } from '../context/NotificationContext';
import { extractErrorMessage } from '../utils/errorUtils';
import StatusIcon from './StatusIcon';
import ActionButton from './ActionButton';
import TableActionMenu from './TableActionMenu';
import { getAtivoAlocacaoStatusMeta, getAtivoAlocacaoTipoMeta } from '../utils/statusMeta/ativoAlocacaoStatus';
import { formatDateBr } from '../utils/masks';

interface AtivoAlocacoesModalProps {
  ativoId: string | null;
  ativoNome?: string;
  onClose: () => void;
  onChanged?: () => void;
}

const isCancelavel = (alocacao: AtivoAlocacaoResponse): boolean => {
  if (alocacao.status !== 'PENDENTE_APROVACAO' && alocacao.status !== 'CONFIRMADA') {
    return false;
  }
  return new Date(alocacao.dataInicio) > new Date();
};

const AtivoAlocacoesModal: React.FC<AtivoAlocacoesModalProps> = ({ ativoId, ativoNome, onClose, onChanged }) => {
  const { showError, showSuccess } = useNotification();
  const [alocacoes, setAlocacoes] = useState<AtivoAlocacaoResponse[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [cancelandoAlocacao, setCancelandoAlocacao] = useState<AtivoAlocacaoResponse | null>(null);
  const [motivoCancelamento, setMotivoCancelamento] = useState('');
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);

  const loadAlocacoes = async (id: string) => {
    setIsLoading(true);
    try {
      const data = await getAlocacoesPorAtivo(id);
      setAlocacoes(data || []);
    } catch (err) {
      showError('Não foi possível carregar o histórico de alocações.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    if (ativoId) {
      loadAlocacoes(ativoId);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [ativoId]);

  const handleConfirmCancelamento = async () => {
    if (!cancelandoAlocacao) return;
    if (motivoCancelamento.trim().length < 10) {
      showError('O motivo do cancelamento deve ter pelo menos 10 caracteres.');
      return;
    }
    try {
      await cancelarAlocacao(cancelandoAlocacao.id, { motivoCancelamento });
      showSuccess('Reserva cancelada com sucesso!');
      setCancelandoAlocacao(null);
      setMotivoCancelamento('');
      if (ativoId) {
        loadAlocacoes(ativoId);
      }
      onChanged?.();
    } catch (err: unknown) {
      showError(extractErrorMessage(err, 'Erro ao cancelar a reserva.'));
    }
  };

  return (
    <>
      <Dialog open={!!ativoId} onClose={onClose} maxWidth="md" fullWidth>
        <DialogTitle>Alocações do Ativo{ativoNome ? ` — ${ativoNome}` : ''}</DialogTitle>
        <DialogContent dividers>
          {isLoading ? (
            <Box>
              <Skeleton variant="rounded" height={40} sx={{ mb: 1 }} />
              <Skeleton variant="rounded" height={40} sx={{ mb: 1 }} />
              <Skeleton variant="rounded" height={40} />
            </Box>
          ) : alocacoes.length === 0 ? (
            <Typography variant="body2" color="text.secondary">
              Nenhuma alocação ou manutenção registrada para este ativo.
            </Typography>
          ) : (
            <TableContainer>
              <Table size="small">
                <TableHead sx={{ backgroundColor: 'action.hover' }}>
                  <TableRow>
                    <TableCell>Tipo</TableCell>
                    <TableCell>Período</TableCell>
                    <TableCell>Status</TableCell>
                    <TableCell align="right">Ações</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {alocacoes
                    .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
                    .map((alocacao) => (
                      <TableRow key={alocacao.id} hover>
                        <TableCell><StatusIcon {...getAtivoAlocacaoTipoMeta(alocacao.tipo)} /></TableCell>
                        <TableCell>{formatDateBr(alocacao.dataInicio)} — {formatDateBr(alocacao.dataFim)}</TableCell>
                        <TableCell>
                          <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.75 }}>
                            <StatusIcon {...getAtivoAlocacaoStatusMeta(alocacao.status)} />
                            {alocacao.impactadaPorAtraso && (
                              <Chip label="Pode atrasar" color="warning" icon={<WarningAmberIcon />} size="small" variant="outlined" />
                            )}
                          </Box>
                        </TableCell>
                        <TableCell align="right">
                          {isCancelavel(alocacao) && (
                            <TableActionMenu actions={[{ label: 'Cancelar reserva', icon: <BlockIcon fontSize="small" />, onClick: () => setCancelandoAlocacao(alocacao) }]} />
                          )}
                        </TableCell>
                      </TableRow>
                    ))}
                </TableBody>
              </Table>
              <TablePagination
                rowsPerPageOptions={[5, 10, 25]}
                component="div"
                count={alocacoes.length}
                rowsPerPage={rowsPerPage}
                page={page}
                onPageChange={(_, newPage) => setPage(newPage)}
                onRowsPerPageChange={(e) => {
                  setRowsPerPage(parseInt(e.target.value, 10));
                  setPage(0);
                }}
                labelRowsPerPage="Linhas por página:"
                labelDisplayedRows={({ from, to, count }) => `${from}–${to} de ${count !== -1 ? count : `mais de ${to}`}`}
              />
            </TableContainer>
          )}
        </DialogContent>
        <DialogActions>
          <ActionButton title="Fechar histórico de alocações" icon={<CloseIcon />} color="secondary" onClick={onClose} />
        </DialogActions>
      </Dialog>

      <Dialog open={!!cancelandoAlocacao} onClose={() => setCancelandoAlocacao(null)} maxWidth="xs" fullWidth>
        <DialogTitle>Cancelar Reserva</DialogTitle>
        <DialogContent dividers>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5, mt: 1 }}>
            <Alert severity="warning">
              Esta ação cancela a reserva/pedido de empréstimo antes do início do período. Não é possível desfazer.
            </Alert>
            <TextField
              label="Motivo do Cancelamento"
              multiline
              rows={3}
              value={motivoCancelamento}
              onChange={(e) => setMotivoCancelamento(e.target.value)}
              fullWidth
              required
              size="small"
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <ActionButton title="Voltar ao histórico" icon={<ArrowBackIcon />} color="secondary" onClick={() => setCancelandoAlocacao(null)} />
          <ActionButton title="Confirmar cancelamento da reserva" icon={<BlockIcon />} color="error" onClick={handleConfirmCancelamento} />
        </DialogActions>
      </Dialog>
    </>
  );
};

export default AtivoAlocacoesModal;
