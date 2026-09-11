import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { Box, Paper, Skeleton, Stack, Typography } from '@mui/material';
import { Inventory as InventoryIcon } from '@mui/icons-material';
import { listarReservas } from '../api/reservas';
import { getReservaStatusMeta } from '../utils/statusMeta/reservaStatus';
import StatusIcon from './StatusIcon';

interface AtivosAlocadosSectionProps {
  eventoId: string;
}

/**
 * Lista os ativos patrimoniais reservados para um evento (via módulo de Reservas) — fonte real
 * da alocação, já que `Evento.ativoIds` é write-only na API (só existe no payload de criação/
 * edição, nunca é devolvido pelo backend). Substitui a antiga listagem baseada nesse campo.
 */
const AtivosAlocadosSection: React.FC<AtivosAlocadosSectionProps> = ({ eventoId }) => {
  const { data: reservas = [], isLoading } = useQuery({
    queryKey: ['reservas', 'evento-ativos', eventoId],
    queryFn: () => listarReservas({ eventoId, tipoItem: 'ATIVO' }),
    enabled: Boolean(eventoId),
  });

  return (
    <Paper variant="outlined" sx={{ p: 1.5, borderRadius: 1.5 }}>
      <Stack direction="row" spacing={0.75} sx={{ alignItems: 'center', mb: 1 }}>
        <InventoryIcon fontSize="small" sx={{ color: 'primary.main' }} />
        <Typography variant="caption" color="text.secondary" sx={{ fontWeight: 600 }}>
          Bens / ativos patrimoniais
        </Typography>
      </Stack>
      {isLoading ? (
        <Skeleton variant="rounded" height={32} />
      ) : reservas.length === 0 ? (
        <Typography variant="body2" color="text.secondary">Nenhum bem/ativo alocado.</Typography>
      ) : (
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.75 }}>
          {reservas.map((reserva) => (
            <StatusIcon
              key={reserva.id}
              {...getReservaStatusMeta(reserva.status)}
              label={`${reserva.itemNome || reserva.itemId} · ${getReservaStatusMeta(reserva.status).label}`}
            />
          ))}
        </Box>
      )}
    </Paper>
  );
};

export default AtivosAlocadosSection;
