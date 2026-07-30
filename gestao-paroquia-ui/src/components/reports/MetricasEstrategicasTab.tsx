import React from 'react';
import {
  Box,
  Card,
  CardContent,
  Grid,
  Typography,
  Chip,
  LinearProgress,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Alert,
  Skeleton,
} from '@mui/material';
import {
  AutoGraph as PredictiveIcon,
  AssignmentTurnedIn as ProjectIcon,
  Timer as SlaIcon,
  VolunteerActivism as SocialIcon,
  WarningAmber as ConflictIcon,
  Domain as PropertyIcon,
} from '@mui/icons-material';
import { usePainelMetricasEstrategicas } from '../../hooks/useMetricasEstrategicas';
import ConcorrenciaAlertaBanner from '../ConcorrenciaAlertaBanner';

export const MetricasEstrategicasTab: React.FC = () => {
  const { data, isLoading, error } = usePainelMetricasEstrategicas();

  if (isLoading) {
    return (
      <Box sx={{ py: 2 }}>
        <Grid container spacing={3}>
          {[1, 2, 3, 4, 5, 6].map((i) => (
            <Grid key={i} size={{ xs: 12, md: 6 }}>
              <Skeleton variant="rounded" height={180} sx={{ borderRadius: 0 }} />
            </Grid>
          ))}
        </Grid>
      </Box>
    );
  }

  if (error || !data) {
    return (
      <Alert severity="error" sx={{ borderRadius: 0, my: 2 }}>
        Não foi possível carregar as métricas estratégicas. Verifique a conexão com o servidor.
      </Alert>
    );
  }

  const {
    sazonalidadeLiturgica,
    taxaProjetos,
    slaDespacho,
    impactoSocial,
    concorrenciaInterpastoral,
    ociosidadePatrimonial,
  } = data;

  return (
    <Box sx={{ py: 2 }}>
      {/* Banner Preventivo de Concorrência se houver conflitos */}
      {concorrenciaInterpastoral && concorrenciaInterpastoral.conflitos.length > 0 && (
        <ConcorrenciaAlertaBanner
          conflitos={concorrenciaInterpastoral.conflitos}
          nivelRisco={concorrenciaInterpastoral.nivelRiscoConcorrencia}
        />
      )}

      <Grid container spacing={3}>
        {/* 1. Sazonalidade Litúrgica (Predictive Pastoral Analytics) */}
        <Grid size={{ xs: 12, md: 6 }}>
          <Card sx={{ borderRadius: 0, height: '100%', borderTop: '4px solid #1a237e' }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 2 }}>
                <PredictiveIcon sx={{ color: '#1a237e' }} />
                <Typography variant="h6" sx={{ fontWeight: 700 }}>
                  1. Sazonalidade Litúrgica (Predictive Analytics)
                </Typography>
              </Box>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                Histórico litúrgico e projeção preditiva de picos operacionais para o Pároco e secretaria.
              </Typography>

              <TableContainer component={Paper} variant="outlined" sx={{ borderRadius: 0 }}>
                <Table size="small">
                  <TableHead sx={{ bgcolor: 'grey.100' }}>
                    <TableRow>
                      <TableCell>Ciclo / Festa</TableCell>
                      <TableCell align="center">Fator Demanda</TableCell>
                      <TableCell align="center">Histórico</TableCell>
                      <TableCell align="center">Projeção</TableCell>
                      <TableCell align="right">Nível</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {sazonalidadeLiturgica.map((s, idx) => (
                      <TableRow key={idx}>
                        <TableCell sx={{ fontWeight: 600 }}>{s.cicloNome}</TableCell>
                        <TableCell align="center">{s.fatorDemanda}x</TableCell>
                        <TableCell align="center">{s.quantidadeEventosHistoricos}</TableCell>
                        <TableCell align="center" sx={{ fontWeight: 700, color: 'primary.main' }}>
                          {s.quantidadeEventosPrevistos}
                        </TableCell>
                        <TableCell align="right">
                          <Chip
                            label={s.nivelDemanda}
                            size="small"
                            color={s.nivelDemanda === 'PICO_OPERACIONAL' ? 'error' : s.nivelDemanda === 'ALTA' ? 'warning' : 'default'}
                            sx={{ borderRadius: 0, fontSize: '0.65rem' }}
                          />
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            </CardContent>
          </Card>
        </Grid>

        {/* 2. Taxa de Conclusão e Abandono de Projetos (CPP/CAE) */}
        <Grid size={{ xs: 12, md: 6 }}>
          <Card sx={{ borderRadius: 0, height: '100%', borderTop: '4px solid #00796b' }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 2 }}>
                <ProjectIcon sx={{ color: '#00796b' }} />
                <Typography variant="h6" sx={{ fontWeight: 700 }}>
                  2. Conclusão & Abandono de Projetos (CPP/CAE)
                </Typography>
              </Box>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                Avaliação da viabilidade das decisões dos conselhos paroquiais.
              </Typography>

              <Box sx={{ mb: 2 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
                  <Typography variant="body2" sx={{ fontWeight: 600 }}>
                    Taxa de Conclusão ({taxaProjetos.taxaConclusaoPercentual}%)
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    {taxaProjetos.projetosConcluidos} de {taxaProjetos.totalProjetos} projetos
                  </Typography>
                </Box>
                <LinearProgress
                  variant="determinate"
                  value={taxaProjetos.taxaConclusaoPercentual}
                  color="success"
                  sx={{ height: 10, borderRadius: 0 }}
                />
              </Box>

              <Box sx={{ mb: 3 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
                  <Typography variant="body2" sx={{ fontWeight: 600 }}>
                    Taxa de Abandono/Cancelamento ({taxaProjetos.taxaAbandonoPercentual}%)
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    {taxaProjetos.projetosCancelados} cancelados
                  </Typography>
                </Box>
                <LinearProgress
                  variant="determinate"
                  value={taxaProjetos.taxaAbandonoPercentual}
                  color="error"
                  sx={{ height: 10, borderRadius: 0 }}
                />
              </Box>

              <Box sx={{ p: 1.5, bgcolor: 'background.default', borderLeft: '4px solid #00796b' }}>
                <Typography variant="caption" color="text.secondary" display="block">
                  Diagnóstico CPP/CAE
                </Typography>
                <Chip
                  label={`Avaliação: ${taxaProjetos.avaliacaoExecutiva}`}
                  color={taxaProjetos.avaliacaoExecutiva === 'SAUDAVEL' ? 'success' : 'warning'}
                  size="small"
                  sx={{ borderRadius: 0, fontWeight: 700, mt: 0.5 }}
                />
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* 3. SLA de Despacho Pastoral */}
        <Grid size={{ xs: 12, md: 6 }}>
          <Card sx={{ borderRadius: 0, height: '100%', borderTop: '4px solid #f57c00' }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 2 }}>
                <SlaIcon sx={{ color: '#f57c00' }} />
                <Typography variant="h6" sx={{ fontWeight: 700 }}>
                  3. SLA de Despacho Pastoral (Tempo de Homologação)
                </Typography>
              </Box>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                Eficiência burocrática entre a solicitação pastoral e o despacho final do Pároco/Conselho.
              </Typography>

              <Grid container spacing={2} sx={{ mb: 2 }}>
                <Grid size={{ xs: 6 }}>
                  <Paper variant="outlined" sx={{ p: 1.5, borderRadius: 0, textAlign: 'center' }}>
                    <Typography variant="caption" color="text.secondary">
                      Tempo Médio
                    </Typography>
                    <Typography variant="h5" sx={{ fontWeight: 700, color: '#f57c00' }}>
                      {slaDespacho.tempoMedioAprovacaoHoras}h
                    </Typography>
                  </Paper>
                </Grid>
                <Grid size={{ xs: 6 }}>
                  <Paper variant="outlined" sx={{ p: 1.5, borderRadius: 0, textAlign: 'center' }}>
                    <Typography variant="caption" color="text.secondary">
                      Dentro do SLA ({slaDespacho.slaMetaHoras}h)
                    </Typography>
                    <Typography variant="h5" sx={{ fontWeight: 700, color: 'success.main' }}>
                      {slaDespacho.percentualDentroSla}%
                    </Typography>
                  </Paper>
                </Grid>
              </Grid>

              <Chip
                label={`Status SLA: ${slaDespacho.statusSla}`}
                color={slaDespacho.statusSla === 'EXCELENTE' ? 'success' : 'warning'}
                sx={{ borderRadius: 0, fontWeight: 700 }}
              />
            </CardContent>
          </Card>
        </Grid>

        {/* 4. Coeficiente de Impacto Social (Pastoral em Saída) */}
        <Grid size={{ xs: 12, md: 6 }}>
          <Card sx={{ borderRadius: 0, height: '100%', borderTop: '4px solid #e91e63' }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 2 }}>
                <SocialIcon sx={{ color: '#e91e63' }} />
                <Typography variant="h6" sx={{ fontWeight: 700 }}>
                  4. Coeficiente de Impacto Social (Pastoral em Saída)
                </Typography>
              </Box>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                Proporção de obras de misericórdia e caridade externa versus reuniões institucionais internas.
              </Typography>

              <Box sx={{ mb: 2 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
                  <Typography variant="body2" sx={{ fontWeight: 600 }}>
                    Índice de Ação Missionária ({impactoSocial.coeficienteImpactoSocialPercentual}%)
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    {impactoSocial.eventosSociaisCaridade} sociais / {impactoSocial.reunioesInternas} reuniões
                  </Typography>
                </Box>
                <LinearProgress
                  variant="determinate"
                  value={impactoSocial.coeficienteImpactoSocialPercentual}
                  sx={{
                    height: 10,
                    borderRadius: 0,
                    bgcolor: 'grey.300',
                    '& .MuiLinearProgress-bar': { bgcolor: '#e91e63' },
                  }}
                />
              </Box>

              <Chip
                label={`Diretriz Pastoral: ${impactoSocial.diagnosticoPastoral}`}
                sx={{ borderRadius: 0, fontWeight: 700, bgcolor: '#fce4ec', color: '#c2185b' }}
              />
            </CardContent>
          </Card>
        </Grid>

        {/* 5. Alerta de Concorrência Interpastoral */}
        <Grid size={{ xs: 12, md: 6 }}>
          <Card sx={{ borderRadius: 0, height: '100%', borderTop: '4px solid #7b1fa2' }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 2 }}>
                <ConflictIcon sx={{ color: '#7b1fa2' }} />
                <Typography variant="h6" sx={{ fontWeight: 700 }}>
                  5. Alerta de Concorrência Interpastoral
                </Typography>
              </Box>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                Prevenção de esvaziamento por eventos do mesmo público em datas próximas.
              </Typography>

              <Paper variant="outlined" sx={{ p: 2, borderRadius: 0, mb: 2 }}>
                <Typography variant="body2" sx={{ fontWeight: 600 }}>
                  Conflitos Detectados: {concorrenciaInterpastoral.totalConflitosDetectados}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  Nível de Risco do Calendário: {concorrenciaInterpastoral.nivelRiscoConcorrencia}
                </Typography>
              </Paper>

              <Chip
                label={`Risco Interpastoral: ${concorrenciaInterpastoral.nivelRiscoConcorrencia}`}
                color={concorrenciaInterpastoral.nivelRiscoConcorrencia === 'BAIXO' ? 'success' : 'warning'}
                sx={{ borderRadius: 0, fontWeight: 700 }}
              />
            </CardContent>
          </Card>
        </Grid>

        {/* 6. Índice de Ociosidade Patrimonial */}
        <Grid size={{ xs: 12, md: 6 }}>
          <Card sx={{ borderRadius: 0, height: '100%', borderTop: '4px solid #455a64' }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 2 }}>
                <PropertyIcon sx={{ color: '#455a64' }} />
                <Typography variant="h6" sx={{ fontWeight: 700 }}>
                  6. Índice de Ociosidade Patrimonial (CAE)
                </Typography>
              </Box>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                Aproveitamento útil das salas de catequese, capelas e auditórios paroquiais.
              </Typography>

              <Grid container spacing={2} sx={{ mb: 2 }}>
                <Grid size={{ xs: 6 }}>
                  <Paper variant="outlined" sx={{ p: 1.5, borderRadius: 0, textAlign: 'center' }}>
                    <Typography variant="caption" color="text.secondary">
                      Horas Ociosas / Semana
                    </Typography>
                    <Typography variant="h5" sx={{ fontWeight: 700, color: 'text.secondary' }}>
                      {ociosidadePatrimonial.horasOciosasSemana}h
                    </Typography>
                  </Paper>
                </Grid>
                <Grid size={{ xs: 6 }}>
                  <Paper variant="outlined" sx={{ p: 1.5, borderRadius: 0, textAlign: 'center' }}>
                    <Typography variant="caption" color="text.secondary">
                      Índice de Ociosidade
                    </Typography>
                    <Typography variant="h5" sx={{ fontWeight: 700, color: 'primary.main' }}>
                      {ociosidadePatrimonial.indiceOciosidadePercentual}%
                    </Typography>
                  </Paper>
                </Grid>
              </Grid>

              <Chip
                label={`Recomendação CAE: ${ociosidadePatrimonial.recomendacaoCae}`}
                color="info"
                sx={{ borderRadius: 0, fontWeight: 700 }}
              />
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default MetricasEstrategicasTab;
