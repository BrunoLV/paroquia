import { Box, Chip, Paper, Skeleton, Stack, Typography } from '@mui/material';
import PendingActionsIcon from '@mui/icons-material/PendingActions';
import TaskAltIcon from '@mui/icons-material/TaskAlt';
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { getAprovacoes } from '../../api/aprovacoes';
import StatusIcon from '../StatusIcon';
import ActionButton from '../ActionButton';
import { getAprovacaoDetalhe, getAprovacaoTipoMeta } from '../../utils/statusMeta/aprovacaoStatus';

const formatTempoPendente = (isoDate: string): string => {
  const minutos = Math.max(0, Math.floor((Date.now() - new Date(isoDate).getTime()) / 60000));
  if (minutos < 60) return `há ${minutos || 1} min`;
  const horas = Math.floor(minutos / 60);
  if (horas < 24) return `há ${horas}h`;
  const dias = Math.floor(horas / 24);
  return `há ${dias} dia${dias > 1 ? 's' : ''}`;
};

const AprovacoesPendentes = () => {
  const navigate = useNavigate();
  const { data, isLoading } = useQuery({
    queryKey: ['aprovacoesPendentes', 'dashboard'],
    queryFn: () => getAprovacoes('PENDENTE', 0, 5),
  });

  const pendentes = data?.content ?? [];
  const total = data?.totalElements ?? 0;

  return (
    <Paper elevation={0} sx={{ p: 3, border: '1px solid', borderColor: 'divider' }}>
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <PendingActionsIcon sx={{ color: 'warning.main', fontSize: 20 }} />
          <Typography variant="h6" sx={{ fontWeight: 700, color: 'text.primary' }}>
            Aprovações Pendentes
          </Typography>
        </Box>
        {total > 0 && <Chip label={total} size="small" color="warning" sx={{ fontWeight: 700 }} />}
      </Box>

      {isLoading ? (
        <Stack spacing={1.5}>
          {[1, 2, 3].map((i) => (
            <Skeleton key={i} variant="rounded" height={40} />
          ))}
        </Stack>
      ) : pendentes.length > 0 ? (
        <Stack spacing={0.5}>
          {pendentes.map((aprovacao) => {
            const meta = getAprovacaoTipoMeta(aprovacao.tipoSolicitacao);
            return (
              <Box key={aprovacao.id} sx={{ display: 'flex', alignItems: 'center', gap: 1.5, py: 1 }}>
                <StatusIcon {...meta} size="small" />
                <Box sx={{ minWidth: 0, flexGrow: 1 }}>
                  <Typography variant="body2" noWrap sx={{ fontWeight: 700 }}>
                    {getAprovacaoDetalhe(aprovacao)}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    {meta.label} · {formatTempoPendente(aprovacao.criadoEmUtc)}
                  </Typography>
                </Box>
              </Box>
            );
          })}

          <ActionButton
            title="Ir para a tela de Aprovações"
            label="Ver Aprovações"
            icon={<PendingActionsIcon />}
            variant="text"
            size="small"
            onClick={() => navigate('/approvals')}
            sx={{ mt: 1, alignSelf: 'flex-start' }}
          />
        </Stack>
      ) : (
        <Box sx={{ py: 3, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 1 }}>
          <TaskAltIcon sx={{ fontSize: 36, color: 'success.main' }} />
          <Typography color="text.secondary" variant="body2">
            Nenhuma aprovação pendente.
          </Typography>
        </Box>
      )}
    </Paper>
  );
};

export default AprovacoesPendentes;
