import React from 'react';
import {
  Alert,
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
import { useQuery } from '@tanstack/react-query';
import { listarHistoricoConsentimento } from '@/contextos/governanca/infrastructure/http/consentimentoPublico';
import BadgeIcon from '@mui/icons-material/Badge';
import BlockIcon from '@mui/icons-material/Block';
import CakeIcon from '@mui/icons-material/Cake';
import ChurchIcon from '@mui/icons-material/Church';
import CloseIcon from '@mui/icons-material/Close';
import ContactMailIcon from '@mui/icons-material/ContactMail';
import Diversity3Icon from '@mui/icons-material/Diversity3';
import EditIcon from '@mui/icons-material/Edit';
import EmailIcon from '@mui/icons-material/Email';
import FamilyRestroomIcon from '@mui/icons-material/FamilyRestroom';
import LocationOnIcon from '@mui/icons-material/LocationOn';
import PersonIcon from '@mui/icons-material/Person';
import PhoneAndroidIcon from '@mui/icons-material/PhoneAndroid';
import PhoneIcon from '@mui/icons-material/Phone';
import PublicIcon from '@mui/icons-material/Public';
import SecurityIcon from '@mui/icons-material/Security';
import VolunteerActivismIcon from '@mui/icons-material/VolunteerActivism';
import WcIcon from '@mui/icons-material/Wc';
import WhatsAppIcon from '@mui/icons-material/WhatsApp';
import { DIALOG_CONTENT_SCROLL_SX } from '@/shared/presentation/theme/dialogStyles';
import ActionButton from '@/shared/presentation/components/ActionButton';
import StatusIcon from '@/shared/presentation/components/StatusIcon';
import { getSituacaoAtivoMeta } from '@/shared/utils/statusMeta/situacaoStatus';
import { Fiel } from '@/shared/domain/types/types';
import { RelacionamentoFamiliar } from '@/contextos/fiel/infrastructure/http/familiares';
import { AjusteCadastral } from '@/contextos/governanca/infrastructure/http/ajustesCadastrais';
import {
  formatCpf,
  formatDateBr,
  formatDocumentoEstrangeiro,
  formatPhone,
  formatRg,
} from '@/shared/utils/masks';
import {
  InfoField,
  SEXO_LABELS,
  TIPO_DOC_LABELS,
  TIPO_RELACAO_LABELS,
  formatarEndereco,
  obterNomePais,
} from './fielConstants';
import VidaSacramentalCard from '@/contextos/sacramento/presentation/components/VidaSacramentalCard';

interface FielViewModalProps {
  open: boolean;
  fiel: Fiel | null;
  detalheFiel?: Fiel | null;
  familiares: RelacionamentoFamiliar[];
  ajustesPendentes: AjusteCadastral[];
  podeGerirCadastro: boolean;
  onClose: () => void;
  onEdit: (fiel: Fiel) => void;
  onInativar: (fiel: Fiel) => void;
}

/**
 * Modal para visualização completa da Ficha Civil e Canônica do Fiel.
 * Apresenta dados de identificação, contato, vínculos comunitários e familiares.
 */
export const FielViewModal: React.FC<FielViewModalProps> = ({
  open,
  fiel,
  detalheFiel,
  familiares,
  ajustesPendentes,
  podeGerirCadastro,
  onClose,
  onEdit,
  onInativar,
}) => {
  if (!fiel) return null;

  const dados = detalheFiel ?? fiel;
  const isAtivo = Boolean(dados.isAtivo ?? dados.ativo);
  const fielId = fiel?.fielId ?? fiel?.id;

  const { data: historicoConsentimentos, isLoading: loadingHistorico } = useQuery({
    queryKey: ['historico-consentimento', 'FIEL', fielId],
    queryFn: () => listarHistoricoConsentimento('FIEL', fielId!),
    enabled: Boolean(open && fielId),
  });

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>
        <Stack direction="row" sx={{ alignItems: 'center', justifyContent: 'space-between' }}>
          <Typography variant="h6" component="div">
            Ficha do fiel
          </Typography>
          <StatusIcon {...getSituacaoAtivoMeta(isAtivo)} />
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
              {(dados.nome ?? 'F').charAt(0).toUpperCase()}
            </Avatar>
            <Box sx={{ flexGrow: 1 }}>
              <Typography variant="h6" sx={{ fontWeight: 700, color: 'primary.main', lineHeight: 1.2 }}>
                {dados.nome}
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
                {formatCpf(dados.dadosIdentificacao?.cpfFormatado ?? dados.cpf) ||
                  formatDocumentoEstrangeiro(
                    dados.dadosIdentificacao?.tipoDocumentoEstrangeiro,
                    dados.dadosIdentificacao?.documentoEstrangeiroFormatado,
                  )}{' '}
                · {dados.igrejaFrequenta?.nome ?? 'Comunidade não informada'}
              </Typography>
              <Stack direction="row" spacing={1} sx={{ mt: 1, flexWrap: 'wrap', gap: 0.5 }}>
                <StatusIcon
                  icon={<ChurchIcon />}
                  color="default"
                  label={dados.igrejaFrequenta?.nome ?? 'Sem vínculo'}
                  tooltip="Igreja/comunidade que o fiel frequenta"
                />
                {dados.isDizimista && (
                  <StatusIcon icon={<VolunteerActivismIcon />} color="primary" label="Dizimista" tooltip="Fiel dizimista" />
                )}
                {dados.permitirDivulgacaoAniversario && (
                  <StatusIcon
                    icon={<CakeIcon />}
                    color="info"
                    label="Aniversário divulgável"
                    tooltip="Autoriza divulgar dia e mês do aniversário"
                  />
                )}
              </Stack>
            </Box>
          </Paper>

          {podeGerirCadastro &&
            ajustesPendentes.map((ajuste) => (
              <Alert key={ajuste.id} severity="warning">
                Ajuste cadastral pendente:{' '}
                {[
                  ajuste.email,
                  formatPhone(ajuste.celular),
                  ajuste.endereco?.logradouro,
                  ajuste.endereco?.cidade,
                ]
                  .filter(Boolean)
                  .join(' · ') || 'dados propostos'}
                .
              </Alert>
            ))}

          {/* Dados Civis */}
          <Card variant="outlined" sx={{ borderRadius: 2 }}>
            <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
              <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
                <BadgeIcon color="primary" fontSize="small" />
                <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                  Dados civis
                </Typography>
              </Stack>
              <Grid container spacing={2}>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <InfoField
                    label="Nome completo"
                    icon={<PersonIcon fontSize="inherit" />}
                    value={dados.nome}
                  />
                </Grid>
                <Grid size={{ xs: 6, sm: 3 }}>
                  <InfoField
                    label="Sexo"
                    icon={<WcIcon fontSize="inherit" />}
                    value={SEXO_LABELS[dados.sexo ?? 'NAO_INFORMADO'] ?? 'Não informado'}
                  />
                </Grid>
                <Grid size={{ xs: 6, sm: 3 }}>
                  <InfoField
                    label="Nascimento"
                    icon={<CakeIcon fontSize="inherit" />}
                    value={formatDateBr(dados.dataNascimento) || '-'}
                  />
                </Grid>
                <Grid size={{ xs: 6, sm: 4 }}>
                  <InfoField
                    label="País de origem"
                    icon={<PublicIcon fontSize="inherit" />}
                    value={obterNomePais(dados.dadosIdentificacao?.paisOrigem ?? 'BRA')}
                  />
                </Grid>
                {(dados.dadosIdentificacao?.paisOrigem ?? 'BRA') !== 'BRA' ? (
                  <>
                    <Grid size={{ xs: 6, sm: 4 }}>
                      <InfoField
                        label="Documento estrangeiro"
                        icon={<BadgeIcon fontSize="inherit" />}
                        value={
                          TIPO_DOC_LABELS[dados.dadosIdentificacao?.tipoDocumentoEstrangeiro ?? ''] ??
                          dados.dadosIdentificacao?.tipoDocumentoEstrangeiro ??
                          '-'
                        }
                      />
                    </Grid>
                    <Grid size={{ xs: 12, sm: 4 }}>
                      <InfoField
                        label="Número do documento"
                        icon={<BadgeIcon fontSize="inherit" />}
                        value={
                          formatDocumentoEstrangeiro(
                            dados.dadosIdentificacao?.tipoDocumentoEstrangeiro,
                            dados.dadosIdentificacao?.documentoEstrangeiroFormatado,
                          ) || '-'
                        }
                      />
                    </Grid>
                  </>
                ) : (
                  <>
                    <Grid size={{ xs: 6, sm: 4 }}>
                      <InfoField
                        label="CPF"
                        icon={<BadgeIcon fontSize="inherit" />}
                        value={formatCpf(dados.dadosIdentificacao?.cpfFormatado ?? dados.cpf) || '-'}
                      />
                    </Grid>
                    <Grid size={{ xs: 12, sm: 4 }}>
                      <InfoField
                        label="RG"
                        icon={<BadgeIcon fontSize="inherit" />}
                        value={formatRg(dados.dadosIdentificacao?.rg ?? dados.rg ?? '') || '-'}
                      />
                    </Grid>
                  </>
                )}
              </Grid>
            </CardContent>
          </Card>

          {/* Contato e Endereço */}
          <Card variant="outlined" sx={{ borderRadius: 2 }}>
            <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
              <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
                <ContactMailIcon color="primary" fontSize="small" />
                <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                  Contato e endereço
                </Typography>
              </Stack>
              <Grid container spacing={2}>
                <Grid size={{ xs: 12, sm: 4 }}>
                  <InfoField
                    label="E-mail"
                    icon={<EmailIcon fontSize="inherit" />}
                    value={dados.contato?.email ?? dados.email ?? '-'}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 4 }}>
                  <InfoField
                    label="Celular"
                    icon={
                      (dados.contato?.isWhatsapp ?? true) ? (
                        <WhatsAppIcon fontSize="inherit" color="success" />
                      ) : (
                        <PhoneAndroidIcon fontSize="inherit" />
                      )
                    }
                    value={
                      (formatPhone(dados.contato?.celularFormatado ?? dados.telefone) || '-') +
                      ((dados.contato?.isWhatsapp ?? true) ? ' (WhatsApp)' : '')
                    }
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 4 }}>
                  <InfoField
                    label="Telefone fixo"
                    icon={<PhoneIcon fontSize="inherit" />}
                    value={formatPhone(dados.contato?.telefoneFixoFormatado ?? dados.telefoneFixo) || '-'}
                  />
                </Grid>
                <Grid size={{ xs: 12 }}>
                  <InfoField
                    label="Endereço residencial"
                    icon={<LocationOnIcon fontSize="inherit" />}
                    value={formatarEndereco(dados.endereco)}
                  />
                </Grid>
              </Grid>
            </CardContent>
          </Card>

          {/* Vínculos Familiares */}
          <Card variant="outlined" sx={{ borderRadius: 2 }}>
            <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
              <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 1.5 }}>
                <FamilyRestroomIcon color="primary" fontSize="small" />
                <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                  Família
                </Typography>
              </Stack>
              {familiares.length === 0 ? (
                <Typography variant="body2" color="text.secondary">
                  Nenhum vínculo familiar registrado.
                </Typography>
              ) : (
                <Grid container spacing={2}>
                  {familiares.map((r) => (
                    <Grid size={{ xs: 12, sm: 6 }} key={r.id}>
                      <Paper
                        variant="outlined"
                        sx={{
                          p: 1.5,
                          borderRadius: 1.5,
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'space-between',
                        }}
                      >
                        <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
                          <Diversity3Icon fontSize="small" color="action" />
                          <Typography variant="body2" sx={{ fontWeight: 600 }}>
                            {r.nome}
                          </Typography>
                        </Stack>
                        <StatusIcon color="default" icon={<Diversity3Icon />} label={TIPO_RELACAO_LABELS[r.relacao] ?? r.relacao} tooltip="Tipo de relação familiar" />
                      </Paper>
                    </Grid>
                  ))}
                </Grid>
              )}
            </CardContent>
          </Card>

          {/* Privacidade e Consentimento LGPD */}
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
                      (fiel?.consentimento?.termoConsentimentoAceito ?? fiel?.consentimentoLgpd?.termoConsentimentoAceito)
                        ? `Aceito (${fiel?.consentimento?.versaoTermoConsentimento || fiel?.consentimentoLgpd?.versaoTermoConsentimento || 'v1.0'}${fiel?.consentimento?.dataHoraConsentimentoUtc ? ` em ${formatDateBr(fiel.consentimento.dataHoraConsentimentoUtc)}` : ''})`
                        : 'Pendente / Não registrado'
                    }
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <InfoField
                    label="Uso de Imagem e Voz"
                    value={
                      (fiel?.consentimento?.usoImagemAutorizado ?? fiel?.consentimentoLgpd?.usoImagemAutorizado)
                        ? 'Autorizado'
                        : 'Não autorizado'
                    }
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <InfoField
                    label="Divulgação de Aniversário"
                    value={
                      fiel?.permitirDivulgacaoAniversario
                        ? 'Autorizada'
                        : 'Não autorizada'
                    }
                  />
                </Grid>
                {(fiel?.consentimento?.responsavelMenorNome ||
                  fiel?.consentimentoLgpd?.responsavelMenorNome ||
                  fiel?.responsavelMenorNome) && (
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <InfoField
                      label="Salvaguarda de Menor (Responsável Legal)"
                      value={`${
                        fiel.consentimento?.responsavelMenorNome ??
                        fiel.consentimentoLgpd?.responsavelMenorNome ??
                        fiel.responsavelMenorNome
                      } (${formatCpf(
                        fiel.consentimento?.responsavelMenorCpf ??
                          fiel.consentimentoLgpd?.responsavelMenorCpf ??
                          fiel.responsavelMenorCpf,
                      )})`}
                    />
                  </Grid>
                )}
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

          {/* Vida sacramental: assentos do módulo `sacramento` vinculados a este fiel; a API serializa o identificador como `fielId` */}
          {fiel && <VidaSacramentalCard pessoaId={fiel.fielId ?? fiel.id} />}
        </Stack>
      </DialogContent>
      <DialogActions sx={{ px: 3, py: 2 }}>
        <ActionButton
          title="Fechar"
          icon={<CloseIcon />}
          onClick={onClose}
          color="secondary"
        />
        {podeGerirCadastro && (
          <>
            {isAtivo && (
              <ActionButton
                title="Inativar"
                color="error"
                icon={<BlockIcon />}
                onClick={() => onInativar(fiel)}
              />
            )}
            <ActionButton
              title="Editar cadastro"
              color="primary"
              icon={<EditIcon />}
              onClick={() => onEdit(fiel)}
            />
          </>
        )}
      </DialogActions>
    </Dialog>
  );
};
