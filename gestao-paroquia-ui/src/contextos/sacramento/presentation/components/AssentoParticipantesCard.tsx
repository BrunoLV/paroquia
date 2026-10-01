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
  onVincular: (p: Participante) => void;
  onDesvincular: (p: Participante) => void;
}

/** Participantes com o texto lavrado (imutável) e o estado do vínculo tardio a fiel/clérigo. */
export const AssentoParticipantesCard: React.FC<AssentoParticipantesCardProps> = ({
  participantes, podeVincular, onVincular, onDesvincular,
}) => (
  <Card variant="outlined" sx={{ borderRadius: 2 }}>
    <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
      <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
        <GroupsIcon color="primary" fontSize="small" />
        <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>Participantes</Typography>
      </Stack>
      <Stack spacing={1.5} divider={<Divider flexItem />}>
        {participantes.map((p) => {
          const vinculado = Boolean(p.fielId ?? p.clerigoId);
          return (
            <Stack key={p.id} direction="row" spacing={2} sx={{ alignItems: 'center', justifyContent: 'space-between' }}>
              <Stack>
                <Typography variant="body2" sx={{ fontWeight: 600 }}>{p.nomeHistorico}</Typography>
                <Typography variant="caption" color="text.secondary">
                  {PAPEL_LABEL[p.papel]} · {vinculado ? 'Vinculado ao cadastro' : 'Sem cadastro vinculado'}
                </Typography>
              </Stack>
              {podeVincular && (
                vinculado
                  ? <ActionButton title={`Desvincular ${p.nomeHistorico}`} label="Desvincular" icon={<LinkOffIcon />} color="secondary" variant="outlined" onClick={() => onDesvincular(p)} />
                  : <ActionButton title={`Vincular ${p.nomeHistorico}`} label="Vincular" icon={<LinkIcon />} color="primary" variant="outlined" onClick={() => onVincular(p)} />
              )}
            </Stack>
          );
        })}
      </Stack>
    </CardContent>
  </Card>
);

export default AssentoParticipantesCard;
