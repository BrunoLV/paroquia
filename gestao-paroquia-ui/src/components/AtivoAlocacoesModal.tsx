import React, { useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Skeleton,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material';
import { cancelarAlocacao, getAlocacoesPorAtivo } from '../api/ativos';
import { AtivoAlocacaoResponse } from '../api/types';
import { useNotification } from '../context/NotificationContext';

interface AtivoAlocacoesModalProps {
  ativoId: string | null;
  ativoNome?: string;
  onClose: () => void;
  onChanged?: () => void;
}

const TIPO_LABELS: Record<string, string> = {
  ALOCACAO_EVENTO: 'Alocação para Evento',
  MANUTENCAO: 'Manutenção',
};

const STATUS_LABELS: Record<string, string> = {
  PENDENTE_APROVACAO: 'Pendente de Aprovação',
  CONFIRMADA: 'Confirmada',
  ATRASADA: 'Atrasada',
  DEVOLVIDA: 'Devolvida',
  CANCELADA: 'Cancelada',
};

const STATUS_COLORS: Record<string, 'success' | 'warning' | 'default' | 'info' | 'error'> = {
  PENDENTE_APROVACAO: 'warning',
  CONFIRMADA: 'info',
  ATRASADA: 'error',
  DEVOLVIDA: 'default',
  CANCELADA: 'default',
};

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

  const loadAlocacoes = async (id: string) => {
    setIsLoading(true);
    try {
      const data = await getAlocacoesPorAtivo(id);
      setAlocacoes(data || []);
    } catch (err) {
      console.error('Erro ao buscar alocações do ativo:', err);
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
    } catch (err: any) {
      showError(err.response?.data?.message || 'Erro ao cancelar a reserva.');
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
                <TableHead>
                  <TableRow>
                    <TableCell>Tipo</TableCell>
                    <TableCell>Período</TableCell>
                    <TableCell>Status</TableCell>
                    <TableCell align="right">Ações</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {alocacoes.map((alocacao) => (
                    <TableRow key={alocacao.id}>
                      <TableCell>{TIPO_LABELS[alocacao.tipo] || alocacao.tipo}</TableCell>
                      <TableCell>{alocacao.dataInicio} — {alocacao.dataFim}</TableCell>
                      <TableCell>
                        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.75 }}>
                          <Chip
                            label={STATUS_LABELS[alocacao.status] || alocacao.status}
                            color={STATUS_COLORS[alocacao.status] || 'default'}
                            size="small"
                          />
                          {alocacao.impactadaPorAtraso && (
                            <Chip
                              label="Pode atrasar"
                              color="warning"
                              size="small"
                              variant="outlined"
                            />
                          )}
                        </Box>
                      </TableCell>
                      <TableCell align="right">
                        {isCancelavel(alocacao) && (
                          <Button size="small" color="error" onClick={() => setCancelandoAlocacao(alocacao)}>
                            Cancelar Reserva
                          </Button>
                        )}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={onClose} color="secondary">Fechar</Button>
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
          <Button onClick={() => setCancelandoAlocacao(null)} color="secondary">Voltar</Button>
          <Button variant="contained" color="error" onClick={handleConfirmCancelamento}>
            Confirmar Cancelamento
          </Button>
        </DialogActions>
      </Dialog>
    </>
  );
};

export default AtivoAlocacoesModal;
