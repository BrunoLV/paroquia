import React from 'react';
import {
  Card,
  CardContent,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Grid,
  Stack,
  Typography,
} from '@mui/material';
import ChurchIcon from '@mui/icons-material/Church';
import CloseIcon from '@mui/icons-material/Close';
import EditIcon from '@mui/icons-material/Edit';
import GroupsIcon from '@mui/icons-material/Groups';
import LocationOnIcon from '@mui/icons-material/LocationOn';
import MeetingRoomIcon from '@mui/icons-material/MeetingRoom';
import ActionButton from '@/components/ActionButton';
import StatusIcon from '@/components/StatusIcon';
import { getSituacaoAtivoMeta } from '@/utils/statusMeta/situacaoStatus';
import { Local, Organizacao } from '@/api/types';

interface LocalViewModalProps {
  open: boolean;
  local: Local | null;
  igrejas?: Organizacao[];
  canManage: boolean;
  onClose: () => void;
  onEdit: (local: Local) => void;
}

/**
 * Modal de visualização detalhada de um Local físico da paróquia.
 * Segue o padrão visual de Cards e destaques de métricas estabelecido em Fiéis.
 */
export const LocalViewModal: React.FC<LocalViewModalProps> = ({
  open,
  local,
  igrejas,
  canManage,
  onClose,
  onEdit,
}) => {
  if (!local) return null;

  const capacidade = local.capacidadeMaxima ?? 0;
  const igrejaEncontrada = igrejas?.find((i) => i.id === local.igrejaId);
  const igrejaNome = local.igrejaNome || igrejaEncontrada?.nome || 'Paróquia Geral';
  const descricao = local.descricao || 'Nenhuma observação ou especificação registrada.';

  const enderecoFormatado = local.enderecoFormatado || (local.logradouro
    ? `${local.logradouro}${local.numero ? `, ${local.numero}` : ''}${
        local.complemento ? ` (${local.complemento})` : ''
      }${local.bairro ? ` - ${local.bairro}` : ''}${
        local.cidade ? `, ${local.cidade}` : ''
      }${local.uf ? `/${local.uf}` : ''}${local.cep ? ` - CEP: ${local.cep}` : ''}`
    : 'Endereço não informado');

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>
        <Stack direction="row" sx={{ alignItems: 'center', justifyContent: 'space-between' }}>
          <Stack direction="row" spacing={1.5} sx={{ alignItems: 'center' }}>
            <MeetingRoomIcon color="primary" sx={{ fontSize: 28 }} />
            <Typography variant="h6" component="div">
              {local.nome}
            </Typography>
          </Stack>
          <StatusIcon {...getSituacaoAtivoMeta(local.ativo)} />
        </Stack>
      </DialogTitle>

      <DialogContent dividers>
        <Grid container spacing={2.5}>
          {/* Destaques principais */}
          <Grid size={{ xs: 12, sm: 6 }}>
            <Card variant="outlined" sx={{ height: '100%', backgroundColor: 'background.default' }}>
              <CardContent>
                <Stack direction="row" spacing={2} sx={{ alignItems: 'center' }}>
                  <GroupsIcon color="primary" sx={{ fontSize: 40 }} />
                  <Stack spacing={0.5}>
                    <Typography variant="caption" color="text.secondary">
                      LOTAÇÃO MÁXIMA
                    </Typography>
                    <Typography variant="h5" sx={{ fontWeight: 700 }}>
                      {capacidade > 0 ? `${capacidade} pessoas` : 'Não informada'}
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
                      IGREJA VINCULADA
                    </Typography>
                    <Typography variant="h6" sx={{ fontWeight: 600 }}>
                      {igrejaNome}
                    </Typography>
                  </Stack>
                </Stack>
              </CardContent>
            </Card>
          </Grid>

          {/* Endereço Estruturado */}
          <Grid size={{ xs: 12 }}>
            <Card variant="outlined">
              <CardContent>
                <Stack spacing={1.5}>
                  <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
                    <LocationOnIcon color="action" fontSize="small" />
                    <Typography variant="subtitle2" color="text.secondary" sx={{ textTransform: 'uppercase' }}>
                      Endereço Completo
                    </Typography>
                  </Stack>
                  <Typography variant="body1" sx={{ fontWeight: 500 }}>
                    {enderecoFormatado}
                  </Typography>
                  {Boolean(local.cep) && (
                    <Typography variant="body2" color="text.secondary">
                      CEP: {local.cep}
                    </Typography>
                  )}
                </Stack>
              </CardContent>
            </Card>
          </Grid>

          {/* Descrição e Equipamentos */}
          <Grid size={{ xs: 12 }}>
            <Card variant="outlined">
              <CardContent>
                <Stack spacing={1}>
                  <Typography variant="subtitle2" color="text.secondary" sx={{ textTransform: 'uppercase' }}>
                    Finalidades de Uso, Equipamentos e Restrições
                  </Typography>
                  <Typography variant="body2" sx={{ whiteSpace: 'pre-wrap', color: 'text.primary' }}>
                    {descricao}
                  </Typography>
                </Stack>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      </DialogContent>

      <DialogActions sx={{ px: 3, py: 2 }}>
        <ActionButton
          title="Fechar"
          icon={<CloseIcon />}
          onClick={onClose}
          color="secondary"
        />
        {canManage && (
          <ActionButton
            title="Editar Registro"
            icon={<EditIcon />}
            onClick={() => {
              onClose();
              onEdit(local);
            }}
            color="primary"
          />
        )}
      </DialogActions>
    </Dialog>
  );
};
