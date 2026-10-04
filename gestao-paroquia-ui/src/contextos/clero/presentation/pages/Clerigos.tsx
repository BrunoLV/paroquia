import SearchableTablePagination from '@/shared/presentation/components/SearchableTablePagination';
import React, { useMemo, useState } from 'react';
import { Box, Card, CardContent, InputAdornment, Skeleton, Stack, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, TextField, Typography } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import BlockIcon from '@mui/icons-material/Block';
import EditIcon from '@mui/icons-material/Edit';
import BadgeIcon from '@mui/icons-material/Badge';
import SearchIcon from '@mui/icons-material/Search';
import VisibilityIcon from '@mui/icons-material/Visibility';
import ActionButton from '@/shared/presentation/components/ActionButton';
import PageHeader from '@/shared/presentation/components/PageHeader';
import StatusIcon from '@/shared/presentation/components/StatusIcon';
import TableActionMenu from '@/shared/presentation/components/TableActionMenu';
import { useAuth } from '@/contextos/iam/presentation/auth/AuthContext';
import { canManageClero } from '@/contextos/iam/presentation/auth/permissions';
import { useNotification } from '@/shared/presentation/context/NotificationContext';
import { useClerigos } from '@/contextos/clero/presentation/hooks/useClerigos';
import type { Clerigo, ClerigoInstitucional, ClerigoPayload } from '@/shared/domain/types/types';
import { getFuncaoEclesiasticaMeta, getGrauOrdemMeta } from '@/shared/utils/statusMeta/cleroStatus';
import { getSituacaoAtivoMeta } from '@/shared/utils/statusMeta/situacaoStatus';
import {
  ClerigoFormModal,
} from '@/contextos/clero/presentation/components/clerigos/ClerigoFormModal';
import {
  ClerigoInativarDialog,
} from '@/contextos/clero/presentation/components/clerigos/ClerigoInativarDialog';
import {
  ClerigoViewModal,
} from '@/contextos/clero/presentation/components/clerigos/ClerigoViewModal';

/**
 * Página principal de Gestão do Clero.
 * Atua como orquestrador fino de listagem, delegação de ações e filtragem,
 * delegando os formulários, visualização e diálogos de confirmação a componentes isolados.
 */
const Clerigos: React.FC = () => {
  const { user } = useAuth();
  const podeGerenciarClero = canManageClero(user);
  const { showSuccess, showError } = useNotification();
  const { consulta, diretorio, criar, atualizar, inativar } = useClerigos();

  const [busca, setBusca] = useState('');
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [dialogCadastroAberto, setDialogCadastroAberto] = useState(false);
  const [clerigoVisualizando, setClerigoVisualizando] = useState<Clerigo | ClerigoInstitucional | null>(null);
  const [clerigoEditando, setClerigoEditando] = useState<Clerigo | null>(null);
  const [clerigoParaInativar, setClerigoParaInativar] = useState<Clerigo | null>(null);

  const fonteDados = podeGerenciarClero ? consulta : diretorio;
  const isLoading = fonteDados.isLoading;
  const isError = fonteDados.isError;
  const dados = fonteDados.data || [];

  const linhas = useMemo(() => {
    if (!busca.trim()) return dados;
    const termo = busca.toLowerCase();
    return dados.filter((item) => {
      const eclesiastico = (item.nomeEclesiastico || '').toLowerCase();
      const paroquia = ('paroquia' in item && typeof item.paroquia === 'string' ? item.paroquia : '').toLowerCase();
      const civil = ('nomeCivil' in item && typeof item.nomeCivil === 'string' ? item.nomeCivil : '').toLowerCase();
      const cpf = ('cpfFormatado' in item && typeof item.cpfFormatado === 'string' ? item.cpfFormatado : '').toLowerCase();
      return (
        eclesiastico.includes(termo) ||
        paroquia.includes(termo) ||
        civil.includes(termo) ||
        cpf.includes(termo)
      );
    });
  }, [dados, busca]);

  const linhasPaginadas = useMemo(() => {
    return linhas.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage);
  }, [linhas, page, rowsPerPage]);

  const handleSalvarCadastro = (payload: ClerigoPayload) => {
    criar.mutate(payload, {
      onSuccess: () => {
        showSuccess('Clérigo cadastrado com sucesso!');
        setDialogCadastroAberto(false);
      },
      onError: (err: Error) => {
        showError(err.message || 'Erro ao cadastrar clérigo.');
      },
    });
  };

  const handleSalvarEdicao = (payload: ClerigoPayload) => {
    if (!clerigoEditando) return;
    atualizar.mutate(
      { id: clerigoEditando.clerigoId, payload },
      {
        onSuccess: () => {
          showSuccess('Cadastro atualizado com sucesso!');
          setClerigoEditando(null);
        },
        onError: (err: Error) => {
          showError(err.message || 'Erro ao atualizar cadastro.');
        },
      }
    );
  };

  const handleConfirmarInativacao = () => {
    if (!clerigoParaInativar) return;
    inativar.mutate(clerigoParaInativar.clerigoId, {
      onSuccess: () => {
        showSuccess('Clérigo inativado com sucesso!');
        setClerigoParaInativar(null);
        if (clerigoVisualizando?.clerigoId === clerigoParaInativar.clerigoId) {
          setClerigoVisualizando(null);
        }
      },
      onError: (err: Error) => {
        showError(err.message || 'Erro ao inativar clérigo.');
      },
    });
  };

  return (
    <Stack spacing={3}>
      <PageHeader
        icon={<BadgeIcon sx={{ fontSize: 32, color: 'primary.main' }} />}
        title={podeGerenciarClero ? "Quadro do Clero" : "Diretório do Clero"}
        subtitle={
          podeGerenciarClero
            ? 'Gestão canônica, provisões e histórico pastoral dos clérigos'
            : 'Diretório eclesiástico do clero paroquial'
        }
        actions={
          podeGerenciarClero ? (
            <ActionButton
              title="Novo clérigo"
              icon={<AddIcon />}
              color="primary"
              onClick={() => setDialogCadastroAberto(true)}
            />
          ) : undefined
        }
      />

      <Card>
        <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
          <Box sx={{ maxWidth: 400, mb: 2 }}>
            <TextField
              fullWidth
              size="small"
              placeholder="Pesquisar por nome ou paróquia..."
              value={busca}
              onChange={(e) => {
                setBusca(e.target.value);
                setPage(0);
              }}
              slotProps={{
                input: {
                  startAdornment: (
                    <InputAdornment position="start">
                      <SearchIcon color="action" />
                    </InputAdornment>
                  ),
                },
              }}
            />
          </Box>

          <TableContainer>
            <Table size="small">
              <TableHead sx={{ backgroundColor: 'action.hover' }}>
                <TableRow>
                  <TableCell sx={{ fontWeight: 600 }}>Nome eclesiástico</TableCell>
                  <TableCell sx={{ fontWeight: 600 }}>Grau da ordem</TableCell>
                  <TableCell sx={{ fontWeight: 600 }}>Encargo atual</TableCell>
                  {podeGerenciarClero ? (
                    <>
                      <TableCell sx={{ fontWeight: 600 }}>Histórico e ordenações</TableCell>
                      <TableCell sx={{ fontWeight: 600 }}>Situação</TableCell>
                    </>
                  ) : (
                    <TableCell sx={{ fontWeight: 600 }}>Paróquia</TableCell>
                  )}
                  <TableCell align="right" sx={{ fontWeight: 600 }}>
                    Ações
                  </TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {isLoading ? (
                  Array.from({ length: 5 }).map((_, idx) => (
                    <TableRow key={idx}>
                      <TableCell colSpan={podeGerenciarClero ? 6 : 5}>
                        <Skeleton height={40} />
                      </TableCell>
                    </TableRow>
                  ))
                ) : isError ? (
                  <TableRow>
                    <TableCell colSpan={podeGerenciarClero ? 6 : 5} align="center" sx={{ py: 3 }}>
                      <Typography color="error">Erro ao carregar dados do clero.</Typography>
                    </TableCell>
                  </TableRow>
                ) : linhasPaginadas.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={podeGerenciarClero ? 6 : 5} align="center" sx={{ py: 3 }}>
                      <Typography color="text.secondary">Nenhum clérigo encontrado.</Typography>
                    </TableCell>
                  </TableRow>
                ) : (
                  linhasPaginadas.map((clerigo) => (
                    <TableRow
                      key={clerigo.clerigoId}
                      hover
                      tabIndex={0}
                      role="button"
                      onClick={() => setClerigoVisualizando(clerigo)}
                      onKeyDown={(e) => {
                        if (e.key === 'Enter' || e.key === ' ') {
                          e.preventDefault();
                          setClerigoVisualizando(clerigo);
                        }
                      }}
                      sx={{ cursor: 'pointer' }}
                    >
                      <TableCell sx={{ fontWeight: 600 }}>{clerigo.nomeEclesiastico}</TableCell>
                      <TableCell>
                        <StatusIcon {...getGrauOrdemMeta(clerigo.grauOrdem)} />
                      </TableCell>
                      <TableCell>
                        <StatusIcon {...getFuncaoEclesiasticaMeta(clerigo.funcaoAtual)} />
                      </TableCell>
                      {podeGerenciarClero ? (
                        <>
                          <TableCell>
                            <Typography variant="body2" color="text.secondary">
                              {'dataOrdenacaoPresbiteral' in clerigo && clerigo.dataOrdenacaoPresbiteral
                                ? 'Ordenação presbiteral registrada'
                                : 'dataOrdenacaoDiaconal' in clerigo && clerigo.dataOrdenacaoDiaconal
                                ? 'Ordenação diaconal registrada'
                                : 'Sem ordenações'}
                            </Typography>
                          </TableCell>
                          <TableCell>
                            {'isAtivo' in clerigo && (
                              <StatusIcon {...getSituacaoAtivoMeta((clerigo as Clerigo).isAtivo)} />
                            )}
                          </TableCell>
                        </>
                      ) : (
                        <TableCell>{('paroquia' in clerigo && clerigo.paroquia) || '-'}</TableCell>
                      )}
                      <TableCell align="right" onClick={(e) => e.stopPropagation()}>
                        <TableActionMenu
                          actions={[
                            {
                              label: 'Visualizar ficha',
                              icon: <VisibilityIcon fontSize="small" />,
                              onClick: () => setClerigoVisualizando(clerigo),
                            },
                            ...(podeGerenciarClero && 'nomeCivil' in clerigo
                              ? [
                                  {
                                    label: 'Editar cadastro',
                                    icon: <EditIcon fontSize="small" />,
                                    onClick: () => setClerigoEditando(clerigo as Clerigo),
                                  },
                                  ...((clerigo as Clerigo).isAtivo
                                    ? [
                                        {
                                          label: 'Inativar',
                                          icon: <BlockIcon fontSize="small" />,
                                          onClick: () => setClerigoParaInativar(clerigo as Clerigo),
                                        },
                                      ]
                                    : []),
                                ]
                              : []),
                          ]}
                        />
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
              <SearchableTablePagination colSpan={7}
                rowsPerPageOptions={[5, 10, 25]}
                count={linhas.length}
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

      {/* Modal de Criação */}
      <ClerigoFormModal
        open={dialogCadastroAberto}
        onClose={() => setDialogCadastroAberto(false)}
        onSubmit={handleSalvarCadastro}
        isPending={criar.isPending}
      />

      {/* Modal de Edição */}
      <ClerigoFormModal
        open={Boolean(clerigoEditando)}
        initialData={clerigoEditando}
        onClose={() => setClerigoEditando(null)}
        onSubmit={handleSalvarEdicao}
        isPending={atualizar.isPending}
      />

      {/* Modal de Visualização da Ficha */}
      <ClerigoViewModal
        open={Boolean(clerigoVisualizando)}
        clerigo={clerigoVisualizando}
        podeGerenciarClero={podeGerenciarClero}
        onClose={() => setClerigoVisualizando(null)}
        onEdit={(c) => {
          setClerigoVisualizando(null);
          setClerigoEditando(c);
        }}
        onInativar={(c) => setClerigoParaInativar(c)}
      />

      {/* Diálogo de Confirmação de Inativação */}
      <ClerigoInativarDialog
        open={Boolean(clerigoParaInativar)}
        clerigo={clerigoParaInativar as never}
        isPending={inativar.isPending}
        onClose={() => setClerigoParaInativar(null)}
        onConfirm={handleConfirmarInativacao}
      />
    </Stack>
  );
};

export default Clerigos;
