import React from 'react';
import { Card, CardContent, Divider, Skeleton, Stack, Typography } from '@mui/material';
import AutoStoriesIcon from '@mui/icons-material/AutoStories';
import { useAuth } from '@/contextos/iam/presentation/auth/AuthContext';
import { canViewSacramentos } from '@/contextos/iam/presentation/auth/permissions';
import StatusIcon from '@/shared/presentation/components/StatusIcon';
import { PAPEL_LABEL } from '@/contextos/sacramento/domain/sacramento';
import { useAssentosVinculados } from '@/contextos/sacramento/presentation/hooks/useAssentos';
import { getStatusAssentoMeta, getTipoSacramentoMeta } from '@/contextos/sacramento/presentation/assentoMeta';

interface VidaSacramentalCardProps {
  pessoaId: string;
}

/**
 * Seção "Vida sacramental" das fichas de Fiel e Clérigo: somente consulta. Os cadastros seguem módulos
 * separados; esta seção apenas exibe os assentos do módulo `sacramento` em que a pessoa está vinculada e o
 * tipo de participação que teve. Vincular, aprovar e desvincular são feitos no registro do sacramento e na
 * tela de Aprovações, nunca a partir da ficha. Oculta-se a quem não lê o livro sacramental.
 */
export const VidaSacramentalCard: React.FC<VidaSacramentalCardProps> = ({ pessoaId }) => {
  const { user } = useAuth();
  const podeVer = canViewSacramentos(user);
  const { data: assentos = [], isLoading } = useAssentosVinculados(pessoaId, podeVer);

  if (!podeVer) return null;

  return (
    <Card variant="outlined" sx={{ borderRadius: 2 }}>
      <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
        <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
          <AutoStoriesIcon color="primary" fontSize="small" />
          <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>Vida sacramental</Typography>
        </Stack>
        {isLoading && <Skeleton variant="rectangular" height={48} />}
        {!isLoading && assentos.length === 0 && (
          <Typography variant="body2" color="text.secondary">Nenhum registro sacramental vinculado.</Typography>
        )}
        <Stack spacing={1.5} divider={<Divider flexItem />}>
          {assentos.map((a) => (
            <Stack key={a.id} spacing={0.5}>
              <StatusIcon {...getTipoSacramentoMeta(a.tipo)} />
              <Typography variant="caption" color="text.secondary">
                {a.papelVinculado ? PAPEL_LABEL[a.papelVinculado] : 'Vinculado'} em «{a.sujeito ?? 'sem identificação'}»
                {[a.livro, a.folha, a.termo].every(Boolean) ? ` · ${a.livro}/${a.folha}/${a.termo}` : ''}
                {a.vinculoPendente ? ' — pendente de aprovação do Pároco' : ''}
              </Typography>
              <StatusIcon {...getStatusAssentoMeta(a.status)} />
            </Stack>
          ))}
        </Stack>
      </CardContent>
    </Card>
  );
};

export default VidaSacramentalCard;
