import React from 'react';
import { Card, CardContent, Divider, Stack, Typography } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditNoteIcon from '@mui/icons-material/EditNote';
import VerifiedIcon from '@mui/icons-material/Verified';
import ActionButton from '@/shared/presentation/components/ActionButton';
import StatusIcon from '@/shared/presentation/components/StatusIcon';
import { Assento, Averbacao, TIPO_EVENTO_AVERBACAO_LABEL } from '@/contextos/sacramento/domain/sacramento';
import { getStatusAverbacaoMeta } from '@/contextos/sacramento/presentation/assentoMeta';

interface AssentoAverbacoesCardProps {
  assento: Assento;
  podeAverbar: boolean;
  podeHomologar: boolean;
  onNova: () => void;
  onHomologar: (a: Averbacao) => void;
}

/**
 * Margem do assento de Batismo (append-only): sem editar nem remover. A secretaria digita; só o
 * Pároco homologa. Averbação exige batismo homologado.
 */
export const AssentoAverbacoesCard: React.FC<AssentoAverbacoesCardProps> = ({
  assento, podeAverbar, podeHomologar, onNova, onHomologar,
}) => (
  <Card variant="outlined" sx={{ borderRadius: 2 }}>
    <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
      <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2, justifyContent: 'space-between' }}>
        <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
          <EditNoteIcon color="primary" fontSize="small" />
          <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>Averbações à margem</Typography>
        </Stack>
        {podeAverbar && assento.status === 'SEALED' && (
          <ActionButton title="Nova averbação" icon={<AddIcon />} color="primary" variant="outlined" onClick={onNova} />
        )}
      </Stack>
      {assento.averbacoes.length === 0 && (
        <Typography variant="body2" color="text.secondary">Nenhuma averbação registrada.</Typography>
      )}
      <Stack spacing={1.5} divider={<Divider flexItem />}>
        {assento.averbacoes.map((a) => (
          <Stack key={a.id} direction="row" spacing={2} sx={{ alignItems: 'center', justifyContent: 'space-between' }}>
            <Stack>
              <Typography variant="body2" sx={{ fontWeight: 600 }}>
                {TIPO_EVENTO_AVERBACAO_LABEL[a.tipoEvento]} · {a.dataAverbacao}
              </Typography>
              <Typography variant="body2">{a.teorNota}</Typography>
              <StatusIcon {...getStatusAverbacaoMeta(a.status)} />
            </Stack>
            {podeHomologar && a.status === 'PENDENTE_HOMOLOGACAO' && (
              <ActionButton title="Homologar averbação" icon={<VerifiedIcon />} color="primary" onClick={() => onHomologar(a)} />
            )}
          </Stack>
        ))}
      </Stack>
    </CardContent>
  </Card>
);

export default AssentoAverbacoesCard;
