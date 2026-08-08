import React from 'react';
import { Box, Paper, Typography, useTheme } from '@mui/material';
import { BarChart as BarIcon } from '@mui/icons-material';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, Cell } from 'recharts';
import { EventDistribution } from '../../api/types';

interface EventVolumeBarChartProps {
  data: EventDistribution[];
}

const ChartCard: React.FC<{ children: React.ReactNode }> = ({ children }) => (
  <Paper
    elevation={0}
    sx={{
      p: 3,
      border: '1px solid',
      borderColor: 'divider',
      height: '100%',
    }}
  >
    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
      <BarIcon sx={{ color: 'primary.main' }} />
      <Typography variant="h6" sx={{ fontWeight: 600, color: 'primary.main' }}>
        Volume de Eventos por Organização
      </Typography>
    </Box>
    {children}
  </Paper>
);

const EventVolumeBarChart: React.FC<EventVolumeBarChartProps> = ({ data }) => {
  const theme = useTheme();

  if (data.length === 0) {
    return (
      <ChartCard>
        <Typography variant="body2" color="text.secondary" sx={{ py: 6, textAlign: 'center' }}>
          Nenhum dado disponível para exibição no gráfico de barras.
        </Typography>
      </ChartCard>
    );
  }

  const palette = [
    theme.palette.primary.main,
    theme.palette.secondary.main,
    theme.palette.info.main,
    theme.palette.warning.main,
    theme.palette.success.main,
  ];

  return (
    <ChartCard>
      <Box sx={{ minHeight: 310, width: '100%' }}>
        <ResponsiveContainer width="100%" height={310}>
          <BarChart data={data} margin={{ top: 10, right: 10, left: 0, bottom: 25 }}>
            <XAxis
              dataKey="organizacaoNome"
              tick={{ fill: theme.palette.text.secondary, fontSize: 12 }}
              interval={0}
            />
            <YAxis tick={{ fill: theme.palette.text.secondary, fontSize: 12 }} />
            <Tooltip
              contentStyle={{
                backgroundColor: theme.palette.mode === 'dark' ? 'rgba(0, 0, 0, 0.85)' : 'rgba(255, 255, 255, 0.92)',
                border: `1px solid ${theme.palette.divider}`,
                color: theme.palette.text.secondary,
              }}
            />
            <Bar dataKey="quantidadeEventos" radius={[4, 4, 0, 0]}>
              {data.map((_, index) => (
                <Cell key={`cell-${index}`} fill={palette[index % palette.length]} />
              ))}
            </Bar>
          </BarChart>
        </ResponsiveContainer>
      </Box>
    </ChartCard>
  );
};

export default EventVolumeBarChart;

