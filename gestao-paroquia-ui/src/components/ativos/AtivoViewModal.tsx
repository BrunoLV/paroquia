import React from 'react';
import { useQuery } from '@tanstack/react-query';
import {
  Avatar,
  Box,
  Card,
  CardContent,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Grid,
  Paper,
  Stack,
  Typography,
} from '@mui/material';
import AttachMoneyIcon from '@mui/icons-material/AttachMoney';
import BookmarkIcon from '@mui/icons-material/Bookmark';
import BuildIcon from '@mui/icons-material/Build';
import ChurchIcon from '@mui/icons-material/Church';
import CloseIcon from '@mui/icons-material/Close';
import EditIcon from '@mui/icons-material/Edit';
import EventIcon from '@mui/icons-material/Event';
import InventoryIcon from '@mui/icons-material/Inventory';
import ActionButton from '@/components/ActionButton';
import StatusIcon from '@/components/StatusIcon';
import { InfoField } from '@/components/fieis/fielConstants';
import { getAtivoStatusMeta } from '@/utils/statusMeta/ativoStatus';
import { getCategoriaAtivoMeta } from '@/utils/statusMeta/categoriaAtivoStatus';
import { formatDateBr } from '@/utils/masks';
import { formatCurrencyBRL } from '@/utils/currencyUtils';
import { getHistoricoManutencao } from '@/api/ativos';
import { AtivoPatrimonial, Organizacao } from '@/api/types';

interface AtivoViewModalProps {
  open: boolean;
  ativo: AtivoPatrimonial | null;
  organizacoes: Organizacao[];
  canManage: boolean;
  onClose: () => void;
  onEdit: (ativo: AtivoPatrimonial) => void;
}

/**
 * Modal de visualização detalhada de um Ativo Patrimonial, com linha do tempo de manutenções.
 * Segue o padrão visual de Cards e destaques de métricas estabelecido em Fiéis / Locais.
 */
export const AtivoViewModal: React.FC<AtivoViewModalProps> = ({
  open,
  ativo,
  organizacoes,
  canManage,
  onClose,
  onEdit,
}) => {
  const { data: historico = [] } = useQuery({
    queryKey: ['ativo-manutencoes', ativo?.id],
    queryFn: () => getHistoricoManutencao(ativo!.id),
    enabled: open && Boolean(ativo),
  });

  if (!ativo) return null;

  const statusMeta = getAtivoStatusMeta(ativo.status);
  const categoriaMeta = getCategoriaAtivoMeta(ativo.categoria);
  const organizacaoNome = organizacoes.find((o) => o.id === ativo.grupoParoquialId)?.nome || '—';

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>
        <Stack direction="row" sx={{ alignItems: 'center', justifyContent: 'space-between' }}>
          <Stack direction="row" spacing={1.5} sx={{ alignItems: 'center' }}>
            <InventoryIcon color="primary" sx={{ fontSize: 28 }} />
            <Typography variant="h6" component="div">
              {ativo.nome}
            </Typography>
          </Stack>
          <StatusIcon {...statusMeta} />
        </Stack>
      </DialogTitle>

      <DialogContent dividers>
        <Grid container spacing={2.5}>
          <Grid size={{ xs: 12 }}>
            <Paper
              variant="outlined"
              sx={{
                p: 2.5,
                borderRadius: 2,
                bgcolor: 'background.default',
                display: 'flex',
                flexDirection: { xs: 'column', sm: 'row' },
                alignItems: { xs: 'flex-start', sm: 'center' },
                gap: 2,
              }}
            >
              <Avatar
                sx={{
                  bgcolor: 'primary.main',
                  color: 'primary.contrastText',
                  width: 56,
                  height: 56,
                  fontSize: '1.5rem',
                  fontWeight: 700,
                }}
              >
                {(ativo.nome ?? 'A').charAt(0).toUpperCase()}
              </Avatar>
              <Box sx={{ flexGrow: 1 }}>
                <Typography variant="h6" sx={{ fontWeight: 700, color: 'primary.main', lineHeight: 1.2 }}>
                  {ativo.nome}
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
                  Tombamento: {ativo.codigoTombamento}
                </Typography>
                <Stack direction="row" spacing={1} sx={{ mt: 1, flexWrap: 'wrap', gap: 0.5 }}>
                  <StatusIcon {...categoriaMeta} />
                  {ativo.isReservavel && (
                    <StatusIcon label="Reservável" color="primary" icon={<BookmarkIcon />} tooltip="Este bem pode ser reservado para eventos" />
                  )}
                </Stack>
              </Box>
            </Paper>
          </Grid>

          <Grid size={{ xs: 12, sm: 6 }}>
            <Card variant="outlined" sx={{ height: '100%', backgroundColor: 'background.default' }}>
              <CardContent>
                <Stack direction="row" spacing={2} sx={{ alignItems: 'center' }}>
                  <AttachMoneyIcon color="primary" sx={{ fontSize: 40 }} />
                  <Stack spacing={0.5}>
                    <Typography variant="caption" color="text.secondary">
                      VALOR DE AQUISIÇÃO
                    </Typography>
                    <Typography variant="h5" sx={{ fontWeight: 700 }}>
                      {formatCurrencyBRL(ativo.valorAquisicao)}
                    </Typography>
                  </Stack>
                </Stack>
              </CardContent>
            </Card>
          </Grid>

          <Grid size={{ xs: 12, sm: 6 }}>
            <Card variant="outlined" sx={{ height: '100%', backgroundColor: 'background.default' }}>
              <CardContent>
                <Stack direction="row" spacing={2} sx={{ alignItems: 'center' }}>
                  <ChurchIcon color="primary" sx={{ fontSize: 40 }} />
                  <Stack spacing={0.5}>
                    <Typography variant="caption" color="text.secondary">
                      ORGANIZAÇÃO DONA
                    </Typography>
                    <Typography variant="h6" sx={{ fontWeight: 600 }}>
                      {organizacaoNome}
                    </Typography>
                  </Stack>
                </Stack>
              </CardContent>
            </Card>
          </Grid>

          <Grid size={{ xs: 12 }}>
            <Card variant="outlined" sx={{ borderRadius: 2 }}>
              <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
                <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
                  <InventoryIcon color="primary" fontSize="small" />
                  <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                    Dados do ativo
                  </Typography>
                </Stack>
                <Grid container spacing={2}>
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <InfoField label="Tipo" value={categoriaMeta.label} />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <InfoField label="Nº de Tombamento" value={ativo.codigoTombamento} />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <InfoField label="Reservável" value={ativo.isReservavel ? 'Sim' : 'Não'} />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <InfoField
                      label="Data de Aquisição"
                      icon={<EventIcon fontSize="inherit" />}
                      value={formatDateBr(ativo.dataAquisicao)}
                    />
                  </Grid>
                  <Grid size={{ xs: 12 }}>
                    <InfoField label="Descrição" value={ativo.descricao || '-'} />
                  </Grid>
                </Grid>
              </CardContent>
            </Card>
          </Grid>

          <Grid size={{ xs: 12 }}>
            <Card variant="outlined" sx={{ borderRadius: 2 }}>
              <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
                <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
                  <BuildIcon color="primary" fontSize="small" />
                  <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                    Histórico de manutenções
                  </Typography>
                </Stack>
                {historico.length === 0 ? (
                  <Typography variant="body2" color="text.secondary">
                    Nenhuma manutenção registrada.
                  </Typography>
                ) : (
                  <Stack spacing={0}>
                    {historico.map((m, index) => {
                      const isLast = index === historico.length - 1;
                      return (
                        <Stack key={m.id} direction="row" spacing={1.5}>
                          <Stack sx={{ alignItems: 'center', width: 12 }}>
                            <Box
                              sx={{
                                width: 10,
                                height: 10,
                                borderRadius: '50%',
                                bgcolor: m.dataFimReal ? 'success.main' : 'warning.main',
                                border: '2px solid',
                                borderColor: 'background.paper',
                                mt: 0.5,
                              }}
                            />
                            {!isLast && <Box sx={{ width: 2, flexGrow: 1, bgcolor: 'divider' }} />}
                          </Stack>
                          <Box sx={{ pb: isLast ? 0 : 2, flexGrow: 1 }}>
                            <Typography variant="body2" sx={{ fontWeight: 600 }}>
                              {formatDateBr(m.dataInicio)} —{' '}
                              {m.dataFimReal ? formatDateBr(m.dataFimReal) : `previsto ${formatDateBr(m.dataFimPrevista)}`}
                              {!m.dataFimReal && ' · em aberto'}
                            </Typography>
                            <Typography variant="body2" color="text.secondary" sx={{ whiteSpace: 'pre-wrap' }}>
                              {m.descricaoReparo}
                            </Typography>
                          </Box>
                        </Stack>
                      );
                    })}
                  </Stack>
                )}
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      </DialogContent>

      <DialogActions sx={{ px: 3, py: 2 }}>
        <ActionButton title="Fechar" icon={<CloseIcon />} onClick={onClose} color="secondary" />
        {canManage && (
          <ActionButton
            title="Editar"
            icon={<EditIcon />}
            color="primary"
            onClick={() => {
              onClose();
              onEdit(ativo);
            }}
          />
        )}
      </DialogActions>
    </Dialog>
  );
};

export default AtivoViewModal;
