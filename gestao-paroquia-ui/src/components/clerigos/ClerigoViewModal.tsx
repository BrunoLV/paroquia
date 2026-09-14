import React from 'react';
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
import AssignmentIndIcon from '@mui/icons-material/AssignmentInd';
import AutoStoriesIcon from '@mui/icons-material/AutoStories';
import BadgeIcon from '@mui/icons-material/Badge';
import BlockIcon from '@mui/icons-material/Block';
import CakeIcon from '@mui/icons-material/Cake';
import CalendarMonthIcon from '@mui/icons-material/CalendarMonth';
import CloseIcon from '@mui/icons-material/Close';
import ContactMailIcon from '@mui/icons-material/ContactMail';
import EditIcon from '@mui/icons-material/Edit';
import { DIALOG_CONTENT_SCROLL_SX } from '@/theme/dialogStyles';
import EmailIcon from '@mui/icons-material/Email';
import HistoryEduIcon from '@mui/icons-material/HistoryEdu';
import LocationCityIcon from '@mui/icons-material/LocationCity';
import LocationOnIcon from '@mui/icons-material/LocationOn';
import PersonIcon from '@mui/icons-material/Person';
import PhoneAndroidIcon from '@mui/icons-material/PhoneAndroid';
import PhoneIcon from '@mui/icons-material/Phone';
import VolunteerActivismIcon from '@mui/icons-material/VolunteerActivism';
import WhatsAppIcon from '@mui/icons-material/WhatsApp';
import WorkspacePremiumIcon from '@mui/icons-material/WorkspacePremium';
import ActionButton from '@/components/ActionButton';
import StatusIcon from '@/components/StatusIcon';
import type { Clerigo, ClerigoInstitucional } from '@/api/types';
import { formatDateBr } from '@/utils/masks';
import { getFuncaoEclesiasticaMeta, getGrauOrdemMeta } from '@/utils/statusMeta/cleroStatus';
import { getSituacaoAtivoMeta } from '@/utils/statusMeta/situacaoStatus';
import { InfoField } from './clerigoConstants';

interface ClerigoViewModalProps {
  clerigo: Clerigo | ClerigoInstitucional | null;
  open: boolean;
  podeGerenciarClero: boolean;
  onClose: () => void;
  onEdit: (clerigo: Clerigo) => void;
  onInativar: (clerigo: Clerigo) => void;
}

/**
 * Modal de visualização detalhada da Ficha Canônica e Civil do Clérigo.
 * Segrega permissões administrativas e institucionais, protegendo dados civis contra IDOR.
 */
export const ClerigoViewModal: React.FC<ClerigoViewModalProps> = ({
  clerigo,
  open,
  podeGerenciarClero,
  onClose,
  onEdit,
  onInativar,
}) => {
  if (!clerigo) return null;

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>
        <Stack direction="row" sx={{ alignItems: 'center', justifyContent: 'space-between' }}>
          <Typography variant="h6" component="div">
            Ficha do clérigo
          </Typography>
          {'isAtivo' in clerigo && (
            <StatusIcon {...getSituacaoAtivoMeta((clerigo as Clerigo).isAtivo)} />
          )}
        </Stack>
      </DialogTitle>
      <DialogContent dividers sx={DIALOG_CONTENT_SCROLL_SX}>
        <Stack spacing={2.5}>
          {/* Header com Avatar e Nome */}
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
              {(clerigo.nomeEclesiastico || 'C').charAt(0).toUpperCase()}
            </Avatar>
            <Box sx={{ flexGrow: 1 }}>
              <Typography
                variant="h6"
                sx={{ fontWeight: 700, color: 'primary.main', lineHeight: 1.2 }}
              >
                {clerigo.nomeEclesiastico}
              </Typography>
              {'nomeCivil' in clerigo && (clerigo as Clerigo).nomeCivil && (
                <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
                  Nome civil: {(clerigo as Clerigo).nomeCivil}
                  {'dioceseIncardinacao' in clerigo &&
                    ` · ${(clerigo as Clerigo).dioceseIncardinacao}`}
                </Typography>
              )}
              <Stack direction="row" spacing={1} sx={{ mt: 1.5, flexWrap: 'wrap', gap: 1 }}>
                <StatusIcon {...getGrauOrdemMeta(clerigo.grauOrdem)} />
                <StatusIcon {...getFuncaoEclesiasticaMeta(clerigo.funcaoAtual)} />
              </Stack>
            </Box>
          </Paper>

          {/* Identificação e Dados Canônicos */}
          <Card variant="outlined" sx={{ borderRadius: 2 }}>
            <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
              <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
                <BadgeIcon color="primary" fontSize="small" />
                <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                  Identificação e dados canônicos
                </Typography>
              </Stack>
              <Grid container spacing={2}>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <InfoField
                    label="Nome eclesiástico"
                    value={clerigo.nomeEclesiastico}
                    icon={<AutoStoriesIcon fontSize="small" />}
                  />
                </Grid>
                {'nomeCivil' in clerigo && (
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <InfoField
                      label="Nome civil"
                      value={(clerigo as Clerigo).nomeCivil}
                      icon={<PersonIcon fontSize="small" />}
                    />
                  </Grid>
                )}
                <Grid size={{ xs: 12, sm: 6 }}>
                  <InfoField
                    label="Grau da ordem"
                    value={<StatusIcon {...getGrauOrdemMeta(clerigo.grauOrdem)} />}
                    icon={<HistoryEduIcon fontSize="small" />}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <InfoField
                    label="Encargo atual"
                    value={<StatusIcon {...getFuncaoEclesiasticaMeta(clerigo.funcaoAtual)} />}
                    icon={<AssignmentIndIcon fontSize="small" />}
                  />
                </Grid>
                {'dioceseIncardinacao' in clerigo && (
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <InfoField
                      label="Diocese de incardinação"
                      value={(clerigo as Clerigo).dioceseIncardinacao}
                      icon={<LocationCityIcon fontSize="small" />}
                    />
                  </Grid>
                )}
                {'cpfFormatado' in clerigo && (
                  <Grid size={{ xs: 12, sm: 3 }}>
                    <InfoField
                      label="CPF"
                      value={(clerigo as Clerigo).cpfFormatado}
                      icon={<BadgeIcon fontSize="small" />}
                    />
                  </Grid>
                )}
                {'rg' in clerigo && (
                  <Grid size={{ xs: 12, sm: 3 }}>
                    <InfoField
                      label="RG"
                      value={(clerigo as Clerigo).rg}
                      icon={<BadgeIcon fontSize="small" />}
                    />
                  </Grid>
                )}
              </Grid>
            </CardContent>
          </Card>

          {/* Ordenações e Datas */}
          <Card variant="outlined" sx={{ borderRadius: 2 }}>
            <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
              <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
                <CalendarMonthIcon color="primary" fontSize="small" />
                <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                  Datas canônicas e nascimento
                </Typography>
              </Stack>
              <Grid container spacing={2}>
                {'dataNascimento' in clerigo && (
                  <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                    <InfoField
                      label="Data de nascimento"
                      value={formatDateBr((clerigo as Clerigo).dataNascimento)}
                      icon={<CakeIcon fontSize="small" />}
                    />
                  </Grid>
                )}
                {'dataOrdenacaoDiaconal' in clerigo && (
                  <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                    <InfoField
                      label="Ordenação diaconal"
                      value={formatDateBr((clerigo as Clerigo).dataOrdenacaoDiaconal)}
                      icon={<VolunteerActivismIcon fontSize="small" />}
                    />
                  </Grid>
                )}
                {'dataOrdenacaoPresbiteral' in clerigo && (
                  <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                    <InfoField
                      label="Ordenação presbiteral"
                      value={formatDateBr((clerigo as Clerigo).dataOrdenacaoPresbiteral)}
                      icon={<HistoryEduIcon fontSize="small" />}
                    />
                  </Grid>
                )}
                {'dataOrdenacaoEpiscopal' in clerigo && (
                  <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                    <InfoField
                      label="Ordenação episcopal"
                      value={formatDateBr((clerigo as Clerigo).dataOrdenacaoEpiscopal)}
                      icon={<WorkspacePremiumIcon fontSize="small" />}
                    />
                  </Grid>
                )}
              </Grid>
            </CardContent>
          </Card>

          {/* Contato e Endereço */}
          {'email' in clerigo && (
            <Card variant="outlined" sx={{ borderRadius: 2 }}>
              <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
                <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
                  <ContactMailIcon color="primary" fontSize="small" />
                  <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                    Contato e endereço
                  </Typography>
                </Stack>
                <Grid container spacing={2}>
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <InfoField
                      label="E-mail"
                      value={(clerigo as Clerigo).email}
                      icon={<EmailIcon fontSize="small" />}
                    />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 3 }}>
                    <InfoField
                      label="Celular"
                      value={
                        <Stack direction="row" spacing={0.5} sx={{ alignItems: 'center' }}>
                          <Typography variant="body2">{(clerigo as Clerigo).celularFormatado}</Typography>
                          {(clerigo as Clerigo).isWhatsapp && (
                            <WhatsAppIcon color="success" sx={{ fontSize: 16 }} />
                          )}
                        </Stack>
                      }
                      icon={<PhoneAndroidIcon fontSize="small" />}
                    />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 3 }}>
                    <InfoField
                      label="Telefone fixo"
                      value={(clerigo as Clerigo).telefoneFixoFormatado}
                      icon={<PhoneIcon fontSize="small" />}
                    />
                  </Grid>
                  <Grid size={{ xs: 12 }}>
                    <InfoField
                      label="Endereço"
                      value={(clerigo as Clerigo).endereco}
                      icon={<LocationOnIcon fontSize="small" />}
                    />
                  </Grid>
                </Grid>
              </CardContent>
            </Card>
          )}
        </Stack>
      </DialogContent>
      <DialogActions sx={{ px: 3, py: 2 }}>
        <ActionButton
          title="Fechar"
          icon={<CloseIcon />}
          onClick={onClose}
          color="secondary"
        />
        {podeGerenciarClero && 'nomeCivil' in clerigo && (
          <>
            {(clerigo as Clerigo).isAtivo && (
              <ActionButton
                title="Inativar"
                icon={<BlockIcon />}
                color="error"
                onClick={() => onInativar(clerigo as Clerigo)}
              />
            )}
            <ActionButton
              title="Editar cadastro"
              icon={<EditIcon />}
              color="primary"
              onClick={() => onEdit(clerigo as Clerigo)}
            />
          </>
        )}
      </DialogActions>
    </Dialog>
  );
};
