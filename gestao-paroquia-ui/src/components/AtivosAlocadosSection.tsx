import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { Box, Skeleton, Typography } from '@mui/material';
import { Inventory as InventoryIcon } from '@mui/icons-material';
import { listarReservas } from '../api/reservas';
import StatusIcon from './StatusIcon';
import { getReservaStatusMeta } from '../utils/statusMeta/reservaStatus';
import { formatDateBr } from '../utils/masks';

interface AtivosAlocadosSectionProps {
  eventoId: string;
}

/**
 * Lista os ativos patrimoniais reservados para um evento (via módulo de Reservas). Substitui a
 * antiga listagem de alocações de ativo, descontinuada.
 */
const AtivosAlocadosSection: React.FC<AtivosAlocadosSectionProps> = ({ eventoId }) => {
  const { data: reservas = [], isLoading } = useQuery({
    queryKey: ['reservas', 'evento-ativos', eventoId],
    queryFn: () => listarReservas({ eventoId, tipoItem: 'ATIVO' }),
    enabled: Boolean(eventoId),
  });

  if (isLoading) {
    return <Skeleton variant="rounded" height={60} />;
  }
  if (reservas.length === 0) {
    return null;
  }

  return (
    <Box sx={{ display: 'flex', gap: 2, alignItems: 'flex-start' }}>
      <InventoryIcon color="action" sx={{ mt: 0.3 }} />
      <Box sx={{ width: '100%' }}>
        <Typography variant="body2" color="text.secondary" sx={{ fontWeight: 500, mb: 0.5 }}>
          Ativos Reservados
        </Typography>
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
          {reservas.map((reserva) => (
            <Box key={reserva.id} sx={{ display: 'flex', alignItems: 'center', gap: 1, flexWrap: 'wrap' }}>
              <Typography variant="body2" sx={{ fontWeight: 600 }}>
                {reserva.itemNome || reserva.itemId}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                {formatDateBr(reserva.dataHoraInicio)} — {formatDateBr(reserva.dataHoraFim)}
              </Typography>
              <StatusIcon {...getReservaStatusMeta(reserva.status)} />
            </Box>
          ))}
        </Box>
      </Box>
    </Box>
  );
};

export default AtivosAlocadosSection;
