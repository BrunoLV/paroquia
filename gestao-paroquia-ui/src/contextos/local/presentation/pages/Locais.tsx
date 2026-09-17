import React, { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Container } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import LocationIcon from '@mui/icons-material/LocationOn';
import { createLocal, deleteLocal, getLocais, updateLocal } from '@/contextos/local/infrastructure/http/locais';
import { getOrganizacoes } from '@/contextos/organizacao/infrastructure/http/organizacoes';
import { Local } from '@/shared/domain/types/types';
import { useAuth } from '@/contextos/iam/presentation/auth/AuthContext';
import { canManageLocais } from '@/contextos/iam/presentation/auth/permissions';
import { useNotification } from '@/shared/presentation/context/NotificationContext';
import { extractErrorMessage } from '@/shared/utils/errorUtils';
import ActionButton from '@/shared/presentation/components/ActionButton';
import PageHeader from '@/shared/presentation/components/PageHeader';
import { LocaisFilterBar } from '@/contextos/local/presentation/components/locais/LocaisFilterBar';
import { LocaisTable } from '@/contextos/local/presentation/components/locais/LocaisTable';
import { LocalFormModal } from '@/contextos/local/presentation/components/locais/LocalFormModal';
import { LocalViewModal } from '@/contextos/local/presentation/components/locais/LocalViewModal';
import { LocalDeleteConfirmDialog } from '@/contextos/local/presentation/components/locais/LocalDeleteConfirmDialog';

const Locais: React.FC = () => {
  const { user } = useAuth();
  const { showError, showSuccess } = useNotification();
  const queryClient = useQueryClient();
  const isAuthorized = canManageLocais(user);

  const [searchTerm, setSearchTerm] = useState('');
  const [tipoFilter, setTipoFilter] = useState('TODOS');
  const [statusFilter, setStatusFilter] = useState('TODOS');
  const [igrejaFilter, setIgrejaFilter] = useState('TODOS');
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);

  const [formModalOpen, setFormModalOpen] = useState(false);
  const [viewModalOpen, setViewModalOpen] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);

  const [selectedLocal, setSelectedLocal] = useState<Local | null>(null);
  const [localToDelete, setLocalToDelete] = useState<Local | null>(null);

  const { data: locais = [], isLoading } = useQuery({
    queryKey: ['locais'],
    queryFn: getLocais,
  });

  const { data: orgs = [] } = useQuery({
    queryKey: ['organizacoes'],
    queryFn: getOrganizacoes,
  });

  const igrejas = useMemo(() => {
    return orgs.filter((o) => (o.tipo === 'MATRIZ' || o.tipo === 'COMUNIDADE') && o.ativo !== false);
  }, [orgs]);

  const saveMutation = useMutation({
    mutationFn: (data: Omit<Local, 'id'>) =>
      selectedLocal ? updateLocal(selectedLocal.id, data) : createLocal(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['locais'] });
      showSuccess(selectedLocal ? 'Local atualizado com sucesso!' : 'Local cadastrado com sucesso!');
      setFormModalOpen(false);
      setSelectedLocal(null);
    },
    onError: (err) => showError(extractErrorMessage(err, 'Erro ao salvar local.')),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => deleteLocal(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['locais'] });
      showSuccess('Local excluído com sucesso!');
      setDeleteDialogOpen(false);
      setLocalToDelete(null);
    },
    onError: (err) => showError(extractErrorMessage(err, 'Erro ao excluir local.')),
  });

  const filteredLocais = useMemo(() => {
    const term = searchTerm.toLowerCase().trim();
    return locais.filter((local) => {
      const matchesSearch =
        !term ||
        local.nome.toLowerCase().includes(term) ||
        Boolean(local.logradouro && local.logradouro.toLowerCase().includes(term)) ||
        Boolean(local.bairro && local.bairro.toLowerCase().includes(term)) ||
        Boolean(local.cidade && local.cidade.toLowerCase().includes(term)) ||
        Boolean(local.enderecoFormatado && local.enderecoFormatado.toLowerCase().includes(term));

      const matchesTipo = tipoFilter === 'TODOS' || local.tipo === tipoFilter;
      const matchesStatus =
        statusFilter === 'TODOS' ||
        (statusFilter === 'ATIVO' && local.ativo) ||
        (statusFilter === 'INATIVO' && !local.ativo);

      const matchesIgreja =
        igrejaFilter === 'TODOS' || local.igrejaId === igrejaFilter;

      return matchesSearch && matchesTipo && matchesStatus && matchesIgreja;
    });
  }, [locais, searchTerm, tipoFilter, statusFilter, igrejaFilter]);

  const navigate = useNavigate();

  return (
    <Container maxWidth="xl" sx={{ py: 4 }}>
      <PageHeader
        icon={<LocationIcon sx={{ fontSize: 32, color: 'primary.main' }} />}
        title="Locais"
        subtitle="Gerencie os espaços físicos e a capacidade da paróquia."
        actions={
          isAuthorized && (
            <ActionButton
              title="Novo Local"
              icon={<AddIcon />}
              color="primary"
              onClick={() => {
                setSelectedLocal(null);
                setFormModalOpen(true);
              }}
            />
          )
        }
      />

      <LocaisFilterBar
        searchTerm={searchTerm}
        onSearchChange={(val) => { setSearchTerm(val); setPage(0); }}
        tipoFilter={tipoFilter}
        onTipoFilterChange={(val) => { setTipoFilter(val); setPage(0); }}
        statusFilter={statusFilter}
        onStatusFilterChange={(val) => { setStatusFilter(val); setPage(0); }}
        igrejaFilter={igrejaFilter}
        onIgrejaFilterChange={(val) => { setIgrejaFilter(val); setPage(0); }}
        igrejas={igrejas}
      />

      <LocaisTable
        locais={filteredLocais}
        igrejas={igrejas}
        isLoading={isLoading}
        page={page}
        rowsPerPage={rowsPerPage}
        onPageChange={setPage}
        onRowsPerPageChange={setRowsPerPage}
        canManage={isAuthorized}
        onView={(local) => {
          setSelectedLocal(local);
          setViewModalOpen(true);
        }}
        onEdit={(local) => {
          setSelectedLocal(local);
          setFormModalOpen(true);
        }}
        onDelete={(local) => {
          setLocalToDelete(local);
          setDeleteDialogOpen(true);
        }}
        onReservar={(local) => {
          navigate('/reservas', { state: { tipoItem: 'LOCAL', itemId: local.id } });
        }}
        isDeletePending={deleteMutation.isPending}
      />

      <LocalViewModal
        open={viewModalOpen}
        local={selectedLocal}
        igrejas={igrejas}
        canManage={isAuthorized}
        onClose={() => {
          setViewModalOpen(false);
          setSelectedLocal(null);
        }}
        onEdit={(local) => {
          setViewModalOpen(false);
          setSelectedLocal(local);
          setFormModalOpen(true);
        }}
      />

      <LocalFormModal
        open={formModalOpen}
        onClose={() => {
          setFormModalOpen(false);
          setSelectedLocal(null);
        }}
        onSave={(data) => saveMutation.mutate(data)}
        local={selectedLocal}
        isPending={saveMutation.isPending}
      />

      <LocalDeleteConfirmDialog
        open={deleteDialogOpen}
        local={localToDelete}
        onClose={() => {
          setDeleteDialogOpen(false);
          setLocalToDelete(null);
        }}
        onConfirm={() => {
          if (localToDelete) {
            deleteMutation.mutate(localToDelete.id);
          }
        }}
        isPending={deleteMutation.isPending}
      />
    </Container>
  );
};

export default Locais;
