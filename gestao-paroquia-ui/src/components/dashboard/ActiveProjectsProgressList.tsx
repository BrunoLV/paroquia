import React from 'react';
import { Paper, Typography, Box, Grid, LinearProgress, Chip, Stack } from '@mui/material';
import { Warning as WarningIcon, AccessTime as TimeIcon, CalendarToday as EventIcon } from '@mui/icons-material';
import { ProjectDetail } from '../../api/types';

interface ActiveProjectsProgressListProps {
  projects: ProjectDetail[];
}

const ActiveProjectsProgressList: React.FC<ActiveProjectsProgressListProps> = ({ projects }) => {
  return (
    <Paper
      elevation={0}
      sx={{
        p: 3,
        borderRadius: 3,
        border: '1px solid',
        borderColor: 'divider',
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
        background: 'linear-gradient(to bottom, #ffffff, #fcfcfc)',
        boxShadow: '0 4px 20px rgba(0, 0, 0, 0.02)',
        transition: 'transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out',
        '&:hover': {
          transform: 'translateY(-2px)',
          boxShadow: '0 8px 30px rgba(0, 0, 0, 0.04)',
        },
      }}
    >
      <Typography variant="h6" sx={{ fontWeight: 700, color: 'text.primary', mb: 1 }}>
        Projetos Ativos
      </Typography>
      <Typography variant="body2" sx={{ color: 'text.secondary', mb: 3 }}>
        Acompanhamento de cronograma e execução de eventos
      </Typography>

      <Stack spacing={3} sx={{ flexGrow: 1 }}>
        {projects.length > 0 ? (
          projects.map((project, index) => {
            const executionRate = project.totalEventos > 0
              ? (project.eventosRealizados / project.totalEventos) * 100
              : 0;

            return (
              <Box
                key={index}
                sx={{
                  p: 2,
                  borderRadius: 2,
                  border: '1px solid',
                  borderColor: project.emRisco ? 'error.light' : 'divider',
                  bgcolor: project.emRisco ? 'error.lighter' : 'background.paper',
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
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 1.5 }}>
                  <Typography variant="subtitle1" sx={{ fontWeight: 700, color: 'text.primary' }}>
                    {project.nome}
                  </Typography>
                  <Stack direction="row" spacing={1} alignItems="center">
                    {project.emRisco && (
                      <Chip
                        icon={<WarningIcon style={{ fontSize: 14, color: '#d32f2f' }} />}
                        label="Em Risco"
                        size="small"
                        color="error"
                        sx={{
                          fontWeight: 600,
                          fontSize: 11,
                          animation: 'pulse 2s infinite',
                          '@keyframes pulse': {
                            '0%': { opacity: 0.8 },
                            '50%': { opacity: 1 },
                            '100%': { opacity: 0.8 },
                          },
                        }}
                      />
                    )}
                    <Chip
                      label={project.status === 'EM_ANDAMENTO' ? 'Em Andamento' : 'Aberto'}
                      size="small"
                      variant="outlined"
                      color={project.status === 'EM_ANDAMENTO' ? 'primary' : 'default'}
                      sx={{ fontSize: 11, fontWeight: 500 }}
                    />
                  </Stack>
                </Box>

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

                {project.envolvidos.length > 0 && (
                  <Box sx={{ mt: 1.5, display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
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
      </Stack>
    </Paper>
  );
};

export default ActiveProjectsProgressList;
