import React from 'react';
import { Card, CardContent, Divider, Stack, Typography } from '@mui/material';
import GroupsIcon from '@mui/icons-material/Groups';
import LinkIcon from '@mui/icons-material/Link';
import LinkOffIcon from '@mui/icons-material/LinkOff';
import ActionButton from '@/shared/presentation/components/ActionButton';
import { PAPEL_LABEL, Participante } from '@/contextos/sacramento/domain/sacramento';

interface AssentoParticipantesCardProps {
  participantes: Participante[];
  podeVincular: boolean;
  /** Pároco: único que desvincula. A aprovação de vínculos da Secretaria acontece na tela de Aprovações. */
  podeDesvincular: boolean;
  /** Abre o diálogo de vínculo; sem argumento, o operador escolhe o tipo de participação. */
  onVincular: (p?: Participante) => void;
  onDesvincular: (p: Participante) => void;
}

/**
 * Participantes com o texto lavrado (imutável) e o estado do vínculo tardio a fiel/clérigo. É deste registro
 * que se relacionam fiéis e clérigos ao sacramento; vínculos propostos pela Secretaria ficam pendentes até o
 * Pároco decidir na tela de Aprovações.
 */
export const AssentoParticipantesCard: React.FC<AssentoParticipantesCardProps> = ({
  participantes, podeVincular, podeDesvincular, onVincular, onDesvincular,
}) => (
  <Card variant="outlined" sx={{ borderRadius: 2 }}>
    <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
      <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2, justifyContent: 'space-between' }}>
        <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
          <GroupsIcon color="primary" fontSize="small" />
          <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>Participantes</Typography>
        </Stack>
        {podeVincular && participantes.some((p) => !p.fielId && !p.clerigoId) && (
          <ActionButton title="Vincular fiel ou clérigo" icon={<LinkIcon />} color="primary" variant="outlined" onClick={() => onVincular()} />
        )}
      </Stack>
      <Stack spacing={1.5} divider={<Divider flexItem />}>
        {participantes.map((p) => {
          const vinculado = Boolean(p.fielId ?? p.clerigoId);
          return (
            <Stack key={p.id} direction="row" spacing={2} sx={{ alignItems: 'center', justifyContent: 'space-between' }}>
              <Stack>
                <Typography variant="body2" sx={{ fontWeight: 600 }}>{p.nomeHistorico}</Typography>
                <Typography variant="caption" color="text.secondary">
                  {PAPEL_LABEL[p.papel]} · {!vinculado ? 'Sem cadastro vinculado' : p.vinculoPendente ? 'Vínculo pendente de aprovação do Pároco' : 'Vinculado ao cadastro'}
                </Typography>
              </Stack>
              {podeDesvincular && vinculado && !p.vinculoPendente && (
                <ActionButton title={`Desvincular ${p.nomeHistorico}`} label="Desvincular" icon={<LinkOffIcon />} color="secondary" variant="outlined" onClick={() => onDesvincular(p)} />
              )}
              {podeVincular && !vinculado && (
                <ActionButton title={`Vincular ${p.nomeHistorico}`} label="Vincular" icon={<LinkIcon />} color="primary" variant="outlined" onClick={() => onVincular(p)} />
              )}
            </Stack>
          );
        })}
      </Stack>
    </CardContent>
  </Card>
);

export default AssentoParticipantesCard;
