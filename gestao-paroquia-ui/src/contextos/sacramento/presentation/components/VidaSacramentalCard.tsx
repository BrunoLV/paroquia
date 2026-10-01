import React, { useState } from 'react';
import { Card, CardContent, Divider, Skeleton, Stack, Typography } from '@mui/material';
import AutoStoriesIcon from '@mui/icons-material/AutoStories';
import LinkIcon from '@mui/icons-material/Link';
import LinkOffIcon from '@mui/icons-material/LinkOff';
import { useAuth } from '@/contextos/iam/presentation/auth/AuthContext';
import { canManageSacramentos, canViewSacramentos } from '@/contextos/iam/presentation/auth/permissions';
import ActionButton from '@/shared/presentation/components/ActionButton';
import StatusIcon from '@/shared/presentation/components/StatusIcon';
import { PAPEL_LABEL } from '@/contextos/sacramento/domain/sacramento';
import { useAssentoMutations, useAssentosVinculados } from '@/contextos/sacramento/presentation/hooks/useAssentos';
import { getStatusAssentoMeta, getTipoSacramentoMeta } from '@/contextos/sacramento/presentation/assentoMeta';
import VincularAssentoDialog from '@/contextos/sacramento/presentation/components/VincularAssentoDialog';

interface VidaSacramentalCardProps {
  pessoaId: string;
  tipoPessoa: 'FIEL' | 'CLERIGO';
}

/**
 * Seção "Vida sacramental" das fichas de Fiel e Clérigo. Os cadastros seguem módulos separados: aqui só
 * se exibem os assentos do módulo `sacramento` em que a pessoa está vinculada e se criam/removem vínculos
 * tardios (Cân. 535), sem copiar dado sacramental para o cadastro. Oculta-se a quem não lê o livro.
 */
export const VidaSacramentalCard: React.FC<VidaSacramentalCardProps> = ({ pessoaId, tipoPessoa }) => {
  const { user } = useAuth();
  const podeVer = canViewSacramentos(user);
  const podeGerir = canManageSacramentos(user);
  const [vinculando, setVinculando] = useState(false);
  const { data: assentos = [], isLoading } = useAssentosVinculados(pessoaId, podeVer);
  const m = useAssentoMutations();

  if (!podeVer) return null;

  return (
    <Card variant="outlined" sx={{ borderRadius: 2 }}>
      <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
        <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2, justifyContent: 'space-between' }}>
          <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
            <AutoStoriesIcon color="primary" fontSize="small" />
            <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>Vida sacramental</Typography>
          </Stack>
          {podeGerir && (
            <ActionButton title="Vincular a registro" icon={<LinkIcon />} color="primary" variant="outlined" onClick={() => setVinculando(true)} />
          )}
        </Stack>
        {isLoading && <Skeleton variant="rectangular" height={48} />}
        {!isLoading && assentos.length === 0 && (
          <Typography variant="body2" color="text.secondary">Nenhum registro sacramental vinculado.</Typography>
        )}
        <Stack spacing={1.5} divider={<Divider flexItem />}>
          {assentos.map((a) => (
            <Stack key={a.id} direction="row" spacing={2} sx={{ alignItems: 'center', justifyContent: 'space-between' }}>
              <Stack spacing={0.5}>
                <StatusIcon {...getTipoSacramentoMeta(a.tipo)} />
                <Typography variant="caption" color="text.secondary">
                  {a.papelVinculado ? PAPEL_LABEL[a.papelVinculado] : 'Vinculado'} em «{a.sujeito ?? 'sem identificação'}»
                  {[a.livro, a.folha, a.termo].every(Boolean) ? ` · ${a.livro}/${a.folha}/${a.termo}` : ''}
                </Typography>
                <StatusIcon {...getStatusAssentoMeta(a.status)} />
              </Stack>
              {podeGerir && a.participanteId && (
                <ActionButton
                  title={`Desvincular de ${a.sujeito ?? 'registro'}`}
                  label="Desvincular"
                  icon={<LinkOffIcon />}
                  color="secondary"
                  variant="outlined"
                  disabled={m.desvincular.isPending}
                  onClick={() => m.desvincular.mutate({ id: a.id, participanteId: a.participanteId as string })}
                />
              )}
            </Stack>
          ))}
        </Stack>
      </CardContent>
      <VincularAssentoDialog
        open={vinculando}
        tipoPessoa={tipoPessoa}
        isPending={m.vincular.isPending}
        onClose={() => setVinculando(false)}
        onConfirm={({ assentoId, participanteId }) =>
          m.vincular.mutate(
            { id: assentoId, participanteId, ...(tipoPessoa === 'FIEL' ? { fielId: pessoaId } : { clerigoId: pessoaId }) },
            { onSuccess: () => setVinculando(false) },
          )}
      />
    </Card>
  );
};

export default VidaSacramentalCard;
