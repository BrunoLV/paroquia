import React from 'react';
import {
  Box,
  Paper,
  Skeleton,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TablePagination,
  TableRow,
  Typography,
} from '@mui/material';
import { Edit as EditIcon, ToggleOff as ToggleOffIcon, ToggleOn as ToggleOnIcon } from '@mui/icons-material';
import { Local } from '@/api/types';
import { FormattedAddress } from '@/components/FormattedCell';
import StatusIcon from '@/components/StatusIcon';
import TableActionMenu from '@/components/TableActionMenu';
import { getLocalTipoMeta } from '@/utils/statusMeta/localTipoStatus';
import { getSituacaoAtivoMeta } from '@/utils/statusMeta/situacaoStatus';

interface LocaisTableProps {
  locais: Local[];
  isLoading: boolean;
  page: number;
  rowsPerPage: number;
  onPageChange: (newPage: number) => void;
  onRowsPerPageChange: (newRows: number) => void;
  canManage: boolean;
  onEdit: (local: Local) => void;
  onToggleStatus: (local: Local) => void;
  isTogglePending?: boolean;
}

export const LocaisTable: React.FC<LocaisTableProps> = ({
  locais,
  isLoading,
  page,
  rowsPerPage,
  onPageChange,
  onRowsPerPageChange,
  canManage,
  onEdit,
  onToggleStatus,
  isTogglePending,
}) => {
  if (isLoading) {
    return (
      <Box sx={{ py: 4 }}>
        <Skeleton variant="text" width={260} height={32} sx={{ mb: 2 }} />
        {[1, 2, 3, 4].map((i) => (
          <Skeleton key={i} variant="rectangular" height={48} sx={{ mb: 1 }} />
        ))}
      </Box>
    );
  }

  const paginatedLocais = locais.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage);

  return (
    <>
      <TableContainer component={Paper}>
        <Table>
          <TableHead sx={{ backgroundColor: 'action.hover' }}>
            <TableRow>
              <TableCell sx={{ fontWeight: 600 }}>Nome</TableCell>
              <TableCell sx={{ fontWeight: 600 }}>Tipo</TableCell>
              <TableCell sx={{ fontWeight: 600 }}>Capacidade</TableCell>
              <TableCell sx={{ fontWeight: 600 }}>Matriz / Comunidade</TableCell>
              <TableCell sx={{ fontWeight: 600 }}>Endereço</TableCell>
              <TableCell sx={{ fontWeight: 600 }}>Status</TableCell>
              {canManage && <TableCell align="right" sx={{ fontWeight: 600 }}>Ações</TableCell>}
            </TableRow>
          </TableHead>
          <TableBody>
            {paginatedLocais.map((local) => (
              <TableRow key={local.id} hover>
                <TableCell sx={{ fontWeight: 600 }}>{local.nome}</TableCell>
                <TableCell><StatusIcon {...getLocalTipoMeta(local.tipo)} /></TableCell>
                <TableCell>{local.capacidade !== null && local.capacidade !== undefined ? local.capacidade : '-'}</TableCell>
                <TableCell>{local.organizacaoNome || '-'}</TableCell>
                <TableCell><FormattedAddress value={local.endereco} /></TableCell>
                <TableCell><StatusIcon {...getSituacaoAtivoMeta(local.ativo)} /></TableCell>
                {canManage && (
                  <TableCell align="right">
                    <TableActionMenu
                      actions={[
                        { label: 'Editar', icon: <EditIcon fontSize="small" />, onClick: () => onEdit(local), disabled: isTogglePending },
                        { label: local.ativo ? 'Inativar' : 'Ativar', icon: local.ativo ? <ToggleOffIcon fontSize="small" /> : <ToggleOnIcon fontSize="small" />, onClick: () => onToggleStatus(local), disabled: isTogglePending },
                      ]}
                    />
                  </TableCell>
                )}
              </TableRow>
            ))}
            {locais.length === 0 && (
              <TableRow>
                <TableCell colSpan={canManage ? 7 : 6} align="center" sx={{ py: 3 }}>
                  <Typography variant="body2" color="text.secondary">
                    Nenhum local encontrado.
                  </Typography>
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>
      <TablePagination
        rowsPerPageOptions={[10, 20, 50, 100]}
        component="div"
        count={locais.length}
        rowsPerPage={rowsPerPage}
        page={page}
        onPageChange={(_, newPage) => onPageChange(newPage)}
        onRowsPerPageChange={(e) => {
          onRowsPerPageChange(parseInt(e.target.value, 10));
          onPageChange(0);
        }}
        labelRowsPerPage="Itens por página:"
      />
    </>
  );
};
