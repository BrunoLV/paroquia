import React from 'react';
import { Box, Card, CardContent, Grid, Typography, Chip, Skeleton, useTheme } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { usePainelMetricasEstrategicas } from '@/contextos/painel/presentation/hooks/useMetricasEstrategicas';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import ArrowForwardIcon from '@mui/icons-material/ArrowForward';
import ActionButton from '@/shared/presentation/components/ActionButton';
import {
  getAvaliacaoExecutivaLabel,
  getDiagnosticoPastoralLabel,
  getNivelRiscoLabel,
  getStatusSlaLabel,
} from '@/shared/utils/statusMeta/metricasEstrategicasLabels';

export const MetricasEstrategicasWidget: React.FC = () => {
  const theme = useTheme();
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
    <Card sx={{ mb: 3, bgcolor: theme.custom?.subtlePanelBg ?? '#F8F9FA' }}>
      <CardContent>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2, flexWrap: 'wrap' }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <TrendingUpIcon sx={{ color: 'primary.main' }} />
            <Typography variant="h6" sx={{ fontWeight: 700, color: 'primary.main' }}>
              Inteligência Paroquial & Governança (CPP/CAE)
            </Typography>
          </Box>
          <ActionButton
            title="Ver Relatório Completo"
            icon={<ArrowForwardIcon />}
            size="small"
            onClick={() => navigate('/relatorios')}
            color="primary"
          />
        </Box>

        <Grid container spacing={2}>
          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <Box sx={{ p: 1.5, bgcolor: 'background.paper', border: '1px solid', borderColor: 'divider' }}>
              <Typography variant="caption" color="text.secondary">
                SLA Despacho Pastoral
              </Typography>
              <Typography variant="h6" sx={{ fontWeight: 700, color: 'warning.main' }}>
                {slaDespacho.tempoMedioAprovacaoHoras}h
              </Typography>
              <Chip label={getStatusSlaLabel(slaDespacho.statusSla)} size="small" color="success" sx={{ fontSize: '0.65rem', mt: 0.5 }} />
            </Box>
          </Grid>

          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <Box sx={{ p: 1.5, bgcolor: 'background.paper', border: '1px solid', borderColor: 'divider' }}>
              <Typography variant="caption" color="text.secondary">
                Conclusão de Projetos
              </Typography>
              <Typography variant="h6" sx={{ fontWeight: 700, color: 'success.main' }}>
                {taxaProjetos.taxaConclusaoPercentual}%
              </Typography>
              <Chip label={getAvaliacaoExecutivaLabel(taxaProjetos.avaliacaoExecutiva)} size="small" color="info" sx={{ fontSize: '0.65rem', mt: 0.5 }} />
            </Box>
          </Grid>

          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <Box sx={{ p: 1.5, bgcolor: 'background.paper', border: '1px solid', borderColor: 'divider' }}>
              <Typography variant="caption" color="text.secondary">
                Pastoral em Saída
              </Typography>
              <Typography variant="h6" sx={{ fontWeight: 700, color: 'secondary.main' }}>
                {impactoSocial.coeficienteImpactoSocialPercentual}%
              </Typography>
              <Chip label={getDiagnosticoPastoralLabel(impactoSocial.diagnosticoPastoral)} size="small" color="secondary" sx={{ fontSize: '0.65rem', mt: 0.5 }} />
            </Box>
          </Grid>

          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <Box sx={{ p: 1.5, bgcolor: 'background.paper', border: '1px solid', borderColor: 'divider' }}>
              <Typography variant="caption" color="text.secondary">
                Risco Interpastoral
              </Typography>
              <Typography variant="h6" sx={{ fontWeight: 700, color: 'text.secondary' }}>
                {concorrenciaInterpastoral.totalConflitosDetectados} conflitos
              </Typography>
              <Chip label={`Risco ${getNivelRiscoLabel(concorrenciaInterpastoral.nivelRiscoConcorrencia)}`} size="small" color={concorrenciaInterpastoral.nivelRiscoConcorrencia === 'BAIXO' ? 'success' : 'warning'} sx={{ fontSize: '0.65rem', mt: 0.5 }} />
            </Box>
          </Grid>
        </Grid>
      </CardContent>
    </Card>
  );
};

export default MetricasEstrategicasWidget;
