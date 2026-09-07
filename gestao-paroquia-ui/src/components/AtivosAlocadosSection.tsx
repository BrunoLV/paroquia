import React, { useEffect, useState } from 'react';
import { Box, Chip, Skeleton, Typography } from '@mui/material';
import { Inventory as InventoryIcon, WarningAmber as WarningAmberIcon } from '@mui/icons-material';
import { getAlocacoesPorEvento } from '../api/ativos';
import { AtivoAlocacaoResponse } from '../api/types';
import StatusIcon from './StatusIcon';
import { getAtivoAlocacaoStatusMeta } from '../utils/statusMeta/ativoAlocacaoStatus';
import { formatDateBr } from '../utils/masks';

interface AtivosAlocadosSectionProps {
  eventoId: string;
}

const AtivosAlocadosSection: React.FC<AtivosAlocadosSectionProps> = ({ eventoId }) => {
  const [alocacoes, setAlocacoes] = useState<AtivoAlocacaoResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    let active = true;
    setIsLoading(true);
    getAlocacoesPorEvento(eventoId)
      .then((data) => {
        if (active) setAlocacoes(data || []);
      })
      .catch((err) => {
      })
      .finally(() => {
        if (active) setIsLoading(false);
      });
    return () => {
      active = false;
    };
  }, [eventoId]);

  if (isLoading) {
    return <Skeleton variant="rounded" height={60} />;
  }

  if (alocacoes.length === 0) {
    return null;
  }

  return (
    <Box sx={{ display: 'flex', gap: 2, alignItems: 'flex-start' }}>
      <InventoryIcon color="action" sx={{ mt: 0.3 }} />
      <Box sx={{ width: '100%' }}>
        <Typography variant="body2" color="text.secondary" sx={{ fontWeight: 500, mb: 0.5 }}>
          Ativos Alocados
        </Typography>
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
          {alocacoes.map((alocacao) => (
            <Box key={alocacao.id} sx={{ display: 'flex', alignItems: 'center', gap: 1, flexWrap: 'wrap' }}>
              <Typography variant="body2" sx={{ fontWeight: 600 }}>
                {alocacao.ativoNome || alocacao.ativoId}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                {formatDateBr(alocacao.dataInicio)} — {formatDateBr(alocacao.dataFim)}
              </Typography>
              <StatusIcon {...getAtivoAlocacaoStatusMeta(alocacao.status)} />
              {alocacao.impactadaPorAtraso && (
                <Chip label="Pode atrasar" color="warning" icon={<WarningAmberIcon />} size="small" variant="outlined" />
              )}
            </Box>
          ))}
        </Box>
      </Box>
    </Box>
  );
};

export default AtivosAlocadosSection;
