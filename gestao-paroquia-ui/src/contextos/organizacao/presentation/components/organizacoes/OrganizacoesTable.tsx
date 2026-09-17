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
import EditIcon from '@mui/icons-material/Edit';
import GroupsIcon from '@mui/icons-material/Groups';
import ToggleOffIcon from '@mui/icons-material/ToggleOff';
import ToggleOnIcon from '@mui/icons-material/ToggleOn';
import { Organizacao } from '@/shared/domain/types/types';
import { FormattedContact } from '@/shared/presentation/components/FormattedCell';
import StatusIcon from '@/shared/presentation/components/StatusIcon';
import TableActionMenu from '@/shared/presentation/components/TableActionMenu';
import { getOrganismoTipoMeta } from '@/shared/utils/statusMeta/organismoTipoStatus';
import { getSituacaoAtivoMeta } from '@/shared/utils/statusMeta/situacaoStatus';

interface OrganizacoesTableProps {
  organizacoes: Organizacao[];
  isLoading: boolean;
  page: number;
  rowsPerPage: number;
  onPageChange: (newPage: number) => void;
  onRowsPerPageChange: (newRows: number) => void;
  canManage: boolean;
  onViewMembers: (org: Organizacao) => void;
  onEdit: (org: Organizacao) => void;
  onToggleStatus: (org: Organizacao) => void;
  isTogglePending?: boolean;
}

export const OrganizacoesTable: React.FC<OrganizacoesTableProps> = ({
  organizacoes,
  isLoading,
  page,
  rowsPerPage,
  onPageChange,
  onRowsPerPageChange,
  canManage,
  onViewMembers,
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

  const paginatedOrgs = organizacoes.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage);

  return (
    <>
      <TableContainer component={Paper}>
        <Table>
          <TableHead sx={{ backgroundColor: 'action.hover' }}>
            <TableRow>
              <TableCell sx={{ fontWeight: 600 }}>Nome</TableCell>
              <TableCell sx={{ fontWeight: 600 }}>Tipo</TableCell>
              <TableCell sx={{ fontWeight: 600 }}>Coordenador</TableCell>
              <TableCell sx={{ fontWeight: 600 }}>Vice-Coordenador</TableCell>
              <TableCell sx={{ fontWeight: 600 }}>Contato</TableCell>
              <TableCell sx={{ fontWeight: 600 }}>Status</TableCell>
              <TableCell align="right" sx={{ fontWeight: 600 }}>Ações</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {paginatedOrgs.map((org) => (
              <TableRow key={org.id} hover>
                <TableCell sx={{ fontWeight: 600 }}>{org.nome}</TableCell>
                <TableCell><StatusIcon {...getOrganismoTipoMeta(org.tipo)} /></TableCell>
                <TableCell>{org.coordenador || '-'}</TableCell>
                <TableCell>{org.viceCoordenador || '-'}</TableCell>
                <TableCell><FormattedContact value={org.contato} /></TableCell>
                <TableCell><StatusIcon {...getSituacaoAtivoMeta(org.ativo)} /></TableCell>
                <TableCell align="right">
                  <TableActionMenu
                    actions={[
                      { label: 'Membros', icon: <GroupsIcon fontSize="small" />, onClick: () => onViewMembers(org) },
                      ...(canManage ? [
                        { label: 'Editar', icon: <EditIcon fontSize="small" />, onClick: () => onEdit(org), disabled: isTogglePending },
                        { label: org.ativo ? 'Inativar' : 'Ativar', icon: org.ativo ? <ToggleOffIcon fontSize="small" /> : <ToggleOnIcon fontSize="small" />, onClick: () => onToggleStatus(org), disabled: isTogglePending },
                      ] : []),
                    ]}
                  />
                </TableCell>
              </TableRow>
            ))}
            {organizacoes.length === 0 && (
              <TableRow>
                <TableCell colSpan={7} align="center" sx={{ py: 3 }}>
                  <Typography variant="body2" color="text.secondary">
                    Nenhuma pastoral ou movimento encontrado.
                  </Typography>
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>
      <TablePagination
        rowsPerPageOptions={[5, 10, 25]}
        component="div"
        count={organizacoes.length}
        rowsPerPage={rowsPerPage}
        page={page}
        onPageChange={(_, newPage) => onPageChange(newPage)}
        onRowsPerPageChange={(e) => {
          onRowsPerPageChange(parseInt(e.target.value, 10));
          onPageChange(0);
        }}
        labelRowsPerPage="Linhas por página:"
        labelDisplayedRows={({ from, to, count }) =>
          `${from}–${to} de ${count !== -1 ? count : `mais de ${to}`}`
        }
      />
    </>
  );
};
