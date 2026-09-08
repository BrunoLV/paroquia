import React from 'react';
import {
  Box,
  Chip,
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
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import VisibilityIcon from '@mui/icons-material/Visibility';
import { Local, Organizacao } from '@/api/types';
import StatusIcon from '@/components/StatusIcon';
import TableActionMenu from '@/components/TableActionMenu';
import { getLocalTipoMeta } from '@/utils/statusMeta/localTipoStatus';
import { getSituacaoAtivoMeta } from '@/utils/statusMeta/situacaoStatus';

interface LocaisTableProps {
  locais: Local[];
  igrejas?: Organizacao[];
  isLoading: boolean;
  page: number;
  rowsPerPage: number;
  onPageChange: (newPage: number) => void;
  onRowsPerPageChange: (newRows: number) => void;
  canManage: boolean;
  onView: (local: Local) => void;
  onEdit: (local: Local) => void;
  onDelete: (local: Local) => void;
  isDeletePending?: boolean;
}

export const LocaisTable: React.FC<LocaisTableProps> = ({
  locais,
  igrejas,
  isLoading,
  page,
  rowsPerPage,
  onPageChange,
  onRowsPerPageChange,
  canManage,
  onView,
  onEdit,
  onDelete,
  isDeletePending,
}) => {
  if (isLoading) {
    return (
      <Box sx={{ py: 4 }}>
        <Skeleton variant="text" width={260} height={32} sx={{ mb: 2 }} />
        {[1, 2, 3, 4].map((i) => (
          <Skeleton key={i} variant="rectangular" height={52} sx={{ mb: 1 }} />
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
            {paginatedLocais.map((local) => {
              const capacidade = local.capacidadeMaxima ?? null;

              const partesEndereco: string[] = [];
              const viaNumero = [local.logradouro, local.numero].filter(Boolean).join(', ');
              if (viaNumero) partesEndereco.push(viaNumero);
              if (local.bairro) partesEndereco.push(local.bairro);
              const cidadeUf = [local.cidade, local.uf].filter(Boolean).join('/');
              if (cidadeUf) partesEndereco.push(cidadeUf);
              const enderecoResumido = local.enderecoFormatado || (partesEndereco.length > 0
                ? partesEndereco.join(' - ')
                : '-');

              const igrejaEncontrada = igrejas?.find((i) => i.id === local.igrejaId);
              const igrejaNome = local.igrejaNome || igrejaEncontrada?.nome || '-';

              return (
                <TableRow key={local.id} hover>
                  <TableCell sx={{ fontWeight: 600 }}>{local.nome}</TableCell>
                  <TableCell>
                    <StatusIcon {...getLocalTipoMeta(local.tipo)} />
                  </TableCell>
                  <TableCell>
                    {capacidade !== null && capacidade !== undefined ? capacidade : '-'}
                  </TableCell>
                  <TableCell>{igrejaNome}</TableCell>
                  <TableCell>
                    <Typography variant="body2" noWrap sx={{ maxWidth: 260 }}>
                      {enderecoResumido}
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <StatusIcon {...getSituacaoAtivoMeta(local.ativo)} />
                  </TableCell>
                  {canManage && (
                    <TableCell align="right">
                      <TableActionMenu
                        actions={[
                          {
                            label: 'Visualizar',
                            icon: <VisibilityIcon fontSize="small" />,
                            onClick: () => onView(local),
                          },
                          {
                            label: 'Editar',
                            icon: <EditIcon fontSize="small" />,
                            onClick: () => onEdit(local),
                            disabled: isDeletePending,
                          },
                          {
                            label: 'Excluir',
                            icon: <DeleteIcon fontSize="small" />,
                            onClick: () => onDelete(local),
                            disabled: isDeletePending,
                          },
                        ]}
                      />
                    </TableCell>
                  )}
                </TableRow>
              );
            })}
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
        rowsPerPageOptions={[5, 10, 25]}
        component="div"
        count={locais.length}
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
