import React, { useEffect, useState } from 'react';
import { Box, Chip, Skeleton, Typography } from '@mui/material';
import { Inventory as InventoryIcon } from '@mui/icons-material';
import { getAlocacoesPorEvento } from '../api/ativos';
import { AtivoAlocacaoResponse } from '../api/types';

interface AtivosAlocadosSectionProps {
  eventoId: string;
}

const STATUS_LABELS: Record<string, string> = {
  PENDENTE_APROVACAO: 'Pendente de Aprovação',
  CONFIRMADA: 'Confirmada',
  ATRASADA: 'Atrasada',
  DEVOLVIDA: 'Devolvida',
  CANCELADA: 'Cancelada',
};

const STATUS_COLORS: Record<string, 'success' | 'warning' | 'default' | 'info' | 'error'> = {
  PENDENTE_APROVACAO: 'warning',
  CONFIRMADA: 'info',
  ATRASADA: 'error',
  DEVOLVIDA: 'default',
  CANCELADA: 'default',
};

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
        console.error('Erro ao buscar ativos alocados ao evento:', err);
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
                {alocacao.dataInicio} — {alocacao.dataFim}
              </Typography>
              <Chip
                label={STATUS_LABELS[alocacao.status] || alocacao.status}
                color={STATUS_COLORS[alocacao.status] || 'default'}
                size="small"
              />
              {alocacao.impactadaPorAtraso && (
                <Chip
                  label="Pode atrasar"
                  color="warning"
                  size="small"
                  variant="outlined"
                />
              )}
            </Box>
          ))}
        </Box>
      </Box>
    </Box>
  );
};

export default AtivosAlocadosSection;
