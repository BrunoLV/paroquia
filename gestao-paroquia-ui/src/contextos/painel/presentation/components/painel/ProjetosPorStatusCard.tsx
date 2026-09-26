import React from 'react';
import { Alert, Box, Chip, LinearProgress, Paper, Stack, Typography } from '@mui/material';
import AssignmentIcon from '@mui/icons-material/Assignment';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import { ProjetosPorStatus } from '@/shared/domain/types/types';

interface ProjetosPorStatusCardProps {
  data?: ProjetosPorStatus;
}

interface FaixaStatus {
  key: keyof ProjetosPorStatus;
  label: string;
  descricao: string;
  color: string;
  progressColor: 'info' | 'success' | 'inherit' | 'error';
}

const FAIXAS: FaixaStatus[] = [
  {
    key: 'planejamento',
    label: 'Planejamento',
    descricao: 'Em estruturação e alinhamento inicial',
    color: 'info.main',
    progressColor: 'info',
  },
  {
    key: 'emExecucao',
    label: 'Em Execução',
    descricao: 'Cronograma ativo com eventos em andamento',
    color: 'success.main',
    progressColor: 'success',
  },
  {
    key: 'concluidos',
    label: 'Concluídos',
    descricao: 'Finalizados e metas atingidas',
    color: 'text.secondary',
    progressColor: 'inherit',
  },
  {
    key: 'atrasados',
    label: 'Atrasados / Em Risco',
    descricao: 'Prazo expirado ou descompasso crítico',
    color: 'error.main',
    progressColor: 'error',
  },
];

/**
 * Quadro analítico descritivo da distribuição de status do portfólio de projetos,
 * com ênfase visual em projetos atrasados ou sob risco operacional.
 */
const ProjetosPorStatusCard: React.FC<ProjetosPorStatusCardProps> = ({ data }) => {
  const total = (data?.planejamento ?? 0) + (data?.emExecucao ?? 0) + (data?.concluidos ?? 0) + (data?.atrasados ?? 0);
  const temAtrasados = (data?.atrasados ?? 0) > 0;

  return (
    <Paper
      elevation={0}
      sx={{
        p: 3,
        border: '1px solid',
        borderColor: 'divider',
        borderRadius: 2,
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
      }}
    >
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 1 }}>
        <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
          <AssignmentIcon color="primary" fontSize="small" />
          <Typography variant="subtitle1" sx={{ fontWeight: 700, color: 'text.primary' }}>
            Projetos por Status
          </Typography>
        </Stack>
        {total > 0 && (
          <Chip label={`${total} projetos`} size="small" variant="outlined" sx={{ fontWeight: 600 }} />
        )}
      </Box>

      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
        Distribuição e saúde operacional do portfólio paroquial
      </Typography>

      {temAtrasados && (
        <Alert
          severity="error"
          icon={<WarningAmberIcon fontSize="small" />}
          sx={{ mb: 2, py: 0.5, '& .MuiAlert-message': { fontSize: 13, fontWeight: 600 } }}
        >
          {data?.atrasados} {data?.atrasados === 1 ? 'projeto com atraso crítico identificado' : 'projetos com atraso crítico identificados'}
        </Alert>
      )}

      <Stack spacing={2} sx={{ flexGrow: 1, justifyContent: 'center' }}>
        {FAIXAS.map((faixa) => {
          const valor = data ? data[faixa.key] : 0;
          const percentual = total > 0 ? (valor / total) * 100 : 0;

          return (
            <Box key={faixa.key}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', mb: 0.5 }}>
                <Box sx={{ minWidth: 0, mr: 1 }}>
                  <Typography variant="body2" sx={{ fontWeight: 700, color: 'text.primary' }}>
                    {faixa.label}
                  </Typography>
                  <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block' }}>
                    {faixa.descricao}
                  </Typography>
                </Box>
                <Typography variant="h6" sx={{ fontWeight: 800, color: faixa.color }}>
                  {data ? valor : '--'}
                </Typography>
              </Box>
              <LinearProgress
                variant="determinate"
                value={percentual}
                color={faixa.progressColor}
                sx={{ height: 6, borderRadius: 3, bgcolor: 'action.hover' }}
              />
            </Box>
          );
        })}
      </Stack>
    </Paper>
  );
};

export default ProjetosPorStatusCard;
