import TableActionMenu from '@/shared/presentation/components/TableActionMenu';
import SearchableTablePagination from '@/shared/presentation/components/SearchableTablePagination';
import { useState } from 'react';
import LidaIcon from '@mui/icons-material/MarkEmailRead';
import NovaIcon from '@mui/icons-material/MarkEmailUnread';
import NotificacaoIcon from '@mui/icons-material/Notifications';
import { Card, CardContent, Skeleton, Stack, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Typography } from '@mui/material';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { listarNotificacoes, marcarNotificacaoLida } from '@/contextos/iam/infrastructure/http/notificacoes';
import PageHeader from '@/shared/presentation/components/PageHeader';
import StatusIcon from '@/shared/presentation/components/StatusIcon';
import { useNotification } from '@/shared/presentation/context/NotificationContext';

const Notificacoes = () => {
  const client = useQueryClient();
  const { showSuccess, showError } = useNotification();
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);

  const itens = useQuery({
    queryKey: ['notificacoes'],
    queryFn: listarNotificacoes,
  });

  const marcar = useMutation({
    mutationFn: marcarNotificacaoLida,
    onSuccess: () => {
      showSuccess('Notificação marcada como lida.');
      client.invalidateQueries({ queryKey: ['notificacoes'] });
    },
    onError: () => {
      showError('Não foi possível marcar a notificação como lida.');
    },
  });

  if (itens.isLoading) {
    return <Skeleton variant="rounded" height={320} />;
  }

  const lista = itens.data ?? [];

  return (
    <Stack spacing={3}>
      <PageHeader
        icon={<NotificacaoIcon sx={{ fontSize: 32, color: 'primary.main' }} />}
        title="Notificações"
        subtitle="Decisões e avisos da paróquia e do seu cadastro."
      />

      <Card>
        <CardContent sx={{ p: 0 }}>
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Mensagem</TableCell>
                  <TableCell>Recebida em</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell align="right">Ações</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {lista.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={4} align="center">
                      <Typography variant="body2" color="text.secondary" sx={{ py: 3 }}>
                        Nenhuma notificação encontrada.
                      </Typography>
                    </TableCell>
                  </TableRow>
                ) : (
                  lista
                    .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
                    .map((n) => {
                      const lida = Boolean(n.lidaEmUtc);
                      return (
                        <TableRow key={n.id} hover>
                          <TableCell>
                            <Typography
                              variant="body2"
                              sx={{ fontWeight: lida ? 400 : 700 }}
                            >
                              {n.mensagem}
                            </Typography>
                          </TableCell>
                          <TableCell>
                            <Typography variant="caption" color="text.secondary">
                              {n.criadaEmUtc
                                ? new Date(n.criadaEmUtc).toLocaleString('pt-BR')
                                : '-'}
                            </Typography>
                          </TableCell>
                          <TableCell>
                            <StatusIcon
                              label={lida ? 'Lida' : 'Nova'}
                              color={lida ? 'default' : 'primary'}
                              icon={lida ? <LidaIcon /> : <NovaIcon />}
                              tooltip={lida ? 'Notificação já lida' : 'Notificação ainda não lida'}
                            />
                          </TableCell>
                          <TableCell align="right">
                            {!lida && (
                              <TableActionMenu actions={[{
                                label: "Marcar notificação como lida", icon: <LidaIcon fontSize="small" color="primary" />,
                                onClick: () => marcar.mutate(n.id), disabled: marcar.isPending,
                              }]} />
                            )}
                          </TableCell>
                        </TableRow>
                      );
                    })
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
        </CardContent>
      </Card>
    </Stack>
  );
};

export default Notificacoes;
