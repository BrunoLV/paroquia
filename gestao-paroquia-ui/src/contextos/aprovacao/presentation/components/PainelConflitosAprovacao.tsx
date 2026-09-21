import React from 'react';
import { Alert, Box, Card, Chip, Stack, Typography } from '@mui/material';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import MeetingRoomIcon from '@mui/icons-material/MeetingRoom';
import InventoryIcon from '@mui/icons-material/Inventory';
import ChurchIcon from '@mui/icons-material/Church';
import { ConflitoEventoItem } from '@/shared/domain/types/types';

interface PainelConflitosAprovacaoProps {
  conflitosJson?: string | null;
}

/**
 * Painel analítico de conflitos para a mesa de deliberação de aprovações.
 * Apresenta choques de espaço físico, ativos patrimoniais e sobreposições litúrgicas
 * de forma visual e estruturada para apoio à decisão do Conselho/Pároco.
 */
const PainelConflitosAprovacao: React.FC<PainelConflitosAprovacaoProps> = ({ conflitosJson }) => {
  if (!conflitosJson) {
    return null;
  }

  let conflitos: ConflitoEventoItem[] = [];
  try {
    conflitos = JSON.parse(conflitosJson);
  } catch {
    return null;
  }

  if (!Array.isArray(conflitos) || conflitos.length === 0) {
    return null;
  }

  const renderIconeTipo = (tipo: string) => {
    switch (tipo) {
      case 'LOCAL':
        return <MeetingRoomIcon fontSize="small" />;
      case 'ATIVO':
        return <InventoryIcon fontSize="small" />;
      case 'AGENDA_ECLESIAL':
      default:
        return <ChurchIcon fontSize="small" />;
    }
  };

  const renderLabelTipo = (tipo: string) => {
    switch (tipo) {
      case 'LOCAL':
        return 'Espaço Físico / Sala';
      case 'ATIVO':
        return 'Ativo Patrimonial';
      case 'AGENDA_ECLESIAL':
        return 'Agenda Eclesial / Litúrgica';
      default:
        return tipo;
    }
  };

  const renderChipColor = (tipo: string): 'warning' | 'error' | 'secondary' => {
    switch (tipo) {
      case 'LOCAL':
        return 'error';
      case 'ATIVO':
        return 'warning';
      case 'AGENDA_ECLESIAL':
      default:
        return 'secondary';
    }
  };

  const temConflitoRecurso = conflitos.some((c: any) => {
    const t = (c.tipo || c.tipoConflito || '').toUpperCase();
    return t === 'LOCAL' || t === 'ATIVO';
  });

  return (
    <Box sx={{ mb: 3 }}>
      <Typography variant="subtitle2" color="textSecondary" gutterBottom sx={{ fontWeight: 700 }}>
        CONFLITOS IDENTIFICADOS ({conflitos.length})
      </Typography>

      {temConflitoRecurso ? (
        <Alert severity="error" sx={{ mb: 2, p: 1.75, borderRadius: 2, fontSize: '0.875rem', fontWeight: 500 }}>
          <strong>Aprovação Bloqueada:</strong> Esta solicitação possui conflito de espaço físico ou ativo patrimonial já reservado por evento aprovado. A aprovação está estritamente bloqueada.
        </Alert>
      ) : (
        <Alert severity="warning" sx={{ mb: 2, p: 1.75, borderRadius: 2, fontSize: '0.875rem' }}>
          Esta solicitação apresenta sobreposição de agenda eclesial/litúrgica. Analise os impactos pastorais antes de deliberar.
        </Alert>
      )}

      <Stack spacing={2}>
        {conflitos.map((c, index) => (
          <Card key={index} variant="outlined" sx={{ p: 2, borderRadius: 2, bgcolor: 'background.paper' }}>
            <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 1, flexWrap: 'wrap', gap: 0.75 }}>
              <Chip
                icon={renderIconeTipo(c.tipo)}
                label={renderLabelTipo(c.tipo)}
                size="small"
                color={renderChipColor(c.tipo)}
                sx={{ fontWeight: 600 }}
              />
              {c.prioridadeSolicitada !== undefined && c.prioridadeExistente !== undefined && (
                <Chip
                  label={`Prioridade Solicitada: ${c.prioridadeSolicitada} | Existente: ${c.prioridadeExistente}`}
                  size="small"
                  variant="outlined"
                />
              )}
            </Stack>

            <Typography variant="body2" sx={{ fontWeight: 600, mb: 0.5 }}>
              {c.itemNome || c.eventoConflitanteTitulo || 'Item / Evento em Colisão'}
            </Typography>

            {c.solicitanteNome && (
              <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mb: 0.25 }}>
                <strong>Ocupante / Solicitante Conflitante:</strong> {c.solicitanteNome}
              </Typography>
            )}

            {(c.inicioOcupadoUtc || c.fimOcupadoUtc) && (
              <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mb: 0.25 }}>
                <strong>Janela Ocupada:</strong>{' '}
                {c.inicioOcupadoUtc ? new Date(c.inicioOcupadoUtc).toLocaleString('pt-BR') : ''}{' '}
                {c.fimOcupadoUtc ? `até ${new Date(c.fimOcupadoUtc).toLocaleString('pt-BR')}` : ''}
              </Typography>
            )}

            {c.detalhe && (
              <Typography variant="caption" color="text.secondary" sx={{ mt: 0.75, fontStyle: 'italic', display: 'block' }}>
                {c.detalhe}
              </Typography>
            )}
          </Card>
        ))}
      </Stack>
    </Box>
  );
};

export default PainelConflitosAprovacao;
