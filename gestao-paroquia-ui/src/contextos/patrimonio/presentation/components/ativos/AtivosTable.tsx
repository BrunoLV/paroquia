import SearchableTablePagination from '@/shared/presentation/components/SearchableTablePagination';
import React from 'react';
import { Box, Paper, Skeleton, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Typography } from '@mui/material';
import BookmarkIcon from '@mui/icons-material/Bookmark';
import { FormattedCurrency } from '@/shared/presentation/components/FormattedCell';
import StatusIcon from '@/shared/presentation/components/StatusIcon';
import TableActionMenu, { TableAction } from '@/shared/presentation/components/TableActionMenu';
import { getAtivoStatusMeta } from '@/shared/utils/statusMeta/ativoStatus';
import { getCategoriaAtivoMeta } from '@/shared/utils/statusMeta/categoriaAtivoStatus';
import { AtivoPatrimonial } from '@/shared/domain/types/types';

interface AtivosTableProps {
  ativos: AtivoPatrimonial[];
  temAtivosCadastrados: boolean;
  isLoading: boolean;
  page: number;
  rowsPerPage: number;
  onPageChange: (newPage: number) => void;
  onRowsPerPageChange: (newRows: number) => void;
  rowActions: (ativo: AtivoPatrimonial) => TableAction[];
  onView?: (ativo: AtivoPatrimonial) => void;
}

/** Tabela de Ativos Patrimoniais — espelha o padrão visual de LocaisTable. */
export const AtivosTable: React.FC<AtivosTableProps> = ({
  ativos,
  temAtivosCadastrados,
  isLoading,
  page,
  rowsPerPage,
  onPageChange,
  onRowsPerPageChange,
  rowActions,
  onView,
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

  const colSpan = 6;
  const paginados = ativos.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage);

  return (
    <>
      <TableContainer component={Paper}>
        <Table>
          <TableHead sx={{ backgroundColor: 'action.hover' }}>
            <TableRow>
              <TableCell sx={{ fontWeight: 600 }}>Nome</TableCell>
              <TableCell sx={{ fontWeight: 600 }}>Tombamento</TableCell>
              <TableCell sx={{ fontWeight: 600 }}>Tipo</TableCell>
              <TableCell sx={{ fontWeight: 600 }}>Status</TableCell>
              <TableCell align="right" sx={{ fontWeight: 600 }}>Valor de Aquisição</TableCell>
              <TableCell align="right" sx={{ fontWeight: 600 }}>Ações</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {paginados.map((ativo) => (
              <TableRow
                key={ativo.id}
                hover
                tabIndex={0}
                role="button"
                onClick={() => onView?.(ativo)}
                onKeyDown={(e) => {
                  if (e.key === 'Enter' || e.key === ' ') {
                    e.preventDefault();
                    onView?.(ativo);
                  }
                }}
                sx={{ cursor: onView ? 'pointer' : 'default' }}
              >
                <TableCell sx={{ fontWeight: 600 }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    {ativo.nome}
                    {ativo.isReservavel && (
                      <StatusIcon label="Reservável" color="primary" icon={<BookmarkIcon />} tooltip="Este bem pode ser reservado para eventos" />
                    )}
                  </Box>
                </TableCell>
                <TableCell>{ativo.codigoTombamento}</TableCell>
                <TableCell>
                  <StatusIcon {...getCategoriaAtivoMeta(ativo.categoria)} />
                </TableCell>
                <TableCell>
                  <StatusIcon {...getAtivoStatusMeta(ativo.status)} />
                </TableCell>
                <TableCell align="right">
                  <FormattedCurrency value={ativo.valorAquisicao} />
                </TableCell>
                <TableCell align="right" onClick={(e) => e.stopPropagation()}>
                  <TableActionMenu actions={rowActions(ativo)} />
                </TableCell>
              </TableRow>
            ))}
            {ativos.length === 0 && (
              <TableRow>
                <TableCell colSpan={colSpan} align="center" sx={{ py: 3 }}>
                  <Typography variant="body2" color="text.secondary">
                    {temAtivosCadastrados
                      ? 'Nenhum ativo encontrado com os filtros aplicados.'
                      : 'Nenhum ativo cadastrado para esta organização.'}
                  </Typography>
                </TableCell>
              </TableRow>
            )}
          </TableBody>
          <SearchableTablePagination colSpan={6}
            rowsPerPageOptions={[5, 10, 25]}
            count={ativos.length}
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
        </Table>
      </TableContainer>
    </>
  );
};

export default AtivosTable;
