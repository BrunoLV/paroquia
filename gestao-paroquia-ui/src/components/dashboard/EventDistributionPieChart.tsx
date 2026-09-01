import React from 'react';
import {Box, Paper, Typography, useTheme} from '@mui/material';
import PieChartOutlineIcon from '@mui/icons-material/PieChartOutlineOutlined';
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
    theme.palette.error.main,
  ];

  return (
    <Paper
      elevation={0}
      sx={{
        p: 3,
        border: '1px solid',
        borderColor: 'divider',
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
      }}
    >
      <Typography variant="h6" sx={{ fontWeight: 700, color: 'text.primary', mb: 1 }}>
        Distribuição de Eventos
      </Typography>
      <Typography variant="body2" sx={{ color: 'text.secondary', mb: 3 }}>
        Proporção de eventos agendados por organização responsável
      </Typography>

      <Box sx={{ flexGrow: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', minHeight: 280, width: '100%' }}>
        {hasData ? (
          <ResponsiveContainer width="100%" height={280}>
            <PieChart margin={{ top: 8, right: 8, bottom: 8, left: 8 }}>
              <Pie
                data={chartData}
                dataKey="value"
                nameKey="label"
                innerRadius={55}
                outerRadius={80}
                paddingAngle={3}
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
                layout="horizontal"
                verticalAlign="bottom"
                align="center"
                wrapperStyle={{ color: theme.palette.text.secondary, fontSize: 12 }}
                formatter={(value: string, _entry: unknown, index: number) => {
                  const total = chartData.reduce((acc, item) => acc + item.value, 0);
                  const sliceValue = chartData[index]?.value ?? 0;
                  const percent = total > 0 ? Math.round((sliceValue / total) * 100) : 0;
                  return `${value} (${percent}%)`;
                }}
              />
            </PieChart>
          </ResponsiveContainer>
        ) : (
          <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 1 }}>
            <PieChartOutlineIcon sx={{ fontSize: 36, color: 'action.disabled' }} />
            <Typography variant="body2" color="text.secondary">
              Sem dados de eventos para exibir
            </Typography>
          </Box>
        )}
      </Box>
    </Paper>
  );
};

export default EventDistributionPieChart;
