import React from 'react';
import { Alert, AlertTitle, Box, Typography, Chip, Stack } from '@mui/material';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import { ConflitoItemDto } from '@/api/metricasTypes';
import { getNivelRiscoLabel } from '@/utils/statusMeta/metricasEstrategicasLabels';

interface ConcorrenciaAlertaBannerProps {
  conflitos: ConflitoItemDto[];
  nivelRisco: 'BAIXO' | 'MEDIO' | 'ALTO';
}

/**
 * Alerta de "concorrência interpastoral": exibido quando o backend detecta que duas pastorais
 * diferentes marcaram eventos em datas próximas disputando o mesmo público-alvo (ex.: dois
 * grupos de jovens no mesmo fim de semana) — não trata conflito de local/recurso, apenas
 * sobreposição de público. Cada item de `conflitos` já vem com a mensagem de alerta formatada
 * pelo backend (`ConflitoItemDto`, ver `api/metricasTypes.ts`).
 *
 * O banner some sozinho quando `conflitos` está vazio: isso acontece quando o período analisado
 * não tem mais sobreposições (evento cancelado/remarcado) ou quando o indicador de concorrência
 * interpastoral ainda não foi carregado/foi recalculado sem risco. `nivelRisco` só eleva a
 * severidade visual (ALTO vira `error`); BAIXO/MEDIO permanecem como aviso (`warning`).
 */
export const ConcorrenciaAlertaBanner: React.FC<ConcorrenciaAlertaBannerProps> = ({
  conflitos,
  nivelRisco,
}) => {
  if (!conflitos || conflitos.length === 0) {
    return null;
  }

  const severity = nivelRisco === 'ALTO' ? 'error' : 'warning';

  return (
    <Box sx={{ mb: 2 }}>
      <Alert
        severity={severity}
        icon={<WarningAmberIcon fontSize="inherit" />}
        sx={{ border: '1px solid', borderColor: severity === 'error' ? 'error.main' : 'warning.main' }}
      >
        <AlertTitle sx={{ fontWeight: 700 }}>
          Atenção: Risco de Concorrência Interpastoral ({getNivelRiscoLabel(nivelRisco)})
        </AlertTitle>
        <Typography variant="body2" sx={{ mb: 1 }}>
          Foram identificadas sobreposições de público/tema em datas próximas por pastorais distintas.
        </Typography>

        <Stack spacing={1}>
          {conflitos.map((c, index) => (
            <Box
              key={index}
              sx={{
                p: 1,
                bgcolor: 'background.paper',
                borderLeft: '4px solid',
                borderColor: severity === 'error' ? 'error.main' : 'warning.main',
              }}
            >
              <Typography variant="caption" sx={{ display: 'block', color: 'text.secondary' }}>
                {c.dataEvento} • Público: {c.publicoAlvoCompartilhado}
              </Typography>
              <Typography variant="body2" sx={{ fontWeight: 600 }}>
                {c.tituloEventoA} ({c.pastoralA}) vs {c.tituloEventoB} ({c.pastoralB})
              </Typography>
              <Chip
                label={c.mensagemAlerta}
                size="small"
                color={severity === 'error' ? 'error' : 'warning'}
                variant="outlined"
                sx={{ mt: 0.5, fontSize: '0.7rem' }}
              />
            </Box>
          ))}
        </Stack>
      </Alert>
    </Box>
  );
};

export default ConcorrenciaAlertaBanner;
