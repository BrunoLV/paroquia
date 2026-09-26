import React from 'react';
import { Box, Chip, Paper, Skeleton, Stack, Typography } from '@mui/material';
import PendingActionsIcon from '@mui/icons-material/PendingActions';
import TaskAltIcon from '@mui/icons-material/TaskAlt';
import ArrowForwardIcon from '@mui/icons-material/ArrowForward';
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { getAprovacoes } from '@/contextos/aprovacao/infrastructure/http/aprovacoes';
import StatusIcon from '@/shared/presentation/components/StatusIcon';
import ActionButton from '@/shared/presentation/components/ActionButton';
import { getAprovacaoTipoMeta, type AprovacaoTipoSolicitacao } from '@/shared/utils/statusMeta/aprovacaoStatus';

/**
 * Quadro sintético de aprovações pendentes exibindo apenas quantidades por tipo
 * e botão de redirecionamento para o módulo completo de aprovações.
 */
const AprovacoesPendentes: React.FC = () => {
  const navigate = useNavigate();
  const { data, isLoading } = useQuery({
    queryKey: ['aprovacoesPendentes', 'dashboard'],
    queryFn: () => getAprovacoes('PENDENTE', 0, 100),
  });

  const pendentes = data?.content ?? [];
  const total = data?.totalElements ?? pendentes.length;

  // Agrupamento estrito por tipo de solicitação
  const contagemPorTipo = React.useMemo(() => {
    return pendentes.reduce((acc, item) => {
      const tipo = item.tipoSolicitacao || 'OUTRO';
      acc[tipo] = (acc[tipo] || 0) + 1;
      return acc;
    }, {} as Record<string, number>);
  }, [pendentes]);

  const tiposOrdenados = React.useMemo(() => {
    return Object.entries(contagemPorTipo).sort((a, b) => b[1] - a[1]);
  }, [contagemPorTipo]);

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
          <PendingActionsIcon sx={{ color: 'warning.main', fontSize: 20 }} />
          <Typography variant="subtitle1" sx={{ fontWeight: 700, color: 'text.primary' }}>
            Aprovações Pendentes
          </Typography>
        </Stack>
        {total > 0 && <Chip label={total} size="small" color="warning" sx={{ fontWeight: 700 }} />}
      </Box>

      <Typography variant="body2" color="text.secondary" sx={{ mb: 2.5 }}>
        Solicitações aguardando decisão agrupadas por tipo
      </Typography>

      {isLoading ? (
        <Stack spacing={1.5} sx={{ flexGrow: 1, mb: 2.5 }}>
          {[1, 2, 3].map((i) => (
            <Skeleton key={i} variant="rounded" height={42} />
          ))}
        </Stack>
      ) : tiposOrdenados.length > 0 ? (
        <>
          <Stack spacing={1.25} sx={{ flexGrow: 1, mb: 2.5 }}>
            {tiposOrdenados.map(([tipo, count]) => {
              const meta = getAprovacaoTipoMeta(tipo as AprovacaoTipoSolicitacao);
              return (
                <Box
                  key={tipo}
                  sx={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    p: 1.25,
                    px: 1.5,
                    borderRadius: 1.5,
                    border: '1px solid',
                    borderColor: 'divider',
                    bgcolor: 'action.hover',
                  }}
                >
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.25, minWidth: 0 }}>
                    <StatusIcon {...meta} size="small" />
                    <Typography variant="body2" sx={{ fontWeight: 600, color: 'text.primary' }}>
                      {meta.label}
                    </Typography>
                  </Box>
                  <Chip
                    label={count}
                    size="small"
                    color={meta.color}
                    sx={{ fontWeight: 700, minWidth: 32 }}
                  />
                </Box>
              );
            })}
          </Stack>

          <ActionButton
            fullWidth
            title="Acessar Módulo de Aprovações"
            label="Acessar Módulo de Aprovações"
            icon={<ArrowForwardIcon />}
            color="primary"
            variant="contained"
            onClick={() => navigate('/aprovacoes')}
            sx={{ mt: 'auto' }}
          />
        </>
      ) : (
        <Box sx={{ py: 4, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 1.5, flexGrow: 1, justifyContent: 'center' }}>
          <TaskAltIcon sx={{ fontSize: 40, color: 'success.main' }} />
          <Typography color="text.secondary" variant="body2" sx={{ fontWeight: 500 }}>
            Nenhuma aprovação pendente no momento.
          </Typography>
          <ActionButton
            title="Ver Histórico de Aprovações"
            label="Ver Histórico de Aprovações"
            icon={<ArrowForwardIcon />}
            color="primary"
            variant="outlined"
            size="small"
            onClick={() => navigate('/aprovacoes')}
            sx={{ mt: 1 }}
          />
        </Box>
      )}
    </Paper>
  );
};

export default AprovacoesPendentes;
