import React from 'react';
import { Box, Card, CardContent, Grid, Typography, Chip, Skeleton } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { usePainelMetricasEstrategicas } from '../../hooks/useMetricasEstrategicas';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import ArrowForwardIcon from '@mui/icons-material/ArrowForward';
import ActionButton from '../ActionButton';

export const MetricasEstrategicasWidget: React.FC = () => {
  const navigate = useNavigate();
  const { data, isLoading } = usePainelMetricasEstrategicas();

  if (isLoading) {
    return <Skeleton variant="rounded" height={140} sx={{ mb: 3 }} />;
  }

  if (!data) {
    return null;
  }

  const { taxaProjetos, slaDespacho, impactoSocial, concorrenciaInterpastoral } = data;

  return (
    <Card sx={{ mb: 3, borderLeft: '4px solid #1a237e', bgcolor: '#f8f9fa' }}>
      <CardContent>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2, flexWrap: 'wrap' }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <TrendingUpIcon sx={{ color: '#1a237e' }} />
            <Typography variant="h6" sx={{ fontWeight: 700, color: 'primary.main' }}>
              Inteligência Paroquial & Governança (CPP/CAE)
            </Typography>
          </Box>
          <ActionButton
            title="Ver Relatório Completo"
            icon={<ArrowForwardIcon />}
            size="small"
            onClick={() => navigate('/reports')}
            color="primary"
          />
        </Box>

        <Grid container spacing={2}>
          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <Box sx={{ p: 1.5, bgcolor: 'background.paper', border: '1px solid', borderColor: 'divider' }}>
              <Typography variant="caption" color="text.secondary">
                SLA Despacho Pastoral
              </Typography>
              <Typography variant="h6" sx={{ fontWeight: 700, color: '#f57c00' }}>
                {slaDespacho.tempoMedioAprovacaoHoras}h
              </Typography>
              <Chip label={slaDespacho.statusSla} size="small" color="success" sx={{ fontSize: '0.65rem', mt: 0.5 }} />
            </Box>
          </Grid>

          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <Box sx={{ p: 1.5, bgcolor: 'background.paper', border: '1px solid', borderColor: 'divider' }}>
              <Typography variant="caption" color="text.secondary">
                Conclusão de Projetos
              </Typography>
              <Typography variant="h6" sx={{ fontWeight: 700, color: '#00796b' }}>
                {taxaProjetos.taxaConclusaoPercentual}%
              </Typography>
              <Chip label={taxaProjetos.avaliacaoExecutiva} size="small" color="info" sx={{ fontSize: '0.65rem', mt: 0.5 }} />
            </Box>
          </Grid>

          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <Box sx={{ p: 1.5, bgcolor: 'background.paper', border: '1px solid', borderColor: 'divider' }}>
              <Typography variant="caption" color="text.secondary">
                Pastoral em Saída
              </Typography>
              <Typography variant="h6" sx={{ fontWeight: 700, color: '#e91e63' }}>
                {impactoSocial.coeficienteImpactoSocialPercentual}%
              </Typography>
              <Chip label={impactoSocial.diagnosticoPastoral} size="small" color="secondary" sx={{ fontSize: '0.65rem', mt: 0.5 }} />
            </Box>
          </Grid>

          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <Box sx={{ p: 1.5, bgcolor: 'background.paper', border: '1px solid', borderColor: 'divider' }}>
              <Typography variant="caption" color="text.secondary">
                Risco Interpastoral
              </Typography>
              <Typography variant="h6" sx={{ fontWeight: 700, color: '#7b1fa2' }}>
                {concorrenciaInterpastoral.totalConflitosDetectados} conflitos
              </Typography>
              <Chip label={`Risco ${concorrenciaInterpastoral.nivelRiscoConcorrencia}`} size="small" color={concorrenciaInterpastoral.nivelRiscoConcorrencia === 'BAIXO' ? 'success' : 'warning'} sx={{ fontSize: '0.65rem', mt: 0.5 }} />
            </Box>
          </Grid>
        </Grid>
      </CardContent>
    </Card>
  );
};

export default MetricasEstrategicasWidget;
