import React from 'react';
import { Alert, Grid, Stack } from '@mui/material';
import DashboardIcon from '@mui/icons-material/Dashboard';
import type { DashboardComunidade, DashboardLogistica, DashboardMetrics, Evento } from '@/shared/domain/types/types';
import PageHeader from '@/shared/presentation/components/PageHeader';
import AprovacoesPendentes from '@/contextos/painel/presentation/components/painel/AprovacoesPendentes';
import DashboardFilterBar from '@/contextos/painel/presentation/components/painel/DashboardFilterBar';
import PainelResumoPeriodo from '@/contextos/painel/presentation/components/painel/PainelResumoPeriodo';
import QuickActions from '@/contextos/painel/presentation/components/painel/QuickActions';
import ChoqueHorarioAlert from '@/contextos/painel/presentation/components/painel/ChoqueHorarioAlert';
import WeeklyTimeline from '@/contextos/painel/presentation/components/painel/WeeklyTimeline';
import UpcomingEvents from '@/contextos/painel/presentation/components/painel/UpcomingEvents';
import ProjetosPorStatusCard from '@/contextos/painel/presentation/components/painel/ProjetosPorStatusCard';
import ListaProgressoProjetosAtivos from '@/contextos/painel/presentation/components/painel/ListaProgressoProjetosAtivos';
import EventDistributionPieChart from '@/contextos/painel/presentation/components/painel/EventDistributionPieChart';

interface ConteudoDashboardProps {
  metrics?: DashboardMetrics;
  logistica?: DashboardLogistica;
  comunidade?: DashboardComunidade;
  proximosEventos: Evento[];
  carregandoEventos: boolean;
  erro?: string;
  onNovoEvento: () => void;
}

/**
 * Layout principal do Painel paroquial estruturado com contêiner canônico,
 * simetria entre blocos operacionais e visualização descritiva de projetos e riscos.
 */
const ConteudoDashboard: React.FC<ConteudoDashboardProps> = ({
  metrics,
  logistica,
  proximosEventos,
  carregandoEventos,
  erro,
  onNovoEvento,
}) => (
  <Stack spacing={3}>
    <PageHeader
      icon={<DashboardIcon sx={{ fontSize: 32, color: 'primary.main' }} />}
      title="Painel"
      subtitle="Visão geral operacional, eventos, projetos e aprovações."
      actions={<DashboardFilterBar />}
    />

    {erro && <Alert severity="error">{erro}</Alert>}

    <QuickActions onNewEvent={onNovoEvento} />

    <PainelResumoPeriodo resumo={metrics?.resumoGeral} />

    {logistica?.choquesHorario && logistica.choquesHorario.length > 0 && (
      <ChoqueHorarioAlert choques={logistica.choquesHorario} />
    )}

    {/* Linha 1: Operações Imediatas & Fila de Decisão */}
    <Grid container spacing={2}>
      <Grid size={{ xs: 12, lg: 4 }}>
        <AprovacoesPendentes />
      </Grid>
      <Grid size={{ xs: 12, md: 7, lg: 5 }}>
        <WeeklyTimeline events={metrics?.eventosSemana ?? []} />
      </Grid>
      <Grid size={{ xs: 12, md: 5, lg: 3 }}>
        <UpcomingEvents events={proximosEventos} isLoading={carregandoEventos} />
      </Grid>
    </Grid>

    {/* Linha 2: Gestão do Portfólio de Projetos e Identificação de Risco */}
    <Grid container spacing={2}>
      <Grid size={{ xs: 12, lg: 4 }}>
        <ProjetosPorStatusCard data={metrics?.projetosPorStatus} />
      </Grid>
      <Grid size={{ xs: 12, lg: 8 }}>
        <ListaProgressoProjetosAtivos projects={metrics?.projetosAtivosDetalhes ?? []} />
      </Grid>
    </Grid>

    {/* Linha 3: Distribuição de Eventos por Organização */}
    <Grid container spacing={2}>
      <Grid size={{ xs: 12, lg: 6 }}>
        <EventDistributionPieChart data={metrics?.eventosPorGrupoParoquial ?? []} />
      </Grid>
    </Grid>
  </Stack>
);

export default ConteudoDashboard;
