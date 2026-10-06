import SearchableTablePagination from '@/shared/presentation/components/SearchableTablePagination';
import React from 'react';
import { Box, Chip, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Typography } from '@mui/material';
import TableActionMenu, { type TableAction } from '@/shared/presentation/components/TableActionMenu';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import HighlightOffIcon from '@mui/icons-material/HighlightOff';
import OutboxIcon from '@mui/icons-material/Outbox';
import AssignmentReturnIcon from '@mui/icons-material/AssignmentReturn';
import BlockIcon from '@mui/icons-material/Block';
import VisibilityIcon from '@mui/icons-material/Visibility';
import MeetingRoomIcon from '@mui/icons-material/MeetingRoom';
import BuildIcon from '@mui/icons-material/Build';
import { ReservaDetalhe } from '@/shared/domain/types/types';
import { getReservaStatusMeta } from '@/shared/utils/statusMeta/reservaStatus';
import StatusIcon from '@/shared/presentation/components/StatusIcon';

const identificarTurno = (inicioIso?: string, fimIso?: string, descricao?: string): string | null => {
  if (descricao) {
    if (descricao.includes('(Manhã)')) return 'Manhã';
    if (descricao.includes('(Tarde)')) return 'Tarde';
    if (descricao.includes('(Noite)')) return 'Noite';
    if (descricao.includes('(Madrugada)')) return 'Madrugada';
    if (descricao.includes('(Dia Inteiro)')) return 'Dia Inteiro';
  }
  if (!inicioIso || !fimIso) return null;
  try {
    const dInicio = new Date(inicioIso);
    const dFim = new Date(fimIso);
    const horaInicio = dInicio.getHours();
    const horaFim = dFim.getHours();
    const minFim = dFim.getMinutes();

    if (horaInicio === 6 && (horaFim === 11 || (horaFim === 12 && minFim === 0))) return 'Manhã';
    if (horaInicio === 12 && (horaFim === 17 || (horaFim === 18 && minFim === 0))) return 'Tarde';
    if (horaInicio === 18 && (horaFim === 23 || (horaFim === 0 && minFim === 0))) return 'Noite';
    if (horaInicio === 0 && (horaFim === 5 || (horaFim === 6 && minFim === 0))) return 'Madrugada';
    if (horaInicio === 6 && horaFim === 23 && minFim >= 59) return 'Dia Inteiro';
    return null;
  } catch {
    return null;
  }
};

interface ReservasTableProps {
  reservas: ReservaDetalhe[];
  podeAprovar: boolean;
  onVisualizar: (reserva: ReservaDetalhe) => void;
  onAprovar: (reserva: ReservaDetalhe) => void;
  onRejeitar: (reserva: ReservaDetalhe) => void;
  onCancelar: (reserva: ReservaDetalhe) => void;
  onRetirar: (reserva: ReservaDetalhe) => void;
  onDevolver: (reserva: ReservaDetalhe) => void;
  page: number;
  rowsPerPage: number;
  onPageChange: (newPage: number) => void;
  onRowsPerPageChange: (newRows: number) => void;
}

export const ReservasTable: React.FC<ReservasTableProps> = ({
  reservas,
  podeAprovar,
  onVisualizar,
  onAprovar,
  onRejeitar,
  onCancelar,
  onRetirar,
  onDevolver,
  page,
  rowsPerPage,
  onPageChange,
  onRowsPerPageChange,
}) => {
  /** O menu comum preserva as ações disponíveis por estado e permissão de aprovação. */
  const rowActions = (reserva: ReservaDetalhe): TableAction[] => [
    { label: 'Ver detalhes', icon: <VisibilityIcon fontSize="small" />, onClick: () => onVisualizar(reserva) },
    ...(reserva.status === 'PENDENTE' && podeAprovar ? [
      { label: 'Aprovar Reserva', icon: <CheckCircleIcon fontSize="small" color="success" />, onClick: () => onAprovar(reserva) },
      { label: 'Rejeitar Solicitação', icon: <HighlightOffIcon fontSize="small" color="error" />, onClick: () => onRejeitar(reserva) },
    ] : []),
    ...(reserva.status === 'CONFIRMADA' && reserva.tipoItem === 'ATIVO' ? [
      { label: 'Registrar Saída (Retirada)', icon: <OutboxIcon fontSize="small" color="info" />, onClick: () => onRetirar(reserva) },
    ] : []),
    ...(reserva.status === 'RETIRADO' && reserva.tipoItem === 'ATIVO' ? [
      { label: 'Registrar Devolução', icon: <AssignmentReturnIcon fontSize="small" color="primary" />, onClick: () => onDevolver(reserva) },
    ] : []),
    ...(['PENDENTE', 'CONFIRMADA'].includes(reserva.status) ? [
      { label: 'Cancelar Reserva', icon: <BlockIcon fontSize="small" color="error" />, onClick: () => onCancelar(reserva) },
    ] : []),
  ];

  const formatarDataHora = (iso: string) => {
    try {
      const d = new Date(iso);
      return d.toLocaleString('pt-BR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    } catch {
      return iso;
    }
  };

  const paginated = reservas.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage);

  return (
    <>
      <TableContainer component={Paper}>
        <Table size="small">
          <TableHead sx={{ backgroundColor: 'action.hover' }}>
            <TableRow>
              <TableCell sx={{ fontWeight: 600 }}>Tipo / Recurso</TableCell>
              <TableCell sx={{ fontWeight: 600 }}>Período & Buffer</TableCell>
              <TableCell sx={{ fontWeight: 600 }}>Finalidade</TableCell>
              <TableCell sx={{ fontWeight: 600 }}>Solicitante & Pastoral</TableCell>
              <TableCell sx={{ fontWeight: 600 }}>Situação</TableCell>
              <TableCell align="right" sx={{ fontWeight: 600 }}>Ações</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {paginated.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} align="center" sx={{ py: 3, color: 'text.secondary' }}>
                  Nenhuma reserva encontrada.
                </TableCell>
              </TableRow>
            ) : (
              paginated.map((r) => {
                const statusMeta = getReservaStatusMeta(r.status);
                return (
                  <TableRow key={r.id} hover>
                    <TableCell>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        {r.tipoItem === 'LOCAL' ? (
                          <MeetingRoomIcon color="primary" fontSize="small" />
                        ) : (
                          <BuildIcon color="secondary" fontSize="small" />
                        )}
                        <Box>
                          <Typography variant="body2" sx={{ fontWeight: 600 }}>
                            {r.itemNome}
                          </Typography>
                          <Typography variant="caption" color="text.secondary">
                            {r.tipoItem === 'LOCAL' ? 'Espaço Físico' : 'Ativo Móvel'}
                          </Typography>
                        </Box>
                      </Box>
                    </TableCell>
                    <TableCell>
                      {(() => {
                        const turno = identificarTurno(r.dataHoraInicio, r.dataHoraFim, r.descricao);
                        return (
                          <Box>
                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.75, flexWrap: 'wrap' }}>
                              <Typography variant="body2" sx={{ fontWeight: 500 }}>
                                {formatarDataHora(r.dataHoraInicio)} até {formatarDataHora(r.dataHoraFim)}
                              </Typography>
                              {turno && (
                                <Chip label={turno} size="small" variant="outlined" color="primary" sx={{ height: 20, fontSize: '0.725rem' }} />
                              )}
                            </Box>
                            {r.bufferMinutos > 0 && (
                              <Typography variant="caption" color="text.secondary" sx={{ display: 'block' }}>
                                + {r.bufferMinutos} min margem logística (limite: {formatarDataHora(r.fimComBuffer)})
                              </Typography>
                            )}
                          </Box>
                        );
                      })()}
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2" sx={{ maxWidth: 220 }} noWrap title={r.descricao}>
                        {r.descricao}
                      </Typography>
                      {r.eventoTitulo && (
                        <Typography variant="caption" color="primary.main">
                          Evento: {r.eventoTitulo}
                        </Typography>
                      )}
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2" sx={{ fontWeight: 500 }}>
                        {r.solicitanteNome}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        {r.pastoralMovimentoNome}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <StatusIcon {...statusMeta} />
                    </TableCell>
                    <TableCell align="right">
                      <TableActionMenu actions={rowActions(r)} />
                    </TableCell>
                  </TableRow>
                );
              })
            )}
          </TableBody>
          <SearchableTablePagination colSpan={6}
            rowsPerPageOptions={[5, 10, 25]}
            count={reservas.length}
            rowsPerPage={rowsPerPage}
            page={page}
            onPageChange={(_, p) => onPageChange(p)}
            onRowsPerPageChange={(e) => onRowsPerPageChange(parseInt(e.target.value, 10))}
            labelRowsPerPage="Linhas por página:"
          />
        </Table>
      </TableContainer>

    </>
  );
};
