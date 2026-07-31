import React from 'react';
import {Box, Paper, Typography, useTheme} from '@mui/material';
import {Cell, Legend, Pie, PieChart, ResponsiveContainer, Tooltip} from 'recharts';
import {EventDistribution} from '../../api/types';

interface EventDistributionPieChartProps {
  data: EventDistribution[];
}

interface ChartSlice {
  label: string;
  value: number;
}

const OUTRAS_THRESHOLD_PERCENT = 3;

export const groupMinorSlices = (data: EventDistribution[]): ChartSlice[] => {
  const total = data.reduce((acc, item) => acc + item.quantidadeEventos, 0);
  if (total <= 0) {
    return [];
  }

  const mainSlices: ChartSlice[] = [];
  let outrasValue = 0;

  data.forEach((item) => {
    const share = (item.quantidadeEventos / total) * 100;
    if (share < OUTRAS_THRESHOLD_PERCENT) {
      outrasValue += item.quantidadeEventos;
    } else {
      mainSlices.push({ label: item.organizacaoNome, value: item.quantidadeEventos });
    }
  });

  if (outrasValue > 0) {
    mainSlices.push({ label: 'Outras', value: outrasValue });
  }

  return mainSlices;
};

const EventDistributionPieChart: React.FC<EventDistributionPieChartProps> = ({ data }) => {
  const theme = useTheme();

  const chartData = groupMinorSlices(data);
  const hasData = chartData.length > 0;

  const sliceColors = [
    theme.palette.primary.main,
    theme.palette.secondary.main,
    theme.palette.info.main,
    theme.palette.warning.main,
    theme.palette.success.main,
  ];

  return (
    <Paper
      elevation={0}
      sx={{
        p: 3,
        borderRadius: 0,
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
        Distribuição de Eventos
      </Typography>
      <Typography variant="body2" sx={{ color: 'text.secondary', mb: 3 }}>
        Proporção de eventos agendados por organização responsável
      </Typography>

      <Box sx={{ flexGrow: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', minHeight: 220 }}>
        {hasData ? (
          <ResponsiveContainer width="100%" height={260}>
            <PieChart>
              <Pie
                data={chartData}
                dataKey="value"
                nameKey="label"
                innerRadius={60}
                outerRadius={90}
                paddingAngle={3}
                labelLine
                label={({ label, percent }: any) => `${label} (${Math.round((percent || 0) * 100)}%)`}
                isAnimationActive
                animationEasing="ease-out"
              >
                {chartData.map((entry, index) => (
                  <Cell key={entry.label} fill={sliceColors[index % sliceColors.length]} />
                ))}
              </Pie>
              <Tooltip
                contentStyle={{
                  backgroundColor: theme.palette.mode === 'dark'
                    ? 'rgba(0, 0, 0, 0.85)'
                    : 'rgba(255, 255, 255, 0.92)',
                  border: `1px solid ${theme.palette.divider}`,
                  color: theme.palette.text.secondary,
                }}
              />
              <Legend
                layout="vertical"
                verticalAlign="middle"
                align="right"
                wrapperStyle={{ color: theme.palette.text.secondary }}
              />
            </PieChart>
          </ResponsiveContainer>
        ) : (
          <Typography variant="body2" color="text.secondary">
            Sem dados de eventos para exibir
          </Typography>
        )}
      </Box>
    </Paper>
  );
};

export default EventDistributionPieChart;
