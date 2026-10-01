import React from 'react';
import { Card, CardContent, Divider, Stack, Typography } from '@mui/material';
import GroupsIcon from '@mui/icons-material/Groups';
import LinkIcon from '@mui/icons-material/Link';
import LinkOffIcon from '@mui/icons-material/LinkOff';
import VerifiedIcon from '@mui/icons-material/Verified';
import CancelIcon from '@mui/icons-material/Cancel';
import ActionButton from '@/shared/presentation/components/ActionButton';
import { PAPEL_LABEL, Participante } from '@/contextos/sacramento/domain/sacramento';

interface AssentoParticipantesCardProps {
  participantes: Participante[];
  podeVincular: boolean;
  /** Pároco: aprova/rejeita vínculos propostos e desvincula. */
  podeAprovar: boolean;
  onVincular: (p: Participante) => void;
  onDesvincular: (p: Participante) => void;
  onAprovar: (p: Participante) => void;
  onRejeitar: (p: Participante) => void;
}

/**
 * Participantes com o texto lavrado (imutável) e o estado do vínculo tardio a fiel/clérigo. A Secretaria
 * propõe vínculos (pendentes); o Pároco aprova, rejeita ou desvincula.
 */
export const AssentoParticipantesCard: React.FC<AssentoParticipantesCardProps> = ({
  participantes, podeVincular, podeAprovar, onVincular, onDesvincular, onAprovar, onRejeitar,
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
                  {PAPEL_LABEL[p.papel]} · {!vinculado ? 'Sem cadastro vinculado' : p.vinculoPendente ? 'Vínculo pendente de aprovação do Pároco' : 'Vinculado ao cadastro'}
                </Typography>
              </Stack>
              <Stack direction="row" spacing={1}>
                {podeAprovar && p.vinculoPendente && (
                  <>
                    <ActionButton title={`Aprovar vínculo de ${p.nomeHistorico}`} label="Aprovar" icon={<VerifiedIcon />} color="primary" onClick={() => onAprovar(p)} />
                    <ActionButton title={`Rejeitar vínculo de ${p.nomeHistorico}`} label="Rejeitar" icon={<CancelIcon />} color="error" variant="outlined" onClick={() => onRejeitar(p)} />
                  </>
                )}
                {podeAprovar && vinculado && !p.vinculoPendente && (
                  <ActionButton title={`Desvincular ${p.nomeHistorico}`} label="Desvincular" icon={<LinkOffIcon />} color="secondary" variant="outlined" onClick={() => onDesvincular(p)} />
                )}
                {podeVincular && !vinculado && (
                  <ActionButton title={`Vincular ${p.nomeHistorico}`} label="Vincular" icon={<LinkIcon />} color="primary" variant="outlined" onClick={() => onVincular(p)} />
                )}
              </Stack>
            </Stack>
          );
        })}
      </Stack>
    </CardContent>
  </Card>
);

export default AssentoParticipantesCard;
