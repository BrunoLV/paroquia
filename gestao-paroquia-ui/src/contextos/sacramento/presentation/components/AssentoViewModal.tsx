import React from 'react';
import {
  Card, CardContent, Dialog, DialogActions, DialogContent, DialogTitle, Grid, Skeleton, Stack, Typography,
} from '@mui/material';
import BadgeIcon from '@mui/icons-material/Badge';
import BlockIcon from '@mui/icons-material/Block';
import CloseIcon from '@mui/icons-material/Close';
import DescriptionIcon from '@mui/icons-material/Description';
import EditIcon from '@mui/icons-material/Edit';
import EventIcon from '@mui/icons-material/Event';
import GavelIcon from '@mui/icons-material/Gavel';
import HistoryIcon from '@mui/icons-material/History';
import LocationOnIcon from '@mui/icons-material/LocationOn';
import MenuBookIcon from '@mui/icons-material/MenuBook';
import VerifiedIcon from '@mui/icons-material/Verified';
import ActionButton from '@/shared/presentation/components/ActionButton';
import StatusIcon from '@/shared/presentation/components/StatusIcon';
import { DIALOG_CONTENT_SCROLL_SX } from '@/shared/presentation/theme/dialogStyles';
import { InfoField } from '@/contextos/fiel/presentation/components/fieis/fielConstants';
import {
  Assento, Averbacao, Participante, TIPO_SACRAMENTO_LABEL,
} from '@/contextos/sacramento/domain/sacramento';
import { useAssento, useHistorico } from '@/contextos/sacramento/presentation/hooks/useAssentos';
import {
  getStatusAssentoMeta, getTipoSacramentoMeta, getVisibilidadeMeta,
} from '@/contextos/sacramento/presentation/assentoMeta';
import AssentoParticipantesCard from '@/contextos/sacramento/presentation/components/AssentoParticipantesCard';
import AssentoAverbacoesCard from '@/contextos/sacramento/presentation/components/AssentoAverbacoesCard';

export interface AssentoViewAcoes {
  podeGerir: boolean;
  podeHomologar: boolean;
  onEditar: (a: Assento) => void;
  onHomologar: (a: Assento) => void;
  onRetificar: (a: Assento) => void;
  onCancelar: (a: Assento) => void;
  onCertidao: (a: Assento) => void;
  onVincular: (a: Assento, p: Participante) => void;
  onDesvincular: (a: Assento, p: Participante) => void;
  onAprovarVinculo: (a: Assento, p: Participante) => void;
  onRejeitarVinculo: (a: Assento, p: Participante) => void;
  onAverbar: (a: Assento) => void;
  onHomologarAverbacao: (a: Assento, av: Averbacao) => void;
}

interface AssentoViewModalProps extends AssentoViewAcoes {
  assentoId: string | null;
  onClose: () => void;
}

const formatar = (iso: string | null): string => (iso ? new Date(iso).toLocaleString('pt-BR') : '—');

/**
 * Ficha completa do assento. As ações respeitam a matriz de alçadas: a secretaria prepara (rascunho,
 * vínculo, averbação, certidão); homologar, retificar, cancelar e consultar a trilha são do Pároco.
 */
export const AssentoViewModal: React.FC<AssentoViewModalProps> = ({ assentoId, onClose, ...acoes }) => {
  const { data: assento, isLoading } = useAssento(assentoId);
  const { data: historico = [] } = useHistorico(assentoId, acoes.podeHomologar);
  const rascunho = assento?.status === 'DRAFT';
  const selado = assento?.status === 'SEALED';

  return (
    <Dialog open={assentoId !== null} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>{assento ? `Assento de ${TIPO_SACRAMENTO_LABEL[assento.tipo]}` : 'Assento sacramental'}</DialogTitle>
      <DialogContent dividers sx={DIALOG_CONTENT_SCROLL_SX}>
        {isLoading || !assento ? (
          <Skeleton variant="rectangular" height={240} />
        ) : (
          <Stack spacing={2.5}>
            <Card variant="outlined" sx={{ borderRadius: 2 }}>
              <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
                <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
                  <MenuBookIcon color="primary" fontSize="small" />
                  <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>Termo</Typography>
                </Stack>
                <Grid container spacing={2}>
                  <Grid size={{ xs: 12, sm: 4 }}><InfoField label="Sacramento" icon={<BadgeIcon fontSize="inherit" />} value={<StatusIcon {...getTipoSacramentoMeta(assento.tipo)} />} /></Grid>
                  <Grid size={{ xs: 12, sm: 4 }}><InfoField label="Situação" icon={<VerifiedIcon fontSize="inherit" />} value={<StatusIcon {...getStatusAssentoMeta(assento.status)} />} /></Grid>
                  <Grid size={{ xs: 12, sm: 4 }}><InfoField label="Livro" icon={<MenuBookIcon fontSize="inherit" />} value={<StatusIcon {...getVisibilidadeMeta(assento.visibilidade)} />} /></Grid>
                  <Grid size={{ xs: 12, sm: 4 }}><InfoField label="Livro / Folha / Termo" icon={<BadgeIcon fontSize="inherit" />} value={[assento.livro, assento.folha, assento.termo].every(Boolean) ? `${assento.livro} / ${assento.folha} / ${assento.termo}` : 'Pendente'} /></Grid>
                  <Grid size={{ xs: 12, sm: 4 }}><InfoField label="Celebração" icon={<EventIcon fontSize="inherit" />} value={formatar(assento.dataCelebracao)} /></Grid>
                  <Grid size={{ xs: 12, sm: 4 }}><InfoField label="Local" icon={<LocationOnIcon fontSize="inherit" />} value={assento.localCelebracao ?? '—'} /></Grid>
                  <Grid size={{ xs: 12, sm: 6 }}><InfoField label="Paróquia custodiante" icon={<LocationOnIcon fontSize="inherit" />} value={assento.paroquiaCustodiante ?? '—'} /></Grid>
                  <Grid size={{ xs: 12, sm: 6 }}><InfoField label="Homologado por" icon={<GavelIcon fontSize="inherit" />} value={assento.homologadoPor ? `${assento.homologadoPor} em ${formatar(assento.homologadoEmUtc)}` : '—'} /></Grid>
                  {assento.motivoCancelamento && (
                    <Grid size={{ xs: 12 }}><InfoField label="Motivo do cancelamento" icon={<BlockIcon fontSize="inherit" />} value={assento.motivoCancelamento} /></Grid>
                  )}
                </Grid>
              </CardContent>
            </Card>

            <AssentoParticipantesCard
              participantes={assento.participantes}
              podeVincular={acoes.podeGerir && assento.status !== 'CANCELADO'}
              onVincular={(p) => acoes.onVincular(assento, p)}
              podeAprovar={acoes.podeHomologar && assento.status !== 'CANCELADO'}
              onDesvincular={(p) => acoes.onDesvincular(assento, p)}
              onAprovar={(p) => acoes.onAprovarVinculo(assento, p)}
              onRejeitar={(p) => acoes.onRejeitarVinculo(assento, p)}
            />

            {assento.tipo === 'BATISMO' && (
              <AssentoAverbacoesCard
                assento={assento}
                podeAverbar={acoes.podeGerir}
                podeHomologar={acoes.podeHomologar}
                onNova={() => acoes.onAverbar(assento)}
                onHomologar={(av) => acoes.onHomologarAverbacao(assento, av)}
              />
            )}

            {assento.ressalvas.length > 0 && (
              <Card variant="outlined" sx={{ borderRadius: 2 }}>
                <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
                  <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
                    <GavelIcon color="primary" fontSize="small" />
                    <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>Ressalvas</Typography>
                  </Stack>
                  <Stack spacing={1}>
                    {assento.ressalvas.map((r) => (
                      <Typography key={r.id} variant="body2">
                        {r.campo}: “{r.valorOriginal ?? '—'}” → “{r.valorCorrigido}” — {r.justificativa}
                      </Typography>
                    ))}
                  </Stack>
                </CardContent>
              </Card>
            )}

            {acoes.podeHomologar && historico.length > 0 && (
              <Card variant="outlined" sx={{ borderRadius: 2 }}>
                <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
                  <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
                    <HistoryIcon color="primary" fontSize="small" />
                    <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>Trilha de auditoria</Typography>
                  </Stack>
                  <Stack spacing={0.5}>
                    {historico.map((h) => (
                      <Typography key={h.id} variant="caption" color="text.secondary">
                        {formatar(h.ocorridoEmUtc)} · {h.acao} · {h.ator} ({h.perfil ?? 'sem perfil'}) · {h.enderecoIp ?? 'IP n/d'}
                      </Typography>
                    ))}
                  </Stack>
                </CardContent>
              </Card>
            )}
          </Stack>
        )}
      </DialogContent>
      <DialogActions sx={{ px: 3, py: 2 }}>
        <ActionButton title="Fechar" icon={<CloseIcon />} color="secondary" onClick={onClose} />
        {assento && selado && acoes.podeGerir && (
          <ActionButton title="Emitir certidão" icon={<DescriptionIcon />} color="primary" variant="outlined" onClick={() => acoes.onCertidao(assento)} />
        )}
        {assento && rascunho && acoes.podeGerir && (
          <ActionButton title="Editar rascunho" icon={<EditIcon />} color="primary" variant="outlined" onClick={() => acoes.onEditar(assento)} />
        )}
        {assento && rascunho && acoes.podeHomologar && (
          <ActionButton title="Homologar" icon={<VerifiedIcon />} color="primary" onClick={() => acoes.onHomologar(assento)} />
        )}
        {assento && selado && acoes.podeHomologar && (
          <ActionButton title="Retificar" icon={<GavelIcon />} color="primary" variant="outlined" onClick={() => acoes.onRetificar(assento)} />
        )}
        {assento && assento.status !== 'CANCELADO' && acoes.podeHomologar && (
          <ActionButton title="Cancelar assento" icon={<BlockIcon />} color="error" variant="outlined" onClick={() => acoes.onCancelar(assento)} />
        )}
      </DialogActions>
    </Dialog>
  );
};

export default AssentoViewModal;
