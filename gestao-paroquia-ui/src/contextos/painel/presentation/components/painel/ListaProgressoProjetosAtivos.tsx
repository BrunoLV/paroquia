import React from 'react';
import { alpha, Box, Chip, Grid, LinearProgress, Paper, Stack, Typography } from '@mui/material';
import TimeIcon from '@mui/icons-material/AccessTime';
import EventIcon from '@mui/icons-material/CalendarToday';
import FlagIcon from '@mui/icons-material/Flag';
import PendingIcon from '@mui/icons-material/HourglassEmpty';
import WarningIcon from '@mui/icons-material/Warning';
import ArrowForwardIcon from '@mui/icons-material/ArrowForward';
import AssignmentTurnedInIcon from '@mui/icons-material/AssignmentTurnedIn';
import { useNavigate } from 'react-router-dom';
import { ProjectDetail } from '@/shared/domain/types/types';
import StatusIcon from '@/shared/presentation/components/StatusIcon';
import ActionButton from '@/shared/presentation/components/ActionButton';
import { getProjetoStatusMeta, type ProjetoStatus } from '@/shared/utils/statusMeta/projetoStatus';

export interface ListaProgressoProjetosAtivosProps {
  projects: ProjectDetail[];
}

export type ActiveProjectsProgressListProps = ListaProgressoProjetosAtivosProps;

/**
 * Gera diagnóstico semântico e explicativo para projetos classificados em risco.
 */
const diagnosticarMotivoRisco = (project: ProjectDetail, executionRate: number): string => {
  const motivos: string[] = [];
  if (project.percentualTempoDecorrido > executionRate + 15) {
    motivos.push(
      `Cronograma descompassado: ${project.percentualTempoDecorrido.toFixed(0)}% do prazo transcorrido com apenas ${executionRate.toFixed(0)}% dos eventos executados.`
    );
  }
  if (project.eventosPendentesAprovacao > 0) {
    motivos.push(
      `${project.eventosPendentesAprovacao} evento(s) travado(s) aguardando aprovação.`
    );
  }
  if (motivos.length === 0) {
    motivos.push('Prazo em faixa crítica exigindo acompanhamento pastoral imediato.');
  }
  return motivos.join(' ');
};

/**
 * Painel de acompanhamento de projetos ativos com identificação e alertas claros de risco.
 */
const ListaProgressoProjetosAtivos: React.FC<ListaProgressoProjetosAtivosProps> = ({ projects }) => {
  const navigate = useNavigate();

  // Projetos em risco recebem prioridade máxima no topo da lista
  const projetosOrdenados = React.useMemo(() => {
    return [...projects].sort((a, b) => {
      if (a.emRisco === b.emRisco) return 0;
      return a.emRisco ? -1 : 1;
    });
  }, [projects]);

  const totalEmRisco = projects.filter((p) => p.emRisco).length;

  return (
    <Paper
      elevation={0}
      sx={{
        p: 3,
        border: '1px solid',
        borderColor: 'divider',
        borderRadius: 2,
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
      }}
    >
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 1 }}>
        <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
          <AssignmentTurnedInIcon color="primary" fontSize="small" />
          <Typography variant="subtitle1" sx={{ fontWeight: 700, color: 'text.primary' }}>
            Acompanhamento de Projetos Ativos
          </Typography>
        </Stack>
        <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
          {totalEmRisco > 0 && (
            <Chip
              icon={<WarningIcon fontSize="small" />}
              label={`${totalEmRisco} em risco`}
              size="small"
              color="error"
              sx={{ fontWeight: 700 }}
            />
          )}
          <Chip label={`${projects.length} ativos`} size="small" variant="outlined" sx={{ fontWeight: 600 }} />
        </Stack>
      </Box>

      <Typography variant="body2" color="text.secondary" sx={{ mb: 2.5 }}>
        Acompanhamento de cronograma, execução de marcos e projetos em risco
      </Typography>

      <Stack spacing={2.5} sx={{ flexGrow: 1 }}>
        {projetosOrdenados.length > 0 ? (
          projetosOrdenados.map((project, index) => {
            const executionRate = project.totalEventos > 0
              ? (project.eventosRealizados / project.totalEventos) * 100
              : 0;

            return (
              <Box
                key={index}
                sx={{
                  p: 2,
                  borderRadius: 1.5,
                  border: '1px solid',
                  borderColor: project.emRisco ? 'error.main' : 'divider',
                  bgcolor: project.emRisco
                    ? (theme) => alpha(theme.palette.error.main, 0.04)
                    : 'background.paper',
                  position: 'relative',
                  overflow: 'hidden',
                  '&::before': project.emRisco ? {
                    content: '""',
                    position: 'absolute',
                    left: 0,
                    top: 0,
                    bottom: 0,
                    width: '4px',
                    backgroundColor: 'error.main',
                  } : undefined,
                }}
              >
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 1 }}>
                  <Typography variant="subtitle1" sx={{ fontWeight: 700, color: 'text.primary' }}>
                    {project.nome}
                  </Typography>
                  <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
                    {project.emRisco && (
                      <Chip
                        icon={<WarningIcon fontSize="small" />}
                        label="Em Risco"
                        size="small"
                        color="error"
                        sx={{ fontWeight: 700, height: 22, fontSize: 11 }}
                      />
                    )}
                    <StatusIcon {...getProjetoStatusMeta(project.status as ProjetoStatus)} />
                  </Stack>
                </Box>

                {project.emRisco && (
                  <Box
                    sx={{
                      mb: 1.5,
                      p: 1,
                      px: 1.25,
                      borderRadius: 1,
                      bgcolor: (theme) => alpha(theme.palette.error.main, 0.08),
                      border: '1px solid',
                      borderColor: (theme) => alpha(theme.palette.error.main, 0.2),
                      display: 'flex',
                      alignItems: 'center',
                      gap: 1,
                    }}
                  >
                    <WarningIcon color="error" sx={{ fontSize: 16, flexShrink: 0 }} />
                    <Typography variant="caption" sx={{ fontWeight: 600, color: 'error.main' }}>
                      {diagnosticarMotivoRisco(project, executionRate)}
                    </Typography>
                  </Box>
                )}

                <Grid container spacing={2}>
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 0.5 }}>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                        <TimeIcon sx={{ fontSize: 14, color: 'text.secondary' }} />
                        <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 500 }}>
                          Tempo Decorrido
                        </Typography>
                      </Box>
                      <Typography variant="caption" sx={{ fontWeight: 600, color: 'text.primary' }}>
                        {project.percentualTempoDecorrido.toFixed(0)}%
                      </Typography>
                    </Box>
                    <LinearProgress
                      variant="determinate"
                      value={Math.min(project.percentualTempoDecorrido, 100)}
                      color={project.percentualTempoDecorrido > 90 ? 'warning' : 'primary'}
                      sx={{ height: 6, borderRadius: 3 }}
                    />
                  </Grid>

                  <Grid size={{ xs: 12, sm: 6 }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 0.5 }}>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                        <EventIcon sx={{ fontSize: 14, color: 'text.secondary' }} />
                        <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 500 }}>
                          Execução de Eventos
                        </Typography>
                      </Box>
                      <Typography variant="caption" sx={{ fontWeight: 600, color: 'text.primary' }}>
                        {project.eventosRealizados}/{project.totalEventos} ({executionRate.toFixed(0)}%)
                      </Typography>
                    </Box>
                    <LinearProgress
                      variant="determinate"
                      value={Math.min(executionRate, 100)}
                      color={executionRate < project.percentualTempoDecorrido - 15 ? 'warning' : 'success'}
                      sx={{ height: 6, borderRadius: 3 }}
                    />
                  </Grid>
                </Grid>

                <Box sx={{ mt: 1.5, display: 'flex', flexWrap: 'wrap', gap: 0.75, alignItems: 'center' }}>
                  {project.proximoMarco && (
                    <StatusIcon
                      icon={<FlagIcon />}
                      color="info"
                      label={`Próximo marco: ${project.proximoMarco.titulo} · ${new Date(project.proximoMarco.inicio).toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit' })}`}
                      tooltip="Próximo marco do projeto"
                    />
                  )}
                  {project.eventosPendentesAprovacao > 0 && (
                    <StatusIcon
                      icon={<PendingIcon />}
                      color="warning"
                      label={`${project.eventosPendentesAprovacao} evento(s) pendente(s) de aprovação`}
                      tooltip="Eventos deste projeto aguardando aprovação"
                    />
                  )}
                </Box>

                {project.envolvidos.length > 0 && (
                  <Box sx={{ mt: 1, display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                    {project.envolvidos.map((env, envIdx) => (
                      <Chip
                        key={envIdx}
                        label={env}
                        size="small"
                        sx={{ fontSize: 10, height: 18, bgcolor: 'action.selected', color: 'text.secondary' }}
                      />
                    ))}
                  </Box>
                )}
              </Box>
            );
          })
        ) : (
          <Box sx={{ py: 4, display: 'flex', justifyContent: 'center', alignItems: 'center', flexGrow: 1 }}>
            <Typography variant="body2" color="text.secondary">
              Nenhum projeto ativo cadastrado.
            </Typography>
          </Box>
        )}

        <ActionButton
          title="Gerenciar Projetos"
          label="Gerenciar Projetos"
          icon={<ArrowForwardIcon />}
          color="primary"
          variant="outlined"
          size="small"
          onClick={() => navigate('/projetos')}
          sx={{ alignSelf: 'flex-start', mt: 'auto' }}
        />
      </Stack>
    </Paper>
  );
};

export const ActiveProjectsProgressList = ListaProgressoProjetosAtivos;
export default ListaProgressoProjetosAtivos;
