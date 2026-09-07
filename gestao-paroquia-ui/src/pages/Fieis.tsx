import React, { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  Box,
  Card,
  CardContent,
  Chip,
  InputAdornment,
  Skeleton,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TablePagination,
  TableRow,
  TextField,
  Typography,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import BlockIcon from '@mui/icons-material/Block';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import EditIcon from '@mui/icons-material/Edit';
import PersonIcon from '@mui/icons-material/Person';
import SearchIcon from '@mui/icons-material/Search';
import VisibilityIcon from '@mui/icons-material/Visibility';
import ActionButton from '@/components/ActionButton';
import PageHeader from '@/components/PageHeader';
import StatusIcon from '@/components/StatusIcon';
import TableActionMenu from '@/components/TableActionMenu';
import {
  alterarStatusFiel,
  createFiel,
  getFiel,
  getFieis,
  updateFiel,
} from '@/api/fieis';
import {
  TipoRelacionamentoFamiliar,
  criarRelacionamentoFamiliar,
  listarFamiliares,
  removerRelacionamentoFamiliar,
} from '@/api/familiares';
import { listarAjustesPendentesDoFiel } from '@/api/ajustesCadastrais';
import { getUnidadesTerritoriais } from '@/api/unidadesTerritoriais';
import { useAuth } from '@/auth/AuthContext';
import { canManageCadastroFieis } from '@/auth/permissions';
import { useNotification } from '@/context/NotificationContext';
import {
  CreateFielPayload,
  Fiel,
  UpdateFielPayload,
} from '@/api/types';
import { getSituacaoAtivoMeta } from '@/utils/statusMeta/situacaoStatus';
import {
  FielCreateModal,
} from '@/components/fieis/FielCreateModal';
import {
  FielEditModal,
} from '@/components/fieis/FielEditModal';
import {
  FielInativarConfirmDialog,
} from '@/components/fieis/FielInativarConfirmDialog';
import {
  FielViewModal,
} from '@/components/fieis/FielViewModal';

/**
 * Página principal de Gestão de Fiéis.
 * Atua como orquestrador do ciclo cadastral de paroquianos, mantendo tabela,
 * paginação e busca, enquanto delega modais especializados para a pasta components/fieis.
 */
const Fieis: React.FC = () => {
  const [busca, setBusca] = useState('');
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [novo, setNovo] = useState(false);
  const [visualizando, setVisualizando] = useState<Fiel | null>(null);
  const [editando, setEditando] = useState<Fiel | null>(null);
  const [fielParaInativar, setFielParaInativar] = useState<Fiel | null>(null);

  const cliente = useQueryClient();
  const { showSuccess, showError } = useNotification();
  const { user } = useAuth();
  const podeGerirCadastro = canManageCadastroFieis(user);

  const fieis = useQuery({ queryKey: ['fieis'], queryFn: getFieis });
  const unidades = useQuery({
    queryKey: ['unidadesTerritoriais'],
    queryFn: getUnidadesTerritoriais,
  });

  const idAtivo = visualizando?.fielId ?? visualizando?.id ?? editando?.fielId ?? editando?.id;

  const detalhe = useQuery({
    queryKey: ['fiel', idAtivo],
    queryFn: () => getFiel(idAtivo!),
    enabled: Boolean(idAtivo),
  });

  const familiares = useQuery({
    queryKey: ['fiel-familiares', idAtivo],
    queryFn: () => listarFamiliares(idAtivo!),
    enabled: Boolean(idAtivo),
  });

  const ajustesPendentes = useQuery({
    queryKey: ['fiel-ajustes-pendentes', idAtivo],
    queryFn: () => listarAjustesPendentesDoFiel(idAtivo!),
    enabled: Boolean(idAtivo && podeGerirCadastro),
  });

  const criar = useMutation({
    mutationFn: (payload: CreateFielPayload) => createFiel(payload),
    onSuccess: () => {
      cliente.invalidateQueries({ queryKey: ['fieis'] });
      setNovo(false);
      showSuccess('Fiel cadastrado com sucesso!');
    },
    onError: (err: Error) => showError(err.message || 'Erro ao cadastrar fiel.'),
  });

  const salvarCadastro = useMutation({
    mutationFn: (payload: UpdateFielPayload) => {
      const fid = editando?.fielId ?? editando?.id;
      return updateFiel(fid!, payload);
    },
    onSuccess: () => {
      cliente.invalidateQueries({ queryKey: ['fieis'] });
      cliente.invalidateQueries({ queryKey: ['fiel', idAtivo] });
      setEditando(null);
      showSuccess('Cadastro atualizado com sucesso!');
    },
    onError: (err: Error) => showError(err.message || 'Erro ao salvar alterações.'),
  });

  const status = useMutation({
    mutationFn: ({ id: fid, ativo }: { id: string; ativo: boolean }) =>
      alterarStatusFiel(fid, ativo),
    onSuccess: () => {
      cliente.invalidateQueries({ queryKey: ['fieis'] });
      cliente.invalidateQueries({ queryKey: ['fiel', idAtivo] });
      showSuccess('Situação do fiel atualizada!');
    },
    onError: (err: Error) => showError(err.message || 'Erro ao alterar situação.'),
  });

  const criarFamiliarMutation = useMutation({
    mutationFn: ({ destinoId, tipoRelacao }: { destinoId: string; tipoRelacao: TipoRelacionamentoFamiliar }) =>
      criarRelacionamentoFamiliar(idAtivo!, destinoId, tipoRelacao),
    onSuccess: () => {
      cliente.invalidateQueries({ queryKey: ['fiel-familiares', idAtivo] });
      showSuccess('Vínculo familiar criado!');
    },
    onError: (err: Error) => showError(err.message || 'Erro ao vincular familiar.'),
  });

  const removerFamiliarMutation = useMutation({
    mutationFn: (relacionamentoId: string) =>
      removerRelacionamentoFamiliar(idAtivo!, relacionamentoId),
    onSuccess: () => {
      cliente.invalidateQueries({ queryKey: ['fiel-familiares', idAtivo] });
      showSuccess('Vínculo familiar inativado!');
    },
    onError: (err: Error) => showError(err.message || 'Erro ao inativar vínculo.'),
  });

  const lista = useMemo(() => {
    const raw = fieis.data ?? [];
    if (!busca.trim()) return raw;
    const termo = busca.toLowerCase();
    return raw.filter((f) => {
      const nome = f.nome?.toLowerCase() ?? '';
      const email = f.contato?.email?.toLowerCase() ?? f.email?.toLowerCase() ?? '';
      const cpf = f.dadosIdentificacao?.cpfFormatado?.toLowerCase() ?? f.cpf?.toLowerCase() ?? '';
      return nome.includes(termo) || email.includes(termo) || cpf.includes(termo);
    });
  }, [fieis.data, busca]);

  const listaPaginada = useMemo(() => {
    return lista.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage);
  }, [lista, page, rowsPerPage]);

  const handleInativarFiel = () => {
    if (!fielParaInativar) return;
    const fid = fielParaInativar.fielId ?? fielParaInativar.id;
    status.mutate(
      { id: fid, ativo: false },
      {
        onSuccess: () => {
          setFielParaInativar(null);
          if (visualizando && (visualizando.fielId ?? visualizando.id) === fid) {
            setVisualizando(null);
          }
        },
      }
    );
  };

  return (
    <Stack spacing={3}>
      <PageHeader
        icon={<PersonIcon sx={{ fontSize: 32, color: 'primary.main' }} />}
        title="Registro de Fiéis"
        subtitle="Gestão canônica e cadastral dos membros da comunidade paroquial"
        actions={
          podeGerirCadastro ? (
            <ActionButton
              title="Novo Fiel"
              icon={<AddIcon />}
              color="primary"
              onClick={() => setNovo(true)}
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
              placeholder="Pesquisar por nome ou e-mail..."
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
                  <TableCell sx={{ fontWeight: 600 }}>Nome completo</TableCell>
                  <TableCell sx={{ fontWeight: 600 }}>Comunidade / Matriz</TableCell>
                  <TableCell sx={{ fontWeight: 600 }}>Contato</TableCell>
                  <TableCell sx={{ fontWeight: 600 }}>Situação</TableCell>
                  <TableCell align="right" sx={{ fontWeight: 600 }}>
                    Ações
                  </TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {fieis.isLoading ? (
                  Array.from({ length: 5 }).map((_, idx) => (
                    <TableRow key={idx}>
                      <TableCell colSpan={5}>
                        <Skeleton height={40} />
                      </TableCell>
                    </TableRow>
                  ))
                ) : fieis.isError ? (
                  <TableRow>
                    <TableCell colSpan={5} align="center" sx={{ py: 3 }}>
                      <Typography color="error">Erro ao carregar lista de fiéis.</Typography>
                    </TableCell>
                  </TableRow>
                ) : listaPaginada.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={5} align="center" sx={{ py: 3 }}>
                      <Typography color="text.secondary">Nenhum fiel encontrado.</Typography>
                    </TableCell>
                  </TableRow>
                ) : (
                  listaPaginada.map((fiel) => {
                    const fid = fiel.fielId ?? fiel.id;
                    const isAtivo = Boolean(fiel.isAtivo ?? fiel.ativo);
                    return (
                      <TableRow
                        key={fid}
                        hover
                        tabIndex={0}
                        role="button"
                        onClick={() => setVisualizando(fiel)}
                        onKeyDown={(e) => {
                          if (e.key === 'Enter' || e.key === ' ') {
                            e.preventDefault();
                            setVisualizando(fiel);
                          }
                        }}
                        sx={{ cursor: 'pointer' }}
                      >
                        <TableCell sx={{ fontWeight: 600 }}>{fiel.nome}</TableCell>
                        <TableCell>{fiel.igrejaFrequenta?.nome ?? '-'}</TableCell>
                        <TableCell>
                          <Typography variant="body2">{fiel.contato?.email ?? fiel.email ?? '-'}</Typography>
                        </TableCell>
                        <TableCell>
                          <StatusIcon {...getSituacaoAtivoMeta(isAtivo)} />
                        </TableCell>
                        <TableCell align="right" onClick={(e) => e.stopPropagation()}>
                          <TableActionMenu
                            actions={[
                              {
                                label: 'Visualizar ficha',
                                icon: <VisibilityIcon fontSize="small" />,
                                onClick: () => setVisualizando(fiel),
                              },
                              ...(podeGerirCadastro
                                ? [
                                    {
                                      label: 'Editar cadastro',
                                      icon: <EditIcon fontSize="small" />,
                                      onClick: () => setEditando(fiel),
                                    },
                                    ...(isAtivo
                                      ? [
                                          {
                                            label: 'Desativar',
                                            icon: <BlockIcon fontSize="small" />,
                                            onClick: () => setFielParaInativar(fiel),
                                          },
                                        ]
                                      : [
                                          {
                                            label: 'Reativar',
                                            icon: <CheckCircleIcon fontSize="small" />,
                                            onClick: () => status.mutate({ id: fid, ativo: true }),
                                          },
                                        ]),
                                  ]
                                : []),
                            ]}
                          />
                        </TableCell>
                      </TableRow>
                    );
                  })
                )}
              </TableBody>
            </Table>
          </TableContainer>
          <TablePagination
            rowsPerPageOptions={[5, 10, 25]}
            component="div"
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
        </CardContent>
      </Card>

      {/* Modal de Criação */}
      <FielCreateModal
        open={novo}
        unidades={unidades.data ?? []}
        isPending={criar.isPending}
        onClose={() => setNovo(false)}
        onSave={(payload) => criar.mutate(payload)}
      />

      {/* Modal de Visualização da Ficha */}
      <FielViewModal
        open={Boolean(visualizando)}
        fiel={visualizando}
        detalheFiel={detalhe.data}
        familiares={familiares.data ?? []}
        ajustesPendentes={ajustesPendentes.data ?? []}
        podeGerirCadastro={podeGerirCadastro}
        onClose={() => setVisualizando(null)}
        onEdit={(f) => {
          setVisualizando(null);
          setEditando(f);
        }}
        onInativar={(f) => setFielParaInativar(f)}
      />

      {/* Modal de Edição */}
      <FielEditModal
        open={Boolean(editando)}
        fiel={editando}
        unidades={unidades.data ?? []}
        familiares={familiares.data ?? []}
        fieisDisponiveis={fieis.data ?? []}
        isPending={salvarCadastro.isPending}
        isVinculoPending={criarFamiliarMutation.isPending}
        onClose={() => setEditando(null)}
        onSave={(payload) => salvarCadastro.mutate(payload)}
        onVincularFamiliar={(destinoId, tipoRelacao) =>
          criarFamiliarMutation.mutate({ destinoId, tipoRelacao })
        }
        onRemoverFamiliar={(relacionamentoId) =>
          removerFamiliarMutation.mutate(relacionamentoId)
        }
      />

      {/* Diálogo de Confirmação de Inativação */}
      <FielInativarConfirmDialog
        open={Boolean(fielParaInativar)}
        fiel={fielParaInativar}
        isPending={status.isPending}
        onClose={() => setFielParaInativar(null)}
        onConfirm={handleInativarFiel}
      />
    </Stack>
  );
};

export default Fieis;
