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
      <BarIcon sx={{ color: 'primary.main', fontSize: 20 }} />
      <Typography variant="h6" sx={{ fontWeight: 700, color: 'text.primary' }}>
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
        <Box sx={{ py: 5, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 1 }}>
          <BarIcon sx={{ fontSize: 36, color: 'action.disabled' }} />
          <Typography variant="body2" color="text.secondary">
            Nenhum dado disponível para exibição no gráfico de barras.
          </Typography>
        </Box>
      </ChartCard>
    );
  }

  const palette = [
    '#1a237e', // Indigo
    '#00897b', // Teal
    '#8e24aa', // Purple
    '#fb8c00', // Amber
    '#d81b60', // Rose
    '#039be5', // Cyan
  ];

  return (
    <ChartCard>
      <Box sx={{ minHeight: 310, width: '100%' }}>
        <ResponsiveContainer width="100%" height={310}>
          <BarChart data={data} margin={{ top: 10, right: 10, left: 0, bottom: 25 }}>
            <XAxis
              dataKey="grupoParoquialNome"
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

