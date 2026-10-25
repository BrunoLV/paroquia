import React from 'react';
import {
  Avatar,
  Box,
  Card,
  CardContent,
  Chip,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Grid,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Tooltip,
  Typography,
} from '@mui/material';
import HistoryIcon from '@mui/icons-material/History';
import SecurityIcon from '@mui/icons-material/Security';
import { useQuery } from '@tanstack/react-query';
import { listarHistoricoConsentimento } from '@/contextos/governanca/infrastructure/http/consentimentoPublico';
import AssignmentIndIcon from '@mui/icons-material/AssignmentInd';
import AutoStoriesIcon from '@mui/icons-material/AutoStories';
import BadgeIcon from '@mui/icons-material/Badge';
import BlockIcon from '@mui/icons-material/Block';
import CakeIcon from '@mui/icons-material/Cake';
import CalendarMonthIcon from '@mui/icons-material/CalendarMonth';
import CloseIcon from '@mui/icons-material/Close';
import ContactMailIcon from '@mui/icons-material/ContactMail';
import EditIcon from '@mui/icons-material/Edit';
import { DIALOG_CONTENT_SCROLL_SX } from '@/shared/presentation/theme/dialogStyles';
import EmailIcon from '@mui/icons-material/Email';
import LocationCityIcon from '@mui/icons-material/LocationCity';
import LocationOnIcon from '@mui/icons-material/LocationOn';
import PersonIcon from '@mui/icons-material/Person';
import PhoneAndroidIcon from '@mui/icons-material/PhoneAndroid';
import PhoneIcon from '@mui/icons-material/Phone';
import VolunteerActivismIcon from '@mui/icons-material/VolunteerActivism';
import WhatsAppIcon from '@mui/icons-material/WhatsApp';
import WorkspacePremiumIcon from '@mui/icons-material/WorkspacePremium';
import ActionButton from '@/shared/presentation/components/ActionButton';
import StatusIcon from '@/shared/presentation/components/StatusIcon';
import type { Clerigo, ClerigoInstitucional } from '@/shared/domain/types/types';
import { formatDateBr } from '@/shared/utils/masks';
import { getFuncaoEclesiasticaMeta, getGrauOrdemMeta } from '@/shared/utils/statusMeta/cleroStatus';
import { getSituacaoAtivoMeta } from '@/shared/utils/statusMeta/situacaoStatus';
import { formatarEndereco } from '@/contextos/fiel/presentation/components/fieis/fielConstants';
import { InfoField } from './clerigoConstants';
import VidaSacramentalCard from '@/contextos/sacramento/presentation/components/VidaSacramentalCard';

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

  const clerigoId = clerigo?.clerigoId;
  const { data: historicoConsentimentos, isLoading: loadingHistorico } = useQuery({
    queryKey: ['historico-consentimento', 'CLERIGO', clerigoId],
    queryFn: () => listarHistoricoConsentimento('CLERIGO', clerigoId!),
    enabled: Boolean(open && clerigoId),
  });

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
                    icon={<WorkspacePremiumIcon fontSize="small" />}
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
                      icon={<WorkspacePremiumIcon fontSize="small" />}
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
                      value={formatarEndereco((clerigo as Clerigo).endereco)}
                      icon={<LocationOnIcon fontSize="small" />}
                    />
                  </Grid>
                </Grid>
              </CardContent>
            </Card>
          )}

          {/* Privacidade e Consentimento LGPD (Paridade Canônica Clérigo/Fiel) */}
          <Card variant="outlined" sx={{ borderRadius: 2 }}>
            <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
              <Stack direction="row" spacing={1} sx={{ alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
                <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
                  <SecurityIcon color="primary" fontSize="small" />
                  <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                    Privacidade e consentimento (LGPD)
                  </Typography>
                </Stack>
                {historicoConsentimentos && historicoConsentimentos.length > 0 && (
                  <Chip
                    size="small"
                    color={
                      historicoConsentimentos[0].tipoEvento === 'REVOGACAO' || !historicoConsentimentos[0].termoAceito
                        ? 'error'
                        : 'success'
                    }
                    label={
                      historicoConsentimentos[0].tipoEvento === 'REVOGACAO' || !historicoConsentimentos[0].termoAceito
                        ? `Revogado via ${historicoConsentimentos[0].canal === 'PORTAL' ? 'Portal (OTP)' : 'Secretaria'}`
                        : `Ativo via ${historicoConsentimentos[0].canal === 'PORTAL' ? 'Portal (OTP)' : 'Secretaria'}`
                    }
                  />
                )}
              </Stack>
              <Grid container spacing={2}>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <InfoField
                    label="Termo de Privacidade"
                    icon={<SecurityIcon fontSize="inherit" />}
                    value={
                      'termoConsentimentoAceito' in clerigo && (clerigo as Clerigo).termoConsentimentoAceito
                        ? `Aceito (${(clerigo as Clerigo).versaoTermoConsentimento || 'v1.0'}${
                            (clerigo as Clerigo).dataHoraConsentimentoUtc
                              ? ` em ${formatDateBr((clerigo as Clerigo).dataHoraConsentimentoUtc)}`
                              : ''
                          })`
                        : 'Pendente / Não registrado'
                    }
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <InfoField
                    label="Uso de Imagem e Voz"
                    value={
                      'usoImagemAutorizado' in clerigo && (clerigo as Clerigo).usoImagemAutorizado
                        ? 'Autorizado'
                        : 'Não autorizado'
                    }
                  />
                </Grid>
              </Grid>

              {/* Trilha de Auditoria Forense Imutável */}
              <Box sx={{ mt: 2.5 }}>
                <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 1 }}>
                  <HistoryIcon fontSize="small" color="action" />
                  <Typography
                    variant="caption"
                    sx={{ fontWeight: 700, color: 'text.secondary', textTransform: 'uppercase' }}
                  >
                    Trilha de auditoria de consentimento
                  </Typography>
                </Stack>
                {loadingHistorico ? (
                  <Stack direction="row" spacing={1} sx={{ alignItems: 'center', py: 1 }}>
                    <CircularProgress size={16} />
                    <Typography variant="caption" color="text.secondary">
                      Carregando histórico...
                    </Typography>
                  </Stack>
                ) : !historicoConsentimentos || historicoConsentimentos.length === 0 ? (
                  <Typography variant="body2" color="text.secondary" sx={{ fontStyle: 'italic', fontSize: '0.8125rem' }}>
                    Nenhuma manifestação digital auditada registrada.
                  </Typography>
                ) : (
                  <TableContainer component={Paper} variant="outlined" sx={{ borderRadius: 1.5 }}>
                    <Table size="small">
                      <TableHead>
                        <TableRow sx={{ bgcolor: 'action.hover' }}>
                          <TableCell sx={{ fontWeight: 700, fontSize: '0.75rem' }}>Data/Hora</TableCell>
                          <TableCell sx={{ fontWeight: 700, fontSize: '0.75rem' }}>Operação</TableCell>
                          <TableCell sx={{ fontWeight: 700, fontSize: '0.75rem' }}>Canal</TableCell>
                          <TableCell sx={{ fontWeight: 700, fontSize: '0.75rem' }}>Autenticação</TableCell>
                          <TableCell sx={{ fontWeight: 700, fontSize: '0.75rem' }}>Hash do Termo</TableCell>
                          <TableCell sx={{ fontWeight: 700, fontSize: '0.75rem' }}>IP / Origem</TableCell>
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {historicoConsentimentos.map((h) => (
                          <TableRow key={h.id}>
                            <TableCell sx={{ fontSize: '0.75rem' }}>{formatDateBr(h.criadoEmUtc)}</TableCell>
                            <TableCell sx={{ fontSize: '0.75rem' }}>
                              <Chip
                                size="small"
                                color={h.tipoEvento === 'REVOGACAO' || !h.termoAceito ? 'error' : 'success'}
                                label={h.tipoEvento === 'REVOGACAO' ? 'Revogação' : 'Concessão'}
                                sx={{ height: 20, fontSize: '0.7rem' }}
                              />
                            </TableCell>
                            <TableCell sx={{ fontSize: '0.75rem' }}>
                              {h.canal === 'PORTAL' ? 'Portal Autoatendimento' : 'Secretaria'}
                            </TableCell>
                            <TableCell sx={{ fontSize: '0.75rem' }}>
                              {h.metodoAutenticacao === 'OTP_EMAIL' ? 'OTP E-mail' : h.metodoAutenticacao}
                            </TableCell>
                            <TableCell sx={{ fontSize: '0.75rem', fontFamily: 'monospace' }}>
                              <Tooltip title={h.termoHash} arrow>
                                <span>{h.termoHash.substring(0, 10)}...</span>
                              </Tooltip>
                            </TableCell>
                            <TableCell sx={{ fontSize: '0.75rem' }}>{h.ipOrigem || '-'}</TableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  </TableContainer>
                )}
              </Box>
            </CardContent>
          </Card>

          {/* Vida sacramental: assentos do módulo `sacramento` em que o clérigo é ministro ou ordenado */}
          <VidaSacramentalCard pessoaId={clerigo.clerigoId} />
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
