import React, { useState } from 'react';
import {
  Alert, Box, Card, Paper, Skeleton, Stack, Table, TableBody, TableCell, TableContainer, TableHead,
  TablePagination, TableRow, Typography,
} from '@mui/material';
import CancelIcon from '@mui/icons-material/Cancel';
import LinkIcon from '@mui/icons-material/Link';
import VerifiedIcon from '@mui/icons-material/Verified';
import ActionButton from '@/shared/presentation/components/ActionButton';
import StatusIcon from '@/shared/presentation/components/StatusIcon';
import { PAPEL_LABEL, TIPO_SACRAMENTO_LABEL } from '@/contextos/sacramento/domain/sacramento';
import { useAssentoMutations, useVinculosPendentes } from '@/contextos/sacramento/presentation/hooks/useAssentos';
import { getTipoSacramentoMeta } from '@/contextos/sacramento/presentation/assentoMeta';

/**
 * Fila de vínculos sacramentais propostos pela Secretaria, na tela de Aprovações. Decidir é ato privativo do
 * Pároco (Cân. 535 §1): aprovar efetiva o vínculo do fiel/clérigo ao termo; rejeitar remove o ponteiro e
 * preserva o texto lavrado. O Pároco confere o cadastro proposto contra o nome atestado no termo.
 */
export const VinculosPendentesPanel: React.FC = () => {
  const { data: pendentes = [], isLoading, isError, refetch } = useVinculosPendentes(true);
  const m = useAssentoMutations();
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const paginados = pendentes.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage);

  return (
    <Card sx={{ width: '100%', mb: 4, overflow: 'hidden' }}>
      <Stack direction="row" spacing={2} sx={{ p: 2.5, pb: 2, alignItems: 'center', borderBottom: '1px solid', borderColor: 'divider' }}>
        <LinkIcon sx={{ fontSize: 26, color: 'primary.main' }} />
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 600, color: 'primary.main' }}>Vínculos Sacramentais</Typography>
          <Typography variant="body2" color="text.secondary">
            Vínculos de fiéis e clérigos a registros sacramentais, propostos pela Secretaria, aguardam sua decisão.
          </Typography>
        </Box>
      </Stack>
      {isLoading ? (
        <Box sx={{ p: 3 }}><Skeleton variant="rounded" height={200} /></Box>
      ) : isError ? (
        <Box sx={{ p: 4 }}>
          <Alert severity="error" action={<ActionButton title="Tentar novamente" icon={<LinkIcon />} color="primary" onClick={() => void refetch()} />}>
            Não foi possível carregar os vínculos pendentes.
          </Alert>
        </Box>
      ) : pendentes.length === 0 ? (
        <Box sx={{ p: 8, textAlign: 'center' }}>
          <Typography variant="body1" color="text.secondary">Nenhum vínculo sacramental aguardando aprovação.</Typography>
        </Box>
      ) : (
        <>
          <TableContainer component={Paper} elevation={0}>
            <Table>
              <TableHead sx={{ backgroundColor: 'action.hover' }}>
                <TableRow>
                  <TableCell sx={{ fontWeight: 600 }}>Sacramento</TableCell>
                  <TableCell sx={{ fontWeight: 600 }}>Termo</TableCell>
                  <TableCell sx={{ fontWeight: 600 }}>Participação</TableCell>
                  <TableCell sx={{ fontWeight: 600 }}>Nome no termo</TableCell>
                  <TableCell sx={{ fontWeight: 600 }}>Cadastro proposto</TableCell>
                  <TableCell align="right" sx={{ fontWeight: 600 }}>Ações</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {paginados.map((v) => (
                  <TableRow key={`${v.assentoId}-${v.participanteId}`} hover>
                    <TableCell><StatusIcon {...getTipoSacramentoMeta(v.tipoSacramento)} /></TableCell>
                    <TableCell>
                      {v.sujeito ?? 'Sem identificação'} · {TIPO_SACRAMENTO_LABEL[v.tipoSacramento]}
                      {[v.livro, v.folha, v.termo].every(Boolean) ? ` (${v.livro}/${v.folha}/${v.termo})` : ''}
                    </TableCell>
                    <TableCell>{PAPEL_LABEL[v.papel]}</TableCell>
                    <TableCell sx={{ fontWeight: 600 }}>{v.nomeHistorico}</TableCell>
                    <TableCell>{v.pessoaNome ?? '—'} ({v.tipoPessoa === 'FIEL' ? 'fiel' : 'clérigo'})</TableCell>
                    <TableCell align="right">
                      <Stack direction="row" spacing={1} sx={{ justifyContent: 'flex-end' }}>
                        <ActionButton
                          title={`Aprovar vínculo de ${v.nomeHistorico}`}
                          label="Aprovar"
                          icon={<VerifiedIcon />}
                          color="primary"
                          disabled={m.aprovarVinculo.isPending}
                          onClick={() => m.aprovarVinculo.mutate({ id: v.assentoId, participanteId: v.participanteId })}
                        />
                        <ActionButton
                          title={`Rejeitar vínculo de ${v.nomeHistorico}`}
                          label="Rejeitar"
                          icon={<CancelIcon />}
                          color="error"
                          variant="outlined"
                          disabled={m.rejeitarVinculo.isPending}
                          onClick={() => m.rejeitarVinculo.mutate({ id: v.assentoId, participanteId: v.participanteId })}
                        />
                      </Stack>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
          <TablePagination
            rowsPerPageOptions={[5, 10, 25]}
            component="div"
            count={pendentes.length}
            rowsPerPage={rowsPerPage}
            page={page}
            onPageChange={(_, p) => setPage(p)}
            onRowsPerPageChange={(e) => {
              setRowsPerPage(parseInt(e.target.value, 10));
              setPage(0);
            }}
            labelRowsPerPage="Linhas por página:"
            labelDisplayedRows={({ from, to, count }) => `${from}–${to} de ${count !== -1 ? count : `mais de ${to}`}`}
          />
        </>
      )}
    </Card>
  );
};

export default VinculosPendentesPanel;
