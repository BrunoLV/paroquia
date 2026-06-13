import React from 'react';
import { Paper, Typography, Box, useTheme } from '@mui/material';
import { PieChart } from '@mui/x-charts';
import { EventDistribution } from '../../api/types';

interface EventDistributionPieChartProps {
  data: EventDistribution[];
}

const EventDistributionPieChart: React.FC<EventDistributionPieChartProps> = ({ data }) => {
  const theme = useTheme();

  const chartData = data.map((item, index) => ({
    id: index,
    value: item.quantidadeEventos,
    label: item.organizacaoNome,
  }));

  const hasData = chartData.length > 0 && chartData.reduce((acc, curr) => acc + curr.value, 0) > 0;

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
        Distribuição de Eventos
      </Typography>
      <Typography variant="body2" sx={{ color: 'text.secondary', mb: 3 }}>
        Proporção de eventos agendados por organização responsável
      </Typography>

      <Box sx={{ flexGrow: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', minHeight: 220 }}>
        {hasData ? (
          <PieChart
            series={[
              {
                data: chartData,
                innerRadius: 60,
                outerRadius: 90,
                paddingAngle: 3,
                cornerRadius: 5,
                cx: 120,
              },
            ]}
            height={200}
            slotProps={{
              legend: {
                direction: 'column',
                position: { vertical: 'middle', horizontal: 'right' },
                itemMarkWidth: 8,
                itemMarkHeight: 8,
                markGap: 6,
                labelStyle: {
                  fontSize: 12,
                  fill: theme.palette.text.secondary,
                },
              },
            }}
          />
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
