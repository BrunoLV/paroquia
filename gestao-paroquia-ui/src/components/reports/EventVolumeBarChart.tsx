import React from 'react';
import {Box, Paper, Typography, useTheme} from '@mui/material';
import {BarChart as BarIcon} from '@mui/icons-material';
import ReactApexChart from 'react-apexcharts';
import {ApexOptions} from 'apexcharts';
import {EventDistribution} from '../../api/types';

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

  const categories = data.map((item) => item.organizacaoNome);
  const seriesData = data.map((item) => item.quantidadeEventos);

  const palette = [
    theme.palette.primary.main,
    theme.palette.secondary.main,
    theme.palette.info.main,
    theme.palette.warning.main,
    theme.palette.success.main,
  ];

  const options: ApexOptions = {
    chart: {
      type: 'bar',
      toolbar: {
        show: true,
        tools: {
          download: true,
          zoom: true,
          zoomin: true,
          zoomout: true,
          pan: true,
        },
        export: {
          csv: { filename: 'volume-eventos-por-organizacao' },
          png: { filename: 'volume-eventos-por-organizacao' },
          svg: { filename: 'volume-eventos-por-organizacao' },
        },
      },
      zoom: { enabled: true, type: 'x' },
      animations: { enabled: true },
      background: 'transparent',
    },
    theme: { mode: theme.palette.mode },
    colors: palette,
    plotOptions: {
      bar: {
        
        columnWidth: categories.length > 8 ? '40%' : '55%',
        distributed: true,
      },
    },
    dataLabels: { enabled: false },
    xaxis: {
      categories,
      labels: { style: { colors: theme.palette.text.secondary } },
    },
    yaxis: {
      labels: { style: { colors: theme.palette.text.secondary } },
    },
    grid: { borderColor: theme.palette.divider },
    legend: { show: false },
    tooltip: { theme: theme.palette.mode },
  };

  const series = [
    {
      name: 'Quantidade de Eventos',
      data: seriesData,
    },
  ];

  return (
    <ChartCard>
      <ReactApexChart
        options={options}
        series={series}
        type="bar"
        height={310}
        width="100%"
      />
    </ChartCard>
  );
};

export default EventVolumeBarChart;
