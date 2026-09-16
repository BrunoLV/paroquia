import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Stack } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import BuildIcon from '@mui/icons-material/Build';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import ReservarIcon from '@mui/icons-material/EventAvailable';
import InventoryIcon from '@mui/icons-material/Inventory';
import VisibilityIcon from '@mui/icons-material/Visibility';
import { useAuth } from '@/auth/AuthContext';
import { canExcluirAtivo, canManageAtivos, canManageManutencaoAtivo, isGlobalCreator } from '@/auth/permissions';
import {
  cadastrarAtivo,
  concluirManutencao,
  editarDescricaoAtivo,
  enviarParaManutencao,
  excluirAtivo,
  getAtivosPorOrganizacao,
} from '@/api/ativos';
import { getOrganizacoes } from '@/api/organizacoes';
import {
  AtivoCategoria,
  AtivoPatrimonial,
  CadastrarAtivoPayload,
  EnviarManutencaoPayload,
  Organizacao,
} from '@/api/types';
import { useNotification } from '@/context/NotificationContext';
import PageHeader from '@/components/PageHeader';
import ActionButton from '@/components/ActionButton';
import { TableAction } from '@/components/TableActionMenu';
import AtivosFilterBar from '@/components/ativos/AtivosFilterBar';
import AtivosTable from '@/components/ativos/AtivosTable';
import AtivoViewModal from '@/components/ativos/AtivoViewModal';
import AtivoFormModal from '@/components/ativos/AtivoFormModal';
import AtivoEditModal from '@/components/ativos/AtivoEditModal';
import AtivoManutencaoModal from '@/components/ativos/AtivoManutencaoModal';
import AtivoExcluirConfirmDialog from '@/components/ativos/AtivoExcluirConfirmDialog';
import { extractErrorMessage } from '@/utils/errorUtils';

const isOrganizacaoElegivelProprietaria = (org: Organizacao): boolean =>
  org.tipo === 'MATRIZ' || org.tipo === 'COMUNIDADE';

const CATEGORIAS: AtivoCategoria[] = ['ELETRONICO', 'MOBILIARIO', 'PARAMENTO', 'INFRAESTRUTURA'];

const Ativos = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { showError, showSuccess } = useNotification();
  const queryClient = useQueryClient();

  const [selectedOrgId, setSelectedOrgId] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  const [categoryFilter, setCategoryFilter] = useState<string>('TODAS');
  const [statusFilter, setStatusFilter] = useState<string>('TODOS');
  const [apenasReservaveis, setApenasReservaveis] = useState(false);
  const [page, setPage] = useState<number>(0);
  const [rowsPerPage, setRowsPerPage] = useState<number>(10);

  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [viewAtivo, setViewAtivo] = useState<AtivoPatrimonial | null>(null);
  const [editAtivo, setEditAtivo] = useState<AtivoPatrimonial | null>(null);
  const [manutencaoAtivo, setManutencaoAtivo] = useState<AtivoPatrimonial | null>(null);
  const [excluirAtivoAlvo, setExcluirAtivoAlvo] = useState<AtivoPatrimonial | null>(null);

  const { data: organizations = [] } = useQuery({
    queryKey: ['organizacoes'],
    queryFn: getOrganizacoes,
  });

  const { data: ativos = [], isLoading } = useQuery({
    queryKey: ['ativos', selectedOrgId],
    queryFn: () => getAtivosPorOrganizacao(selectedOrgId),
    enabled: Boolean(selectedOrgId),
  });

  const getSelectableOrgs = () => {
    if (isGlobalCreator(user)) {
      return organizations;
    }
    if (user && Array.isArray(user.organizations)) {
      const userOrgIds = user.organizations.map((o: { id: string }) => o.id);
      return organizations.filter((o) => userOrgIds.includes(o.id));
    }
    return [];
  };

  const getOrganizacoesElegiveisProprietarias = () =>
    getSelectableOrgs().filter(isOrganizacaoElegivelProprietaria);

  const filteredAtivos = useMemo(() => {
    return ativos.filter((a) => {
      const search = searchTerm.trim().toLowerCase();
      const matchesSearch =
        !search ||
        a.nome.toLowerCase().includes(search) ||
        a.codigoTombamento.toLowerCase().includes(search);
      const matchesCategory = categoryFilter === 'TODAS' || a.categoria === categoryFilter;
      const matchesStatus = statusFilter === 'TODOS' || a.status === statusFilter;
      const matchesReservavel = !apenasReservaveis || a.isReservavel;
      return matchesSearch && matchesCategory && matchesStatus && matchesReservavel;
    });
  }, [ativos, searchTerm, categoryFilter, statusFilter, apenasReservaveis]);

  useEffect(() => {
    if (!selectedOrgId && organizations.length > 0) {
      const elegiveis = getOrganizacoesElegiveisProprietarias();
      if (elegiveis.length > 0) {
        setSelectedOrgId(elegiveis[0].id);
      }
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [organizations, selectedOrgId]);

  const invalidateAtivos = () => queryClient.invalidateQueries({ queryKey: ['ativos'] });

  const cadastrarMutation = useMutation({
    mutationFn: (payload: CadastrarAtivoPayload) => cadastrarAtivo(payload),
    onSuccess: () => {
      showSuccess('Ativo cadastrado com sucesso!');
      setCreateDialogOpen(false);
      invalidateAtivos();
    },
    onError: (err) => showError(extractErrorMessage(err, 'Erro ao cadastrar ativo.')),
  });

  const editarDescricaoMutation = useMutation({
    mutationFn: ({ id, descricao }: { id: string; descricao: string | null }) =>
      editarDescricaoAtivo(id, { descricao }),
    onSuccess: () => {
      showSuccess('Descrição atualizada.');
      setEditAtivo(null);
      invalidateAtivos();
    },
    onError: (err) => showError(extractErrorMessage(err, 'Erro ao atualizar a descrição.')),
  });

  const enviarManutencaoMutation = useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: EnviarManutencaoPayload }) =>
      enviarParaManutencao(id, payload),
    onSuccess: () => {
      showSuccess('Ativo enviado para manutenção.');
      setManutencaoAtivo(null);
      invalidateAtivos();
      queryClient.invalidateQueries({ queryKey: ['ativo-manutencoes'] });
    },
    onError: (err) => showError(extractErrorMessage(err, 'Erro ao enviar para manutenção.')),
  });

  const concluirManutencaoMutation = useMutation({
    mutationFn: (id: string) => concluirManutencao(id),
    onSuccess: () => {
      showSuccess('Manutenção concluída — ativo disponível novamente.');
      invalidateAtivos();
      queryClient.invalidateQueries({ queryKey: ['ativo-manutencoes'] });
    },
    onError: (err) => showError(extractErrorMessage(err, 'Erro ao concluir a manutenção.')),
  });

  const excluirMutation = useMutation({
    mutationFn: (id: string) => excluirAtivo(id),
    onSuccess: () => {
      showSuccess('Ativo excluído.');
      setExcluirAtivoAlvo(null);
      invalidateAtivos();
    },
    onError: (err) => showError(extractErrorMessage(err, 'Erro ao excluir o ativo.')),
  });

  const buildRowActions = (ativo: AtivoPatrimonial): TableAction[] => {
    const actions: TableAction[] = [];
    const podeGerir = canManageManutencaoAtivo(user, ativo.grupoParoquialId);

    if (ativo.isReservavel && ativo.status === 'DISPONIVEL') {
      actions.push({
        label: 'Reservar',
        icon: <ReservarIcon fontSize="small" color="primary" />,
        onClick: () => navigate('/reservas', { state: { tipoItem: 'ATIVO', itemId: ativo.id } }),
      });
    }
    actions.push({
      label: 'Visualizar',
      icon: <VisibilityIcon fontSize="small" />,
      onClick: () => setViewAtivo(ativo),
    });
    if (podeGerir && ativo.status === 'DISPONIVEL') {
      actions.push({
        label: 'Enviar para Manutenção',
        icon: <BuildIcon fontSize="small" color="warning" />,
        onClick: () => setManutencaoAtivo(ativo),
      });
    }
    if (podeGerir && ativo.status === 'EM_MANUTENCAO') {
      actions.push({
        label: 'Concluir Manutenção',
        icon: <CheckCircleIcon fontSize="small" color="success" />,
        onClick: () => concluirManutencaoMutation.mutate(ativo.id),
      });
    }
    if (canManageAtivos(user)) {
      actions.push({
        label: 'Editar',
        icon: <EditIcon fontSize="small" />,
        onClick: () => setEditAtivo(ativo),
      });
    }
    if (canExcluirAtivo(user, ativo.grupoParoquialId)) {
      actions.push({
        label: 'Excluir',
        icon: <DeleteIcon fontSize="small" color="error" />,
        onClick: () => setExcluirAtivoAlvo(ativo),
      });
    }
    return actions;
  };

  const orgsElegiveis = getOrganizacoesElegiveisProprietarias();

  return (
    <Stack spacing={3}>
      <PageHeader
        icon={<InventoryIcon sx={{ fontSize: 32, color: 'primary.main' }} />}
        title="Ativos Patrimoniais"
        subtitle="Cadastro, manutenção e ciclo de vida dos bens patrimoniais da paróquia."
        actions={
          canManageAtivos(user) && (
            <ActionButton
              title="Novo Ativo"
              icon={<AddIcon />}
              color="primary"
              disabled={!selectedOrgId}
              onClick={() => setCreateDialogOpen(true)}
              sx={{ border: '1px solid', borderColor: 'primary.main' }}
            />
          )
        }
      />

      <AtivosFilterBar
        organizacoes={orgsElegiveis}
        selectedOrgId={selectedOrgId}
        onOrgChange={(val) => { setSelectedOrgId(val); setPage(0); }}
        categorias={CATEGORIAS}
        categoryFilter={categoryFilter}
        onCategoryFilterChange={(val) => { setCategoryFilter(val); setPage(0); }}
        statusFilter={statusFilter}
        onStatusFilterChange={(val) => { setStatusFilter(val); setPage(0); }}
        apenasReservaveis={apenasReservaveis}
        onApenasReservaveisChange={(val) => { setApenasReservaveis(val); setPage(0); }}
        searchTerm={searchTerm}
        onSearchChange={(val) => { setSearchTerm(val); setPage(0); }}
      />

      <AtivosTable
        ativos={filteredAtivos}
        temAtivosCadastrados={ativos.length > 0}
        isLoading={isLoading}
        page={page}
        rowsPerPage={rowsPerPage}
        onPageChange={setPage}
        onRowsPerPageChange={setRowsPerPage}
        rowActions={buildRowActions}
      />

      <AtivoViewModal
        open={!!viewAtivo}
        ativo={viewAtivo}
        organizacoes={organizations}
        canManage={canManageAtivos(user)}
        onClose={() => setViewAtivo(null)}
        onEdit={setEditAtivo}
      />

      <AtivoFormModal
        open={createDialogOpen}
        onClose={() => setCreateDialogOpen(false)}
        onSave={(payload) => cadastrarMutation.mutate(payload)}
        organizacoes={orgsElegiveis}
        categorias={CATEGORIAS}
        defaultOrgId={
          orgsElegiveis.some((org) => org.id === selectedOrgId) ? selectedOrgId : orgsElegiveis[0]?.id
        }
        isPending={cadastrarMutation.isPending}
      />

      <AtivoEditModal
        open={!!editAtivo}
        ativo={editAtivo}
        isPending={editarDescricaoMutation.isPending}
        onClose={() => setEditAtivo(null)}
        onSave={(descricao) => {
          if (editAtivo) editarDescricaoMutation.mutate({ id: editAtivo.id, descricao });
        }}
      />

      <AtivoManutencaoModal
        open={!!manutencaoAtivo}
        ativo={manutencaoAtivo}
        isPending={enviarManutencaoMutation.isPending}
        onClose={() => setManutencaoAtivo(null)}
        onSubmit={(payload) => {
          if (manutencaoAtivo) enviarManutencaoMutation.mutate({ id: manutencaoAtivo.id, payload });
        }}
      />

      <AtivoExcluirConfirmDialog
        open={!!excluirAtivoAlvo}
        ativo={excluirAtivoAlvo}
        isPending={excluirMutation.isPending}
        onClose={() => setExcluirAtivoAlvo(null)}
        onConfirm={() => {
          if (excluirAtivoAlvo) excluirMutation.mutate(excluirAtivoAlvo.id);
        }}
      />
    </Stack>
  );
};

export default Ativos;
