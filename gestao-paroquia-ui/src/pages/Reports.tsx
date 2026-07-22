import React, { useEffect, useState } from 'react';
import {
  Box,
  Typography,
  Grid,
  Card,
  CardContent,
  Skeleton,
  Alert,
  Stack,
  LinearProgress,
} from '@mui/material';
import {
  People as PeopleIcon,
  SupervisorAccount as LeadersIcon,
  AccountBalance as CommunityIcon,
  BarChart as ActivityIcon,
} from '@mui/icons-material';
import apiClient from '../api/client';

interface RelatoriosResumo {
  totalMembros: number;
  totalLideres: number;
  totalComunidades: number;
  volumeAtividadesMes: number;
  distribuicaoMembrosPorComunidade?: { comunidadeNome: string; quantidade: number }[];
}

const Reports: React.FC = () => {
  const [data, setData] = useState<RelatoriosResumo | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchReports = async () => {
      setIsLoading(true);
      setError('');
      try {
        const res = await apiClient.get<RelatoriosResumo>('/relatorios/resumo');
        setData(res.data);
      } catch (err: any) {
        console.error('Error fetching reports summary:', err);
        setError('Não foi possível carregar os relatórios.');
      } finally {
        setIsLoading(false);
      }
    };

    fetchReports();
  }, []);

  if (isLoading) {
    return (
      <Box>
        <Skeleton variant="text" width={240} height={50} sx={{ mb: 3 }} />
        <Grid container spacing={3}>
          {[1, 2, 3, 4].map((i) => (
            <Grid key={i} size={{ xs: 12, sm: 6, md: 3 }}>
              <Skeleton variant="rounded" height={130} sx={{ borderRadius: 0 }} />
            </Grid>
          ))}
        </Grid>
        <Box sx={{ mt: 4 }}>
          <Skeleton variant="rounded" height={260} sx={{ borderRadius: 0 }} />
        </Box>
      </Box>
    );
  }

  const cards = [
    {
      title: 'Membros Leigos',
      value: data?.totalMembros ?? 0,
      icon: PeopleIcon,
      color: '#0288d1',
      bgGradient: 'linear-gradient(135deg, #e0f7fa 0%, #b2ebf2 100%)',
    },
    {
      title: 'Líderes & Coordenadores',
      value: data?.totalLideres ?? 0,
      icon: LeadersIcon,
      color: '#00796b',
      bgGradient: 'linear-gradient(135deg, #e0f2f1 0%, #b2dfdb 100%)',
    },
    {
      title: 'Comunidades & Igrejas',
      value: data?.totalComunidades ?? 0,
      icon: CommunityIcon,
      color: '#7b1fa2',
      bgGradient: 'linear-gradient(135deg, #f3e5f5 0%, #e1bee7 100%)',
    },
    {
      title: 'Volume de Atividades',
      value: data?.volumeAtividadesMes ?? 0,
      icon: ActivityIcon,
      color: '#f57c00',
      bgGradient: 'linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%)',
    },
  ];

  return (
    <Box>
      <Typography variant="h4" sx={{ mb: 4, fontWeight: 700, color: '#37474f' }}>
        Relatórios & Métricas
      </Typography>

      {error && <Alert severity="error" sx={{ mb: 3 }}>{error}</Alert>}

      <Grid container spacing={3}>
        {cards.map((card, idx) => {
          const IconComp = card.icon;
          return (
            <Grid key={idx} size={{ xs: 12, sm: 6, md: 3 }}>
              <Card
                elevation={0}
                sx={{
                  borderRadius: 0,
                  border: '1px solid',
                  borderColor: 'divider',
                  background: 'linear-gradient(to bottom, #ffffff, #fcfcfc)',
                  boxShadow: '0 4px 12px rgba(0,0,0,0.02)',
                  transition: 'transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out',
                  '&:hover': {
                    transform: 'translateY(-3px)',
                    boxShadow: '0 10px 25px rgba(0,0,0,0.06)',
                  },
                }}
              >
                <CardContent sx={{ p: 3, '&:last-child': { pb: 3 } }}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <Box>
                      <Typography color="text.secondary" variant="subtitle2" sx={{ fontWeight: 600, mb: 1 }}>
                        {card.title}
                      </Typography>
                      <Typography variant="h3" sx={{ fontWeight: 800, color: 'text.primary' }}>
                        {card.value}
                      </Typography>
                    </Box>
                    <Box
                      sx={{
                        p: 1.5,
                        background: card.bgGradient,
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                      }}
                    >
                      <IconComp sx={{ fontSize: 28, color: card.color }} />
                    </Box>
                  </Box>
                </CardContent>
              </Card>
            </Grid>
          );
        })}
      </Grid>

      {data?.distribuicaoMembrosPorComunidade && (
        <Card
          elevation={0}
          sx={{
            mt: 4,
            p: 3,
            border: '1px solid',
            borderColor: 'divider',
            borderRadius: 0,
          }}
        >
          <Typography variant="h6" sx={{ mb: 3, fontWeight: 600 }}>
            Distribuição de Membros por Comunidade
          </Typography>
          <Stack spacing={2}>
            {data.distribuicaoMembrosPorComunidade.map((item, index) => {
              const percentage = data.totalMembros > 0 ? Math.round((item.quantidade / data.totalMembros) * 100) : 0;
              return (
                <Box key={index}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
                    <Typography variant="body2" sx={{ fontWeight: 500 }}>
                      {item.comunidadeNome}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      {item.quantidade} membros ({percentage}%)
                    </Typography>
                  </Box>
                  <LinearProgress
                    variant="determinate"
                    value={percentage}
                    sx={{
                      height: 8,
                      borderRadius: 0,
                      backgroundColor: '#eceff1',
                      '& .MuiLinearProgress-bar': {
                        backgroundColor: index % 2 === 0 ? '#0288d1' : '#00796b',
                      },
                    }}
                  />
                </Box>
              );
            })}
          </Stack>
        </Card>
      )}
    </Box>
  );
};

export default Reports;
