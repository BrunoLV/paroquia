import React from 'react';
import { Alert, AlertTitle, Box, Typography, Chip, Stack } from '@mui/material';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import { ConflitoItemDto } from '../api/metricasTypes';

interface ConcorrenciaAlertaBannerProps {
  conflitos: ConflitoItemDto[];
  nivelRisco: 'BAIXO' | 'MEDIO' | 'ALTO';
}

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
        sx={{ borderRadius: 0, border: '1px solid', borderColor: severity === 'error' ? 'error.main' : 'warning.main' }}
      >
        <AlertTitle sx={{ fontWeight: 700 }}>
          Atenção: Risco de Concorrência Interpastoral ({nivelRisco})
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
              <Typography variant="caption" display="block" color="text.secondary">
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
                sx={{ mt: 0.5, borderRadius: 0, fontSize: '0.7rem' }}
              />
            </Box>
          ))}
        </Stack>
      </Alert>
    </Box>
  );
};

export default ConcorrenciaAlertaBanner;
