import React from 'react';
import { useQuery } from '@tanstack/react-query';
import {
  Accordion,
  AccordionDetails,
  AccordionSummary,
  Box,
  CircularProgress,
  IconButton,
  Stack,
  Tooltip,
  Typography,
} from '@mui/material';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import CheckCircleOutlineIcon from '@mui/icons-material/CheckCircleOutlineOutlined';
import HighlightOffIcon from '@mui/icons-material/HighlightOff';
import EventRepeatIcon from '@mui/icons-material/EventRepeat';
import { getAprovacaoOcorrencias } from '@/contextos/aprovacao/infrastructure/http/aprovacoes';
import StatusIcon from '@/shared/presentation/components/StatusIcon';
import { getAprovacaoStatusMeta } from '@/shared/utils/statusMeta/aprovacaoStatus';

interface OcorrenciasRecorrenciaAprovacaoProps {
  aprovacaoPaiId: string;
  totalOcorrencias?: number | null;
  statusPai: string;
  onDecidirOcorrencia: (id: string, status: 'APROVADA' | 'REPROVADA') => void;
  disabled?: boolean;
}

/**
 * Visualizador mestre-detalhe para solicitações de séries recorrentes.
 * Permite inspecionar todas as ocorrências filhas programadas e decidir
 * granularmente cada uma se necessário.
 */
const OcorrenciasRecorrenciaAprovacao: React.FC<OcorrenciasRecorrenciaAprovacaoProps> = ({
  aprovacaoPaiId,
  totalOcorrencias,
  statusPai,
  onDecidirOcorrencia,
  disabled = false,
}) => {
  const { data: ocorrencias, isLoading } = useQuery({
    queryKey: ['aprovacao-ocorrencias', aprovacaoPaiId],
    queryFn: () => getAprovacaoOcorrencias(aprovacaoPaiId),
    enabled: Boolean(aprovacaoPaiId),
  });

  return (
    <Box sx={{ mb: 3 }}>
      <Accordion variant="outlined" defaultExpanded sx={{ borderRadius: 1.5 }}>
        <AccordionSummary expandIcon={<ExpandMoreIcon />}>
          <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
            <EventRepeatIcon color="primary" fontSize="small" />
            <Typography variant="subtitle2" sx={{ fontWeight: 700 }}>
              Ocorrências da Série ({ocorrencias?.length ?? totalOcorrencias ?? '...'})
            </Typography>
          </Stack>
        </AccordionSummary>
        <AccordionDetails sx={{ pt: 0 }}>
          <Typography variant="caption" color="text.secondary" sx={{ mb: 1.5, display: 'block' }}>
            Você pode aprovar ou rejeitar a série completa pelo painel principal, ou aprovar/reprovar datas individuais abaixo:
          </Typography>

          {isLoading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', p: 2 }}>
              <CircularProgress size={24} />
            </Box>
          ) : !ocorrencias || ocorrencias.length === 0 ? (
            <Typography variant="caption" color="text.secondary">
              Nenhuma ocorrência filha registrada.
            </Typography>
          ) : (
            <Stack spacing={1.25}>
              {ocorrencias.map((item, idx) => (
                <Box
                  key={item.id}
                  sx={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    p: 1.5,
                    border: '1px solid',
                    borderColor: 'divider',
                    borderRadius: 2,
                    bgcolor: 'background.paper',
                  }}
                >
                  <Box>
                    <Typography variant="body2" sx={{ fontWeight: 600, textTransform: 'capitalize' }}>
                      #{idx + 1} -{' '}
                      {item.dataOcorrenciaUtc
                        ? new Date(item.dataOcorrenciaUtc).toLocaleDateString('pt-BR', {
                            weekday: 'long',
                            day: '2-digit',
                            month: '2-digit',
                            year: 'numeric',
                          })
                        : 'Data não informada'}
                    </Typography>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.75, mt: 0.5 }}>
                      <StatusIcon {...getAprovacaoStatusMeta(item.status)} />
                    </Box>
                  </Box>

                  {item.status === 'PENDENTE' && statusPai === 'PENDENTE' && (
                    <Stack direction="row" spacing={1}>
                      <Tooltip title="Aprovar esta data">
                        <span>
                          <IconButton
                            size="small"
                            color="success"
                            disabled={disabled}
                            onClick={() => onDecidirOcorrencia(item.id, 'APROVADA')}
                          >
                            <CheckCircleOutlineIcon fontSize="small" />
                          </IconButton>
                        </span>
                      </Tooltip>
                      <Tooltip title="Rejeitar esta data">
                        <span>
                          <IconButton
                            size="small"
                            color="error"
                            disabled={disabled}
                            onClick={() => onDecidirOcorrencia(item.id, 'REPROVADA')}
                          >
                            <HighlightOffIcon fontSize="small" />
                          </IconButton>
                        </span>
                      </Tooltip>
                    </Stack>
                  )}
                </Box>
              ))}
            </Stack>
          )}
        </AccordionDetails>
      </Accordion>
    </Box>
  );
};

export default OcorrenciasRecorrenciaAprovacao;
