import React from 'react';
import {Typography, useTheme} from '@mui/material';
import ReactApexChart from 'react-apexcharts';
import {ApexOptions} from 'apexcharts';
import {EventDistribution} from '../../api/types';

interface EventVolumeBarChartProps {
  data: EventDistribution[];
}

const EventVolumeBarChart: React.FC<EventVolumeBarChartProps> = ({ data }) => {
  const theme = useTheme();

  if (data.length === 0) {
    return (
      <Typography variant="body2" color="text.secondary" sx={{ py: 6, textAlign: 'center' }}>
        Nenhum dado disponível para exibição no gráfico de barras.
      </Typography>
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
        borderRadius: 0,
        columnWidth: categories.length > 8 ? '40%' : '55%',
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
    <ReactApexChart
      options={options}
      series={series}
      type="bar"
      height={310}
      width="100%"
    />
  );
};

export default EventVolumeBarChart;
