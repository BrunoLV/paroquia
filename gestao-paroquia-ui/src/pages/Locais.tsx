import React, { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Container } from '@mui/material';
import { Add as AddIcon, LocationOn as LocationIcon } from '@mui/icons-material';
import { createLocal, deleteLocal, getLocais, updateLocal } from '@/api/locais';
import { Local } from '@/api/types';
import { useAuth } from '@/auth/AuthContext';
import { canManageLocais } from '@/auth/permissions';
import { useNotification } from '@/context/NotificationContext';
import { extractErrorMessage } from '@/utils/errorUtils';
import ActionButton from '@/components/ActionButton';
import PageHeader from '@/components/PageHeader';
import { LocaisFilterBar } from '@/components/locais/LocaisFilterBar';
import { LocaisTable } from '@/components/locais/LocaisTable';
import LocalFormModal from '@/pages/LocalFormModal';

const Locais: React.FC = () => {
  const { user } = useAuth();
  const { showError, showSuccess } = useNotification();
  const queryClient = useQueryClient();
  const isAuthorized = canManageLocais(user);

  const [searchTerm, setSearchTerm] = useState('');
  const [tipoFilter, setTipoFilter] = useState('TODOS');
  const [statusFilter, setStatusFilter] = useState('TODOS');
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [modalOpen, setModalOpen] = useState(false);
  const [selectedLocal, setSelectedLocal] = useState<Local | null>(null);

  const { data: locais = [], isLoading } = useQuery({
    queryKey: ['locais'],
    queryFn: getLocais,
  });

  const saveMutation = useMutation({
    mutationFn: (data: Omit<Local, 'id'>) =>
      selectedLocal ? updateLocal(selectedLocal.id, data) : createLocal(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['locais'] });
      showSuccess(selectedLocal ? 'Local atualizado com sucesso!' : 'Local cadastrado com sucesso!');
      setModalOpen(false);
      setSelectedLocal(null);
    },
    onError: (err) => showError(extractErrorMessage(err, 'Erro ao salvar local.')),
  });

  const toggleStatusMutation = useMutation({
    mutationFn: async (local: Local) => {
      if (local.ativo) {
        await deleteLocal(local.id);
      } else {
        await updateLocal(local.id, { ...local, ativo: true });
      }
    },
    onSuccess: (_, local) => {
      queryClient.invalidateQueries({ queryKey: ['locais'] });
      showSuccess(`Local ${local.ativo ? 'inativado' : 'ativado'} com sucesso!`);
    },
    onError: (err) => showError(extractErrorMessage(err, 'Erro ao alterar status do local.')),
  });

  const filteredLocais = useMemo(() => {
    return locais.filter((local) => {
      const matchesSearch =
        local.nome.toLowerCase().includes(searchTerm.toLowerCase().trim()) ||
        (local.endereco && String(local.endereco).toLowerCase().includes(searchTerm.toLowerCase().trim()));
      const matchesTipo = tipoFilter === 'TODOS' || local.tipo === tipoFilter;
      const matchesStatus =
        statusFilter === 'TODOS' ||
        (statusFilter === 'ATIVO' && local.ativo) ||
        (statusFilter === 'INATIVO' && !local.ativo);
      return matchesSearch && matchesTipo && matchesStatus;
    });
  }, [locais, searchTerm, tipoFilter, statusFilter]);

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
                setModalOpen(true);
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
      />

      <LocaisTable
        locais={filteredLocais}
        isLoading={isLoading}
        page={page}
        rowsPerPage={rowsPerPage}
        onPageChange={setPage}
        onRowsPerPageChange={setRowsPerPage}
        canManage={isAuthorized}
        onEdit={(local) => { setSelectedLocal(local); setModalOpen(true); }}
        onToggleStatus={(local) => toggleStatusMutation.mutate(local)}
        isTogglePending={toggleStatusMutation.isPending}
      />

      <LocalFormModal
        open={modalOpen}
        onClose={() => { setModalOpen(false); setSelectedLocal(null); }}
        onSave={(data) => saveMutation.mutate(data)}
        local={selectedLocal}
        isPending={saveMutation.isPending}
      />
    </Container>
  );
};

export default Locais;
