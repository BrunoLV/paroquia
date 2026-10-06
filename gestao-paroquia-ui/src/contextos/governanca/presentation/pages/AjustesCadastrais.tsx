import TableActionMenu from '@/shared/presentation/components/TableActionMenu';
import SearchableTablePagination from '@/shared/presentation/components/SearchableTablePagination';
import { useState } from 'react';
import BlockIcon from '@mui/icons-material/Block';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import CloseIcon from '@mui/icons-material/Close';
import ContactMailIcon from '@mui/icons-material/ContactMail';
import AjusteIcon from '@mui/icons-material/HowToReg';
import VisibilityIcon from '@mui/icons-material/Visibility';
import { Card, CardContent, Chip, Dialog, DialogActions, DialogContent, DialogTitle, Grid, Paper, Skeleton, Stack, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, TextField, Typography } from '@mui/material';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  decidirAjusteCadastral,
  listarAjustesCadastrais,
  type AjusteCadastral,
} from '@/contextos/governanca/infrastructure/http/ajustesCadastrais';
import PageHeader from '@/shared/presentation/components/PageHeader';
import ActionButton from '@/shared/presentation/components/ActionButton';
import { useNotification } from '@/shared/presentation/context/NotificationContext';
import { formatDateBr, formatPhone } from '@/shared/utils/masks';

const InfoField = ({
  label,
  value,
}: {
  label: string;
  value?: React.ReactNode;
}) => (
  <Paper
    variant="outlined"
    sx={{
      p: 1.5,
      borderRadius: 1.5,
      bgcolor: 'background.paper',
      height: '100%',
      display: 'flex',
      flexDirection: 'column',
      justifyContent: 'center',
    }}
  >
    <Typography variant="caption" color="text.secondary" sx={{ fontWeight: 600, mb: 0.5 }}>
      {label}
    </Typography>
    <Typography variant="body2" sx={{ fontWeight: 500, color: 'text.primary' }}>
      {value || '-'}
    </Typography>
  </Paper>
);

const AjustesCadastrais = () => {
  const client = useQueryClient();
  const { showSuccess, showError } = useNotification();
  const [selecionado, setSelecionado] = useState<AjusteCadastral | null>(null);
  const [justificativa, setJustificativa] = useState('');
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);

  const itens = useQuery({
    queryKey: ['ajustes-cadastrais'],
    queryFn: listarAjustesCadastrais,
  });

  const decidir = useMutation({
    mutationFn: ({ aprovar }: { aprovar: boolean }) =>
      decidirAjusteCadastral(selecionado!.id, aprovar, justificativa),
    onSuccess: (_, variables) => {
      showSuccess(
        variables.aprovar
          ? 'Ajuste cadastral aprovado com sucesso.'
          : 'Ajuste cadastral rejeitado com sucesso.'
      );
      setSelecionado(null);
      setJustificativa('');
      client.invalidateQueries({ queryKey: ['ajustes-cadastrais'] });
    },
    onError: () => {
      showError('Não foi possível processar a decisão do ajuste cadastral.');
    },
  });

  if (itens.isLoading) {
    return <Skeleton variant="rounded" height={320} />;
  }

  const lista = itens.data ?? [];

  return (
    <Stack spacing={3}>
      <PageHeader
        icon={<AjusteIcon sx={{ fontSize: 32, color: 'primary.main' }} />}
        title="Ajustes Cadastrais"
        subtitle="Solicitações pendentes de validação e aprovação cadastral."
      />

      <TableContainer component={Paper}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Contato Proposto</TableCell>
                  <TableCell>Endereço Proposto</TableCell>
                  <TableCell>Solicitado em</TableCell>
                  <TableCell align="right">Ações</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {lista.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={4} align="center">
                      <Typography variant="body2" color="text.secondary" sx={{ py: 3 }}>
                        Nenhuma solicitação de ajuste cadastral pendente.
                      </Typography>
                    </TableCell>
                  </TableRow>
                ) : (
                  lista
                    .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
                    .map((a) => (
                      <TableRow key={a.id} hover>
                        <TableCell>
                          <Stack spacing={0.25}>
                            <Typography variant="body2" sx={{ fontWeight: 600 }}>
                              {a.email || 'E-mail não informado'}
                            </Typography>
                            {a.celular && (
                              <Typography variant="caption" color="text.secondary">
                                {formatPhone(a.celular)}
                              </Typography>
                            )}
                          </Stack>
                        </TableCell>
                        <TableCell>
                          <Typography variant="body2">
                            {[a.endereco?.logradouro, a.endereco?.cidade, a.endereco?.estado]
                              .filter(Boolean)
                              .join(' - ') || 'Endereço não informado'}
                          </Typography>
                        </TableCell>
                        <TableCell>
                          <Chip
                            size="small"
                            variant="outlined"
                            label={formatDateBr(a.criadaEmUtc?.slice(0, 10))}
                          />
                        </TableCell>
                        <TableCell align="right">
                          <TableActionMenu actions={[{
                            label: "Analisar solicitação", icon: <VisibilityIcon fontSize="small" color="primary" />,
                            onClick: () => {
                              setSelecionado(a);
                              setJustificativa('');
                            },
                          }]} />
                        </TableCell>
                      </TableRow>
                    ))
                )}
              </TableBody>
              <SearchableTablePagination colSpan={4}
                rowsPerPageOptions={[5, 10, 25]}
                count={lista.length}
                rowsPerPage={rowsPerPage}
                page={page}
                onPageChange={(_, newPage) => setPage(newPage)}
                onRowsPerPageChange={(e) => {
                  setRowsPerPage(parseInt(e.target.value, 10));
                  setPage(0);
                }}
                labelRowsPerPage="Linhas por página:"
                labelDisplayedRows={({ from, to, count }) =>
                  `${from}–${to} de ${count !== -1 ? count : `mais de ${to}`}`
                }
              />
            </Table>
      </TableContainer>

      {/* Modal de Análise e Decisão */}
      <Dialog
        open={Boolean(selecionado)}
        onClose={() => setSelecionado(null)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>Analisar ajuste cadastral</DialogTitle>
        <DialogContent dividers>
          <Stack spacing={2.5}>
            {selecionado && (
              <Card variant="outlined" sx={{ borderRadius: 2 }}>
                <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
                  <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
                    <ContactMailIcon color="primary" fontSize="small" />
                    <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                      Dados cadastrais propostos
                    </Typography>
                  </Stack>
                  <Grid container spacing={2}>
                    <Grid size={{ xs: 12, sm: 6 }}>
                      <InfoField label="E-mail" value={selecionado.email} />
                    </Grid>
                    <Grid size={{ xs: 12, sm: 6 }}>
                      <InfoField label="Celular" value={formatPhone(selecionado.celular)} />
                    </Grid>
                    <Grid size={{ xs: 12 }}>
                      <InfoField
                        label="Endereço"
                        value={
                          [
                            selecionado.endereco?.logradouro,
                            selecionado.endereco?.cidade,
                            selecionado.endereco?.estado,
                          ]
                            .filter(Boolean)
                            .join(', ') || '-'
                        }
                      />
                    </Grid>
                  </Grid>
                </CardContent>
              </Card>
            )}

            <TextField
              autoFocus
              required
              fullWidth
              size="small"
              multiline
              minRows={3}
              label="Justificativa da decisão"
              placeholder="Descreva o motivo da aprovação ou rejeição..."
              value={justificativa}
              onChange={(e) => setJustificativa(e.target.value)}
            />
          </Stack>
        </DialogContent>
        <DialogActions sx={{ px: 3, py: 2 }}>
          <ActionButton
            title="Cancelar"
            icon={<CloseIcon />}
            onClick={() => setSelecionado(null)}
            color="secondary"
          />
          <ActionButton
            color="error"
            title="Rejeitar solicitação"
            label="Rejeitar"
            icon={<BlockIcon />}
            disabled={!justificativa.trim() || decidir.isPending}
            onClick={() => decidir.mutate({ aprovar: false })}
          />
          <ActionButton
            color="success"
            title="Aprovar solicitação"
            label="Aprovar"
            icon={<CheckCircleIcon />}
            disabled={!justificativa.trim() || decidir.isPending}
            onClick={() => decidir.mutate({ aprovar: true })}
          />
        </DialogActions>
      </Dialog>
    </Stack>
  );
};

export default AjustesCadastrais;
