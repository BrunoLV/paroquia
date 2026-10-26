import React from 'react';
import { Box, Paper, Skeleton, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Typography } from '@mui/material';
import SearchableTablePagination from '@/shared/presentation/components/SearchableTablePagination';
import StatusIcon from '@/shared/presentation/components/StatusIcon';
import TableActionMenu, { TableAction } from '@/shared/presentation/components/TableActionMenu';
import { getAtaStatusMeta } from '@/shared/utils/statusMeta/ataStatus';
import { formatDateBr } from '@/shared/utils/masks';
import { AtaResumo } from '@/contextos/conselho/domain/ata';

interface AtasTableProps {
  atas: AtaResumo[];
  total: number;
  isLoading: boolean;
  page: number;
  rowsPerPage: number;
  onPageChange: (page: number) => void;
  onRowsPerPageChange: (rows: number) => void;
  onAbrir: (ata: AtaResumo) => void;
  rowActions: (ata: AtaResumo) => TableAction[];
}

const COLUNAS = 6;

/**
 * Listagem de atas. A linha inteira abre o detalhe por teclado (Enter/Espaço); o menu de ações interrompe a
 * propagação para não abrir o detalhe junto. A paginação é do servidor, que também aplica o escopo de leitura.
 */
export const AtasTable: React.FC<AtasTableProps> = ({
  atas, total, isLoading, page, rowsPerPage, onPageChange, onRowsPerPageChange, onAbrir, rowActions,
}) => {
  if (isLoading) {
    return (
      <Box sx={{ py: 4 }}>
        {[1, 2, 3, 4].map((i) => <Skeleton key={i} variant="rectangular" height={52} sx={{ mb: 1 }} />)}
      </Box>
    );
  }

  return (
    <TableContainer component={Paper}>
      <Table>
        <TableHead sx={{ backgroundColor: 'action.hover' }}>
          <TableRow>
            <TableCell sx={{ fontWeight: 600 }}>Ata</TableCell>
            <TableCell sx={{ fontWeight: 600 }}>Tipo de reunião</TableCell>
            <TableCell sx={{ fontWeight: 600 }}>Órgão</TableCell>
            <TableCell sx={{ fontWeight: 600 }}>Data</TableCell>
            <TableCell sx={{ fontWeight: 600 }}>Situação</TableCell>
            <TableCell align="right" sx={{ fontWeight: 600 }}>Ações</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {atas.map((a) => (
            <TableRow
              key={a.id}
              hover
              role="button"
              tabIndex={0}
              sx={{ cursor: 'pointer' }}
              onClick={() => onAbrir(a)}
              onKeyDown={(e) => {
                if (e.key === 'Enter' || e.key === ' ') {
                  e.preventDefault();
                  onAbrir(a);
                }
              }}
            >
              <TableCell sx={{ fontWeight: 600 }}>
                {a.titulo}
                <Typography variant="caption" color="text.secondary" sx={{ display: 'block' }}>
                  {a.numero ? `Ata nº ${a.numero}` : 'Minuta'}
                </Typography>
              </TableCell>
              <TableCell>{a.tipoRotulo}</TableCell>
              <TableCell>{a.organizacaoConvocante ?? '—'}</TableCell>
              <TableCell>{formatDateBr(a.dataReuniao)}</TableCell>
              <TableCell><StatusIcon {...getAtaStatusMeta(a.status)} /></TableCell>
              <TableCell align="right" onClick={(e) => e.stopPropagation()} onKeyDown={(e) => e.stopPropagation()}>
                <TableActionMenu actions={rowActions(a)} />
              </TableCell>
            </TableRow>
          ))}
          {atas.length === 0 && (
            <TableRow>
              <TableCell colSpan={COLUNAS} align="center" sx={{ py: 3 }}>
                <Typography variant="body2" color="text.secondary">
                  Nenhuma ata encontrada para os filtros selecionados.
                </Typography>
              </TableCell>
            </TableRow>
          )}
        </TableBody>
        <SearchableTablePagination
          colSpan={COLUNAS}
          rowsPerPageOptions={[5, 10, 25]}
          count={total}
          rowsPerPage={rowsPerPage}
          page={page}
          onPageChange={(_, nova) => onPageChange(nova)}
          onRowsPerPageChange={(e) => onRowsPerPageChange(parseInt(e.target.value, 10))}
          labelRowsPerPage="Linhas por página:"
          labelDisplayedRows={({ from, to, count }) => `${from}–${to} de ${count !== -1 ? count : `mais de ${to}`}`}
        />
      </Table>
    </TableContainer>
  );
};

export default AtasTable;
