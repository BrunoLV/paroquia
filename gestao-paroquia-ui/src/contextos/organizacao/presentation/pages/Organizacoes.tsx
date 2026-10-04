import React, { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Container } from '@mui/material';
import GroupAddIcon from '@mui/icons-material/GroupAdd';
import VolunteerActivismIcon from '@mui/icons-material/VolunteerActivism';
import { createOrganizacao, deleteOrganizacao, getOrganizacoes, updateOrganizacao } from '@/contextos/organizacao/infrastructure/http/organizacoes';
import { getUsuarios } from '@/contextos/iam/infrastructure/http/usuarios';
import { getPastoraisOficiais } from '@/contextos/organizacao/infrastructure/http/pastoraisOficiais';
import { Organizacao } from '@/shared/domain/types/types';
import { useAuth } from '@/contextos/iam/presentation/auth/AuthContext';
import { canManageOrganizacoes } from '@/contextos/iam/presentation/auth/permissions';
import { useNotification } from '@/shared/presentation/context/NotificationContext';
import { extractErrorMessage } from '@/shared/utils/errorUtils';
import ActionButton from '@/shared/presentation/components/ActionButton';
import PageHeader from '@/shared/presentation/components/PageHeader';
import { OrganizacoesFilterBar } from '@/contextos/organizacao/presentation/components/organizacoes/OrganizacoesFilterBar';
import { OrganizacoesTable } from '@/contextos/organizacao/presentation/components/organizacoes/OrganizacoesTable';
import { OrganizacaoMembrosModal } from '@/contextos/organizacao/presentation/components/organizacoes/OrganizacaoMembrosModal';
import OrganizacaoFormModal from '@/contextos/organizacao/presentation/pages/OrganizacaoFormModal';

const Organizacoes: React.FC = () => {
  const { user } = useAuth();
  const { showError, showSuccess } = useNotification();
  const queryClient = useQueryClient();
  const isAuthorized = canManageOrganizacoes(user);

  const [searchTerm, setSearchTerm] = useState('');
  const [tipoFilter, setTipoFilter] = useState('TODOS');
  const [statusFilter, setStatusFilter] = useState('TODOS');
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);

  const [modalOpen, setModalOpen] = useState(false);
  const [selectedOrg, setSelectedOrg] = useState<Organizacao | null>(null);

  const [membrosModalOpen, setMembrosModalOpen] = useState(false);
  const [selectedOrgParaMembros, setSelectedOrgParaMembros] = useState<Organizacao | null>(null);

  const { data: orgs = [], isLoading } = useQuery({
    queryKey: ['organizacoes'],
    queryFn: getOrganizacoes,
  });

  const { data: usuarios = [] } = useQuery({
    queryKey: ['usuarios'],
    queryFn: getUsuarios,
    enabled: isAuthorized || membrosModalOpen,
  });

  const { data: pastoraisOficiais = [] } = useQuery({
    queryKey: ['pastoraisOficiais'],
    queryFn: getPastoraisOficiais,
    enabled: isAuthorized,
  });

  const saveMutation = useMutation({
    mutationFn: (data: Omit<Organizacao, 'id'>) =>
      selectedOrg ? updateOrganizacao(selectedOrg.id, data) : createOrganizacao(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['organizacoes'] });
      showSuccess(selectedOrg ? 'Pastoral/Movimento atualizado com sucesso!' : 'Pastoral/Movimento cadastrado com sucesso!');
      setModalOpen(false);
      setSelectedOrg(null);
    },
    onError: (err) => showError(extractErrorMessage(err, 'Erro ao salvar pastoral/movimento.')),
  });

  const toggleStatusMutation = useMutation({
    mutationFn: async (org: Organizacao) => {
      if (org.ativo) {
        await deleteOrganizacao(org.id);
      } else {
        await updateOrganizacao(org.id, { ...org, ativo: true });
      }
    },
    onSuccess: (_, org) => {
      queryClient.invalidateQueries({ queryKey: ['organizacoes'] });
      showSuccess(`Pastoral/Movimento ${org.ativo ? 'inativado' : 'ativado'} com sucesso!`);
    },
    onError: (err) => showError(extractErrorMessage(err, 'Erro ao alterar status da pastoral/movimento.')),
  });

  const igrejas = useMemo(
    () => orgs.filter((o) => o.ativo && (o.tipo === 'MATRIZ' || o.tipo === 'COMUNIDADE')),
    [orgs]
  );

  const filteredOrgs = useMemo(() => {
    return orgs.filter((org) => {
      const query = searchTerm.toLowerCase().trim();
      const matchesSearch =
        !query ||
        org.nome.toLowerCase().includes(query) ||
        (org.coordenador && org.coordenador.toLowerCase().includes(query)) ||
        (org.tipo && org.tipo.toLowerCase().includes(query));
      const matchesTipo = tipoFilter === 'TODOS' || org.tipo === tipoFilter;
      const matchesStatus =
        statusFilter === 'TODOS' ||
        (statusFilter === 'ATIVO' && org.ativo) ||
        (statusFilter === 'INATIVO' && !org.ativo);
      return matchesSearch && matchesTipo && matchesStatus;
    });
  }, [orgs, searchTerm, tipoFilter, statusFilter]);

  return (
    <Container maxWidth="xl" sx={{ py: 4 }}>
      <PageHeader
        icon={<VolunteerActivismIcon sx={{ fontSize: 32, color: 'primary.main' }} />}
        title="Pastorais e Movimentos"
        subtitle="Gerencie pastorais, movimentos, lideranças e vínculos."
        actions={
          isAuthorized && (
            <ActionButton
              title="Nova Pastoral / Movimento"
              icon={<GroupAddIcon />}
              color="primary"
              onClick={() => {
                setSelectedOrg(null);
                setModalOpen(true);
              }}
            />
          )
        }
      />

      <OrganizacoesFilterBar
        searchTerm={searchTerm}
        onSearchChange={(val) => { setSearchTerm(val); setPage(0); }}
        tipoFilter={tipoFilter}
        onTipoFilterChange={(val) => { setTipoFilter(val); setPage(0); }}
        statusFilter={statusFilter}
        onStatusFilterChange={(val) => { setStatusFilter(val); setPage(0); }}
      />

      <OrganizacoesTable
        organizacoes={filteredOrgs}
        isLoading={isLoading}
        page={page}
        rowsPerPage={rowsPerPage}
        onPageChange={setPage}
        onRowsPerPageChange={setRowsPerPage}
        canManage={isAuthorized}
        onViewMembers={(org) => {
          setSelectedOrgParaMembros(org);
          setMembrosModalOpen(true);
        }}
        onEdit={(org) => {
          setSelectedOrg(org);
          setModalOpen(true);
        }}
        onToggleStatus={(org) => toggleStatusMutation.mutate(org)}
        isTogglePending={toggleStatusMutation.isPending}
      />

      <OrganizacaoFormModal
        open={modalOpen}
        onClose={() => { setModalOpen(false); setSelectedOrg(null); }}
        onSave={(data) => saveMutation.mutate(data)}
        organizacao={selectedOrg}
        isPending={saveMutation.isPending}
        usuarios={usuarios}
        igrejas={igrejas}
        organizacoes={orgs}
        pastoraisOficiais={pastoraisOficiais}
      />

      <OrganizacaoMembrosModal
        open={membrosModalOpen}
        onClose={() => {
          setMembrosModalOpen(false);
          setSelectedOrgParaMembros(null);
        }}
        organizacao={selectedOrgParaMembros}
        igrejas={igrejas}
        usuarios={usuarios}
      />
    </Container>
  );
};

export default Organizacoes;
