import SearchableTablePagination from '@/shared/presentation/components/SearchableTablePagination';
import React, { useState } from 'react';
import { Alert, Box, Card, Divider, Drawer, Grid, Paper, Skeleton, Stack, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Typography } from '@mui/material';
import BadgeIcon from '@mui/icons-material/Badge';
import CancelIcon from '@mui/icons-material/Cancel';
import CategoryIcon from '@mui/icons-material/Category';
import CloseIcon from '@mui/icons-material/Close';
import MenuBookIcon from '@mui/icons-material/MenuBook';
import PersonIcon from '@mui/icons-material/Person';
import LinkIcon from '@mui/icons-material/Link';
import VerifiedIcon from '@mui/icons-material/Verified';
import VisibilityIcon from '@mui/icons-material/Visibility';
import ActionButton from '@/shared/presentation/components/ActionButton';
import StatusIcon from '@/shared/presentation/components/StatusIcon';
import TableActionMenu from '@/shared/presentation/components/TableActionMenu';
import { InfoField } from '@/contextos/fiel/presentation/components/fieis/fielConstants';
import { PAPEL_LABEL, TIPO_SACRAMENTO_LABEL, VinculoPendente } from '@/contextos/sacramento/domain/sacramento';
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
  const [selecionado, setSelecionado] = useState<VinculoPendente | null>(null);
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
                      <TableActionMenu actions={[{ label: 'Visualizar detalhes', icon: <VisibilityIcon fontSize="small" />, onClick: () => setSelecionado(v) }]} />
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
          <SearchableTablePagination
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
      {/* Drawer de detalhes, no padrão das demais solicitações: a decisão (aprovar/rejeitar) só existe aqui,
          nunca como botões soltos na tabela. */}
      <Drawer
        anchor="right"
        open={selecionado !== null}
        onClose={() => setSelecionado(null)}
        slotProps={{ paper: { sx: { width: { xs: '100%', sm: 580, md: 680 }, p: { xs: 2.5, sm: 3.5 }, overflowY: 'auto' } } }}
      >
        {selecionado && (
          <Box>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
              <Typography variant="h6" sx={{ fontWeight: 700 }}>Detalhes do Vínculo Sacramental</Typography>
              <ActionButton title="Fechar" icon={<CloseIcon />} onClick={() => setSelecionado(null)} />
            </Box>
            <Grid container spacing={2}>
              <Grid size={{ xs: 12, sm: 6 }}><InfoField label="Sacramento" icon={<CategoryIcon fontSize="inherit" />} value={<StatusIcon {...getTipoSacramentoMeta(selecionado.tipoSacramento)} />} /></Grid>
              <Grid size={{ xs: 12, sm: 6 }}><InfoField label="Termo" icon={<MenuBookIcon fontSize="inherit" />} value={`${selecionado.sujeito ?? 'Sem identificação'}${[selecionado.livro, selecionado.folha, selecionado.termo].every(Boolean) ? ` (${selecionado.livro}/${selecionado.folha}/${selecionado.termo})` : ''}`} /></Grid>
              <Grid size={{ xs: 12, sm: 6 }}><InfoField label="Tipo de participação" icon={<BadgeIcon fontSize="inherit" />} value={PAPEL_LABEL[selecionado.papel]} /></Grid>
              <Grid size={{ xs: 12, sm: 6 }}><InfoField label="Nome no termo" icon={<PersonIcon fontSize="inherit" />} value={selecionado.nomeHistorico} /></Grid>
              <Grid size={{ xs: 12 }}><InfoField label={selecionado.tipoPessoa === 'FIEL' ? 'Fiel proposto' : 'Clérigo proposto'} icon={<LinkIcon fontSize="inherit" />} value={selecionado.pessoaNome ?? '—'} /></Grid>
            </Grid>
            <Box sx={{ mt: 4 }}>
              <Divider sx={{ mb: 3 }} />
              <Typography variant="subtitle2" color="textSecondary" gutterBottom>TOMADA DE DECISÃO</Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                Confira se o cadastro proposto corresponde à pessoa atestada no termo. Rejeitar remove o vínculo e preserva o texto lavrado.
              </Typography>
              <Box sx={{ display: 'flex', gap: 2 }}>
                <ActionButton
                  title="Aprovar vínculo"
                  icon={<VerifiedIcon />}
                  color="success"
                  disabled={m.aprovarVinculo.isPending || m.rejeitarVinculo.isPending}
                  onClick={() => m.aprovarVinculo.mutate(
                    { id: selecionado.assentoId, participanteId: selecionado.participanteId },
                    { onSuccess: () => setSelecionado(null) },
                  )}
                  sx={{ border: '1px solid', borderColor: 'success.main' }}
                />
                <ActionButton
                  title="Rejeitar vínculo"
                  icon={<CancelIcon />}
                  color="error"
                  disabled={m.aprovarVinculo.isPending || m.rejeitarVinculo.isPending}
                  onClick={() => m.rejeitarVinculo.mutate(
                    { id: selecionado.assentoId, participanteId: selecionado.participanteId },
                    { onSuccess: () => setSelecionado(null) },
                  )}
                  sx={{ border: '1px solid', borderColor: 'error.main' }}
                />
              </Box>
            </Box>
          </Box>
        )}
      </Drawer>
    </Card>
  );
};

export default VinculosPendentesPanel;
