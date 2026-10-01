import SearchableTablePagination from '@/shared/presentation/components/SearchableTablePagination';
import React, { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Box, Card, CardContent, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, Grid, Skeleton, Stack, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Typography } from '@mui/material';
import AccountBalanceIcon from '@mui/icons-material/AccountBalance';
import AddIcon from '@mui/icons-material/Add';
import ChurchIcon from '@mui/icons-material/Church';
import CloseIcon from '@mui/icons-material/Close';
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import VisibilityIcon from '@mui/icons-material/Visibility';
import ActionButton from '@/shared/presentation/components/ActionButton';
import PageHeader from '@/shared/presentation/components/PageHeader';
import SearchField from '@/shared/presentation/components/SearchField';
import StatusIcon from '@/shared/presentation/components/StatusIcon';
import TableActionMenu from '@/shared/presentation/components/TableActionMenu';
import {
  getUnidadeTerritorialTipoMeta,
  getSacrarioMeta,
  getPiaBatismalMeta,
  getAvcbStatusMeta,
} from '@/shared/utils/statusMeta/unidadeTerritorialStatus';
import { useAuth } from '@/contextos/iam/presentation/auth/AuthContext';
import { canManageUnidadesTerritoriais } from '@/contextos/iam/presentation/auth/permissions';
import { useNotification } from '@/shared/presentation/context/NotificationContext';
import { extractErrorMessage } from '@/shared/utils/errorUtils';
import { formatDateBr } from '@/shared/utils/masks';
import { DIALOG_CONTENT_SCROLL_SX } from '@/shared/presentation/theme/dialogStyles';
import { UnidadeTerritorial } from '@/shared/domain/types/types';
import {
  getUnidadesTerritoriais,
  createUnidadeTerritorial,
  updateUnidadeTerritorial,
  deleteUnidadeTerritorial,
  UnidadeTerritorialFormData,
} from '@/contextos/organizacao/infrastructure/http/unidadesTerritoriais';
import {
  getGovernoEclesiastico,
  saveGovernoEclesiastico,
  getDioceses,
  createDiocese,
  DioceseFormData,
  GovernoEclesiasticoFormData,
} from '@/contextos/clero/infrastructure/http/governoEclesiastico';
import { listarClerigos } from '@/contextos/clero/infrastructure/http/clerigos';
import UnidadeTerritorialFormModal from '../components/unidadesTerritoriais/UnidadeTerritorialFormModal';
import UnidadeTerritorialViewModal from '../components/unidadesTerritoriais/UnidadeTerritorialViewModal';
import GovernoEclesiasticoModal from '@/contextos/clero/presentation/components/governoEclesiastico/GovernoEclesiasticoModal';
import DioceseFormModal from '@/contextos/clero/presentation/components/governoEclesiastico/DioceseFormModal';

/**
 * Página do Módulo Governo & Território.
 *
 * ARQUITETURA DE DOMÍNIO & DESIGN SYSTEM:
 * - Contêiner raiz Stack spacing={3} (padrão canônico DESIGN_SYSTEM.md).
 * - PageHeader com ícone institucional e botões semânticos ActionButton.
 * - Card de Governo Eclesiástico estruturado com grid responsivo.
 * - Tabela de Unidades Territoriais com busca (SearchField), paginação (TablePagination),
 *   Skeletons de carregamento e linhas clicáveis abrindo UnidadeTerritorialViewModal.
 * - Indicadores visuais de Tipo, Sacrário, Pia Batismal e AVCB padronizados com StatusIcon.
 * - Ações da linha estritamente padronizadas com TableActionMenu (DESIGN_SYSTEM.md).
 */
export const UnidadesTerritoriais: React.FC = () => {
  const { user } = useAuth();
  const queryClient = useQueryClient();
  const { showNotification } = useNotification();
  const podeGerenciar = canManageUnidadesTerritoriais(user);

  // Estados de busca e paginação
  const [busca, setBusca] = useState('');
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);

  // Estados dos modais
  const [modalUnidadeOpen, setModalUnidadeOpen] = useState(false);
  const [unidadeSelecionada, setUnidadeSelecionada] = useState<UnidadeTerritorial | null>(null);
  const [unidadeVisualizando, setUnidadeVisualizando] = useState<UnidadeTerritorial | null>(null);

  const [modalGovernoOpen, setModalGovernoOpen] = useState(false);
  const [modalDioceseOpen, setModalDioceseOpen] = useState(false);

  const [deleteConfirmOpen, setDeleteConfirmOpen] = useState(false);
  const [unidadeParaExcluir, setUnidadeParaExcluir] = useState<UnidadeTerritorial | null>(null);

  // Queries
  const { data: unidades = [], isLoading: isLoadingUnidades } = useQuery({
    queryKey: ['unidades-territoriais'],
    queryFn: getUnidadesTerritoriais,
  });

  const { data: governo, isLoading: isLoadingGoverno } = useQuery({
    queryKey: ['governo-eclesiastico'],
    queryFn: getGovernoEclesiastico,
  });

  const { data: dioceses = [] } = useQuery({
    queryKey: ['dioceses'],
    queryFn: getDioceses,
  });

  const { data: clerigos = [] } = useQuery({
    queryKey: ['clerigos-ativos'],
    queryFn: listarClerigos,
  });

  // Mutations
  const createUnidadeMutation = useMutation({
    mutationFn: createUnidadeTerritorial,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['unidades-territoriais'] });
      showNotification('Unidade territorial criada com sucesso!', 'success');
      setModalUnidadeOpen(false);
    },
    onError: (err) => showNotification(extractErrorMessage(err, 'Erro ao criar unidade territorial'), 'error'),
  });

  const updateUnidadeMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: UnidadeTerritorialFormData }) =>
      updateUnidadeTerritorial(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['unidades-territoriais'] });
      showNotification('Unidade territorial atualizada com sucesso!', 'success');
      setModalUnidadeOpen(false);
      setUnidadeSelecionada(null);
    },
    onError: (err) => showNotification(extractErrorMessage(err, 'Erro ao atualizar unidade territorial'), 'error'),
  });

  const deleteUnidadeMutation = useMutation({
    mutationFn: deleteUnidadeTerritorial,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['unidades-territoriais'] });
      showNotification('Unidade territorial excluída com sucesso!', 'success');
      setDeleteConfirmOpen(false);
      setUnidadeParaExcluir(null);
    },
    onError: (err) => showNotification(extractErrorMessage(err, 'Erro ao excluir unidade territorial'), 'error'),
  });

  const saveGovernoMutation = useMutation({
    mutationFn: saveGovernoEclesiastico,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['governo-eclesiastico'] });
      showNotification('Governo eclesiástico salvo com sucesso!', 'success');
      setModalGovernoOpen(false);
    },
    onError: (err) => showNotification(extractErrorMessage(err, 'Erro ao salvar governo eclesiástico'), 'error'),
  });

  const createDioceseMutation = useMutation({
    mutationFn: createDiocese,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['dioceses'] });
      showNotification('Diocese cadastrada com sucesso!', 'success');
      setModalDioceseOpen(false);
    },
    onError: (err) => showNotification(extractErrorMessage(err, 'Erro ao cadastrar diocese'), 'error'),
  });

  const handleOpenNovaUnidade = () => {
    setUnidadeSelecionada(null);
    setModalUnidadeOpen(true);
  };

  const handleOpenEditarUnidade = (u: UnidadeTerritorial) => {
    setUnidadeSelecionada(u);
    setModalUnidadeOpen(true);
  };

  const handleOpenConfirmDelete = (u: UnidadeTerritorial) => {
    setUnidadeParaExcluir(u);
    setDeleteConfirmOpen(true);
  };

  const handleSaveUnidade = (data: UnidadeTerritorialFormData) => {
    if (unidadeSelecionada) {
      updateUnidadeMutation.mutate({ id: unidadeSelecionada.id, data });
    } else {
      createUnidadeMutation.mutate(data);
    }
  };

  const matrizesDisponiveis = useMemo(() => unidades.filter((u) => u.tipo === 'MATRIZ'), [unidades]);

  const unidadesFiltradas = useMemo(() => {
    if (!busca.trim()) return unidades;
    const termo = busca.toLowerCase();
    return unidades.filter(
      (u) =>
        (u.nome && u.nome.toLowerCase().includes(termo)) ||
        (u.padroeiro && u.padroeiro.toLowerCase().includes(termo)) ||
        (u.enderecoCidade && u.enderecoCidade.toLowerCase().includes(termo))
    );
  }, [unidades, busca]);

  const unidadesPaginadas = useMemo(() => {
    return unidadesFiltradas.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage);
  }, [unidadesFiltradas, page, rowsPerPage]);

  return (
    <Stack spacing={3}>
      <PageHeader
        title="Governo & Território"
        subtitle="Gestão canônica de circunscrição diocesana, provisão pastoral do Pároco e administração física de Igreja Matriz e Comunidades."
        icon={<ChurchIcon sx={{ fontSize: 32, color: 'primary.main' }} />}
        actions={
          podeGerenciar && (
            <Stack direction="row" spacing={1}>
              <ActionButton
                title="Nova Diocese"
                icon={<AccountBalanceIcon />}
                variant="outlined"
                color="secondary"
                onClick={() => setModalDioceseOpen(true)}
              />
              <ActionButton
                title="Governo Eclesiástico"
                icon={<ChurchIcon />}
                variant="outlined"
                color="primary"
                onClick={() => setModalGovernoOpen(true)}
              />
              <ActionButton
                title="Nova Unidade Territorial"
                icon={<AddIcon />}
                variant="contained"
                color="primary"
                onClick={handleOpenNovaUnidade}
              />
            </Stack>
          )
        }
      />

      {/* Card do Governo Eclesiástico */}
      <Card variant="outlined" sx={{ borderRadius: 2 }}>
        <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
          <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
            <ChurchIcon color="primary" fontSize="small" />
            <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
              Estrutura Canônica de Governo Paroquial
            </Typography>
          </Stack>

          {isLoadingGoverno ? (
            <Skeleton height={80} />
          ) : governo ? (
            <Grid container spacing={2}>
              <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                <Typography variant="caption" color="text.secondary" sx={{ display: 'block', fontWeight: 600 }}>
                  Diocese Circunscricional
                </Typography>
                <Typography variant="subtitle2" sx={{ fontWeight: 700 }}>
                  {governo.dioceseNome || 'Diocese não identificada'}
                </Typography>
                {governo.bispoDiocesano && (
                  <Typography variant="caption" color="text.secondary" sx={{ display: 'block' }}>
                    Bispo: {governo.bispoDiocesano}
                  </Typography>
                )}
              </Grid>

              <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                <Typography variant="caption" color="text.secondary" sx={{ display: 'block', fontWeight: 600 }}>
                  Pároco (Administrador Canônico)
                </Typography>
                <Typography variant="subtitle2" sx={{ fontWeight: 700 }}>
                  {governo.parocoNome || governo.parocoNomeEclesiastico || 'Pároco não vinculado'}
                </Typography>
                {governo.dataPosseParoco && (
                  <Typography variant="caption" color="text.secondary" sx={{ display: 'block' }}>
                    Posse Canônica: {formatDateBr(governo.dataPosseParoco)}
                  </Typography>
                )}
              </Grid>

              <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                <Typography variant="caption" color="text.secondary" sx={{ display: 'block', fontWeight: 600 }}>
                  Ereção e Instalação Paroquial
                </Typography>
                <Typography variant="body2">
                  <strong>Decreto:</strong> {governo.decretoErecaoParoquia || 'Não registrado'}
                </Typography>
                {governo.dataInstalacaoParoquia && (
                  <Typography variant="caption" color="text.secondary" sx={{ display: 'block' }}>
                    Instalada em: {formatDateBr(governo.dataInstalacaoParoquia)}
                  </Typography>
                )}
              </Grid>

              <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                <Typography variant="caption" color="text.secondary" sx={{ display: 'block', fontWeight: 600 }}>
                  Registro Eclesiástico
                </Typography>
                <Typography variant="body2">
                  <strong>Livro Tombo:</strong> {governo.livroTomboNumero || 'Não informado'}
                </Typography>
              </Grid>
            </Grid>
          ) : (
            <Typography variant="body2" color="text.secondary">
              O Governo Eclesiástico ainda não foi configurado para esta paróquia. Clique em &quot;Governo Eclesiástico&quot; para associar a Diocese e o Pároco.
            </Typography>
          )}
        </CardContent>
      </Card>

      {/* Tabela de Unidades Territoriais */}
      <Card>
        <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
          <Box sx={{ maxWidth: 400, mb: 2 }}>
            <SearchField
              value={busca}
              onChange={(val) => {
                setBusca(val);
                setPage(0);
              }}
              placeholder="Pesquisar por nome, padroeiro ou cidade..."
            />
          </Box>

          <TableContainer>
            <Table size="small">
              <TableHead sx={{ backgroundColor: 'action.hover' }}>
                <TableRow>
                  <TableCell sx={{ fontWeight: 600 }}>Nome da Unidade</TableCell>
                  <TableCell sx={{ fontWeight: 600 }}>Tipo</TableCell>
                  <TableCell sx={{ fontWeight: 600 }}>Padroeiro / Festa</TableCell>
                  <TableCell sx={{ fontWeight: 600 }}>Sacrário / Pia Batismal</TableCell>
                  <TableCell sx={{ fontWeight: 600 }}>Segurança AVCB</TableCell>
                  <TableCell sx={{ fontWeight: 600 }}>Cidade / UF</TableCell>
                  <TableCell align="right" sx={{ fontWeight: 600 }}>Ações</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {isLoadingUnidades ? (
                  Array.from({ length: 4 }).map((_, idx) => (
                    <TableRow key={idx}>
                      <TableCell colSpan={7}>
                        <Skeleton height={40} />
                      </TableCell>
                    </TableRow>
                  ))
                ) : unidadesPaginadas.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={7} align="center" sx={{ py: 4 }}>
                      <Typography color="text.secondary">
                        Nenhuma unidade territorial encontrada.
                      </Typography>
                    </TableCell>
                  </TableRow>
                ) : (
                  unidadesPaginadas.map((u) => (
                    <TableRow
                      key={u.id}
                      hover
                      tabIndex={0}
                      role="button"
                      onClick={() => setUnidadeVisualizando(u)}
                      onKeyDown={(e) => {
                        if (e.key === 'Enter' || e.key === ' ') {
                          e.preventDefault();
                          setUnidadeVisualizando(u);
                        }
                      }}
                      sx={{ cursor: 'pointer' }}
                    >
                      <TableCell>
                        <Typography variant="body2" sx={{ fontWeight: 700 }}>
                          {u.nome}
                        </Typography>
                        {u.tipo === 'COMUNIDADE' && u.parentId && (
                          <Typography variant="caption" color="text.secondary">
                            Vinculada à Matriz
                          </Typography>
                        )}
                      </TableCell>
                      <TableCell>
                        <StatusIcon {...getUnidadeTerritorialTipoMeta(u.tipo)} />
                      </TableCell>
                      <TableCell>
                        {u.padroeiro ? (
                          <>
                            <Typography variant="body2">{u.padroeiro}</Typography>
                            {u.dataFestaPadroeira && (
                              <Typography variant="caption" color="text.secondary">
                                Festa: {u.dataFestaPadroeira}
                              </Typography>
                            )}
                          </>
                        ) : (
                          '-'
                        )}
                      </TableCell>
                      <TableCell>
                        {!u.possuiSacrario && !u.possuiPiaBatismal ? (
                          <Typography variant="body2" color="text.secondary">-</Typography>
                        ) : (
                          <Stack direction="row" spacing={1.5} sx={{ alignItems: 'center' }}>
                            {u.possuiSacrario && <StatusIcon {...getSacrarioMeta(true)} />}
                            {u.possuiPiaBatismal && <StatusIcon {...getPiaBatismalMeta(true)} />}
                          </Stack>
                        )}
                      </TableCell>
                      <TableCell>
                        <StatusIcon {...getAvcbStatusMeta(u.avcbNumero, u.avcbValidade)} />
                      </TableCell>
                      <TableCell>
                        {u.enderecoCidade ? `${u.enderecoCidade} - ${u.enderecoUf || ''}` : '-'}
                      </TableCell>
                      <TableCell align="right" onClick={(e) => e.stopPropagation()}>
                        <TableActionMenu
                          actions={[
                            {
                              label: 'Visualizar detalhes',
                              icon: <VisibilityIcon fontSize="small" />,
                              onClick: () => setUnidadeVisualizando(u),
                            },
                            ...(podeGerenciar
                              ? [
                                  {
                                    label: 'Editar unidade',
                                    icon: <EditIcon fontSize="small" />,
                                    onClick: () => handleOpenEditarUnidade(u),
                                  },
                                  {
                                    label: 'Excluir unidade',
                                    icon: <DeleteIcon fontSize="small" />,
                                    onClick: () => handleOpenConfirmDelete(u),
                                  },
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
                count={unidadesFiltradas.length}
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

      {/* Modais */}
      {unidadeVisualizando && (
        <UnidadeTerritorialViewModal
          open={Boolean(unidadeVisualizando)}
          onClose={() => setUnidadeVisualizando(null)}
          unidade={unidadeVisualizando}
          clerigos={clerigos}
        />
      )}

      {modalUnidadeOpen && (
        <UnidadeTerritorialFormModal
          open={modalUnidadeOpen}
          onClose={() => setModalUnidadeOpen(false)}
          onSave={handleSaveUnidade}
          unidade={unidadeSelecionada}
          isPending={createUnidadeMutation.isPending || updateUnidadeMutation.isPending}
          matrizesDisponiveis={matrizesDisponiveis}
          clerigos={clerigos}
        />
      )}

      {modalGovernoOpen && (
        <GovernoEclesiasticoModal
          open={modalGovernoOpen}
          onClose={() => setModalGovernoOpen(false)}
          onSave={(data: GovernoEclesiasticoFormData) => saveGovernoMutation.mutate(data)}
          governo={governo}
          dioceses={dioceses}
          clerigos={clerigos}
          isPending={saveGovernoMutation.isPending}
        />
      )}

      {modalDioceseOpen && (
        <DioceseFormModal
          open={modalDioceseOpen}
          onClose={() => setModalDioceseOpen(false)}
          onSave={(data: DioceseFormData) => createDioceseMutation.mutate(data)}
          isPending={createDioceseMutation.isPending}
        />
      )}

      {/* Confirmação de Exclusão */}
      <Dialog open={deleteConfirmOpen} onClose={() => setDeleteConfirmOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Confirmar Exclusão</DialogTitle>
        <DialogContent dividers sx={DIALOG_CONTENT_SCROLL_SX}>
          <DialogContentText>
            Tem certeza de que deseja remover a unidade territorial{' '}
            <strong>{unidadeParaExcluir?.nome}</strong>? Essa operação não pode ser desfeita.
          </DialogContentText>
        </DialogContent>
        <DialogActions sx={{ px: 3, py: 2 }}>
          <ActionButton
            title="Cancelar"
            icon={<CloseIcon />}
            onClick={() => setDeleteConfirmOpen(false)}
            color="secondary"
          />
          <ActionButton
            title="Excluir"
            icon={<DeleteIcon />}
            onClick={() => unidadeParaExcluir && deleteUnidadeMutation.mutate(unidadeParaExcluir.id)}
            color="error"
            disabled={deleteUnidadeMutation.isPending}
          />
        </DialogActions>
      </Dialog>
    </Stack>
  );
};

export default UnidadesTerritoriais;
