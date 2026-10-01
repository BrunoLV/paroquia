import React from 'react';
import {
  Box, Paper, Skeleton, Table, TableBody, TableCell, TableContainer, TableHead, TablePagination, TableRow, Typography,
} from '@mui/material';
import StatusIcon from '@/shared/presentation/components/StatusIcon';
import TableActionMenu, { TableAction } from '@/shared/presentation/components/TableActionMenu';
import { AssentoResumo } from '@/contextos/sacramento/domain/sacramento';
import { getStatusAssentoMeta, getVisibilidadeMeta } from '@/contextos/sacramento/presentation/assentoMeta';

interface AssentosTableProps {
  assentos: AssentoResumo[];
  temAssentos: boolean;
  isLoading: boolean;
  page: number;
  rowsPerPage: number;
  onPageChange: (page: number) => void;
  onRowsPerPageChange: (rows: number) => void;
  onAbrir: (assento: AssentoResumo) => void;
  rowActions: (assento: AssentoResumo) => TableAction[];
}

const formatarCelebracao = (iso: string | null): string =>
  iso ? new Date(iso).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' }) : '—';

/**
 * Listagem de assentos. A linha inteira abre o detalhe por teclado (Enter/Espaço); o menu de ações
 * interrompe a propagação para não abrir o detalhe junto.
 */
export const AssentosTable: React.FC<AssentosTableProps> = ({
  assentos, temAssentos, isLoading, page, rowsPerPage, onPageChange, onRowsPerPageChange, onAbrir, rowActions,
}) => {
  if (isLoading) {
    return (
      <Box sx={{ py: 4 }}>
        {[1, 2, 3, 4].map((i) => (
          <Skeleton key={i} variant="rectangular" height={52} sx={{ mb: 1 }} />
        ))}
      </Box>
    );
  }

  const paginados = assentos.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage);

  return (
    <>
      <TableContainer component={Paper}>
        <Table>
          <TableHead sx={{ backgroundColor: 'action.hover' }}>
            <TableRow>
              <TableCell sx={{ fontWeight: 600 }}>Sujeito</TableCell>
              <TableCell sx={{ fontWeight: 600 }}>Livro / Folha / Termo</TableCell>
              <TableCell sx={{ fontWeight: 600 }}>Celebração</TableCell>
              <TableCell sx={{ fontWeight: 600 }}>Situação</TableCell>
              <TableCell sx={{ fontWeight: 600 }}>Livro</TableCell>
              <TableCell align="right" sx={{ fontWeight: 600 }}>Ações</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {paginados.map((a) => (
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
                <TableCell sx={{ fontWeight: 600 }}>{a.sujeito ?? 'Sem identificação'}</TableCell>
                <TableCell>{[a.livro, a.folha, a.termo].every(Boolean) ? `${a.livro} / ${a.folha} / ${a.termo}` : 'Pendente'}</TableCell>
                <TableCell>{formatarCelebracao(a.dataCelebracao)}</TableCell>
                <TableCell><StatusIcon {...getStatusAssentoMeta(a.status)} /></TableCell>
                <TableCell><StatusIcon {...getVisibilidadeMeta(a.visibilidade)} /></TableCell>
                <TableCell align="right" onClick={(e) => e.stopPropagation()} onKeyDown={(e) => e.stopPropagation()}>
                  <TableActionMenu actions={rowActions(a)} />
                </TableCell>
              </TableRow>
            ))}
            {assentos.length === 0 && (
              <TableRow>
                <TableCell colSpan={6} align="center" sx={{ py: 3 }}>
                  <Typography variant="body2" color="text.secondary">
                    {temAssentos ? 'Nenhum assento encontrado com a busca aplicada.' : 'Nenhum assento lavrado neste livro.'}
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
        count={assentos.length}
        rowsPerPage={rowsPerPage}
        page={page}
        onPageChange={(_, p) => onPageChange(p)}
        onRowsPerPageChange={(e) => {
          onRowsPerPageChange(parseInt(e.target.value, 10));
          onPageChange(0);
        }}
        labelRowsPerPage="Linhas por página:"
        labelDisplayedRows={({ from, to, count }) => `${from}–${to} de ${count !== -1 ? count : `mais de ${to}`}`}
      />
    </>
  );
};

export default AssentosTable;
