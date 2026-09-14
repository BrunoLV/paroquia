import React, { useEffect, useMemo, useState } from 'react';
import { useLocation } from 'react-router-dom';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  Box,
  Card,
  CardContent,
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  Stack,
  Tab,
  Tabs,
  Typography,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import CalendarMonthIcon from '@mui/icons-material/CalendarMonth';
import TableViewIcon from '@mui/icons-material/TableView';
import EventAvailableIcon from '@mui/icons-material/EventAvailable';
import ActionButton from '@/components/ActionButton';
import PageHeader from '@/components/PageHeader';
import SearchField from '@/components/SearchField';
import { useAuth } from '@/auth/AuthContext';
import { extractErrorMessage } from '@/utils/errorUtils';
import { canApprove } from '@/auth/permissions';
import { useNotification } from '@/context/NotificationContext';
import {
  aprovarReserva,
  cancelarReserva,
  criarReserva,
  criarReservaRecorrente,
  listarReservas,
  obterMetricasReservas,
  registrarDevolucaoAtivo,
  registrarRetiradaAtivo,
  rejeitarReserva,
} from '@/api/reservas';
import { getLocais } from '@/api/locais';
import { getAtivos } from '@/api/ativos';
import { getOrganizacoes } from '@/api/organizacoes';
import { listEventos } from '@/api/eventos';
import {
  CriarReservaPayload,
  CriarReservaRecorrentePayload,
  ReservaDetalhe,
  StatusReserva,
  TipoItemReserva,
} from '@/api/types';
import { ReservaFormData } from '@/schemas/reservaSchema';
import { ReservaMetricasCards } from '@/components/reservas/ReservaMetricasCards';
import { ReservasTable } from '@/components/reservas/ReservasTable';
import { ReservaCalendarView } from '@/components/reservas/ReservaCalendarView';
import { ReservaFormDrawer } from '@/components/reservas/ReservaFormDrawer';
import { ReservaDetalhesDrawer } from '@/components/reservas/ReservaDetalhesDrawer';
import { ReservaCancelarModal } from '@/components/reservas/ReservaCancelarModal';
import { ReservaDevolucaoModal } from '@/components/reservas/ReservaDevolucaoModal';

const Reservas: React.FC = () => {
  const [tabIndex, setTabIndex] = useState(0); // 0: Tabela, 1: Calendário
  const [busca, setBusca] = useState('');
  const [filtroTipo, setFiltroTipo] = useState<string>('TODOS');
  const [filtroStatus, setFiltroStatus] = useState<string>('TODOS');
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);

  const location = useLocation();
  const stateLocation = location.state as { tipoItem?: TipoItemReserva; itemId?: string } | null;

  // Drawers & Modais
  const [drawerFormOpen, setDrawerFormOpen] = useState(Boolean(stateLocation?.itemId));
  const [preselectedTipo, setPreselectedTipo] = useState<TipoItemReserva | undefined>(stateLocation?.tipoItem);
  const [preselectedItemId, setPreselectedItemId] = useState<string | undefined>(stateLocation?.itemId);
  const [detalhesReserva, setDetalhesReserva] = useState<ReservaDetalhe | null>(null);
  const [cancelarReservaAlvo, setCancelarReservaAlvo] = useState<ReservaDetalhe | null>(null);
  const [isAcaoRejeitar, setIsAcaoRejeitar] = useState(false);
  const [devolucaoReservaAlvo, setDevolucaoReservaAlvo] = useState<ReservaDetalhe | null>(null);

  const cliente = useQueryClient();
  const { showSuccess, showError } = useNotification();
  const { user } = useAuth();
  const podeAprovar = canApprove(user);

  // Queries
  const metricasQuery = useQuery({
    queryKey: ['reservas-metricas'],
    queryFn: obterMetricasReservas,
  });

  const reservasQuery = useQuery({
    queryKey: ['reservas'],
    queryFn: () => listarReservas(),
  });

  const locaisQuery = useQuery({
    queryKey: ['locais'],
    queryFn: getLocais,
  });

  const ativosQuery = useQuery({
    queryKey: ['ativos'],
    queryFn: getAtivos,
  });

  const orgsQuery = useQuery({
    queryKey: ['organizacoes'],
    queryFn: getOrganizacoes,
  });

  const eventosQuery = useQuery({
    queryKey: ['eventos-futuros'],
    queryFn: () => {
      const now = new Date();
      const nextYear = new Date();
      nextYear.setFullYear(now.getFullYear() + 1);
      return listEventos({
        dataInicio: now.toISOString(),
        dataFim: nextYear.toISOString(),
      });
    },
  });

  // Mutations
  const criarMutation = useMutation({
    mutationFn: (payload: CriarReservaPayload) => criarReserva(payload),
    onSuccess: () => {
      cliente.invalidateQueries({ queryKey: ['reservas'] });
      cliente.invalidateQueries({ queryKey: ['reservas-metricas'] });
      setDrawerFormOpen(false);
      showSuccess('Reserva solicitada com sucesso!');
    },
    onError: (err: unknown) => {
      showError(extractErrorMessage(err, 'Erro ao criar reserva.'));
    },
  });

  const criarRecorrenteMutation = useMutation({
    mutationFn: (payload: CriarReservaRecorrentePayload) => criarReservaRecorrente(payload),
    onSuccess: (res) => {
      cliente.invalidateQueries({ queryKey: ['reservas'] });
      cliente.invalidateQueries({ queryKey: ['reservas-metricas'] });
      setDrawerFormOpen(false);
      showSuccess(`${res.length} reservas recorrentes geradas com sucesso!`);
    },
    onError: (err: unknown) => {
      showError(extractErrorMessage(err, 'Erro ao criar agendamento recorrente.'));
    },
  });

  const aprovarMutation = useMutation({
    mutationFn: (id: string) => aprovarReserva(id),
    onSuccess: () => {
      cliente.invalidateQueries({ queryKey: ['reservas'] });
      cliente.invalidateQueries({ queryKey: ['reservas-metricas'] });
      showSuccess('Reserva aprovada com sucesso!');
    },
    onError: (err: unknown) => {
      showError(extractErrorMessage(err, 'Erro ao aprovar reserva.'));
    },
  });

  const rejeitarMutation = useMutation({
    mutationFn: ({ id, motivo }: { id: string; motivo: string }) => rejeitarReserva(id, motivo),
    onSuccess: () => {
      cliente.invalidateQueries({ queryKey: ['reservas'] });
      cliente.invalidateQueries({ queryKey: ['reservas-metricas'] });
      setCancelarReservaAlvo(null);
      showSuccess('Solicitação de reserva rejeitada.');
    },
    onError: (err: unknown) => {
      showError(extractErrorMessage(err, 'Erro ao rejeitar reserva.'));
    },
  });

  const cancelarMutation = useMutation({
    mutationFn: ({ id, motivo }: { id: string; motivo: string }) => cancelarReserva(id, { motivo }),
    onSuccess: () => {
      cliente.invalidateQueries({ queryKey: ['reservas'] });
      cliente.invalidateQueries({ queryKey: ['reservas-metricas'] });
      setCancelarReservaAlvo(null);
      showSuccess('Reserva cancelada com sucesso.');
    },
    onError: (err: unknown) => {
      showError(extractErrorMessage(err, 'Erro ao cancelar reserva.'));
    },
  });

  const retirarMutation = useMutation({
    mutationFn: (id: string) => registrarRetiradaAtivo(id),
    onSuccess: () => {
      cliente.invalidateQueries({ queryKey: ['reservas'] });
      cliente.invalidateQueries({ queryKey: ['reservas-metricas'] });
      showSuccess('Saída do ativo registrada com sucesso!');
    },
    onError: (err: unknown) => {
      showError(extractErrorMessage(err, 'Erro ao registrar saída de ativo.'));
    },
  });

  const devolverMutation = useMutation({
    mutationFn: ({ id, checkAvarias }: { id: string; checkAvarias: string }) =>
      registrarDevolucaoAtivo(id, { checkAvarias }),
    onSuccess: () => {
      cliente.invalidateQueries({ queryKey: ['reservas'] });
      cliente.invalidateQueries({ queryKey: ['reservas-metricas'] });
      setDevolucaoReservaAlvo(null);
      showSuccess('Devolução do ativo concluída!');
    },
    onError: (err: unknown) => {
      showError(extractErrorMessage(err, 'Erro ao registrar devolução.'));
    },
  });

  const handleFormSubmit = (data: ReservaFormData) => {
    const inicioIso = new Date(data.dataHoraInicio).toISOString();
    const fimIso = new Date(data.dataHoraFim).toISOString();

    if (data.isRecorrente && data.tipoRecorrencia && data.quantidadeOcorrencias) {
      criarRecorrenteMutation.mutate({
        tipoItem: data.tipoItem,
        itemId: data.itemId,
        dataHoraInicio: inicioIso,
        dataHoraFim: fimIso,
        bufferMinutos: data.bufferMinutos,
        descricao: data.descricao,
        pastoralMovimentoId: data.pastoralMovimentoId,
        eventoId: data.eventoId || null,
        tipoRecorrencia: data.tipoRecorrencia,
        quantidadeOcorrencias: data.quantidadeOcorrencias,
      });
    } else {
      criarMutation.mutate({
        tipoItem: data.tipoItem,
        itemId: data.itemId,
        dataHoraInicio: inicioIso,
        dataHoraFim: fimIso,
        bufferMinutos: data.bufferMinutos,
        descricao: data.descricao,
        pastoralMovimentoId: data.pastoralMovimentoId,
        eventoId: data.eventoId || null,
      });
    }
  };

  const listaFiltrada = useMemo(() => {
    const raw = reservasQuery.data ?? [];
    return raw.filter((r) => {
      if (filtroTipo !== 'TODOS' && r.tipoItem !== filtroTipo) return false;
      if (filtroStatus !== 'TODOS' && r.status !== filtroStatus) return false;
      if (!busca.trim()) return true;
      const termo = busca.toLowerCase();
      const item = r.itemNome.toLowerCase();
      const desc = r.descricao.toLowerCase();
      const solicitante = r.solicitanteNome.toLowerCase();
      const org = r.pastoralMovimentoNome.toLowerCase();
      return (
        item.includes(termo) ||
        desc.includes(termo) ||
        solicitante.includes(termo) ||
        org.includes(termo)
      );
    });
  }, [reservasQuery.data, filtroTipo, filtroStatus, busca]);

  return (
    <Stack spacing={3}>
      <PageHeader
        icon={<EventAvailableIcon sx={{ fontSize: 32, color: 'primary.main' }} />}
        title="Reservas de Locais e Ativos"
        subtitle="Agendamento unificado, controle de margens logísticas e custódia física de patrimônio"
        actions={
          <ActionButton
            title="Nova Reserva"
            icon={<AddIcon />}
            color="primary"
            onClick={() => setDrawerFormOpen(true)}
          />
        }
      />

      <ReservaMetricasCards
        metricas={metricasQuery.data}
        isLoading={metricasQuery.isLoading}
      />

      <Card elevation={0} sx={{ border: '1px solid', borderColor: 'divider', borderRadius: 2 }}>
        <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
          <Stack
            direction={{ xs: 'column', md: 'row' }}
            spacing={2}
            sx={{
              alignItems: { xs: 'stretch', md: 'center' },
              justifyContent: 'space-between',
              mb: 2,
            }}
          >
            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ flex: 1 }}>
              <SearchField
                placeholder="Pesquisar por item, responsável ou finalidade..."
                value={busca}
                onChange={(value) => {
                  setBusca(value);
                  setPage(0);
                }}
                sx={{ minWidth: { sm: 260 } }}
              />

              <FormControl size="small" sx={{ minWidth: 140 }}>
                <InputLabel>Tipo</InputLabel>
                <Select
                  value={filtroTipo}
                  label="Tipo"
                  onChange={(e) => {
                    setFiltroTipo(e.target.value);
                    setPage(0);
                  }}
                >
                  <MenuItem value="TODOS">Todos os tipos</MenuItem>
                  <MenuItem value="LOCAL">Espaços (Locais)</MenuItem>
                  <MenuItem value="ATIVO">Bens (Ativos)</MenuItem>
                </Select>
              </FormControl>

              <FormControl size="small" sx={{ minWidth: 160 }}>
                <InputLabel>Situação</InputLabel>
                <Select
                  value={filtroStatus}
                  label="Situação"
                  onChange={(e) => {
                    setFiltroStatus(e.target.value);
                    setPage(0);
                  }}
                >
                  <MenuItem value="TODOS">Todas as situações</MenuItem>
                  <MenuItem value="PENDENTE">Pendentes</MenuItem>
                  <MenuItem value="CONFIRMADA">Confirmadas</MenuItem>
                  <MenuItem value="RETIRADO">Em Custódia</MenuItem>
                  <MenuItem value="FINALIZADA">Finalizadas</MenuItem>
                  <MenuItem value="CANCELADA">Canceladas</MenuItem>
                  <MenuItem value="EXPIRADA">Expiradas</MenuItem>
                </Select>
              </FormControl>
            </Stack>

            <Tabs
              value={tabIndex}
              onChange={(_, val) => setTabIndex(val)}
              sx={{ minHeight: 40, borderBottom: { xs: 1, md: 0 }, borderColor: 'divider' }}
            >
              <Tab icon={<TableViewIcon fontSize="small" />} iconPosition="start" label="Tabela" sx={{ minHeight: 40 }} />
              <Tab icon={<CalendarMonthIcon fontSize="small" />} iconPosition="start" label="Calendário" sx={{ minHeight: 40 }} />
            </Tabs>
          </Stack>

          {tabIndex === 0 ? (
            <ReservasTable
              reservas={listaFiltrada}
              podeAprovar={podeAprovar}
              page={page}
              rowsPerPage={rowsPerPage}
              onPageChange={setPage}
              onRowsPerPageChange={(r) => {
                setRowsPerPage(r);
                setPage(0);
              }}
              onVisualizar={(r) => setDetalhesReserva(r)}
              onAprovar={(r) => aprovarMutation.mutate(r.id)}
              onRejeitar={(r) => {
                setIsAcaoRejeitar(true);
                setCancelarReservaAlvo(r);
              }}
              onCancelar={(r) => {
                setIsAcaoRejeitar(false);
                setCancelarReservaAlvo(r);
              }}
              onRetirar={(r) => retirarMutation.mutate(r.id)}
              onDevolver={(r) => setDevolucaoReservaAlvo(r)}
            />
          ) : (
            <ReservaCalendarView
              reservas={listaFiltrada}
              onSelectReserva={(r) => setDetalhesReserva(r)}
            />
          )}
        </CardContent>
      </Card>

      <ReservaFormDrawer
        open={drawerFormOpen}
        onClose={() => {
          setDrawerFormOpen(false);
          setPreselectedItemId(undefined);
        }}
        onSubmit={handleFormSubmit}
        isLoading={criarMutation.isPending || criarRecorrenteMutation.isPending}
        locais={locaisQuery.data ?? []}
        ativos={ativosQuery.data ?? []}
        pastorais={orgsQuery.data ?? []}
        eventos={eventosQuery.data ?? []}
        preselectedTipo={preselectedTipo}
        preselectedItemId={preselectedItemId}
      />

      <ReservaDetalhesDrawer
        open={Boolean(detalhesReserva)}
        onClose={() => setDetalhesReserva(null)}
        reserva={detalhesReserva}
      />

      <ReservaCancelarModal
        open={Boolean(cancelarReservaAlvo)}
        onClose={() => setCancelarReservaAlvo(null)}
        itemNome={cancelarReservaAlvo?.itemNome}
        isPendente={isAcaoRejeitar}
        onConfirm={(motivo) => {
          if (!cancelarReservaAlvo) return;
          if (isAcaoRejeitar) {
            rejeitarMutation.mutate({ id: cancelarReservaAlvo.id, motivo });
          } else {
            cancelarMutation.mutate({ id: cancelarReservaAlvo.id, motivo });
          }
        }}
      />

      <ReservaDevolucaoModal
        open={Boolean(devolucaoReservaAlvo)}
        onClose={() => setDevolucaoReservaAlvo(null)}
        itemNome={devolucaoReservaAlvo?.itemNome}
        onConfirm={(checkAvarias) => {
          if (!devolucaoReservaAlvo) return;
          devolverMutation.mutate({ id: devolucaoReservaAlvo.id, checkAvarias });
        }}
      />
    </Stack>
  );
};

export default Reservas;
